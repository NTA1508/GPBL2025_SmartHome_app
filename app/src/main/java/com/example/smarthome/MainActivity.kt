package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.reference
    private lateinit var btnSwitch: Switch
    private lateinit var btnSwitch1: Switch
    private lateinit var btnSwitch2: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        findViewById<Button>(R.id.button1).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

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
}
