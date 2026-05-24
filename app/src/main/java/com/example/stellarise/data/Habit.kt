package com.example.stellarise.data

import com.example.stellarise.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a habit
 * @param id Unique identifier for the habit
 * @param name Name of the habit
 * @param description Optional description
 * @param isCompleted Whether the habit is completed today
 * @param createdAt When the habit was created
 * @param completedDates List of dates when the habit was completed
 */
data class Habit(
    val id: String = "",
    val name: String,
    val description: String = "",
    val iconResId: Int = R.drawable.ic_1,
    var isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedDates: MutableList<Long> = mutableListOf(),
    val steps: MutableList<String> = mutableListOf(),
    var currentStep: Int = 0
) {
    
    /**
     * Mark habit as completed for today
     */
    fun markCompleted() {
        if (!isCompleted) {
            isCompleted = true
            val today = getTodayTimestamp()
            if (!completedDates.contains(today)) {
                completedDates.add(today)
            }
        }
    }
    
    /**
     * Mark habit as incomplete for today
     */
    fun markIncomplete() {
        isCompleted = false
        val today = getTodayTimestamp()
        completedDates.remove(today)
    }
    
    /**
     * Add a step to the habit
     */
    fun addStep(step: String) {
        if (step.isNotBlank()) {
            steps.add(step.trim())
        }
    }
    
    /**
     * Remove a step from the habit
     */
    fun removeStep(index: Int) {
        if (index in 0 until steps.size) {
            steps.removeAt(index)
            if (currentStep >= steps.size) {
                currentStep = maxOf(0, steps.size - 1)
            }
        }
    }
    
    /**
     * Get current step
     */
    fun getCurrentStep(): String? {
        return if (steps != null && currentStep < steps.size) steps[currentStep] else null
    }
    
    /**
     * Move to next step
     */
    fun nextStep() {
        if (steps != null && currentStep < steps.size) {
            currentStep++
        }
    }
    
    /**
     * Move to previous step
     */
    fun previousStep() {
        if (currentStep > 0) {
            currentStep--
        }
    }
    
    /**
     * Check if all steps are completed
     */
    fun areAllStepsCompleted(): Boolean {
        val stepsSize = steps?.size ?: 0
        if (stepsSize == 0) return true
        return currentStep >= stepsSize - 1
    }
    
    /**
     * Check if we're on the last step
     */
    fun isOnLastStep(): Boolean {
        val stepsSize = steps?.size ?: 0
        if (stepsSize == 0) return true
        return currentStep == stepsSize - 1
    }
    
    /**
     * Get completion rate for the last 7 days
     */
    fun getCompletionRate(): Float {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val recentCompletions = completedDates.count { it >= weekAgo }
        return recentCompletions.toFloat() / 7f
    }
    
    /**
     * Get completion rate for all time
     */
    fun getOverallCompletionRate(): Float {
        val daysSinceCreation = getDaysSinceCreation()
        if (daysSinceCreation == 0) return 0f
        return completedDates.size.toFloat() / daysSinceCreation.toFloat()
    }
    
    /**
     * Check if habit was completed on a specific date
     */
    fun wasCompletedOn(date: Long): Boolean {
        return completedDates.contains(date)
    }
    
    /**
     * Get streak count (consecutive days completed)
     */
    fun getCurrentStreak(): Int {
        val today = getTodayTimestamp()
        var streak = 0
        var currentDate = today
        
        while (completedDates.contains(currentDate)) {
            streak++
            currentDate -= (24 * 60 * 60 * 1000L) // Go back one day
        }
        
        return streak
    }
    
    /**
     * Get longest streak
     */
    fun getLongestStreak(): Int {
        if (completedDates.isEmpty()) return 0
        
        val sortedDates = completedDates.sorted()
        var maxStreak = 1
        var currentStreak = 1
        
        for (i in 1 until sortedDates.size) {
            val daysDiff = (sortedDates[i] - sortedDates[i - 1]) / (24 * 60 * 60 * 1000L)
            if (daysDiff == 1L) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }
        
        return maxStreak
    }
    
    /**
     * Get formatted creation date
     */
    fun getFormattedCreatedDate(): String {
        val date = Date(createdAt)
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Get days since creation
     */
    private fun getDaysSinceCreation(): Int {
        val daysDiff = (System.currentTimeMillis() - createdAt) / (24 * 60 * 60 * 1000L)
        return daysDiff.toInt() + 1
    }
    
    /**
     * Get today's timestamp at midnight
     */
    private fun getTodayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
