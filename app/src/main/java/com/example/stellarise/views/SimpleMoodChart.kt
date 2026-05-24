package com.example.stellarise.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.stellarise.R
import com.example.stellarise.data.MoodEntry

/**
 * Simple mood chart view that displays mood entries as a line chart
 */
class SimpleMoodChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var moodEntries: List<MoodEntry> = emptyList()
    private val chartPadding = 50f
    private val pointRadius = 10f
    
    // Cache for performance
    private var cachedBitmap: Bitmap? = null
    private var needsRedraw = true
    
    init {
        // Enable hardware acceleration for better performance
        setLayerType(LAYER_TYPE_HARDWARE, null)
        setupPaints()
    }
    
    private fun setupPaints() {
        // Line paint
        linePaint.color = context.getColor(R.color.primary_star)
        linePaint.strokeWidth = 6f
        linePaint.style = Paint.Style.STROKE
        
        // Point paint
        pointPaint.color = context.getColor(R.color.primary_star)
        pointPaint.style = Paint.Style.FILL
        
        // Text paint
        textPaint.color = context.getColor(R.color.text_secondary)
        textPaint.textSize = 24f
        textPaint.textAlign = Paint.Align.CENTER
        
        // Axis paint
        axisPaint.color = context.getColor(R.color.text_secondary)
        axisPaint.strokeWidth = 2f
        axisPaint.style = Paint.Style.STROKE
        
        // Label paint
        labelPaint.color = context.getColor(R.color.text_secondary)
        labelPaint.textSize = 12f
        labelPaint.textAlign = Paint.Align.CENTER
    }
    
    fun updateMoodEntries(entries: List<MoodEntry>) {
        moodEntries = entries.takeLast(7) // Show last 7 entries
        needsRedraw = true
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (moodEntries.isEmpty()) {
            drawEmptyState(canvas)
            return
        }
        
        // Use cached bitmap for better performance
        if (needsRedraw || cachedBitmap == null) {
            createCachedBitmap()
            needsRedraw = false
        }
        
        cachedBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }
    }
    
    private fun createCachedBitmap() {
        if (width <= 0 || height <= 0) return
        
        try {
            cachedBitmap?.recycle()
            // Use RGB_565 for better performance and less memory
            cachedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            val cacheCanvas = Canvas(cachedBitmap!!)
            drawChart(cacheCanvas)
        } catch (e: OutOfMemoryError) {
            // Fallback to simple drawing if memory issues
            android.util.Log.w("SimpleMoodChart", "Out of memory, using simple drawing")
            cachedBitmap = null
        }
    }
    
    private fun drawEmptyState(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        
        textPaint.color = context.getColor(R.color.text_secondary)
        textPaint.textSize = 32f
        canvas.drawText("📊", centerX, centerY - 20, textPaint)
        
        textPaint.textSize = 16f
        canvas.drawText("No mood data yet", centerX, centerY + 20, textPaint)
    }
    
    private fun drawChart(canvas: Canvas) {
        // Draw white background
        val backgroundPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        val chartWidth = width - (chartPadding * 2)
        val chartHeight = height - (chartPadding * 2)
        
        // Simplified drawing - only essential elements
        drawAxes(canvas, chartWidth, chartHeight)
        drawMoodLine(canvas, chartWidth, chartHeight)
        drawMoodPoints(canvas, chartWidth, chartHeight)
    }
    
    private fun drawAxes(canvas: Canvas, chartWidth: Float, chartHeight: Float) {
        val startX = chartPadding
        val endX = chartPadding + chartWidth
        val startY = chartPadding
        val endY = chartPadding + chartHeight
        
        // Draw X-axis (bottom)
        canvas.drawLine(startX, endY, endX, endY, axisPaint)
        
        // Draw Y-axis (left)
        canvas.drawLine(startX, startY, startX, endY, axisPaint)
        
        // Draw Y-axis labels (mood levels 1-8)
        for (i in 1..8) {
            val y = endY - (chartHeight * (i - 1) / 7)
            val label = i.toString()
            canvas.drawText(label, startX - 20, y + 4, labelPaint)
        }
        
        // Draw X-axis labels (time)
        if (moodEntries.isNotEmpty()) {
            val stepX = if (moodEntries.size > 1) chartWidth / (moodEntries.size - 1) else 0f
            moodEntries.forEachIndexed { index, entry ->
                val x = startX + (index * stepX)
                val timeLabel = formatTimeForChart(entry.timestamp)
                canvas.drawText(timeLabel, x, endY + 20, labelPaint)
            }
        }
    }
    
    private fun drawGridLines(canvas: Canvas, chartWidth: Float, chartHeight: Float) {
        val gridPaint = Paint().apply {
            color = context.getColor(R.color.primary_star_light)
            strokeWidth = 1f
            alpha = 100
        }
        
        // Horizontal grid lines (mood levels 1-8)
        for (i in 1..8) {
            val y = chartPadding + (chartHeight * (8 - i) / 7)
            canvas.drawLine(chartPadding, y, chartPadding + chartWidth, y, gridPaint)
        }
    }
    
    private fun drawMoodLine(canvas: Canvas, chartWidth: Float, chartHeight: Float) {
        if (moodEntries.size < 2) return
        
        val path = Path()
        val stepX = chartWidth / (moodEntries.size - 1)
        
        moodEntries.forEachIndexed { index, entry ->
            val x = chartPadding + (index * stepX)
            val moodLevel = entry.getMoodIntensity()
            val y = chartPadding + (chartHeight * (8 - moodLevel) / 7)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        canvas.drawPath(path, linePaint)
    }
    
    private fun drawMoodPoints(canvas: Canvas, chartWidth: Float, chartHeight: Float) {
        val stepX = if (moodEntries.size > 1) chartWidth / (moodEntries.size - 1) else 0f
        
        moodEntries.forEachIndexed { index, entry ->
            val x = chartPadding + (index * stepX)
            val moodLevel = entry.getMoodIntensity()
            val y = chartPadding + (chartHeight * (8 - moodLevel) / 7)
            
            // Draw mood image at each point
            val moodImageRes = entry.moodImage
            drawMoodImage(canvas, moodImageRes, x, y)
        }
    }
    
    
    private fun drawMoodImage(canvas: Canvas, moodImageRes: Int, x: Float, y: Float) {
        try {
            val drawable = context.getDrawable(moodImageRes)
            drawable?.let { d ->
                val imageSize = 60f
                val left = x - imageSize / 2
                val top = y - imageSize / 2
                val right = x + imageSize / 2
                val bottom = y + imageSize / 2
                
                d.setBounds(
                    left.toInt(),
                    top.toInt(),
                    right.toInt(),
                    bottom.toInt()
                )
                d.draw(canvas)
            }
        } catch (e: Exception) {
            // Fallback to simple circle
            canvas.drawCircle(x, y, pointRadius, pointPaint)
        }
    }
    
    private fun formatTimeForChart(timestamp: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        
        return String.format("%02d:%02d", hour, minute)
    }
    
}
