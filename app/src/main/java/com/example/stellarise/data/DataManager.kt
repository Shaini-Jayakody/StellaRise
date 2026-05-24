package com.example.stellarise.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class DataManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("stellarise_data", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_HABITS = "habits"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_HYDRATION_DATA = "hydration_data"
        private const val KEY_USER_SETTINGS = "user_settings"
    }
    
    // Habit Management
    fun saveHabits(habits: List<Habit>) {
        val habitsJson = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, habitsJson).apply()
    }
    
    fun loadHabits(): List<Habit> {
        val habitsJson = prefs.getString(KEY_HABITS, null)
        return if (habitsJson != null) {
            try {
                val type = object : TypeToken<List<Habit>>() {}.type
                gson.fromJson(habitsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    fun addHabit(habit: Habit) {
        val habits = loadHabits().toMutableList()
        val newHabit = habit.copy(id = System.currentTimeMillis().toString())
        habits.add(newHabit)
        saveHabits(habits)
    }
    
    fun updateHabit(habit: Habit) {
        val habits = loadHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
            saveHabits(habits)
        }
    }
    
    fun deleteHabit(habitId: String) {
        val habits = loadHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
    }
    
    // Mood Entry Management
    fun saveMoodEntries(entries: List<MoodEntry>) {
        val entriesJson = gson.toJson(entries)
        prefs.edit().putString(KEY_MOOD_ENTRIES, entriesJson).apply()
    }
    
    fun loadMoodEntries(): List<MoodEntry> {
        val entriesJson = prefs.getString(KEY_MOOD_ENTRIES, null)
        return if (entriesJson != null) {
            try {
                val type = object : TypeToken<List<MoodEntry>>() {}.type
                gson.fromJson(entriesJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    fun addMoodEntry(entry: MoodEntry) {
        try {
            val entries = loadMoodEntries().toMutableList()
            val newEntry = entry.copy(id = System.currentTimeMillis().toString())
            entries.add(newEntry)
            saveMoodEntries(entries)
            android.util.Log.d("DataManager", "Mood entry saved: ${newEntry.emoji} - ${newEntry.note}")
        } catch (e: Exception) {
            android.util.Log.e("DataManager", "Error saving mood entry: ${e.message}")
            throw e
        }
    }
    
    fun getMoodEntriesForWeek(): List<MoodEntry> {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        return loadMoodEntries().filter { it.timestamp >= weekAgo }
    }
    
    fun updateMoodEntry(entry: MoodEntry) {
        try {
            val entries = loadMoodEntries().toMutableList()
            val index = entries.indexOfFirst { it.id == entry.id }
            if (index != -1) {
                entries[index] = entry
                saveMoodEntries(entries)
                android.util.Log.d("DataManager", "Mood entry updated: ${entry.emoji} - ${entry.note}")
            }
        } catch (e: Exception) {
            android.util.Log.e("DataManager", "Error updating mood entry: ${e.message}")
            throw e
        }
    }
    
    fun deleteMoodEntry(entryId: String) {
        try {
            val entries = loadMoodEntries().toMutableList()
            entries.removeAll { it.id == entryId }
            saveMoodEntries(entries)
            android.util.Log.d("DataManager", "Mood entry deleted: $entryId")
        } catch (e: Exception) {
            android.util.Log.e("DataManager", "Error deleting mood entry: ${e.message}")
            throw e
        }
    }
    
    // Hydration Data Management
    fun saveHydrationData(data: HydrationData) {
        val dataJson = gson.toJson(data)
        prefs.edit().putString(KEY_HYDRATION_DATA, dataJson).apply()
    }
    
    fun loadHydrationData(): HydrationData {
        val dataJson = prefs.getString(KEY_HYDRATION_DATA, null)
        return if (dataJson != null) {
            try {
                gson.fromJson(dataJson, HydrationData::class.java) ?: HydrationData()
            } catch (e: Exception) {
                HydrationData()
            }
        } else {
            HydrationData()
        }
    }
    
    // User Settings Management
    fun saveUserSettings(settings: Map<String, Any>) {
        val editor = prefs.edit()
        settings.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
            }
        }
        editor.apply()
    }
    
    fun getUserSetting(key: String, defaultValue: Any): Any {
        return when (defaultValue) {
            is String -> prefs.getString(key, defaultValue) ?: defaultValue
            is Int -> prefs.getInt(key, defaultValue)
            is Boolean -> prefs.getBoolean(key, defaultValue)
            is Long -> prefs.getLong(key, defaultValue)
            is Float -> prefs.getFloat(key, defaultValue)
            else -> defaultValue
        }
    }
    
    // Utility methods
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
    
    fun getTodayHabitsCompletionRate(): Float {
        val habits = loadHabits()
        if (habits.isEmpty()) return 0f
        
        val completedToday = habits.count { it.isCompleted }
        return completedToday.toFloat() / habits.size.toFloat()
    }
    
    fun getTotalHabitsCount(): Int {
        return loadHabits().size
    }
    
    fun getCompletedHabitsCount(): Int {
        return loadHabits().count { it.isCompleted }
    }
}




