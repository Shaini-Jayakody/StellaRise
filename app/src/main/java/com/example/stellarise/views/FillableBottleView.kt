package com.example.stellarise.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.stellarise.R

/**
 * Custom view that displays a bottle with fillable water level using bottle.png
 */
class FillableBottleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var bottleBitmap: Bitmap? = null
    private var fillPercentage: Float = 0f // 0.0 to 1.0
    private val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bottlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Bottle fill area - adjust these based on your bottle.png shape
    private val fillAreaTop = 0.25f // Start fill from 25% from top
    private val fillAreaBottom = 0.90f // End fill at 90% from top
    private val fillAreaLeft = 0.25f // Start fill from 25% from left
    private val fillAreaRight = 0.75f // End fill at 75% from right
    
    init {
        setupPaints()
        loadBottleImage()
    }
    
    private fun setupPaints() {
        // Water fill paint with gradient
        waterPaint.apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                context.getColor(R.color.info_blue),
                context.getColor(R.color.primary_star_light),
                Shader.TileMode.CLAMP
            )
        }
        
        // Bottle paint
        bottlePaint.apply {
            isFilterBitmap = true
            isDither = true
        }
        
        // Mask paint for clipping
        maskPaint.apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
    }
    
    private fun loadBottleImage() {
        try {
            val drawable = context.getDrawable(R.drawable.bottle)
            drawable?.let {
                bottleBitmap = Bitmap.createBitmap(
                    it.intrinsicWidth,
                    it.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bottleBitmap!!)
                it.setBounds(0, 0, canvas.width, canvas.height)
                it.draw(canvas)
            }
        } catch (e: Exception) {
            android.util.Log.e("FillableBottleView", "Error loading bottle image: ${e.message}")
        }
    }
    
    fun setFillPercentage(percentage: Float) {
        fillPercentage = percentage.coerceIn(0f, 1f)
        invalidate()
    }
    
    fun getFillPercentage(): Float = fillPercentage
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        
        // Save canvas state
        val saveCount = canvas.saveLayer(0f, 0f, width, height, null)
        
        // Draw bottle background
        bottleBitmap?.let { bitmap ->
            val destRect = RectF(0f, 0f, width, height)
            canvas.drawBitmap(bitmap, null, destRect, bottlePaint)
        }
        
        // Draw water fill
        if (fillPercentage > 0f) {
            val fillHeight = height * (fillAreaBottom - fillAreaTop) * fillPercentage
            val fillTop = height * fillAreaTop + (height * (fillAreaBottom - fillAreaTop) - fillHeight)
            val fillLeft = width * fillAreaLeft
            val fillRight = width * fillAreaRight
            val fillBottom = height * fillAreaBottom
            
            val fillRect = RectF(fillLeft, fillTop, fillRight, fillBottom)
            
            // Create rounded rectangle for water fill
            val path = Path()
            val cornerRadius = 12f
            path.addRoundRect(fillRect, cornerRadius, cornerRadius, Path.Direction.CW)
            
            canvas.drawPath(path, waterPaint)
        }
        
        // Restore canvas state
        canvas.restoreToCount(saveCount)
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Update gradient when size changes
        setupPaints()
    }
}
