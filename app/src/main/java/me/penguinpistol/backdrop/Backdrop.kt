package me.penguinpistol.backdrop

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.annotation.IdRes
import com.google.android.material.shape.MaterialShapeDrawable

class Backdrop @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    @IdRes private val frontLayoutId: Int
    @IdRes private val backLayoutId: Int
    private var openRadius: Int

    private var frontLayout: View? = null
    private var backLayout: View? = null

    private var frontLayoutBackground: MaterialShapeDrawable? = null
    private var outlineProvider: ViewOutlineProvider? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.Backdrop).apply {
            frontLayoutId = getResourceId(R.styleable.Backdrop_frontLayout, 0)
            backLayoutId = getResourceId(R.styleable.Backdrop_backLayout, 0)
            openRadius = getDimensionPixelSize(R.styleable.Backdrop_openRadius, 0)
            recycle()
        }

        initFrontLayoutBackground()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        backLayout = findViewById<View?>(backLayoutId)?.apply {
            visibility = GONE
        }

        frontLayout = findViewById<View?>(frontLayoutId)?.apply {
            background = frontLayoutBackground
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        TODO("Not yet implemented")
    }

    private fun initFrontLayoutBackground() {
        frontLayoutBackground = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(Color.WHITE)
        }

        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.setRoundRect(
                    0,
                    0,
                    view?.width ?: 0,
                    if(view?.height != null) view.height + openRadius else 0,
                    openRadius.toFloat())
            }
        }
    }

    companion object {
        const val ANIMATE_DURATION = 200L
    }
}