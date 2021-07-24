package com.hfad.android.pomodoro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes

class ProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var periodMs = 0L
    private var currentMs = 0L
    private var color = 0
    private var style = FILL
    private val paint = Paint()

    var wasStarted = false
    init {
        if (attrs != null) {
            val styledAttrs = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ProgressView,
                defStyleAttr,
                0
            )
            color = styledAttrs.getColor(R.styleable.ProgressView_custom_color, Color.RED)
            style = styledAttrs.getInt(R.styleable.ProgressView_custom_style, FILL)
            styledAttrs.recycle()
        }
        paint.color = color
        paint.style = if (style == FILL) Paint.Style.FILL else Paint.Style.STROKE
        paint.strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (periodMs == 0L || currentMs == 0L) return
        val startAngel = (((currentMs % periodMs).toFloat() / periodMs) * 360)
        canvas.drawArc(0f, 0f, width.toFloat(), height.toFloat(), -90f, startAngel, true, paint)
    }

    fun setCurrent(current:Long){
        currentMs = current
        wasStarted = true
        invalidate()
    }

    fun  setPeriod(period:Long){
        periodMs = period
    }

    

    private companion object {
        private const val FILL = 0
    }
}