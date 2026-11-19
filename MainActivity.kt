package ro.example.powerdisconnectcaller

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("settings", Context.MODE_PRIVATE) }
    private lateinit var numberInput: EditText
    private lateinit var delayInput: EditText
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        numberInput = EditText(this).apply {
            hint = "Număr (ex. +407xxxxxxxx)"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setText(prefs.getString("phone", ""))
        }
        delayInput = EditText(this).apply {
            hint = "Întârziere înainte de apel (secunde, 0–5)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(prefs.getInt("delay_seconds", 3).toString())
        }
        status = TextView(this).apply { textSize = 16f }

        val save = Button(this).apply {
            text = "Salvează setările"
            setOnClickListener {
                val phone = numberInput.text.toString().trim()
                val delay = delayInput.text.toString().toIntOrNull()
                if (phone.isBlank() || delay == null || delay !in 0..5) {
                    Toast.makeText(this@MainActivity, "Introdu un număr și o întârziere între 0 și 5 secunde.", Toast.LENGTH_LONG).show()
                } else {
                    prefs.edit().putString("phone", phone).putInt("delay_seconds", delay).apply()
                    Toast.makeText(this@MainActivity, "Setări salvate.", Toast.LENGTH_SHORT).show()
                    refreshStatus()
                }
            }
        }
        val enable = Button(this).apply {
            text = "Activează detectorul"
            setOnClickListener {
                if (numberInput.text.toString().trim().isBlank()) {
                    Toast.makeText(this@MainActivity, "Salvează mai întâi numărul de telefon.", Toast.LENGTH_LONG).show()
                } else if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 100)
                } else {
                    val plugged = isCurrentlyPluggedIn()
                    prefs.edit()
                        .putBoolean("enabled", true)
                        .putBoolean("armed", plugged)
                        .putBoolean("alarm_sent", false)
                        .apply()
                    refreshStatus()
                    Toast.makeText(this@MainActivity,
                        if (plugged) "Detector activ. Alimentarea este prezentă." else "Detector activ, dar telefonul nu pare conectat la curent.",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
        val disable = Button(this).apply {
            text = "Dezactivează detectorul"
            setOnClickListener {
                prefs.edit().putBoolean("enabled", false).putBoolean("armed", false).apply()
                refreshStatus()
            }
        }
        val batterySettings = Button(this).apply {
            text = "Deschide setările bateriei"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 40)
            addView(TextView(this@MainActivity).apply {
                text = "Detector pană de curent"
                textSize = 23f
            })
            addView(TextView(this@MainActivity).apply {
                text = "Telefonul trebuie ținut conectat la încărcător. La pierderea alimentării, aplicația încearcă să sune numărul salvat."
                textSize = 15f
            })
            addView(numberInput, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            addView(delayInput, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            addView(save)
            addView(enable)
            addView(disable)
            addView(batterySettings)
            addView(status)
        }
        setContentView(layout)
        refreshStatus()
    }

    private fun isCurrentlyPluggedIn(): Boolean {
        val battery = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = battery?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        return plugged != 0
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val plugged = isCurrentlyPluggedIn()
            prefs.edit().putBoolean("enabled", true).putBoolean("armed", plugged).putBoolean("alarm_sent", false).apply()
            refreshStatus()
        } else if (requestCode == 100) {
            Toast.makeText(this, "Este necesară permisiunea de apelare.", Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshStatus() {
        val active = prefs.getBoolean("enabled", false)
        val armed = prefs.getBoolean("armed", false)
        status.text = when {
            !active -> "Stare: detector INACTIV"
            armed -> "Stare: ACTIV — alimentarea este monitorizată"
            else -> "Stare: ACTIV — așteaptă conectarea la încărcător"
        }
    }
}
