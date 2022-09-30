package com.cwl.sample.layoutmanager

import android.view.View
import androidx.collection.ArraySet
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.cwl.sample.util.logi
import kotlin.math.abs

/**
 * 里面对于一些padding,margin的边界没有考虑
 * @property orientationHelper (OrientationHelper..OrientationHelper?)
 * @property currentPosition Int
 * @property fillAnchor Int
 * @property outChildren ArraySet<View>
 */
class SimpleLayoutManager2 : RecyclerView.LayoutManager() {
    companion object {
        const val FILL_START_TO_END = 1
        const val FILL_END_TO_START = -1
    }

    private val orientationHelper = OrientationHelper.createVerticalHelper(this)

    //当前要填充view的索引
    private var currentPosition: Int = 0

    //填充view的锚点,对应layoutChuck中要布局的view的top or start位置
    private var fillAnchor = 0

    //填充view的方向
    private var fillDirection = FILL_START_TO_END

    //将要回收的view
    private var outChildren = ArraySet<View>()

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    private var available = 0

    /**
     * 处理滚动同时处理填充和回收child
     * @param dy Int
     * @param recycler Recycler
     * @param state State
     * @return Int
     */
    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (childCount == 0 || dy == 0) {
            return 0
        }
        //确定布局方向
        fillDirection = if (dy > 0) FILL_START_TO_END else FILL_END_TO_START
        //滑动绝对值
        val absDelta = abs(dy)
        val scrollingOffset = updateLayoutParamOnScroll(absDelta, state)
        //实际消耗的位置为屏幕外的偏移值+布局实际消耗的大小
        val consumed = scrollingOffset + fill(available, recycler, state)
//        val consume = fillScroll(dy, recycler, state)
        //如果滑动的值大于消耗的值，则取消耗值，不然取滑动值
        val scrolled: Int = if (absDelta > consumed) fillDirection * consumed else dy
        offsetChildrenVertical(-scrolled)
        recycleChildren(dy, recycler)
        return scrolled
    }

    //初次填充可能会多次执行
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0) {
            removeAndRecycleAllViews(recycler)
        }
        if (state.isPreLayout) return
        currentPosition = 0
        fillAnchor = 0
        fillDirection = FILL_START_TO_END
        detachAndScrapAttachedViews(recycler)
        fill(orientationHelper.totalSpace, recycler, state)
    }

    /**
     * 回收child
     * @param delta Int 滑动的距离
     * @param recycler Recycler
     */
    private fun recycleChildren(delta: Int, recycler: RecyclerView.Recycler) {
        if (childCount == 0 || delta == 0) return
        //>0 回收前面的,<0回收后面的
        if (fillDirection == FILL_START_TO_END) {
            recycleStart()
        } else {
            recycleEnd()
        }
        recycleOutChildren(recycler)

        logChildrenPosition(recycler)
    }

    private fun recycleStart() {
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: break
            val end = getDecoratedBottom(child)
            //>0说明不需要回收，可以结束了
            if (end > 0) break
            //Log.i(javaClass.simpleName,"recycleStart -- ${getPosition(child)}")
            outChildren.add(child)
        }
    }

    private fun recycleEnd() {
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i) ?: break
            val start = getDecoratedTop(child)
            if (start < height) break
            //Log.i(javaClass.simpleName,"recycleEnd -- ${getPosition(child)}")
            outChildren.add(child)
        }
    }

    private fun recycleOutChildren(recycler: RecyclerView.Recycler) {
        for (view in outChildren) {
            removeAndRecycleView(view, recycler)
        }
        outChildren.clear()
    }

//    private fun fillScroll(
//        delta: Int,
//        recycler: RecyclerView.Recycler,
//        state: RecyclerView.State
//    ): Int {
//        return if (delta > 0) {
//            fillDirection = FILL_START_TO_END
//            fillEnd(delta, recycler, state)
//        } else {
//            fillDirection = FILL_END_TO_START
//            fillStart(delta, recycler, state)
//        }
//    }

    /**
     * 更新布局参数
     * @param absDelta 滑动绝对值
     * @param state 状态
     * @return 屏幕外的尺寸大小
     */
    private fun updateLayoutParamOnScroll(absDelta: Int, state: RecyclerView.State): Int {
        val layoutToEnd = fillDirection == FILL_START_TO_END
        //根据布局方向获取锚点视图
        val anchorView = getAnchorView()
        var scrollingOffset = 0;
        if (layoutToEnd) {
            //如果是从上到下布局
            //先计算超过屏幕的空间
            scrollingOffset = (orientationHelper.getDecoratedEnd(anchorView)
                    - orientationHelper.endAfterPadding)
            //如果是最后一个view，需要判断是不是滑动多了，如果是就进行修正
            val decoratedEndViewEnd = orientationHelper.getDecoratedEnd(anchorView)
            val endPosition = getPosition(anchorView ?: return 0)
            if (endPosition == state.itemCount - 1 && decoratedEndViewEnd - absDelta <= orientationHelper.end) {
                return decoratedEndViewEnd - orientationHelper.end
            }
            //currentPosition = endPosition + 1
            currentPosition = endPosition + fillDirection
            //计算布局起点位置
            fillAnchor = decoratedEndViewEnd
        } else {
            //如果是从下到上布局
            val decorateStartViewStart = orientationHelper.getDecoratedStart(anchorView)
            val startPosition = getPosition(anchorView ?: return 0)
            if (startPosition == 0 && decorateStartViewStart - absDelta >= paddingTop) {
                return decorateStartViewStart - paddingTop
            }
            //计算超过屏幕的空间
            scrollingOffset = (-orientationHelper.getDecoratedStart(anchorView)
                    + orientationHelper.startAfterPadding)
            //currentPosition = startPosition - 1
            currentPosition = startPosition + fillDirection
            //计算布局起点位置
            fillAnchor = decorateStartViewStart
        }
        //计算可用空间
        available = absDelta - scrollingOffset
        return scrollingOffset
    }

    /**
     * 获取锚点视图
     * @return 锚点视图
     */
    private fun getAnchorView(): View? {
        return if (fillDirection == FILL_START_TO_END) {
            getEndView()
        } else {
            getStartView()
        }
    }

