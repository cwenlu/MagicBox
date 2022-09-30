package com.cwl.sample.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.cwl.sample.util.logi

/**
 *
 * @Author cwl
 * @Date 2022/4/24 5:18 下午
 * @Description
 *
 */
class TouchMoveTopView(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    /**
     * 移动view到最上层,可以保持原有顺序
     * @param target View
     */
    private fun move2Top(target: View) {
        val startIndex = indexOfChild(target)
        val count = childCount - 1 - startIndex
        //每次把target往上更新一下索引
        for (i in 0 until count) {
            val fromIndex = indexOfChild(target)
            val toIndex = fromIndex + 1
            val from = target
            val to = getChildAt(toIndex)

            //这个移除注意必须先移除大的索引，再移除小的索引,不然索引会出现问题，类似循环中删除的问题
            detachViewFromParent(toIndex)
            detachViewFromParent(fromIndex)

            attachViewToParent(to, fromIndex, to.layoutParams)
            attachViewToParent(from, toIndex, from.layoutParams)
        }
        invalidate()
    }

    /**
     * 交换
     * @param target View
     */
    private fun exchangeOrder(target: View) {
        val fromIndex = indexOfChild(target)
        val toIndex = childCount - 1
        val to = getChildAt(toIndex)

        detachViewFromParent(toIndex)
        detachViewFromParent(fromIndex)

        attachViewToParent(to, fromIndex, to.layoutParams)
        attachViewToParent(target, toIndex, target.layoutParams)

        invalidate()
    }


    /**
     * 判断是否点击某个view
     * @param target View
     * @param points FloatArray
     * @return Boolean
     */
    private fun pointInView(target: View, points: FloatArray): Boolean {
        points[0] = points[0] - target.left
        points[1] = points[1] - target.top
        val matrix = target.matrix
        //矩阵不是唯一,说明有应用变换
        if (!matrix.isIdentity) {
            //反转回去
            matrix.invert(matrix)
            //映射坐标点
            matrix.mapPoints(points)
        }
        //是否在target范围内
        return points[0] >= 0 && points[1] >= 0 && points[0] <= target.width && points[1] <= target.height
    }

    private fun findHitView(x: Float, y: Float): View? {
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)
            val points = FloatArray(2).apply {
                set(0, x)
                set(1, y)
            }
            if (pointInView(child, points)) {
                return child
            }
        }
        return null
    }

    private var target: View? = null
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            target = null
            val hitView = findHitView(ev.x, ev.y)
            val index = hitView?.let { indexOfChild(it) }
            if (index != childCount - 1) {
                target = hitView
                return true
            }
        }

        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            target?.let {
                //move2Top(it)
                exchangeOrder(it)
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var baseLineX = 200
        val baseLineY = height / 2
        for (i in 0 until childCount) {
            val child = getChildAt(i)

            var childWidth: Int = 0
            var childHeight: Int = 0

            if (child.width > 0 && child.height > 0) {
                childWidth = child.width
                childHeight = child.height
            } else {
                childWidth = child.measuredWidth
                childHeight = child.measuredHeight
            }

            var left = 0
            var top = 0
            var right = 0
            var bottom = 0

            left = baseLineX
            right = left + childWidth
            top = baseLineY - childHeight / 2
            bottom = top + childHeight
            baseLineX = right - 50

            child.layout(left, top, right, bottom)
            child.rotation = 45f
        }


    }
}