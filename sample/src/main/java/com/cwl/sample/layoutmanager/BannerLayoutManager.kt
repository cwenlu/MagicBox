package com.cwl.sample.layoutmanager

import android.graphics.PointF
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.cwl.sample.util.logi

/**
 * @Author cwl
 * @Date 2022/3/30 4:32 下午
 * @Description
 */
class BannerLayoutManager : RecyclerView.LayoutManager(),
    RecyclerView.SmoothScroller.ScrollVectorProvider {
    companion object {
        const val FILL_START_TO_END = 1
        const val FILL_END_TO_START = -1
    }

    private val orientationHelper = OrientationHelper.createHorizontalHelper(this)

    private var hasLayout = false
    private var currentPosition = 0
    private var fillAnchor = 0
    private var fillDirection = FILL_START_TO_END


    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }

    override fun canScrollHorizontally(): Boolean {
        return true
    }

    //需要重写这个外面配合PagerSnapHelper滑动才行,仅仅返回null空实现就行
    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        //if (childCount == 0) return null
        //val firstChildPosition = getPosition(getChildAt(0) ?: return null)
        //val direction = if (targetPosition < firstChildPosition) -1 else 1
        //return PointF(0f, direction.toFloat())
        return null
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        recycler ?: return dx
        state ?: return dx
        if (childCount == 0) return dx
        fillDirection = if (dx < 0) FILL_END_TO_START else FILL_START_TO_END
        when (fillDirection) {
            //>0
            FILL_START_TO_END -> {
                val view = getChildAt(childCount - 1)
                val end = orientationHelper.getDecoratedEnd(view)
                currentPosition = fillDirection + getPosition(view ?: return dx)
                fillAnchor = end
                if (end - dx <= orientationHelper.end) {
                    fill(recycler, state)
                    return dx
                }
            }
            //<0
            FILL_END_TO_START -> {
                val view = getChildAt(0)
                val start = orientationHelper.getDecoratedStart(view)
                currentPosition = fillDirection + getPosition(view ?: return dx)
                fillAnchor = start
                if (start - dx >= 0) {
                    fill(recycler, state)
                    return dx
                }
            }
        }
        offsetChildrenHorizontal(-dx)
        recycleChildren(recycler)
        //回收没写
        logi(javaClass.simpleName, "childCount----${childCount},${recycler.scrapList.size}")
        return dx
    }

    private fun recycleChildren(recycler: RecyclerView.Recycler) {
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: break
            //这里是模拟的vp,始终多缓存了一个，等完全隐藏再回收
            if (fillDirection == FILL_START_TO_END) {
                val right = getDecoratedRight(child)
                //right<orientationHelper.totalSpace
                if (right < 0) {
                    removeAndRecycleView(child, recycler)
                }
            } else {
                val left = getDecoratedLeft(child)
                //left>0
                if (left > orientationHelper.totalSpace) {
                    removeAndRecycleView(child, recycler)
                }
            }

        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        recycler ?: return
        state ?: return
        if (itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }
        if (state.isPreLayout) return

        if (hasLayout) return
        detachAndScrapAttachedViews(recycler)

        currentPosition = 0
        fillAnchor = 0
        fillDirection = FILL_START_TO_END
        fill(recycler, state)
        hasLayout = true
    }

    private fun fill(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        if (hasMore(state)) {
            val child = recycler.getViewForPosition(currentPosition)
            if (fillDirection == FILL_START_TO_END) {
                currentPosition += 1
                addView(child)
            } else {
                currentPosition -= 1
                addView(child, 0)
            }
            measureChildWithMargins(child, 0, 0)
            val width = orientationHelper.getDecoratedMeasurement(child)
            val left = if (fillDirection == FILL_START_TO_END) fillAnchor else fillAnchor - width
            val right = if (fillDirection == FILL_START_TO_END) fillAnchor + width else fillAnchor
            layoutDecoratedWithMargins(
                child,
                left,
                0,
                right,
                orientationHelper.getDecoratedMeasurementInOther(child)
            )
            fillAnchor = right
            //scaleChild()
        }

    }

    private fun scaleChild() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            //orientationHelper.getDecoratedMeasurement(child)/2
            //orientationHelper.totalSpace/2
            child?.scaleX = 0.5f
            child?.scaleY = 0.5f
        }
    }

    private fun hasMore(state: RecyclerView.State): Boolean =
        currentPosition >= 0 && currentPosition < state.itemCount

}