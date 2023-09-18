package me.penguinpistol.backdrop

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.children
import kotlin.math.max
import kotlin.math.min

class Backdrop @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    @IdRes private val frontLayoutId: Int
    @IdRes private val backLayoutId: Int
    private var openRadius: Int
    private val frontLayoutMinHeight: Int
    private var duration: Long

    private var frontLayout: View? = null
    private var backLayout: View? = null

    private val translateAnimator = ValueAnimator.ofFloat(0F, 1F)
    private var viewSize = IntArray(2)
    private var isOpen = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.Backdrop).apply {
            background = getDrawable(R.styleable.Backdrop_android_background) ?: getDefaultBackground()
            frontLayoutId = getResourceId(R.styleable.Backdrop_frontLayout, 0)
            backLayoutId = getResourceId(R.styleable.Backdrop_backLayout, 0)
            openRadius = getDimensionPixelSize(R.styleable.Backdrop_openRadius, 0)
            frontLayoutMinHeight = getDimensionPixelSize(R.styleable.Backdrop_frontLayoutMinHeight, getActionBarSize())
            duration = getInt(R.styleable.Backdrop_duration, DEFAULT_ANIMATION_DURATION).toLong()
            recycle()
        }

        initTranslateAnimation()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        backLayout = findViewById<View?>(backLayoutId)?.apply {
            visibility = GONE
        }

        frontLayout = findViewById<View?>(frontLayoutId)?.apply {
            if(background == null) {
                setBackgroundColor(Color.WHITE)
            }
            clipToOutline = true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        children.forEach {
            measureChild(it, widthMeasureSpec, heightMeasureSpec)
            viewSize[0] = max(viewSize[0], it.measuredWidth)
            viewSize[1] = max(viewSize[1], it.measuredHeight)
        }

        setMeasuredDimension(
            measureSizeWithSpec(widthMeasureSpec, viewSize[0]),
            measureSizeWithSpec(heightMeasureSpec, viewSize[1])
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        children.forEach {
            it.layout(
                paddingLeft,
                paddingTop,
                paddingLeft + it.measuredWidth,
                paddingTop + it.measuredHeight
            )
        }

        if(isInEditMode) {
            backLayout?.visibility = VISIBLE
            animateFrontLayout(1F)
        }
    }

    override fun onInterceptTouchEvent(motionEvent: MotionEvent?): Boolean {
        if(isOpen && frontLayout != null) {
            motionEvent?.run {
                if(action == MotionEvent.ACTION_DOWN) {
                    val hitRect = Rect()
                    frontLayout!!.getHitRect(hitRect)
                    if(hitRect.contains(x.toInt(), y.toInt())) {
                        close()
                        return@onInterceptTouchEvent true
                    }
                }
            }
        }
        return false
    }

    private fun getDefaultBackground(): Drawable {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        return ColorDrawable(ContextCompat.getColor(context, typedValue.resourceId))
    }

    private fun getActionBarSize(): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
        return TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
    }

    private fun initTranslateAnimation() {
        translateAnimator.apply {
            duration = this@Backdrop.duration
            addUpdateListener { animator -> animateFrontLayout(animator.animatedFraction) }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    backLayout?.visibility = VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    backLayout?.visibility = if(isOpen) VISIBLE else GONE
                }
            })
        }
    }

    private fun measureSizeWithSpec(measureSpec: Int, value: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)

        return when(mode) {
            MeasureSpec.UNSPECIFIED, MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> min(size, value)
            else -> 0
        }
    }

    private fun animateFrontLayout(fraction: Float) {
        val targetY = (backLayout?.measuredHeight ?: 0) + this@Backdrop.paddingTop
        val maxTargetY = measuredHeight - frontLayoutMinHeight

        frontLayout?.run {
            y = min(targetY, maxTargetY) * fraction
            outlineProvider = object: ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(
                        0,
                        0,
                        view?.width ?: 0,
                        if(view?.height != null) view.height + openRadius else 0,
                        openRadius * fraction)
                }
            }
        }
    }

    fun open() {
        if(frontLayout == null || isOpen) {
            return
        }
        isOpen = true
        translateAnimator.start()
    }

    fun close() {
        if(frontLayout == null || !isOpen) {
            return
        }
        isOpen = false
        translateAnimator.reverse()
    }

    companion object {
        const val DEFAULT_ANIMATION_DURATION = 200
    }
}