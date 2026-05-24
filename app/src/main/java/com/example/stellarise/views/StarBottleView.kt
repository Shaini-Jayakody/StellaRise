package com.example.stellarise.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.stellarise.R

/**
 * Custom view that displays a bottle with gradient fill using PNG mask
 */
class StarBottleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var fillPercentage: Float = 0.0f
    private var bottleBitmap: Bitmap? = null
    private var gradientPaint: Paint? = null
    private var needsRedraw = true

    init {
        // Enable hardware acceleration for better performance
        setLayerType(LAYER_TYPE_HARDWARE, null)
        loadBottleImage()
    }

    private fun loadBottleImage() {
        try {
            val drawable = context.getDrawable(R.drawable.bottle)
            drawable?.let { d ->
                val width = 200
                val height = 300
                bottleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bottleBitmap!!)
                d.setBounds(0, 0, width, height)
                d.draw(canvas)
            }
        } catch (e: Exception) {
            // Fallback if bottle image not found
        }
    }

    fun setFillPercentage(percentage: Float) {
        val newPercentage = percentage.coerceIn(0f, 1f)
        if (newPercentage != fillPercentage) {
            fillPercentage = newPercentage
            needsRedraw = true
            invalidate()
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        
        if (width <= 0 || height <= 0) return
        
        // Draw the bottle image as background
        bottleBitmap?.let { bitmap ->
            val bottleWidth = width * 0.8f
            val bottleHeight = height * 0.9f
            val left = (width - bottleWidth) / 2f
            val top = (height - bottleHeight) / 2f
            val right = left + bottleWidth
            val bottom = top + bottleHeight
            
            val destRect = RectF(left, top, right, bottom)
            canvas.drawBitmap(bitmap, null, destRect, null)
        }
        
        // Draw gradient fill inside the bottle
        if (fillPercentage > 0f) {
            val bottleWidth = width * 0.8f
            val bottleHeight = height * 0.9f
            val left = (width - bottleWidth) / 2f
            val top = (height - bottleHeight) / 2f
            val right = left + bottleWidth
            val bottom = top + bottleHeight
            
            // Create gradient fill bitmap
            val fillBitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
            val fillCanvas = Canvas(fillBitmap)
            
            // Create gradient paint
            val gradient = LinearGradient(
                0f, bottom, 0f, top,
                Color.parseColor("#4FC3F7"),
                Color.parseColor("#81D4FA"),
                Shader.TileMode.CLAMP
            )
            
            val fillPaint = Paint().apply {
                shader = gradient
                isAntiAlias = true
            }
            
            // Calculate fill height from bottom to top
            val fillHeight = bottleHeight * fillPercentage
            val fillTop = bottom - fillHeight
            
            // Draw gradient rectangle
            val fillRect = RectF(left, fillTop, right, bottom)
            fillCanvas.drawRect(fillRect, fillPaint)
            
            // Use bottle image as mask
            val maskPaint = Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
                isAntiAlias = true
            }
            
            // Apply bottle image as mask to the gradient
            bottleBitmap?.let { bottle ->
                val srcRect = Rect(0, 0, bottle.width, bottle.height)
                val destRect = RectF(left, top, right, bottom)
                fillCanvas.drawBitmap(bottle, srcRect, destRect, maskPaint)
            }
            
            // Draw the masked gradient fill
            canvas.drawBitmap(fillBitmap, 0f, 0f, null)
        }
    }
}