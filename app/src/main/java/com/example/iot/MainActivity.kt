package com.example.iot

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvGar1: TextView = findViewById<TextView>(R.id.tv_garden1)
        tvGar1.setOnClickListener {
            val intent = Intent(this, GardenActivity::class.java)
            intent.putExtra("garden", 1)
            startActivity(intent)
        }
        val tvGar2: TextView = findViewById<TextView>(R.id.tv_garden2)
        tvGar2.setOnClickListener {
            val intent = Intent(this, Garden2Activity::class.java)
            intent.putExtra("garden", 2)
            startActivity(intent)
        }
    }
}
