package com.example.smarthome

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest

class MainActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.reference
    private lateinit var btnSwitch: Switch
    private lateinit var btnSwitch1: Switch
    private lateinit var btnSwitch2: Switch
    private lateinit var bluetoothService: BluetoothService
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1  // 権限リクエストのコード

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkBluetoothPermissions()  // 🚀 Bluetooth権限を確認＆リクエスト

        bluetoothService = BluetoothService()
        if (bluetoothService.connect("ESP32_FireSensor")) {
            Log.d("Bluetooth", "Connected to ESP32")

            // Bluetoothデータの監視を開始
            startListeningForFireAlerts()
        } else {
            Log.d("Bluetooth", "Failed to connect")
        }

        btnSwitch = findViewById(R.id.switch2)
        btnSwitch1 = findViewById(R.id.toggleButton4)
        btnSwitch2 = findViewById(R.id.toggleButton5)

        myRef.child("Toggle/switch").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                btnSwitch.visibility = View.VISIBLE
                if (snapshot.value.toString() == "1") {
                    btnSwitch.isChecked = true
                    btnSwitch1.visibility = View.VISIBLE
                    btnSwitch2.visibility = View.VISIBLE

                    myRef.child("automation/AC").addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}

                        override fun onDataChange(snapshot: DataSnapshot) {
                            btnSwitch1.isChecked = snapshot.value.toString() == "1"
                        }
                    })

                    myRef.child("automation/light").addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}

                        override fun onDataChange(snapshot: DataSnapshot) {
                            btnSwitch2.isChecked = snapshot.value.toString() == "1"
                        }
                    })
                } else {
                    btnSwitch.isChecked = false
                    btnSwitch1.visibility = View.GONE
                    btnSwitch2.visibility = View.GONE
                }
            }
        })


        btnSwitch.setOnClickListener {
            myRef.child("Toggle/switch").setValue(if (btnSwitch.isChecked) "1" else "0")
        }

        btnSwitch1.setOnClickListener {
            myRef.child("automation/AC").setValue(if (btnSwitch1.isChecked) "1" else "0")
        }

        btnSwitch2.setOnClickListener {
            myRef.child("automation/light").setValue(if (btnSwitch2.isChecked) "1" else "0")
        }
    }

    private fun checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            ), REQUEST_BLUETOOTH_PERMISSIONS)
        }
    }

    private fun startListeningForFireAlerts() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val data = bluetoothService.readData() ?: continue
                if (data.contains("FIRE_ALERT")) {
                    withContext(Dispatchers.Main) {
                        showFireAlert(data)
                    }
                }
            }
        }
    }


    private fun showFireAlert(message: String) {
        Toast.makeText(this, "🔥 Fire Alert: $message", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Bluetooth", "BLUETOOTH_CONNECT permission granted")
            } else {
                Log.e("Bluetooth", "BLUETOOTH_CONNECT permission denied")
                Toast.makeText(this, "Bluetooth の権限が必要です", Toast.LENGTH_LONG).show()
            }
        }
    }


}



