package com.example.stellarise.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
// import androidx.recyclerview.widget.LinearLayoutManager // No longer needed
import com.example.stellarise.R
// import com.example.stellarise.adapters.HabitAdapter // No longer needed
import com.example.stellarise.adapters.HabitIconAdapter
import com.example.stellarise.data.DataManager
import com.example.stellarise.data.Habit
import java.util.*


class HabitsFragment : Fragment() {

    private lateinit var dataManager: DataManager
    // No longer using HabitAdapter since we're using LinearLayout
    private lateinit var llHabitsContainer: LinearLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var tvCompletionRate: TextView
    private lateinit var tvHabitsCount: TextView
    private lateinit var fabAddHabit: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var habitsCalendarView: CalendarView
    
    // Star chart views
    private lateinit var star1: TextView
    private lateinit var star2: TextView
    private lateinit var star3: TextView
    private lateinit var star4: TextView
    private lateinit var star5: TextView
    private lateinit var tvStarCompletion: TextView

    private val habitIcons = listOf(
        R.drawable.ic_1,
        R.drawable.ic_2,
        R.drawable.ic_3,
        R.drawable.ic_4,
        R.drawable.ic_5,
        R.drawable.ic_6,
        R.drawable.ic_7,
        R.drawable.ic_8,
        R.drawable.ic_9,
        R.drawable.ic_10
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dataManager = DataManager(requireContext())
        setupViews(view)
        setupCalendar()
        loadHabits()
    }
    
    override fun onResume() {
        super.onResume()
        // Only refresh if data might have changed
        // This prevents excessive reloading that causes performance issues
        if (::dataManager.isInitialized) {
            loadHabits()
        }
    }

