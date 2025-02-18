package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class automation : AppCompatActivity() {

    var database = FirebaseDatabase.getInstance()
    var myRef = database.reference
    internal lateinit var btnSwitch: Switch
    internal lateinit var btnSwitch1:Switch
    internal lateinit var btnSwitch2:Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myRef.child("Toggle/switch").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                switch1.visibility = View.VISIBLE
                if (p0.value.toString().equals("1")){
                    switch1.isChecked = true;
                    toggleButton2.visibility = View.VISIBLE
                    toggleButton3.visibility = View.VISIBLE

                    myRef.child("automation/AC").addValueEventListener(object :ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if(p0.value.toString().equals("1")){
                                toggleButton2.isChecked = true
                            }
                            else
                                toggleButton2.isChecked = false

                        }
                    })

                    myRef.child("automation/light").addValueEventListener(object :ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if(p0.value.toString().equals("1")){
                                toggleButton3.isChecked = true
                            }
                            else
                                toggleButton3.isChecked = false

                        }
                    })
                }
                else{
                    switch1.isChecked = false
                    toggleButton2.visibility = View.GONE
                    toggleButton3.visibility = View.GONE
                }

                // Log.d("ahsan", p0.value.toString())
            }
        })

        button3.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnSwitch = findViewById<View>(R.id.switch1) as Switch
        btnSwitch1 = findViewById<View>(R.id.toggleButton2) as Switch
        btnSwitch2 = findViewById<View>(R.id.toggleButton3) as Switch

        btnSwitch.setOnClickListener{
            if(btnSwitch.isChecked)
            {
                myRef.child("Toggle/switch").setValue("1")

                btnSwitch1.setOnClickListener{
                    if(btnSwitch1.isChecked)
                    {
                        myRef.child("automation/AC").setValue("1")
                    }
                    else
                    {
                        myRef.child("automation/AC").setValue("0")
                    }
                }


                btnSwitch2.setOnClickListener{
                    if(btnSwitch2.isChecked)
                    {
                        myRef.child("automation/light").setValue("1")
                    }
                    else
                    {
                        myRef.child("automation/light").setValue("0")
                    }
                }
            }
            else
            {
                myRef.child("Toggle/switch").setValue("0")
            }
        }
    }
}