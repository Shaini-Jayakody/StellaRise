package com.example.stellarise.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import android.widget.LinearLayout
import com.example.stellarise.R
import com.example.stellarise.data.DataManager
import com.example.stellarise.data.MoodEntry
import com.example.stellarise.views.SimpleMoodChart
import java.text.SimpleDateFormat
import java.util.*


class MoodFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private lateinit var llMoodHistory: LinearLayout
    private lateinit var btnLogMood: Button
    private lateinit var tvMoodTitle: TextView
    private lateinit var tvMoodSubtitle: TextView
    private lateinit var moodChart: SimpleMoodChart

    private val moodImages = listOf(
        R.drawable.mood_1,  // Most positive
        R.drawable.mood_2,
        R.drawable.mood_3,
        R.drawable.mood_4,  // Neutral
        R.drawable.mood_5,
        R.drawable.mood_6,
        R.drawable.mood_7   // Most negative
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            dataManager = DataManager(requireContext())
            setupViews(view)
            setupMoodHistory()
            loadMoodHistory()
        } catch (e: Exception) {
            android.util.Log.e("MoodFragment", "Error in onViewCreated: ${e.message}", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Only refresh if data might have changed
        if (::dataManager.isInitialized) {
            loadMoodHistory()
        }
    }

    private fun setupViews(view: View) {
        llMoodHistory = view.findViewById(R.id.llMoodHistory)
        btnLogMood = view.findViewById(R.id.btnLogMood)
        tvMoodTitle = view.findViewById(R.id.tvMoodTitle)
        tvMoodSubtitle = view.findViewById(R.id.tvMoodSubtitle)
        moodChart = view.findViewById(R.id.moodChart) as SimpleMoodChart
        
        tvMoodTitle.text = "Mood Journal"
        tvMoodSubtitle.text = "Track your emotional journey"
        
        btnLogMood.setOnClickListener {
                    showMoodDialog()
        }
    }

    private fun setupMoodHistory() {
        // Simple LinearLayout setup - no adapter needed for now
    }

    private fun loadMoodHistory() {
        try {
            val moodEntries = dataManager.loadMoodEntries()
            android.util.Log.d("MoodFragment", "loadMoodHistory: loaded ${moodEntries.size} mood entries")
            llMoodHistory.removeAllViews()
            
            // Update mood chart
            moodChart.updateMoodEntries(moodEntries)
            
            if (moodEntries.isEmpty()) {
                // Show empty state
                android.util.Log.d("MoodFragment", "No mood entries found, showing empty state")
                return
            }
        
        // Display mood entries with date and time (limit to last 50 entries for performance)
        val displayEntries = moodEntries.reversed().take(50)
        android.util.Log.d("MoodFragment", "Displaying ${displayEntries.size} mood entries in history")
        displayEntries.forEach { moodEntry ->
            val moodItemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_mood_history, llMoodHistory, false)
            
            val ivMoodImage = moodItemView.findViewById<ImageView>(R.id.ivMoodImage)
            val tvMoodNote = moodItemView.findViewById<TextView>(R.id.tvMoodNote)
            val tvMoodDateTime = moodItemView.findViewById<TextView>(R.id.tvMoodDateTime)
            val btnEditMood = moodItemView.findViewById<ImageButton>(R.id.btnEditMood)
            val btnDeleteMood = moodItemView.findViewById<ImageButton>(R.id.btnDeleteMood)
            
            if (moodEntry.moodImage != 0) {
                ivMoodImage.setImageResource(moodEntry.moodImage)
                ivMoodImage.visibility = View.VISIBLE
            } else {
                ivMoodImage.visibility = View.GONE
            }
            tvMoodNote.text = if (moodEntry.note.isNotEmpty()) moodEntry.note else "No note"
            tvMoodDateTime.text = moodEntry.getFormattedDateTime()
            
            // Set up edit button
            btnEditMood.setOnClickListener {
                showEditMoodDialog(moodEntry)
            }
            
            // Set up delete button
            btnDeleteMood.setOnClickListener {
                showDeleteConfirmation(moodEntry)
            }
            
            llMoodHistory.addView(moodItemView)
        }
        } catch (e: Exception) {
            android.util.Log.e("MoodFragment", "Error in loadMoodHistory: ${e.message}", e)
        }
    }

    private fun showMoodDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_log_mood, null)
        
        val etMoodNote = dialogView.findViewById<EditText>(R.id.etMoodNote)
        val llEmojiSelector = dialogView.findViewById<LinearLayout>(R.id.llEmojiSelector)
        
        // Setup mood image selector
        var selectedMoodImage = moodImages[0] // Default to first mood (most positive)
        
        // Add mood image buttons to LinearLayout
        moodImages.forEachIndexed { _, moodImage ->
            val moodButton = ImageView(requireContext()).apply {
                setImageResource(moodImage)
                layoutParams = LinearLayout.LayoutParams(
                    100, 100
                ).apply {
                    marginEnd = 12
                }
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                background = context.getDrawable(R.drawable.emoji_background)
                setPadding(10, 10, 10, 10)
                setOnClickListener {
                    selectedMoodImage = moodImage
                    // Update button states
                    for (i in 0 until llEmojiSelector.childCount) {
                        val child = llEmojiSelector.getChildAt(i)
                        if (child is ImageView) {
                            child.alpha = if (child.drawable?.constantState == context.getDrawable(moodImage)?.constantState) 1.0f else 0.5f
                        }
                    }
                }
            }
            llEmojiSelector.addView(moodButton)
        }
        
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Log Your Mood")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val note = etMoodNote.text.toString().trim()
                saveMoodEntry(selectedMoodImage, note)
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }

    private fun saveMoodEntry(moodImage: Int, note: String) {
        try {
            // Convert mood image to mood level (1-7)
            val moodLevel = moodImages.indexOf(moodImage) + 1
            val moodEmoji = when (moodLevel) {
                1 -> "😄" // Most positive
                2 -> "😊"
                3 -> "🙂"
                4 -> "😐" // Neutral
                5 -> "😔"
                6 -> "😢"
                7 -> "😭" // Most negative
                else -> "😐"
            }
            
            val moodEntry = MoodEntry(
                id = System.currentTimeMillis().toString(),
                emoji = moodEmoji,
                moodImage = moodImage,
                note = note,
                timestamp = System.currentTimeMillis()
            )
            
            dataManager.addMoodEntry(moodEntry)
            loadMoodHistory() // Refresh the history
            
            Toast.makeText(
                requireContext(),
                "Mood logged successfully! 😊",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Failed to save mood entry",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showEditMoodDialog(moodEntry: MoodEntry) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_log_mood, null)
        
        val etMoodNote = dialogView.findViewById<EditText>(R.id.etMoodNote)
        val llEmojiSelector = dialogView.findViewById<LinearLayout>(R.id.llEmojiSelector)
        
        // Pre-fill with existing data
        etMoodNote.setText(moodEntry.note)
        
        // Setup mood image selector
        var selectedMoodImage = moodEntry.moodImage
        if (selectedMoodImage == 0) selectedMoodImage = moodImages[0]
        
        // Add mood image buttons to LinearLayout
        moodImages.forEachIndexed { _, moodImage ->
            val moodButton = ImageView(requireContext()).apply {
                setImageResource(moodImage)
                layoutParams = LinearLayout.LayoutParams(
                    100, 100
                ).apply {
                    marginEnd = 12
                }
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                background = context.getDrawable(R.drawable.emoji_background)
                setPadding(10, 10, 10, 10)
                alpha = if (moodImage == selectedMoodImage) 1.0f else 0.5f
                setOnClickListener {
                    selectedMoodImage = moodImage
                    // Update button states
                    for (i in 0 until llEmojiSelector.childCount) {
                        val child = llEmojiSelector.getChildAt(i)
                        if (child is ImageView) {
                            child.alpha = if (child.drawable?.constantState == context.getDrawable(moodImage)?.constantState) 1.0f else 0.5f
                        }
                    }
                }
            }
            llEmojiSelector.addView(moodButton)
        }
        
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Edit Mood Entry")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val note = etMoodNote.text.toString().trim()
                updateMoodEntry(moodEntry, selectedMoodImage, note)
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }

    private fun showDeleteConfirmation(moodEntry: MoodEntry) {
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMoodEntry(moodEntry)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateMoodEntry(originalEntry: MoodEntry, moodImage: Int, note: String) {
        try {
            // Convert mood image to mood level (1-7)
            val moodLevel = moodImages.indexOf(moodImage) + 1
            val moodEmoji = when (moodLevel) {
                1 -> "😄" // Most positive
                2 -> "😊"
                3 -> "🙂"
                4 -> "😐" // Neutral
                5 -> "😔"
                6 -> "😢"
                7 -> "😭" // Most negative
                else -> "😐"
            }
            
            val updatedEntry = originalEntry.copy(
                emoji = moodEmoji,
                moodImage = moodImage,
                note = note
            )
            
            dataManager.updateMoodEntry(updatedEntry)
            loadMoodHistory() // Refresh the history
            
            Toast.makeText(
                requireContext(),
                "Mood entry updated! 😊",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Failed to update mood entry",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        try {
            dataManager.deleteMoodEntry(moodEntry.id)
            loadMoodHistory() // Refresh the history
            
            Toast.makeText(
                requireContext(),
                "Mood entry deleted",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Failed to delete mood entry",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}
