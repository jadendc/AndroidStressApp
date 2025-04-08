package com.anxietystressselfmanagement

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2

class ControlGaugeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    private var controlLevel: Int = 0
    private var listener: OnControlLevelSelectedListener? = null

    interface OnControlLevelSelectedListener {
        fun onControlLevelSelected(level: Int)
    }

    fun setOnControlLevelSelectedListener(l: OnControlLevelSelectedListener) {
        listener = l
    }

    fun setControlLevel(level: Int) {
        controlLevel = level
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height * 0.9f
        val radius = width * 0.4f
        val rectF = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        val levelColors = arrayOf(
            Color.RED,                        // Level 1
            Color.parseColor("#FFA500"),     // Orange (Level 2)
            Color.YELLOW,                    // Level 3
            Color.parseColor("#A8E6CF"),     // Light green (Level 4)
            Color.parseColor("#4CAF50")      // Darker green (Level 5)
        )

        for (i in 0 until 5) {
            paint.color = if (i < controlLevel) levelColors[i] else Color.LTGRAY
            canvas.drawArc(rectF, 180f + i * 36f, 36f, true, paint)
        }

        for (i in 0 until 5) {
            val angle = Math.toRadians((198 + i * 36).toDouble())
            val labelX = (cx + radius * 0.7 * Math.cos(angle)).toFloat()
            val labelY = (cy + radius * 0.7 * Math.sin(angle)).toFloat()
            canvas.drawText((i + 1).toString(), labelX, labelY, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val cx = width / 2f
                val cy = height * 0.9f
                val dx = event.x - cx
                val dy = event.y - cy
                val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                val normalizedAngle = (angle + 360) % 360

                when {
                    normalizedAngle in 180.0..216.0 -> setControlLevelAndNotify(1)
                    normalizedAngle in 216.0..252.0 -> setControlLevelAndNotify(2)
                    normalizedAngle in 252.0..288.0 -> setControlLevelAndNotify(3)
                    normalizedAngle in 288.0..324.0 -> setControlLevelAndNotify(4)
                    normalizedAngle in 324.0..360.0 -> setControlLevelAndNotify(5)
                    else -> return false
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun setControlLevelAndNotify(level: Int) {
        if (controlLevel != level) {
            controlLevel = level
            invalidate()
            listener?.onControlLevelSelected(level)
        }
    }
}
