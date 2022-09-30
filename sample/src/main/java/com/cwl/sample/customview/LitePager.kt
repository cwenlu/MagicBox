package com.cwl.sample.customview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import kotlin.math.abs

/**
 * @Author cwl
 * @Date 2022/4/26 10:36 上午
 * @Description
 *
 */
class LitePager(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var lastX = 0f
    private var downX = 0f
    private var beginDrag = false
    private var offset = 0f
    private var offsetPercent = 0f
    private var isChangeOrder = false
    private var animator: Animator? = null

    init {
        paint.color = Color.GREEN
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        setWillNotDraw(false)
    }

    companion object {
        class LitePagerLayoutParams : MarginLayoutParams {

            constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

            constructor(p: ViewGroup.LayoutParams?) : super(p)

            constructor(width: Int, height: Int) : super(width, height)

            var scale = 0f
            var alpha = 0f

            //左边的是0，右边的是1，中间的是2
            var from = 0
            var to = 0

        }

        const val MIN_SCALE = 0.8f
        const val MIN_ALPHA = 0.3f
    }

    //水平滑动一定距离则进行拦截
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.x
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
            }
            MotionEvent.ACTION_MOVE -> {
                val offsetX = x - lastX
                if (abs(offsetX) > touchSlop) {
                    lastX = x
                    beginDrag = true
                }
            }
            MotionEvent.ACTION_UP -> {
                beginDrag = false
            }
        }
        return beginDrag
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
            }
            MotionEvent.ACTION_MOVE -> {
                val offsetX = x - lastX
                offset += offsetX
                itemMove()
            }
            MotionEvent.ACTION_UP -> {
                beginDrag = false
                handleActionUp()
            }
        }
        lastX = x
        return true
    }

    //不做啥限制，简单的测量下
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val baseLineCenterY = height / 2
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            (child.layoutParams as LitePagerLayoutParams).run {
                child.alpha = alpha
                child.scaleX = scale
                child.scaleY = scale
            }
            var childWidth: Int = 0
            var childHeight: Int = 0

            if (child.width > 0 && child.height > 0) {
                childWidth = child.width
                childHeight = child.height
            } else {
                childWidth = child.measuredWidth
                childHeight = child.measuredHeight
            }

            val left = baseLineByChild(child) - childWidth / 2
            val right = left + childWidth
            val top = baseLineCenterY - childHeight / 2
            val bottom = top + childHeight
            child.layout(left, top, right, bottom)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 3) throw IllegalStateException("LitePager can only 3 child")
        //测试直接在这里设置了
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            (child.layoutParams as LitePagerLayoutParams).run {
                from = i
                if (i == childCount - 1) {
                    scale = 1f
                    alpha = 1f
                } else {
                    scale = MIN_SCALE
                    alpha = MIN_ALPHA
                }
            }

        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LitePagerLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return LitePagerLayoutParams(p)
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LitePagerLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDraw(canvas: Canvas) {
        //绘制下基线背景
        val child = getChildAt(0)
        if (child != null) {
            val bottom = child.top.toFloat()
            val top = child.top.toFloat()
            canvas.drawLine(0f, top, width.toFloat(), bottom, paint)
            canvas.drawLine(0f, top + child.height, width.toFloat(), bottom + child.height, paint)
        }

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val bottom = child.top.toFloat()
            val top = bottom + child.height
            val left = baseLineByChild(child).toFloat()
            canvas.drawLine(left, top, left, bottom, paint)
        }
        super.onDraw(canvas)
    }


    ///**
    // * 获取对应view该布局的基线位置
    // * @param child View
    // */
    //private fun baseLineByChild(child: View) = when (indexOfChild(child)) {
    //    //左边的
    //    0 -> width / 4
    //    //右边的
    //    1 -> width / 2 + width / 4
    //    //中间的
    //    2 -> width / 2
    //    else -> 0
    //}

    /**
     * 获取对应view该布局的基线位置
     * @param child View
     */
    private fun baseLineByChild(child: View): Int {
        val baseLineLeft = width / 4
        val baseLineRight = width - baseLineLeft
        val baseLineCenter = width / 2
        val lp = child.layoutParams as? LitePagerLayoutParams
        //计算 from--->to 的路径基线
        val baseLine = when (lp?.from) {
            0 -> when (lp?.to) {
                //目的是1,说明是左滑动
                1 -> baseLineLeft + (baseLineRight - baseLineLeft) * -offsetPercent
                2 -> baseLineLeft + (baseLineCenter - baseLineLeft) * offsetPercent
                else -> baseLineLeft
            }
            1 -> when (lp?.to) {
                0 -> baseLineRight + (baseLineRight - baseLineLeft) * -offsetPercent
                2 -> baseLineRight + (baseLineRight - baseLineCenter) * offsetPercent
                else -> baseLineRight
            }
            2 -> when (lp?.to) {
                0 -> baseLineCenter + (baseLineCenter - baseLineLeft) * offsetPercent
                1 -> baseLineCenter + (baseLineRight - baseLineCenter) * offsetPercent
                else -> baseLineCenter
            }
            //正常不会走这个分支
            else -> 0
        }
        return baseLine.toInt()
    }

    private fun itemMove() {
        offsetPercent = offset / width
        //更新from ,to 从而改变baseLine的位置,requestLayout就会导致绘制的位置发生变化
        updateChildrenFromAndTo()
        updateChildrenOrder()
        updateChildrenAlphaAndScale()
        requestLayout()
    }

    private fun updateChildrenFromAndTo() {
        if (abs(offsetPercent) >= 1) {
            isChangeOrder = false
            for (i in 0 until childCount) {
                val lp = getChildAt(i).layoutParams as? LitePagerLayoutParams
                lp?.from = lp?.to ?: 0
            }
            //处理溢出
            offset %= width
            offsetPercent %= 1
        } else {
            for (i in 0 until childCount) {
                val lp = getChildAt(i).layoutParams as? LitePagerLayoutParams
                lp?.to = when (lp?.from ?: 0) {
                    //如果是左边的view，向右滑动的话,则目标是中间，即2
                    // 向左的话目标是最右边，即1
                    0 -> if (offsetPercent > 0) 2 else 1
                    1 -> if (offsetPercent > 0) 0 else 2
                    2 -> if (offsetPercent > 0) 1 else 0
                    else -> return
                }
            }
        }
    }

    private fun updateChildrenOrder() {

        if (abs(offsetPercent) > .5f) {
            if (!isChangeOrder) {
                isChangeOrder = true
                exchangeOrder(1, 2)
            }
        } else {
            if (isChangeOrder) {
                exchangeOrder(1, 2)
                isChangeOrder = false
            }
        }
    }

    private fun exchangeOrder(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val from = getChildAt(fromIndex)
        val to = getChildAt(toIndex)

        detachViewFromParent(to)
        detachViewFromParent(from)

        attachViewToParent(
            from,
            if (toIndex > childCount) childCount else toIndex,
            from.layoutParams
        )
        attachViewToParent(
            to,
            if (fromIndex > childCount) childCount else fromIndex,
            to.layoutParams
        )

        invalidate()
    }

    private fun updateChildrenAlphaAndScale() {

        for (i in 0 until childCount) {
            updateAlphaAndScale(getChildAt(i))
        }
    }

    private fun updateAlphaAndScale(child: View) {
        val lp = child.layoutParams as LitePagerLayoutParams
        when (lp.from) {

            0 -> when (lp.to) {

                1 -> {
                    setAsBottom(child)
                }

                2 -> {
                    lp.alpha = MIN_ALPHA + (1f - MIN_ALPHA) * offsetPercent
                    lp.scale = MIN_SCALE + (1f - MIN_SCALE) * offsetPercent
                }
            }

            1 -> when (lp.to) {
                0 -> {
                    setAsBottom(child)
                }
                2 -> {
                    lp.alpha = MIN_ALPHA + (1f - MIN_ALPHA) * -offsetPercent
                    lp.scale = MIN_SCALE + (1f - MIN_SCALE) * -offsetPercent
                }
            }

            2 -> {
                lp.alpha = 1F - (1f - MIN_ALPHA) * abs(offsetPercent)
                lp.scale = 1F - (1f - MIN_SCALE) * abs(offsetPercent)
            }
        }
    }

    private fun setAsBottom(child: View) {
        exchangeOrder(indexOfChild(child), 0)
    }

    private fun handleActionUp() {
        if (childCount == 0) return
        val start = offset
        val end = when {
            offsetPercent > .5f -> width
            offsetPercent < -.5f -> -width
            else -> 0
        }
        startAnimator(start, end.toFloat())
    }

    private fun startAnimator(start: Float, end: Float) {
        if (start == end) return
        abortAnimator()
        animator = with(ValueAnimator.ofFloat(start, end)) {
            addUpdateListener {
                (it.animatedValue as? Float)?.run {
                    offset = this
                    itemMove()
                }
            }
            start()
            this
        }

    }

    private fun abortAnimator() {
        animator?.let { if (it.isRunning) it.cancel() }
    }
}