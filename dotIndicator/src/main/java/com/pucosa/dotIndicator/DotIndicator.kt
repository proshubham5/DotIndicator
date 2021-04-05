package com.pucosa.dotIndicator

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.Interpolator
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlin.math.abs

class DotIndicator(
    context: Context,
    attrs: AttributeSet? = null
): LinearLayout(context, attrs){

    //attributes declared
    private var selectOnClick: Boolean = true
    private var dotHeight: Int = -1
    private var dotWidth: Int = -1
    private var dotsCount: Int = 4
    private var selectedDotResId: Int = R.drawable.black_dot
    private var unselectedDotResId: Int = R.drawable.black_dot
    private var marginsBetweenDots: Int = -1
    private var animatorResId: Int = 0
    private var reverseAnimatorResId: Int = 0
    private var selectedIndex: Int = 0
    private var dotsTint: Int = 0

    //other variables
    private var animatorOut: Animator
    private var animatorIn: Animator
    private var immediateAnimatorOut: Animator
    private var immediateAnimatorIn: Animator

    var onClickListener: ((position: Int) -> Unit)? = null
    var onSelectionChangeListener: ((previousPos: Int, currentPos: Int) -> Unit)? = null

    //init dotIndicator
    init {
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.DotIndicator)
        val intrinsicWidth: Int
        val intrinsicHeight: Int
        val intrinsicMargin: Int
        val intrinsicOrientation: Int
        val intrinsicGravity: Int

        try {
            intrinsicWidth = ta.getDimensionPixelSize(R.styleable.DotIndicator_dot_width, -1)
            intrinsicHeight = ta.getDimensionPixelSize(R.styleable.DotIndicator_dot_height, -1)
            intrinsicMargin = ta.getDimensionPixelSize(R.styleable.DotIndicator_margins_between_dots, -1)
            intrinsicOrientation = ta.getInt(R.styleable.DotIndicator_dots_orientation, -1)
            intrinsicGravity = ta.getInt(R.styleable.DotIndicator_dots_gravity, -1)

            this.animatorResId = ta.getResourceId(
                R.styleable.DotIndicator_animator_res,
                R.animator.scale_with_alpha
            )
            this.reverseAnimatorResId =
                ta.getResourceId(R.styleable.DotIndicator_reverse_animator_res, 0)

            this.dotsTint = ta.getColor(R.styleable.DotIndicator_dots_tint, 0)
            this.selectedIndex = ta.getInt(R.styleable.DotIndicator_initial_selected_index, 0)
            this.selectOnClick = ta.getBoolean(R.styleable.DotIndicator_select_on_click, true)
            this.dotsCount = ta.getInt(R.styleable.DotIndicator_dots_count, 4)

        }
        finally {
            ta.recycle()
        }

        val miniSize = (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DEFAULT_INDICATOR_WIDTH.toFloat(),
            resources.displayMetrics
        ) + 0.5f).toInt()
        dotWidth = if (intrinsicWidth < 0) miniSize else intrinsicWidth
        dotHeight = if (intrinsicHeight < 0) miniSize else intrinsicHeight
        marginsBetweenDots = if (intrinsicMargin < 0) miniSize else intrinsicMargin

        animatorOut = createAnimatorOut()
        immediateAnimatorOut = createAnimatorOut()
        immediateAnimatorOut.duration = 0

        animatorIn = createAnimatorIn()
        immediateAnimatorIn = createAnimatorIn()
        immediateAnimatorIn.duration = 0


        orientation =
            if (intrinsicOrientation == VERTICAL) VERTICAL else HORIZONTAL
        gravity =
            if (intrinsicGravity >= 0) intrinsicGravity else Gravity.CENTER

        //render the dots now
        renderDots()

    }

    private fun renderDots() {
        removeAllViews()
        createDots(dotsCount)
    }

    private fun createDots(count: Int) {
        for (i in 0 until count) {
            val bgDrawable =
                if (selectedIndex == i) selectedDotResId else unselectedDotResId
            val animator =
                if (selectedIndex == i) immediateAnimatorOut else immediateAnimatorIn
            addDot(
                orientation = orientation,
                drawableRes = bgDrawable,
                animator = animator,
                    index = i
            )
        }
    }

    private fun addDot(
        orientation: Int,
        @DrawableRes drawableRes: Int,
        animator: Animator,
        index: Int
    ) {
        if (animator.isRunning) {
            animator.end()
            animator.cancel()
        }
        val indicator = View(context)

        var bgDrawable: Drawable? = ContextCompat.getDrawable(context, drawableRes)
        if (this.dotsTint != 0) {
            bgDrawable = bgDrawable?.tint(this.dotsTint)
        }
        indicator.background = bgDrawable

        indicator.setOnClickListener {
            onClickListener?.invoke(index)
            if (selectOnClick) {
                setSelectedIndex(index)
            }
        }

        addView(indicator)
        setLayoutParamOfDot(indicator, dotWidth, dotHeight, marginsBetweenDots, orientation)

        animator.setTarget(indicator)
        animator.start()
    }

    private fun changeBackgroundAndTintOfAllDots() {
        for (i in 0 until childCount) {
            val indicator = getChildAt(i)
            val bgDrawableRes =
                if (selectedIndex == i) selectedDotResId else unselectedDotResId
            var bgDrawable = ContextCompat.getDrawable(context, bgDrawableRes)
            if (this.dotsTint != 0) {
                bgDrawable = bgDrawable?.tint(this.dotsTint)
            }
            indicator.background = bgDrawable
        }
    }

    private fun setLayoutParamOfDot(dot: View, width: Int, height: Int, margin: Int, orientation: Int) {
        val lp = dot.layoutParams as LayoutParams
        lp.width = width
        lp.height = height

        if (orientation == HORIZONTAL) {
            lp.leftMargin = margin
            lp.rightMargin = margin
        } else {
            lp.topMargin = margin
            lp.bottomMargin = margin
        }

        dot.layoutParams = lp
    }

    private fun createAnimatorOut() = AnimatorInflater.loadAnimator(context, this.animatorResId)

    private fun createAnimatorIn(): Animator {
        val animatorIn: Animator
        if (this.reverseAnimatorResId == 0) {
            animatorIn = AnimatorInflater.loadAnimator(context, this.animatorResId)
            animatorIn.interpolator = ReverseInterpolator()
        } else {
            animatorIn = AnimatorInflater.loadAnimator(context, this.reverseAnimatorResId)
        }
        return animatorIn
    }

    private inner class ReverseInterpolator : Interpolator {
        override fun getInterpolation(value: Float) = abs(1.0f - value)
    }

    fun setSelectedIndex(index: Int) {
        if(index < 0 || index >= dotsCount) {
            return
        }

        if (animatorIn.isRunning) {
            animatorIn.end()
            animatorIn.cancel()
        }
        if (animatorOut.isRunning) {
            animatorOut.end()
            animatorOut.cancel()
        }

        val currentSelectedDot = getChildAt(selectedIndex)
        val toSelectDot = getChildAt(index)

        currentSelectedDot.setBackgroundResource(unselectedDotResId)
        animatorIn.setTarget(currentSelectedDot)
        animatorIn.start()

        toSelectDot.setBackgroundResource(selectedDotResId)
        animatorOut.setTarget(toSelectDot)
        animatorOut.start()

        onSelectionChangeListener?.invoke(selectedIndex, index)
        selectedIndex = index

    }

    fun getSelectedIndex(): Int {
        return selectedIndex
    }

    fun setDotTint(@ColorInt tint: Int) {
        this.dotsTint = tint
        changeBackgroundAndTintOfAllDots()
    }

    companion object {

        private const val DEFAULT_INDICATOR_WIDTH = 10
    }
}