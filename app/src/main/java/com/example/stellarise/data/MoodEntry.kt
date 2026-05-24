package com.example.stellarise.data

import com.example.stellarise.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a mood entry
 * @param id Unique identifier for the mood entry
 * @param mood The mood emoji/icon
 * @param note Optional note about the mood
 * @param timestamp When the mood was recorded
 * @param tags Optional tags for categorization
 */
data class MoodEntry(
    val id: String = "",
    val emoji: String,
    val moodImage: Int = 0,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList()
) {
    
    /**
     * Get formatted timestamp
     */
    fun getFormattedTime(): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Get formatted date
     */
    fun getFormattedDate(): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Get formatted date and time
     */
    fun getFormattedDateTime(): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Get mood intensity (1-7 scale based on mood image)
     * mood_1 = most positive (level 7), mood_7 = most negative (level 1)
     */
    fun getMoodIntensity(): Int {
        return when (moodImage) {
            R.drawable.mood_1 -> 7 // Most positive
            R.drawable.mood_2 -> 6 // Very positive
            R.drawable.mood_3 -> 5 // Positive
            R.drawable.mood_4 -> 4 // Neutral
            R.drawable.mood_5 -> 3 // Negative
            R.drawable.mood_6 -> 2 // Very negative
            R.drawable.mood_7 -> 1 // Most negative
            else -> when (emoji) {
                "😢", "😭", "😔" -> 1 // Very sad
                "😕", "😞", "😟" -> 2 // Sad
                "😐", "😑", "😶" -> 3 // Neutral
                "😊", "🙂", "😌" -> 4 // Happy
                "😄", "😁", "🤩", "🥳" -> 5 // Very happy
                else -> 4 // Default to neutral
            }
        }
    }
    
    /**
     * Get mood category
     */
    fun getMoodCategory(): String {
        return when (moodImage) {
            R.drawable.mood_1 -> "Very Happy"
            R.drawable.mood_2 -> "Happy"
            R.drawable.mood_3 -> "Good"
            R.drawable.mood_4 -> "Neutral"
            R.drawable.mood_5 -> "Bad"
            R.drawable.mood_6 -> "Sad"
            R.drawable.mood_7 -> "Very Sad"
            else -> when (emoji) {
                "😢", "😭", "😔", "😕", "😞", "😟" -> "Sad"
                "😐", "😑", "😶", "😑" -> "Neutral"
                "😊", "🙂", "😌", "😄", "😁", "🤩", "🥳" -> "Happy"
                "😴", "😪", "🥱" -> "Tired"
                "😰", "😨", "😱" -> "Anxious"
                "😡", "😠", "🤬" -> "Angry"
                "😍", "🥰", "😘" -> "Loving"
                else -> "Other"
            }
        }
    }
    
    /**
     * Check if this mood entry is from today
     */
    fun isToday(): Boolean {
        val today = Calendar.getInstance()
        val entryDate = Calendar.getInstance().apply { timeInMillis = timestamp }
        
        return today.get(Calendar.YEAR) == entryDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == entryDate.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Check if this mood entry is from this week
     */
    fun isThisWeek(): Boolean {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        return timestamp >= weekAgo
    }
    
    /**
     * Get relative time string (e.g., "2 hours ago")
     */
    fun getRelativeTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
            else -> getFormattedDate()
        }
    }
    
    companion object {
        /**
         * Get all available mood options
         */
        fun getAvailableMoods(): List<String> {
            return listOf(
                "😢", "😭", "😔", "😕", "😞", "😟", // Sad
                "😐", "😑", "😶", // Neutral
                "😊", "🙂", "😌", "😄", "😁", "🤩", "🥳", // Happy
                "😴", "😪", "🥱", // Tired
                "😰", "😨", "😱", // Anxious
                "😡", "😠", "🤬", // Angry
                "😍", "🥰", "😘" // Loving
            )
        }
        
        /**
         * Get mood options by category
         */
        fun getMoodsByCategory(): Map<String, List<String>> {
            return mapOf(
                "Sad" to listOf("😢", "😭", "😔", "😕", "😞", "😟"),
                "Neutral" to listOf("😐", "😑", "😶"),
                "Happy" to listOf("😊", "🙂", "😌", "😄", "😁", "🤩", "🥳"),
                "Tired" to listOf("😴", "😪", "🥱"),
                "Anxious" to listOf("😰", "😨", "😱"),
                "Angry" to listOf("😡", "😠", "🤬"),
                "Loving" to listOf("😍", "🥰", "😘")
            )
        }
    }
}



