package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Lấy TextView từ layout
        val tvMessage = findViewById<TextView>(R.id.tvMessage)

        // Hiển thị nội dung lên màn hình
        tvMessage.text = "Xin chào, đây là test Kotlin!"
    }
}
