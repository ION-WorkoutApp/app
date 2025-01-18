package com.ion606.workoutapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class SuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        val continueButton: Button = findViewById(R.id.continueButton)

        // Handle button click
        continueButton.setOnClickListener {
//            val intent = Intent(this, )
//            startActivity(intent)

            finish()
        }
    }
}
