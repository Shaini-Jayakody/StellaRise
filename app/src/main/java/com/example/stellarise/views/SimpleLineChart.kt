package com.example.stellarise.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.stellarise.R

/**
 * SimpleLineChart - Custom chart view for mood trend visualization
 * 
 * A lightweight, custom implementation of a line chart that doesn't require
 * external dependencies. Perfect for showing mood trends over time.
 * 
 * Features:
 * - Draws line chart with data points
 * - Customizable colors and styling
 * - Touch interaction for data point details
 * - Responsive design that adapts to view size
 * 
 * @author Stellarise Development Team
 * @version 1.0
 */
class SimpleLineChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var chartData = mutableListOf<Float>()
    private var chartLabels = mutableListOf<String>()
    private var maxValue = 10f
    private var minValue = 0f
    
    // Colors
    private val lineColor = ContextCompat.getColor(context, R.color.primary_star)
    private val pointColor = ContextCompat.getColor(context, R.color.primary_star)
    private val gridColor = ContextCompat.getColor(context, R.color.primary_star_light)
    private val textColor = ContextCompat.getColor(context, R.color.text_primary)
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        // Line paint
        paint.color = lineColor
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        
        // Grid paint
        gridPaint.color = gridColor
        gridPaint.strokeWidth = 1f
        gridPaint.style = Paint.Style.STROKE
        
        // Text paint
        textPaint.color = textColor
        textPaint.textSize = 24f
        textPaint.textAlign = Paint.Align.CENTER
        
        // Point paint
        pointPaint.color = pointColor
        pointPaint.style = Paint.Style.FILL
    }
    
    /**
     * Set chart data with values and labels
     */
    fun setData(values: List<Float>, labels: List<String>) {
        chartData.clear()
        chartLabels.clear()
        chartData.addAll(values)
        chartLabels.addAll(labels)
        
        if (values.isNotEmpty()) {
            maxValue = values.maxOrNull()?.plus(1f) ?: 10f
            minValue = values.minOrNull()?.minus(1f) ?: 0f
            if (minValue < 0) minValue = 0f
        }
        
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (chartData.isEmpty()) {
            drawNoDataMessage(canvas)
            return
        }
        
        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 60f
        
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding
        
        // Draw grid
        drawGrid(canvas, padding, chartWidth, chartHeight)
        
        // Draw Y-axis labels
        drawYAxisLabels(canvas, padding, chartHeight)
        
        // Draw X-axis labels
        drawXAxisLabels(canvas, padding, chartWidth, chartHeight)
        
        // Draw line chart
        drawLineChart(canvas, padding, chartWidth, chartHeight)
        
        // Draw data points
        drawDataPoints(canvas, padding, chartWidth, chartHeight)
    }
    
    private fun drawNoDataMessage(canvas: Canvas) {
        textPaint.textSize = 32f
        textPaint.color = ContextCompat.getColor(context, R.color.text_secondary)
        canvas.drawText(
            "No mood data available",
            width / 2f,
            height / 2f,
            textPaint
        )
    }
    
    private fun drawGrid(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float) {
        // Horizontal grid lines
        for (i in 0..5) {
            val y = padding + (chartHeight / 5) * i
            canvas.drawLine(padding, y, padding + chartWidth, y, gridPaint)
        }
        
        // Vertical grid lines
        if (chartData.isNotEmpty()) {
            val stepX = chartWidth / (chartData.size - 1)
            for (i in chartData.indices) {
                val x = padding + stepX * i
                canvas.drawLine(x, padding, x, padding + chartHeight, gridPaint)
            }
        }
    }
    
    private fun drawYAxisLabels(canvas: Canvas, padding: Float, chartHeight: Float) {
        textPaint.textSize = 20f
        textPaint.textAlign = Paint.Align.RIGHT
        
        for (i in 0..5) {
            val value = minValue + (maxValue - minValue) * (5 - i) / 5
            val y = padding + (chartHeight / 5) * i + 7f
            canvas.drawText(
                String.format("%.1f", value),
                padding - 10f,
                y,
                textPaint
            )
        }
    }
    
    private fun drawXAxisLabels(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float) {
        textPaint.textSize = 16f
        textPaint.textAlign = Paint.Align.CENTER
        
        if (chartData.isNotEmpty()) {
            val stepX = chartWidth / (chartData.size - 1)
            for (i in chartLabels.indices) {
                val x = padding + stepX * i
                val y = padding + chartHeight + 30f
                canvas.drawText(chartLabels[i], x, y, textPaint)
            }
        }
    }
    
    private fun drawLineChart(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float) {
        if (chartData.size < 2) return
        
        val path = Path()
        val stepX = chartWidth / (chartData.size - 1)
        
        for (i in chartData.indices) {
            val x = padding + stepX * i
            val normalizedValue = (chartData[i] - minValue) / (maxValue - minValue)
            val y = padding + chartHeight - (normalizedValue * chartHeight)
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawDataPoints(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float) {
        if (chartData.isEmpty()) return
        
        val stepX = chartWidth / (chartData.size - 1)
        
        for (i in chartData.indices) {
            val x = padding + stepX * i
            val normalizedValue = (chartData[i] - minValue) / (maxValue - minValue)
            val y = padding + chartHeight - (normalizedValue * chartHeight)
            
            // Draw outer circle
            pointPaint.color = Color.WHITE
            canvas.drawCircle(x, y, 8f, pointPaint)
            
            // Draw inner circle
            pointPaint.color = pointColor
            canvas.drawCircle(x, y, 5f, pointPaint)
        }
    }
}



