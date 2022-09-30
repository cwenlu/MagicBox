package com.cwl.sample.layoutmanager

import android.graphics.PointF
import android.view.View
import androidx.collection.ArraySet
import androidx.recyclerview.widget.LinearSmoothScroller
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
class SimpleLayoutManager : RecyclerView.LayoutManager(),
    RecyclerView.SmoothScroller.ScrollVectorProvider {
    companion object {
        const val FILL_START_TO_END = 1
        const val FILL_END_TO_START = -1
    }

    private val orientationHelper = OrientationHelper.createVerticalHelper(this)

    private var fixOffset: Int = 0

    //当前要填充view的索引
    private var currentPosition: Int = 0

    //填充view的锚点,对应layoutChuck中要布局的view的top or start位置
    private var fillAnchor = 0

    //填充view的方向
    private var fillDirection = FILL_START_TO_END

    //将要回收的view
    private var outChildren = ArraySet<View>()

    //将要scrollTo的Position
    private var pendingScrollToPosition = RecyclerView.NO_POSITION

    //每次fill view后就记录下开始child和结束child的position
    private var startPosition = RecyclerView.NO_POSITION
    private var endPosition = RecyclerView.NO_POSITION

    //是否已经布局过,避免多次布局,
    private var hasLayout = false

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
        return scrollBy(dy, recycler, state)
    }

    private fun scrollBy(
        delta: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (childCount == 0 || delta == 0) {
            return 0
        }

        val consume = fillScroll(delta, recycler, state)
        offsetChildrenVertical(-consume)
        recycleChildren(delta, recycler)


        return consume
    }

    override fun scrollToPosition(position: Int) {
        if (childCount == 0 || position < 0 || position > itemCount - 1) return
        if (startPosition == RecyclerView.NO_POSITION || endPosition == RecyclerView.NO_POSITION) return
        if (pendingScrollToPosition in startPosition..endPosition) return
        pendingScrollToPosition = position
        requestLayout()
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        val linearSmoothScroller = LinearSmoothScroller(recyclerView.context)
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (childCount == 0) return null
        val firstChildPosition = getPosition(getStartView() ?: return null)
        val direction = if (targetPosition < firstChildPosition) -1 else 1
        return PointF(0f, direction.toFloat())
    }

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        pendingScrollToPosition = RecyclerView.NO_POSITION
    }

    //布局初始化的方法,初次填充可能会多次执行
    //键盘弹出或收起会重新回调这个方法
    //scrollToPosition也会，smoothScrollToPosition不会
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0) {
            removeAndRecycleAllViews(recycler)
        }
        if (state.isPreLayout) return
        when {
            isScrollToCase() -> {
                currentPosition = pendingScrollToPosition
                calcLayoutDirectionByScrollToPosition()
            }
            //防止反复布局,也可以处理输入框导致的重布局
            hasLayout -> {
                return
            }
            //这个方式再次填充处理输入框导致的重新布局
            //isKeyBoardCase() -> {
            //    currentPosition = getPosition(getStartView() ?: return)
            //    fixOffset = orientationHelper.getDecoratedStart(getStartView() ?: return)
            //}
            else -> {
                currentPosition = 0
            }
        }
        fillAnchor = 0
        fillDirection = FILL_START_TO_END
        detachAndScrapAttachedViews(recycler)
        fill(orientationHelper.totalSpace, recycler, state)
        hasLayout = true
        //fixScrollOffset(recycler, state)
    }

    private fun fixScrollOffset(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        if (fixOffset != 0) {
            scrollBy(-fixOffset, recycler, state)
            fixOffset = 0
        }
    }

    /**
     * 回收child
     * @param delta Int 滑动的距离
     * @param recycler Recycler
     */
    private fun recycleChildren(delta: Int, recycler: RecyclerView.Recycler) {
        if (childCount == 0 || delta == 0) return
        //>0 回收前面的,<0回收后面的
        if (delta > 0) {
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
            //logi(javaClass.simpleName,"recycleStart -- ${getPosition(child)}")
            outChildren.add(child)
        }
    }

    private fun recycleEnd() {
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i) ?: break
            val start = getDecoratedTop(child)
            //height 用orientationHelper.totalSpace更准确
            if (start < height) break
            //logi(javaClass.simpleName,"recycleEnd -- ${getPosition(child)}")
            outChildren.add(child)
        }
    }

    private fun recycleOutChildren(recycler: RecyclerView.Recycler) {
        for (view in outChildren) {
            removeAndRecycleView(view, recycler)
        }
        outChildren.clear()
    }

    private fun fillScroll(
        delta: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        return if (delta > 0) {
            fillDirection = FILL_START_TO_END
            fillEnd(delta, recycler, state)
        } else {
            fillDirection = FILL_END_TO_START
            fillStart(delta, recycler, state)
        }
    }

    //delta<0
    private fun fillStart(
        delta: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        val startView = getStartView()
        val startPosition = getPosition(startView ?: return delta)

        //第一个孩子top+dy不在屏幕内并且不是第一个孩子，则不用填充
        val decorateStartViewStart = orientationHelper.getDecoratedStart(startView)
        if (decorateStartViewStart - delta < paddingTop && startPosition != 0) {
            return delta
        }
        //处理修正
        if (startPosition == 0 && decorateStartViewStart - delta >= paddingTop) {
            return decorateStartViewStart - paddingTop
        }

        //currentPosition = startPosition - 1
        currentPosition = startPosition + fillDirection
        fillAnchor = decorateStartViewStart

        logi(javaClass.simpleName, "fillStart--${currentPosition}")

        return fill(delta, recycler, state)
    }

    //delta>0
    private fun fillEnd(
        delta: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        //最后一个view的开始边减去delta还没出现在屏幕内,说明不需要要填充
        val endView = getEndView()
        val endPosition = getPosition(endView ?: return delta)

        //如果是最后一个view，
        val decoratedEndViewEnd = orientationHelper.getDecoratedEnd(endView)
        if (decoratedEndViewEnd - delta > orientationHelper.end) {
            return delta
        }
        //处理修正
        if (endPosition == state.itemCount - 1 && decoratedEndViewEnd - delta <= orientationHelper.end) {
            return decoratedEndViewEnd - orientationHelper.end
        }

        //currentPosition = endPosition + 1
        currentPosition = endPosition + fillDirection
        fillAnchor = decoratedEndViewEnd

        logi(javaClass.simpleName, "fillEnd--${currentPosition}")

        return fill(delta, recycler, state)
    }

    private fun fill(
        available: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        var remainSpace = abs(available)
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

        if (!state.isMeasuring) {
            calcStartEndPosition()
        }

        //logChildrenPosition(recycler)
        return available
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

    private fun calcStartEndPosition() {
        if (childCount == 0) return
        startPosition = getPosition(getStartView() ?: return)
        endPosition = getPosition(getEndView() ?: return)
    }

    private fun isScrollToCase() = pendingScrollToPosition != RecyclerView.NO_POSITION
    private fun isKeyBoardCase() =
        startPosition != RecyclerView.NO_POSITION && endPosition != RecyclerView.NO_POSITION

    private fun calcLayoutDirectionByScrollToPosition() {
        if (pendingScrollToPosition >= endPosition) {
            fillDirection = FILL_END_TO_START
        }
        if (pendingScrollToPosition <= startPosition) {
            fillDirection = FILL_START_TO_END
        }
    }

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