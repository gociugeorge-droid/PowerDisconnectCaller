package ro.example.powerdisconnectcaller

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager

class PowerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                // Revenirea alimentării rearmează detectorul pentru următoarea pană.
                prefs.edit().putBoolean("armed", true).putBoolean("alarm_sent", false).apply()
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                // După repornire, detectorul se rearmează doar dacă telefonul este alimentat.
                val battery = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val plugged = (battery?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0) != 0
                if (prefs.getBoolean("enabled", false) && plugged) {
                    prefs.edit().putBoolean("armed", true).putBoolean("alarm_sent", false).apply()
                }
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                if (!prefs.getBoolean("enabled", false)) return
                if (!prefs.getBoolean("armed", false)) return
                if (prefs.getBoolean("alarm_sent", false)) return

                val phone = prefs.getString("phone", null)?.trim().orEmpty()
                if (phone.isBlank()) return

                // Dezarmare imediată: un singur apel pentru fiecare întrerupere.
                prefs.edit().putBoolean("armed", false).putBoolean("alarm_sent", true).apply()

                if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) return

                val delayMs = prefs.getInt("delay_seconds", 3).coerceIn(0, 5) * 1000L
                val pending = goAsync()
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        val telecom = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                        telecom.placeCall(Uri.fromParts("tel", phone, null), Bundle())
                    } catch (_: Exception) {
                        // Apelarea din fundal poate depinde de versiunea Android/operator.
                    } finally {
                        pending.finish()
                    }
                }, delayMs)
            }
        }
    }
}
