package com.example.stellarise.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stellarise.R
import com.example.stellarise.data.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodTrendAdapter(
    private val moodEntries: List<MoodEntry>
) : RecyclerView.Adapter<MoodTrendAdapter.MoodTrendViewHolder>() {

    class MoodTrendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMoodEmoji: TextView = itemView.findViewById(R.id.tvMoodEmoji)
        val tvDayName: TextView = itemView.findViewById(R.id.tvDayName)
        val vMoodBar: View = itemView.findViewById(R.id.vMoodBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodTrendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_trend, parent, false)
        return MoodTrendViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodTrendViewHolder, position: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6 + position) // Last 7 days
        
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        holder.tvDayName.text = dayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        
        // Find mood entry for this day
        val dayMood = moodEntries.find { entry ->
            val entryDate = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
            entryDate.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) &&
            entryDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
        }
        
        if (dayMood != null) {
            holder.tvMoodEmoji.text = dayMood.emoji
            holder.vMoodBar.setBackgroundColor(getMoodColor(dayMood.emoji, holder.itemView.context))
        } else {
            holder.tvMoodEmoji.text = "😐"
            holder.vMoodBar.setBackgroundColor(holder.itemView.context.getColor(R.color.text_secondary))
        }
    }

    override fun getItemCount(): Int = 7 // Show 7 days

    private fun getMoodColor(emoji: String, context: android.content.Context): Int {
        return when (emoji) {
            "😄", "🤩" -> context.getColor(R.color.accent_gold)
            "😊", "😌" -> context.getColor(R.color.primary_star)
            "😐" -> context.getColor(R.color.text_secondary)
            "😔", "😢" -> context.getColor(R.color.info_blue)
            "😠", "😰" -> context.getColor(R.color.error_red)
            else -> context.getColor(R.color.text_secondary)
        }
    }
}
