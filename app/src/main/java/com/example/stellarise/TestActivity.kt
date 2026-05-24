package com.example.stellarise

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Simple test activity to verify app can start
 * Use this to test if the basic app structure works
 */
class TestActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create a simple layout programmatically
        val textView = TextView(this).apply {
            text = "🌟 Stellarise Test - App is Working!"
            textSize = 18f
            setPadding(50, 50, 50, 50)
        }
        
        setContentView(textView)
    }
}




