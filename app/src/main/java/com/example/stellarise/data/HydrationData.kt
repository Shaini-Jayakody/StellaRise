package com.example.stellarise.data

import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing hydration tracking data
 * @param glassesDrunk Number of glasses drunk today
 * @param goalGlasses Daily goal for glasses
 * @param lastDrinkTime Timestamp of last drink
 * @param reminderEnabled Whether reminders are enabled
 * @param reminderIntervalMinutes Reminder interval in minutes
 */
data class HydrationData(
    var glassesDrunk: Int = 0,
    var goalGlasses: Int = 8,
    var lastDrinkTime: Long = 0L,
    var reminderEnabled: Boolean = true,
    private var _reminderIntervalMinutes: Int = 60
) {
    var reminderIntervalMinutes: Int
        get() = _reminderIntervalMinutes
        set(value) {
            _reminderIntervalMinutes = value.coerceIn(1, 1440) // 1 minute to 24 hours
        }
    /**
     * Get completion percentage
     */
    fun getCompletionPercentage(): Float {
        return if (goalGlasses > 0) {
            (glassesDrunk.toFloat() / goalGlasses.toFloat()).coerceAtMost(1f)
        } else 0f
    }
    
    /**
     * Add a glass of water
     */
    fun addGlass() {
        glassesDrunk++
        lastDrinkTime = System.currentTimeMillis()
    }
    
    /**
     * Remove a glass of water
     */
    fun removeGlass() {
        if (glassesDrunk > 0) {
            glassesDrunk--
        }
    }
    
    /**
     * Reset daily count (call at midnight)
     */
    fun resetDaily() {
        glassesDrunk = 0
        lastDrinkTime = 0L
    }
    
    /**
     * Check if it's a new day and reset if needed
     */
    fun checkAndResetIfNewDay() {
        val today = System.currentTimeMillis()
        val lastDrinkDate = if (lastDrinkTime > 0) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = lastDrinkTime
            calendar.get(Calendar.DAY_OF_YEAR)
        } else -1
        
        val currentDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        
        if (lastDrinkDate != currentDate) {
            resetDaily()
        }
    }
    
    /**
     * Get formatted last drink time
     */
    fun getFormattedLastDrinkTime(): String {
        if (lastDrinkTime == 0L) return "Never"
        
        val date = Date(lastDrinkTime)
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
}




