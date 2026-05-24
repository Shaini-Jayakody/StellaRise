package com.example.stellarise.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stellarise.R
import com.example.stellarise.data.Habit
import android.widget.ImageView


/**
 * RecyclerView adapter for displaying habits
 */
class HabitAdapter(
    private var habits: List<Habit> = emptyList(),
    private val onHabitToggle: (Habit) -> Unit,
    private val onHabitEdit: (Habit) -> Unit,
    private val onHabitDelete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivHabitIcon: ImageView = itemView.findViewById(R.id.ivHabitIcon)
        val cbCompleted: CheckBox = itemView.findViewById(R.id.cbCompleted)
        val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
        val tvHabitDescription: TextView = itemView.findViewById(R.id.tvHabitDescription)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val tvProgressText: TextView = itemView.findViewById(R.id.tvProgressText)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        
        holder.ivHabitIcon.setImageResource(habit.iconResId)
        holder.tvHabitName.text = habit.name
        holder.cbCompleted.isChecked = habit.isCompleted
        
        // Show description if available
        if (habit.description.isNotEmpty()) {
            holder.tvHabitDescription.text = habit.description
            holder.tvHabitDescription.visibility = View.VISIBLE
        } else {
            holder.tvHabitDescription.visibility = View.GONE
        }
        
        // Update progress
        val completionRate = (habit.getCompletionRate() * 100).toInt()
        holder.progressBar.progress = completionRate
        holder.tvProgressText.text = "${completionRate}%"
        
        // Set click listeners
        holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                habit.markCompleted()
            } else {
                habit.markIncomplete()
            }
            onHabitToggle(habit)
        }
        
        holder.btnEdit.setOnClickListener {
            onHabitEdit(habit)
        }
        
        holder.btnDelete.setOnClickListener {
            onHabitDelete(habit)
        }
        
        // Make entire card clickable for toggle
        holder.itemView.setOnClickListener {
            holder.cbCompleted.isChecked = !holder.cbCompleted.isChecked
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateHabits(newHabits: List<Habit>) {
        habits = newHabits
        notifyDataSetChanged()
    }
}

