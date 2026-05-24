package com.example.stellarise.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stellarise.R
import com.example.stellarise.data.MoodEntry

/**
 * RecyclerView adapter for displaying mood entries
 */
class MoodAdapter(
    private var moodEntries: List<MoodEntry> = emptyList()
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMoodImage: android.widget.ImageView = itemView.findViewById(R.id.ivMoodImage)
        val tvMoodNote: TextView = itemView.findViewById(R.id.tvMoodNote)
        val tvMoodDateTime: TextView = itemView.findViewById(R.id.tvMoodDateTime)
        val btnEditMood: android.widget.ImageButton = itemView.findViewById(R.id.btnEditMood)
        val btnDeleteMood: android.widget.ImageButton = itemView.findViewById(R.id.btnDeleteMood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_history, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodEntry = moodEntries[position]
        
        // Set mood image
        holder.ivMoodImage.setImageResource(moodEntry.moodImage)
        
        // Set date and time
        holder.tvMoodDateTime.text = "${moodEntry.getFormattedDate()} ${moodEntry.getFormattedTime()}"
        
        // Show note if available, otherwise show default message
        if (moodEntry.note.isNotEmpty()) {
            holder.tvMoodNote.text = moodEntry.note
        } else {
            holder.tvMoodNote.text = "No note added"
        }
        
        // Set up click listeners for edit and delete buttons
        holder.btnEditMood.setOnClickListener {
            // TODO: Implement edit functionality
        }
        
        holder.btnDeleteMood.setOnClickListener {
            // TODO: Implement delete functionality
        }
    }

    override fun getItemCount(): Int = moodEntries.size

    fun updateMoodEntries(newEntries: List<MoodEntry>) {
        moodEntries = newEntries.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
    }
}




