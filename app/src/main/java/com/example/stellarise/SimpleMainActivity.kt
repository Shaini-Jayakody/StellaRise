package com.example.stellarise

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Simple MainActivity for debugging
 */
class SimpleMainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            android.util.Log.d("SimpleMainActivity", "onCreate started")
            
            // Create a simple layout programmatically
            val textView = TextView(this).apply {
                text = "Stellarise App - Simple Test"
                textSize = 24f
                setPadding(50, 50, 50, 50)
            }
            
            setContentView(textView)
            android.util.Log.d("SimpleMainActivity", "Simple layout set successfully")
            
        } catch (e: Exception) {
            android.util.Log.e("SimpleMainActivity", "Error in onCreate: ${e.message}", e)
        }
    }
}