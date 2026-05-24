package com.example.stellarise

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.stellarise.adapters.MainPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * MainActivity - Entry point of the Stellarise wellness app
 * 
 * This activity manages the main navigation between different wellness features:
 * - Habits tracking and management
 * - Mood journaling with emoji selection
 * - Hydration tracking with visual bottle filling
 * - Settings and preferences
 * 
 * Uses ViewPager2 with FragmentStateAdapter for efficient fragment management
 * and BottomNavigationView for intuitive navigation between features.
 * 
 * @author Stellarise Development Team
 * @version 1.0
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var pagerAdapter: MainPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Enable fullscreen mode to hide status bar
            enableEdgeToEdge()
            
            // Hide the status bar completely and extend into system UI
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.decorView.systemUiVisibility = 
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            
            setContentView(R.layout.activity_main)
            
            // Handle window insets - add padding for system bars except top
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
                insets
            }
            
            // Handle header insets to extend into status bar area
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appHeader)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Position header at absolute top - no offset
                v.setPadding(v.paddingLeft, 0, v.paddingRight, v.paddingBottom)
                insets
            }
            
            setupViews()
            setupViewPager()
            setupNavigation()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
            // Show error dialog to user
            android.app.AlertDialog.Builder(this)
                .setTitle("App Error")
                .setMessage("There was an error starting the app: ${e.message}\n\nPlease restart the app.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .show()
        }
    }

    /**
     * Initialize and bind UI components to their respective views
     * Sets up ViewPager2 for fragment navigation and navigation components
     */
    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }



    private fun setupViewPager() {
        pagerAdapter = MainPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        
        // Allow swipe between pages for better UX
        viewPager.isUserInputEnabled = true
        
        // Set initial page
        viewPager.currentItem = MainPagerAdapter.HABITS_PAGE
    }


    private fun setupNavigation() {
        try {
            setupPhoneNavigation()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in setupNavigation: ${e.message}")
        }
    }
    
    
    /**
     * Configure phone navigation using BottomNavigationView
     */
    private fun setupPhoneNavigation() {
        if (!::bottomNavigation.isInitialized) {
            android.util.Log.w("MainActivity", "BottomNavigationView not initialized")
            return
        }
        
        // Set initial selection
        bottomNavigation.selectedItemId = R.id.nav_habits
        
        bottomNavigation.setOnItemSelectedListener { item ->
            try {
                android.util.Log.d("MainActivity", "Navigation item selected: ${item.itemId}")
                when (item.itemId) {
                    R.id.nav_habits -> {
                        android.util.Log.d("MainActivity", "Switching to Habits page")
                        viewPager.currentItem = MainPagerAdapter.HABITS_PAGE
                        true
                    }
                    R.id.nav_mood -> {
                        android.util.Log.d("MainActivity", "Switching to Mood page")
                        viewPager.currentItem = MainPagerAdapter.MOOD_PAGE
                        true
                    }
                    R.id.nav_hydration -> {
                        android.util.Log.d("MainActivity", "Switching to Hydration page")
                        viewPager.currentItem = MainPagerAdapter.HYDRATION_PAGE
                        true
                    }
                    R.id.nav_settings -> {
                        android.util.Log.d("MainActivity", "Switching to Settings page")
                        viewPager.currentItem = MainPagerAdapter.SETTINGS_PAGE
                        true
                    }
                    else -> {
                        android.util.Log.w("MainActivity", "Unknown navigation item: ${item.itemId}")
                        false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error in navigation: ${e.message}", e)
                false
            }
        }
        
        // Sync ViewPager with BottomNavigation
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                try {
                    super.onPageSelected(position)
                    when (position) {
                        MainPagerAdapter.HABITS_PAGE -> bottomNavigation.selectedItemId = R.id.nav_habits
                        MainPagerAdapter.MOOD_PAGE -> bottomNavigation.selectedItemId = R.id.nav_mood
                        MainPagerAdapter.HYDRATION_PAGE -> bottomNavigation.selectedItemId = R.id.nav_hydration
                        MainPagerAdapter.SETTINGS_PAGE -> bottomNavigation.selectedItemId = R.id.nav_settings
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error in page selection: ${e.message}", e)
                }
            }
        })
    }
    
    /**
     * Refresh all fragments after data is cleared
     * This ensures the UI reflects the cleared state
     */
    fun refreshAllFragments() {
        try {
            android.util.Log.d("MainActivity", "Refreshing all fragments after data clear")
            // Force refresh the current fragment by recreating the adapter
            pagerAdapter.notifyDataSetChanged()
            
            // If we're on the habits page, refresh it specifically
            if (viewPager.currentItem == MainPagerAdapter.HABITS_PAGE) {
                // The habits fragment will refresh when it becomes visible again
                android.util.Log.d("MainActivity", "Habits page will refresh on next visibility")
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error refreshing fragments: ${e.message}", e)
        }
    }

}