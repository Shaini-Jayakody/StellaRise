package com.example.stellarise.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.stellarise.R
import com.google.android.material.card.MaterialCardView

class HabitIconAdapter(
    private val icons: List<Int>,
    private val onIconSelected: (Int) -> Unit
) : RecyclerView.Adapter<HabitIconAdapter.IconViewHolder>() {

    private var selectedPosition = 0

    class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconView: ImageView = itemView.findViewById(R.id.ivIcon)
        val cardView: MaterialCardView = itemView as MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_icon, parent, false)
        return IconViewHolder(view)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.iconView.setImageResource(icons[position])
        
        // Enhanced selection styling
        if (position == selectedPosition) {
            holder.cardView.strokeColor = holder.itemView.context.getColor(R.color.primary_star)
            holder.cardView.strokeWidth = 6
            holder.cardView.cardElevation = 12f
            holder.cardView.setCardBackgroundColor(holder.itemView.context.getColor(R.color.sparkle_purple))
            holder.cardView.alpha = 1.0f
        } else {
            holder.cardView.strokeColor = holder.itemView.context.getColor(R.color.primary_star_light)
            holder.cardView.strokeWidth = 2
            holder.cardView.cardElevation = 4f
            holder.cardView.setCardBackgroundColor(holder.itemView.context.getColor(R.color.white))
            holder.cardView.alpha = 0.8f
        }
        
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onIconSelected(icons[position])
        }
    }

    override fun getItemCount(): Int = icons.size
}



