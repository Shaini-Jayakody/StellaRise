package com.example.stellarise.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.stellarise.R
import com.example.stellarise.data.DataManager
import com.example.stellarise.data.HydrationData
import com.example.stellarise.services.HydrationReminderService
import com.example.stellarise.views.StarBottleView


class HydrationFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private lateinit var hydrationData: HydrationData
    
    private lateinit var tvGlassesConsumed: TextView
    private lateinit var tvProgressPercentage: TextView
    private lateinit var switchReminder: android.widget.Switch
    private lateinit var tvIntervalValue: TextView
    private lateinit var waterBottle: StarBottleView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hydration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            dataManager = DataManager(requireContext())
            hydrationData = dataManager.loadHydrationData()
            setupViews(view)
            updateUI()
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error in onViewCreated: ${e.message}", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Only refresh if data might have changed
        if (::dataManager.isInitialized) {
            hydrationData = dataManager.loadHydrationData()
            updateUI()
        }
    }

    private fun setupViews(view: View) {
        tvGlassesConsumed = view.findViewById(R.id.tvGlassesConsumed)
        tvProgressPercentage = view.findViewById(R.id.tvProgressPercentage)
        switchReminder = view.findViewById(R.id.switchReminder)
        tvIntervalValue = view.findViewById(R.id.tvIntervalValue)
        waterBottle = view.findViewById<StarBottleView>(R.id.waterBottle)
        
        // Set switch colors to match app theme
        // Switch colors are now handled by the color selectors in XML
        
        // Add water button
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAddWater)
            .setOnClickListener {
                addWater()
            }
        
        // Remove water button
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRemoveWater)
            .setOnClickListener {
                removeWater()
            }
        
        // Reminder toggle
        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            hydrationData.reminderEnabled = isChecked
            dataManager.saveHydrationData(hydrationData)
            updateReminderService()
        }
        
        // Edit interval button
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSetInterval)
            .setOnClickListener {
                showIntervalDialog()
            }
        
        // Set goal button
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSetGoal)
            .setOnClickListener {
                showGoalDialog()
            }
    }

    private fun updateUI() {
        // Check if it's a new day and reset if needed
        hydrationData.checkAndResetIfNewDay()
        
        tvGlassesConsumed.text = "${hydrationData.glassesDrunk}/${hydrationData.goalGlasses}"
        
        val progress = (hydrationData.getCompletionPercentage() * 100).toInt()
        tvProgressPercentage.text = "$progress%"
        
        // Update bottle visual
        android.util.Log.d("HydrationFragment", "Updating UI. Glasses: ${hydrationData.glassesDrunk}")
        updateBottleVisual()
        
        switchReminder.isChecked = hydrationData.reminderEnabled
        updateIntervalText(tvIntervalValue, hydrationData.reminderIntervalMinutes)
        // Last drink time display removed as view doesn't exist in current layout
    }

    private fun updateBottleVisual() {
        val glasses = hydrationData.glassesDrunk
        val maxGlasses = hydrationData.goalGlasses
        val fillPercentage = (glasses.toFloat() / maxGlasses).coerceAtMost(1.0f)
        
        // Debug: Log the current state
        android.util.Log.d("HydrationFragment", "Glasses: $glasses, Max: $maxGlasses, Fill Percentage: $fillPercentage")
        
        try {
            // Add a subtle scale animation when the bottle changes
            waterBottle.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction {
                    waterBottle.setFillPercentage(fillPercentage)
                    waterBottle.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(200)
                        .start()
                }
                .start()
            
            // Show completion message if goal is reached
            if (glasses >= maxGlasses) {
                showCompletionMessage()
            }
            
            android.util.Log.d("HydrationFragment", "Successfully set fill level with animation")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error setting fill level: ${e.message}")
            // Fallback without animation
            waterBottle.setFillPercentage(fillPercentage)
        }
    }
    
    private fun showCompletionMessage() {
        android.widget.Toast.makeText(
            requireContext(),
            "🎉 Great job! You've reached your daily hydration goal!",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    private fun showGoalDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_set_goal, null)
        
        val etGoalGlasses = dialogView.findViewById<android.widget.EditText>(R.id.etGoalGlasses)
        etGoalGlasses.setText(hydrationData.goalGlasses.toString())
        
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Set Daily Goal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val goalText = etGoalGlasses.text.toString()
                try {
                    val newGoal = goalText.toInt().coerceIn(1, 20) // Min 1, Max 20 glasses
                    hydrationData.goalGlasses = newGoal
                    dataManager.saveHydrationData(hydrationData)
                    updateUI()
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Daily goal set to $newGoal glasses",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } catch (e: NumberFormatException) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Please enter a valid number",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun addWater() {
        hydrationData.addGlass()
        dataManager.saveHydrationData(hydrationData)
        android.util.Log.d("HydrationFragment", "Added water. Glasses: ${hydrationData.glassesDrunk}")
        updateUI()
    }

    private fun removeWater() {
        hydrationData.removeGlass()
        dataManager.saveHydrationData(hydrationData)
        updateUI()
    }

    private fun showIntervalDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_reminder_interval_improved, null)
        
        val tvCurrentSelection = dialogView.findViewById<TextView>(R.id.tvCurrentSelection)
        val etCustomMinutes = dialogView.findViewById<android.widget.EditText>(R.id.etCustomMinutes)
        
        // Initialize current selection display
        updateCurrentSelection(tvCurrentSelection, hydrationData.reminderIntervalMinutes)
        
        // Set up preset buttons
        val presetButtons = mapOf(
            R.id.btn5min to 5,
            R.id.btn15min to 15,
            R.id.btn30min to 30,
            R.id.btn1hour to 60,
            R.id.btn2hours to 120,
            R.id.btn4hours to 240
        )
        
        presetButtons.forEach { (buttonId, minutes) ->
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(buttonId)
                .setOnClickListener {
                    hydrationData.reminderIntervalMinutes = minutes
                    updateCurrentSelection(tvCurrentSelection, minutes)
                    etCustomMinutes.setText(minutes.toString())
                }
        }
        
        // Set up custom input
        etCustomMinutes.setText(hydrationData.reminderIntervalMinutes.toString())
        etCustomMinutes.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                try {
                    val customMinutes = etCustomMinutes.text.toString().toInt().coerceIn(1, 1440)
                    hydrationData.reminderIntervalMinutes = customMinutes
                    updateCurrentSelection(tvCurrentSelection, customMinutes)
                } catch (e: NumberFormatException) {
                    // Invalid input, keep current value
                    etCustomMinutes.setText(hydrationData.reminderIntervalMinutes.toString())
                }
            }
        }
        
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Set Reminder Interval")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                try {
                    val customMinutes = etCustomMinutes.text.toString().toInt().coerceIn(1, 1440)
                    hydrationData.reminderIntervalMinutes = customMinutes
                    dataManager.saveHydrationData(hydrationData)
                    updateUI()
                    updateReminderService()
                } catch (e: NumberFormatException) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Please enter a valid number",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateCurrentSelection(textView: TextView, minutes: Int) {
        val formattedTime = formatTimeDisplay(minutes)
        textView.text = "Current: $formattedTime"
    }
    
    private fun updateIntervalText(textView: TextView, minutes: Int) {
        textView.text = formatTimeDisplay(minutes)
    }
    
    private fun formatTimeDisplay(minutes: Int): String {
        return when {
            minutes < 60 -> "$minutes minutes"
            minutes < 1440 -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                if (remainingMinutes == 0) {
                    "$hours hour${if (hours > 1) "s" else ""}"
                } else {
                    "$hours hour${if (hours > 1) "s" else ""} $remainingMinutes minute${if (remainingMinutes > 1) "s" else ""}"
                }
            }
            else -> {
                val days = minutes / 1440
                "$days day${if (days > 1) "s" else ""}"
            }
        }
    }
    
    private fun updateReminderService() {
        try {
            if (hydrationData.reminderEnabled) {
                HydrationReminderService.scheduleReminder(requireContext(), hydrationData.reminderIntervalMinutes)
            } else {
                HydrationReminderService.cancelReminder(requireContext())
            }
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error updating reminder service: ${e.message}")
        }
    }
}