    private fun setupViews(view: View) {
        try {
            llHabitsContainer = view.findViewById(R.id.llHabitsContainer)
            emptyState = view.findViewById(R.id.emptyState)
            tvCompletionRate = view.findViewById(R.id.tvCompletionRate)
            tvHabitsCount = view.findViewById(R.id.tvHabitsCount)
            fabAddHabit = view.findViewById(R.id.fabAddHabit)
            habitsCalendarView = view.findViewById(R.id.habitsCalendarView)
            
            // Initialize star chart views
            star1 = view.findViewById(R.id.star1)
            star2 = view.findViewById(R.id.star2)
            star3 = view.findViewById(R.id.star3)
            star4 = view.findViewById(R.id.star4)
            star5 = view.findViewById(R.id.star5)
            tvStarCompletion = view.findViewById(R.id.tvStarCompletion)
            
            // Set up floating add button
            fabAddHabit.setOnClickListener {
                showAddHabitDialog()
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error in setupViews: ${e.message}", e)
            throw e
        }
    }

    private fun setupCalendar() {
        // Set calendar to current date
        habitsCalendarView.date = System.currentTimeMillis()
        
        // Enable month navigation
        habitsCalendarView.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    // Allow touch events to pass through for scrolling
                    false
                }
                else -> false
            }
        }
        
        // Set up calendar listener
        habitsCalendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            
            // Show habits completion for selected date
            val selectedDateStart = selectedDate.timeInMillis
            val selectedDateEnd = selectedDateStart + (24 * 60 * 60 * 1000L)
            
            val habits = dataManager.loadHabits()
            // Filter habits completed on selected date for future use
            // val habitsForDate = habits.filter { habit ->
            //     habit.completedDates.any { date ->
            //         date >= selectedDateStart && date < selectedDateEnd
            //     }
            // }
            
            // Update UI to show completion status for selected date
            updateHabitsForDate(habits, selectedDateStart, selectedDateEnd)
        }
    }

    private fun updateHabitsForDate(habits: List<Habit>, dateStart: Long, dateEnd: Long) {
        // Update habits to show completion status for selected date
        val updatedHabits = habits.map { habit ->
            val wasCompletedOnDate = habit.completedDates.any { date ->
                date >= dateStart && date < dateEnd
            }
            habit.copy(isCompleted = wasCompletedOnDate)
        }
        populateHabitsList(updatedHabits)
    }

    private fun setupRecyclerView() {
        // No longer using RecyclerView, using LinearLayout instead
        // This method is kept for compatibility but does nothing
    }

    private fun loadHabits() {
        try {
            val habits = dataManager.loadHabits()
            populateHabitsList(habits)
            updateProgress()
            updateEmptyState(habits.isEmpty())
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error in loadHabits: ${e.message}", e)
            // Show empty state on error
            updateEmptyState(true)
        }
    }

    private fun populateHabitsList(habits: List<Habit>) {
        try {
            llHabitsContainer.removeAllViews()
            
            // Limit the number of habits displayed to prevent UI lag
            val limitedHabits = habits.take(5) // Further reduced for performance
            
            limitedHabits.forEach { habit ->
            val habitView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_habit, llHabitsContainer, false)
            
            // Set up habit view
            val ivHabitIcon = habitView.findViewById<ImageView>(R.id.ivHabitIcon)
            val cbCompleted = habitView.findViewById<CheckBox>(R.id.cbCompleted)
            val tvHabitName = habitView.findViewById<TextView>(R.id.tvHabitName)
            val tvHabitDescription = habitView.findViewById<TextView>(R.id.tvHabitDescription)
            val btnEdit = habitView.findViewById<ImageButton>(R.id.btnEdit)
            val btnDelete = habitView.findViewById<ImageButton>(R.id.btnDelete)
            
            // Steps views
            val llStepsContainer = habitView.findViewById<LinearLayout>(R.id.llStepsContainer)
            val tvCurrentStep = habitView.findViewById<TextView>(R.id.tvCurrentStep)
            val tvStepDescription = habitView.findViewById<TextView>(R.id.tvStepDescription)
            val progressBarSteps = habitView.findViewById<ProgressBar>(R.id.progressBarSteps)
            val btnPreviousStep = habitView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPreviousStep)
            val btnNextStep = habitView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnNextStep)
            
            // Populate data
            ivHabitIcon.setImageResource(habit.iconResId)
            tvHabitName.text = habit.name
            cbCompleted.isChecked = habit.isCompleted
            cbCompleted.isEnabled = true // Always enable checkbox
            
            if (habit.description.isNotEmpty()) {
                tvHabitDescription.text = habit.description
                tvHabitDescription.visibility = View.VISIBLE
            } else {
                tvHabitDescription.visibility = View.GONE
            }
            
            // Setup steps functionality
            if (habit.steps.isNotEmpty()) {
                llStepsContainer.visibility = View.VISIBLE
                updateStepsUI(habit, tvCurrentStep, tvStepDescription, progressBarSteps, btnPreviousStep, btnNextStep)
                
                // Set initial checkbox state based on step completion
                if (habit.areAllStepsCompleted()) {
                    cbCompleted.isEnabled = true
                    cbCompleted.alpha = 1.0f
                } else {
                    cbCompleted.isEnabled = false
                    cbCompleted.alpha = 0.4f
                }
                
                btnPreviousStep.setOnClickListener {
                    habit.previousStep()
                    dataManager.updateHabit(habit)
                    updateStepsUI(habit, tvCurrentStep, tvStepDescription, progressBarSteps, btnPreviousStep, btnNextStep)
                    
                    // Remove tick if going back to previous step
                    val allCompleted = habit.areAllStepsCompleted()
                    if (!allCompleted) {
                        cbCompleted.isChecked = false
                        cbCompleted.isEnabled = false
                        cbCompleted.alpha = 0.4f
                        habit.markIncomplete()
                        dataManager.updateHabit(habit)
                        updateProgress()
                    }
                }
                
                btnNextStep.setOnClickListener {
                    habit.nextStep()
                    dataManager.updateHabit(habit)
                    updateStepsUI(habit, tvCurrentStep, tvStepDescription, progressBarSteps, btnPreviousStep, btnNextStep)
                    
                    // Update checkbox state based on step completion
                    val allCompleted = habit.areAllStepsCompleted()
                    if (allCompleted) {
                        cbCompleted.isEnabled = true
                        cbCompleted.alpha = 1.0f
                        android.widget.Toast.makeText(
                            requireContext(),
                            "🎉 All steps completed! You can now mark the habit as complete.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        cbCompleted.isEnabled = false
                        cbCompleted.alpha = 0.4f
                    }
                }
            } else {
                llStepsContainer.visibility = View.GONE
            }
            
            // Set click listeners
            cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                android.util.Log.d("HabitsFragment", "Checkbox clicked: $isChecked")
                if (isChecked) {
                    // Check if steps exist and are not completed
                    val hasSteps = habit.steps.isNotEmpty()
                    val allStepsCompleted = habit.areAllStepsCompleted()
                    val stepsSize = habit.steps.size
                    
                    android.util.Log.d("HabitsFragment", "Has steps: $hasSteps, All completed: $allStepsCompleted, Current step: ${habit.currentStep}, Steps size: $stepsSize")
                    
                    if (hasSteps && !allStepsCompleted) {
                        // Show message that steps need to be completed first
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Complete all steps first!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        cbCompleted.isChecked = false
                        return@setOnCheckedChangeListener
                    } else {
                        // Allow completion - either no steps or all steps completed
                        android.util.Log.d("HabitsFragment", "Allowing completion")
                        habit.markCompleted()
                        dataManager.updateHabit(habit)
                        updateProgress()
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Habit completed! 🎉",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    android.util.Log.d("HabitsFragment", "Marking as incomplete")
                    habit.markIncomplete()
                    dataManager.updateHabit(habit)
                    updateProgress()
                }
            }
            
            btnEdit.setOnClickListener {
                showEditHabitDialog(habit)
            }
            
            btnDelete.setOnClickListener {
                showDeleteHabitDialog(habit)
            }
            
            // Make entire card clickable for toggle
            habitView.setOnClickListener {
                cbCompleted.isChecked = !cbCompleted.isChecked
            }
            
            llHabitsContainer.addView(habitView)
        }
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error in populateHabitsList: ${e.message}", e)
        }
    }


    private fun updateProgress() {
        val completedCount = dataManager.getCompletedHabitsCount()
        val totalCount = dataManager.getTotalHabitsCount()
        val completionRate = dataManager.getTodayHabitsCompletionRate()
        
        tvHabitsCount.text = "$completedCount/$totalCount"
        tvCompletionRate.text = "${(completionRate * 100).toInt()}%"
        
        // Update star chart
        updateStarChart(completionRate)
    }

    private fun updateStarChart(completionRate: Float) {
        val stars = listOf(star1, star2, star3, star4, star5)
        val filledStars = (completionRate * 5).toInt()
        
        // Update star visibility and color
        stars.forEachIndexed { index, star ->
            if (index < filledStars) {
                // Filled star - bright and visible
                star.alpha = 1.0f
                star.setTextColor(requireContext().getColor(R.color.accent_gold))
            } else {
                // Empty star - dimmed
                star.alpha = 0.3f
                star.setTextColor(requireContext().getColor(R.color.text_secondary))
            }
        }
        
        // Update completion text
        tvStarCompletion.text = "$filledStars/5 Stars"
        
        // Add animation for filled stars
        if (filledStars > 0) {
            stars.take(filledStars).forEach { star ->
                star.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(200)
                    .withEndAction {
                        star.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200)
                            .start()
                    }
                    .start()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            llHabitsContainer.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            llHabitsContainer.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    fun showAddHabitDialog() {
        showHabitDialog(null)
    }

    private fun showEditHabitDialog(habit: Habit) {
        showHabitDialog(habit)
    }

    private fun showHabitDialog(habit: Habit?) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_habit, null)
        
        val etName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etHabitDescription)
        val rvIcons = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvHabitIcons)
        val llStepsContainer = dialogView.findViewById<LinearLayout>(R.id.llStepsContainer)
        val etNewStep = dialogView.findViewById<EditText>(R.id.etNewStep)
        val btnAddStep = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAddStep)
        
        // Set up icon selection
        var selectedIcon = habit?.iconResId ?: R.drawable.ic_1
        val iconAdapter = HabitIconAdapter(habitIcons) { iconResId ->
            selectedIcon = iconResId
        }
        rvIcons.layoutManager = GridLayoutManager(requireContext(), 3)
        rvIcons.adapter = iconAdapter
        
        // Steps management
        val stepsList = mutableListOf<String>()
        habit?.steps?.let { stepsList.addAll(it) }
        
        fun updateStepsDisplay() {
            llStepsContainer.removeAllViews()
            stepsList.forEachIndexed { index, step ->
                val stepView = LayoutInflater.from(requireContext())
                    .inflate(android.R.layout.simple_list_item_1, llStepsContainer, false)
                val textView = stepView.findViewById<TextView>(android.R.id.text1)
                textView.text = "${index + 1}. $step"
                textView.setTextColor(resources.getColor(R.color.text_primary, null))
                
                val deleteButton = com.google.android.material.button.MaterialButton(requireContext()).apply {
                    text = "✕"
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setOnClickListener {
                        stepsList.removeAt(index)
                        updateStepsDisplay()
                    }
                }
                
                val stepContainer = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    addView(stepView)
                    addView(deleteButton)
                }
                
                llStepsContainer.addView(stepContainer)
            }
        }
        
        btnAddStep.setOnClickListener {
            val newStep = etNewStep.text.toString().trim()
            if (newStep.isNotEmpty()) {
                stepsList.add(newStep)
                etNewStep.text?.clear()
                updateStepsDisplay()
            }
        }
        
        // Pre-fill if editing
        habit?.let {
            etName.setText(it.name)
            etDescription.setText(it.description)
            updateStepsDisplay()
        }
        
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle(if (habit == null) "Add Habit" else "Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val description = etDescription.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    if (habit == null) {
                        // Add new habit
                        val newHabit = Habit(
                            name = name,
                            description = description,
                            iconResId = selectedIcon,
                            steps = stepsList.toMutableList()
                        )
                        dataManager.addHabit(newHabit)
                    } else {
                        // Update existing habit
                        val updatedHabit = habit.copy(
                            name = name,
                            description = description,
                            iconResId = selectedIcon,
                            steps = stepsList.toMutableList()
                        )
                        dataManager.updateHabit(updatedHabit)
                    }
                    loadHabits()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteHabitDialog(habit: Habit) {
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                dataManager.deleteHabit(habit.id)
                loadHabits()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateStepsUI(
        habit: Habit,
        tvCurrentStep: TextView,
        tvStepDescription: TextView,
        progressBarSteps: ProgressBar,
        btnPreviousStep: com.google.android.material.button.MaterialButton,
        btnNextStep: com.google.android.material.button.MaterialButton
    ) {
        val currentStep = habit.getCurrentStep()
        val stepNumber = habit.currentStep + 1
        val totalSteps = habit.steps.size
        
        tvCurrentStep.text = "Step $stepNumber of $totalSteps"
        tvStepDescription.text = currentStep ?: "No steps available"
        
        // Update progress bar
        if (totalSteps > 0) {
            val progress = ((stepNumber.toFloat() / totalSteps.toFloat()) * 100).toInt()
            progressBarSteps.progress = progress
        } else {
            progressBarSteps.progress = 0
        }
        
        // Update button states
        btnPreviousStep.isEnabled = habit.currentStep > 0
        btnNextStep.isEnabled = habit.currentStep < habit.steps.size - 1
        
        if (habit.isOnLastStep()) {
            btnNextStep.text = "→"
        } else {
            btnNextStep.text = "→"
        }
        
        if (habit.areAllStepsCompleted()) {
            tvStepDescription.text = "All steps completed! 🎉"
            progressBarSteps.progress = 100
        }
    }
}

