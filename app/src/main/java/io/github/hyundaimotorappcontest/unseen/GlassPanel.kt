package io.github.hyundaimotorappcontest.unseen

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout

class GlassPanel(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val density = resources.displayMetrics.density
    private val corner = 36 * density
    private val bounds = RectF()
    private val lightTransform = Matrix()
    private val light = RadialGradient(
        0f, 0f, 200 * density,
        intArrayOf(0xDDFFFFFF.toInt(), 0x686CE8FF, 0x006CE8FF),
        floatArrayOf(0f, 0.48f, 1f),
        Shader.TileMode.CLAMP,
    )
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = light }
    private var intensity = 0f
    private val fade = ValueAnimator.ofFloat(1f, 0f).apply {
        duration = 250
        addUpdateListener {
            intensity = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        setWillNotDraw(false)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                fade.cancel()
                lightTransform.setTranslate(event.x, event.y)
                light.setLocalMatrix(lightTransform)
                intensity = 1f
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                fade.setFloatValues(intensity, 0f)
                fade.start()
            }
        }

        // Children retain their normal click and accessibility behavior.
        return super.dispatchTouchEvent(event) || event.actionMasked != MotionEvent.ACTION_CANCEL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (intensity == 0f) return

        bounds.set(0f, 0f, width.toFloat(), height.toFloat())
        paint.alpha = (255 * intensity).toInt()
        canvas.drawRoundRect(bounds, corner, corner, paint)

        bounds.inset(density, density)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2 * density
        canvas.drawRoundRect(bounds, corner, corner, paint)
        paint.style = Paint.Style.FILL
    }

    override fun onDetachedFromWindow() {
        fade.cancel()
        intensity = 0f
        super.onDetachedFromWindow()
    }
}