//    //delta<0
//    private fun fillStart(
//        delta: Int,
//        recycler: RecyclerView.Recycler,
//        state: RecyclerView.State
//    ): Int {
//        //第一个view结束的边加移动的距离还不在屏幕内,说明不需要要填充
//        val startView = getStartView()
//        val decorateStartViewEnd = orientationHelper.getDecoratedEnd(startView)
//        if (decorateStartViewEnd - delta < paddingTop) {
//            return delta
//        }
//
//        val decorateStartViewStart = orientationHelper.getDecoratedStart(startView)
//        val startPosition = getPosition(startView ?: return delta)
//        if (startPosition == 0 && decorateStartViewStart - delta >= paddingTop) {
//            return decorateStartViewStart - paddingTop
//        }
//        val scrollingOffset = (-orientationHelper.getDecoratedStart(startView)
//                + orientationHelper.startAfterPadding)
//        //currentPosition = startPosition - 1
//        currentPosition = startPosition + fillDirection
//        fillAnchor = decorateStartViewStart + getChildHeight(startView)/*/2*/
//        logi(
//            javaClass.simpleName,
//            "fillAnchor--->${fillAnchor}    currentPosition---->${currentPosition}"
//        )
//        val available = delta + scrollingOffset
//        if (available >= 0) {
//            return delta
//        }
//        return fill(available, recycler, state)
//    }
//
//    //delta>0
//    private fun fillEnd(
//        delta: Int,
//        recycler: RecyclerView.Recycler,
//        state: RecyclerView.State
//    ): Int {
//        //最后一个view的开始边减去delta还没出现在屏幕内,说明不需要要填充
//        val endView = getEndView()
//        val decoratedEndViewStart = orientationHelper.getDecoratedStart(endView)
//        if (decoratedEndViewStart - delta > orientationHelper.end) {
//            return delta
//        }
//        val scrollingOffset = (orientationHelper.getDecoratedEnd(endView)
//                - orientationHelper.endAfterPadding)
//        //如果是最后一个view，需要判断是不是滑动多了，如果是就进行修正
//        val decoratedEndViewEnd = orientationHelper.getDecoratedEnd(endView)
//        val endPosition = getPosition(endView ?: return delta)
//        if (endPosition == state.itemCount - 1 && decoratedEndViewEnd - delta <= orientationHelper.end) {
//            return decoratedEndViewEnd - orientationHelper.end
//        }
//
//        //currentPosition = endPosition + 1
//        currentPosition = endPosition + fillDirection
//        fillAnchor = decoratedEndViewEnd
//        val available = delta - scrollingOffset
//        if (available <= 0) {
//            return delta
//        }
//        return fill(available, recycler, state)
//    }

    private fun fill(
        available: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        //开始大小
        val start = available
        var remainSpace = available
        while (remainSpace > 0 && hasMore(state)) {
            val child = nextView(recycler)
            if (fillDirection == FILL_START_TO_END) {
                addView(child)
            } else {
                addView(child, 0)
            }
            measureChildWithMargins(child, 0, 0)
            layoutChunk(child)
            remainSpace -= getDecoratedMeasuredHeight(child)
        }
        //logChildrenPosition(recycler)
        //实际消耗大小
        return start - remainSpace
    }

    private fun layoutChunk(child: View) {
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0

        left = paddingLeft
        right = left + getChildWidth(child)
        if (fillDirection == FILL_START_TO_END) {
            top = fillAnchor
            bottom = top + getChildHeight(child)
        } else {
            bottom = fillAnchor
            top = bottom - getChildHeight(child)
        }
        layoutDecoratedWithMargins(child, left, top, right, bottom)
        if (fillDirection == FILL_START_TO_END) {
            fillAnchor += getChildHeight(child)
        } else {
            fillAnchor -= getChildHeight(child)
        }
    }

    private fun hasMore(state: RecyclerView.State) =
        currentPosition >= 0 && currentPosition < state.itemCount

    /**
     * 获取下一个待填充的view
     */
    private fun nextView(recycler: RecyclerView.Recycler): View {
        val view = recycler.getViewForPosition(currentPosition)
        currentPosition += fillDirection
        return view
    }

    private fun getChildWidth(child: View): Int {
        val params = child.layoutParams as RecyclerView.LayoutParams
        return getDecoratedMeasuredWidth(child) + params.marginStart + params.marginEnd
    }

    private fun getChildHeight(child: View): Int {
        val params = child.layoutParams as RecyclerView.LayoutParams
        return getDecoratedMeasuredHeight(child) + params.topMargin + params.bottomMargin
    }

    private fun getStartView() = getChildAt(0)

    private fun getEndView() = getChildAt(childCount - 1)

    private fun logChildrenPosition(recycler: RecyclerView.Recycler) {
        val builder = StringBuilder()
        for (i in 0 until childCount) {
            val child = getChildAt(i)!!
            builder.append(getPosition(child))
            builder.append(",")
        }
        logi(javaClass.simpleName, "child position == $builder,child count == ${childCount}")
    }

}