package com.woodys.frame

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.IdRes
import android.support.annotation.MainThread
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.woodys.frame.transition.FrameTransition
import cz.widget.debugLog
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Created by woodys on 2017/7/23.
 */
open class TemplateView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {
    //初始化相关参数
    private val blockFrames = ConcurrentLinkedQueue<Frame>()//并发安全堆栈
    private val interceptFrames = mutableListOf<Int>()
    private val contentLayout = RelativeLayout(context)
    private val frameLayout: FrameLayout = FrameLayout(context)
    private var intercept: ((Int, Int) -> Boolean) = { _, _ -> false }
    private var lastFrame = FRAME_CONTENT
    private var delayAction: Runnable? = null
    private var startTransition = false
    //设置frame的堆栈信息记录实体对象
    private class Frame(val id: Int, val delayTime: Long)

    constructor(context: Context) : this(context, null, 0) {
        initContentsLayout()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    companion object {
        val FRAME_CONTENT = 0
        val INVALID_LAYOUT = 0
        private val frameItems = mutableListOf<FrameItem>()
        private var contentFrameItem = FrameItem()

        init {
            frameItems.add(contentFrameItem)
        }

        /**
         * 添加桢方法
         */
        fun frame(closure: FrameItem.() -> Unit) {//注意这里需要学习dsl的用法
            val frameItem = FrameItem().apply(closure)
            if (frameItems.any { it.id == frameItem.id }) {
                throw IllegalArgumentException("Frame id exists! frame:$frameItem")
            } else if (INVALID_LAYOUT == frameItem.layout) {
                throw IllegalArgumentException("Frame layout is invalid! frame:$frameItem")
            } else {
                frameItems.add(frameItem)
            }
        }
    }

    /**
     * 桢拦截器
     */
    fun intercept(intercept: (Int, Int) -> Boolean) {
        this.intercept = intercept
    }

    /**
     * 内容桢转换器
     */
    fun setContentTransition(closure: () -> FrameTransition) {
        contentFrameItem.transition = closure.invoke()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        //初始化布局
        initContentsLayout()
    }

    private fun initContentsLayout() {
        //添加子控件
        (0..childCount - 1).map { getChildAt(it) }.forEach {
            removeView(it)
            contentLayout.addView(it, it.layoutParams)
        }
        frameLayout.id = getFrameId(frameLayout)
        //添加内容体
        contentLayout.id = getFrameId(this)
        if (null == layoutParams) {
            layoutParams= LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }
        addView(contentLayout,layoutParams)
    }



    /**
     * 当此桢被装载
     */
    open fun onLoadFrame(id: Int, v: View) {
    }

    private fun getFrameId(view: View, frameId: Int = 0) = System.identityHashCode(view) + frameId

    fun findFrameViewById(@IdRes id: Int): View = frameLayout.findViewById(id)

    /**
     * 查找到桢布局
     */
    private fun findFrameLayoutById(frameId: Int): View {
        val findFrameLayout = findViewById(getFrameId(frameLayout))
        if (null == findFrameLayout) {
            //添加桢布局体
            val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            addView(frameLayout, layoutParams)
        }
        //检测桢是否存在,未添加及时添加
        var frameView = findViewById(getFrameId(this, frameId))
        //未装载
        if (null == frameView) {
            frameItems.find { it.id == frameId }?.let {
                frameView = LayoutInflater.from(context).inflate(it.layout, frameLayout, false)
                frameView.id = getFrameId(this, frameId)
                frameLayout.addView(frameView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
                //装载桢回调
                onLoadFrame(frameId, frameView)
            }
        }
        return frameView
    }

    /**
     * 设置展示桢view
     * @param frameId   当前展示桢
     */
    @MainThread
    fun setFrame(frameId: Int, delayTime: Long = 0) {
        blockFrames.add(Frame(frameId, delayTime))
        debugLog("setFrame:$frameId")
        if (1 == blockFrames.size && !startTransition) {
            popNextBlockFrame()
        }
    }

    private fun setFrameInner(frameId: Int) {
        if (lastFrame == frameId) {
            popNextBlockFrame()
        } else {
            startTransition = true
            var lastFrameView = findFrameLayoutById(lastFrame)
            var lastFrameItem = frameItems.find { it.id == lastFrame }
            val frameView = findFrameLayoutById(frameId)
            val frameItem = frameItems.find { it.id == frameId }
            //处理上一桢
            var outAnimator: Animator? = null
            if (null != lastFrameItem && !intercept.invoke(frameId, lastFrame)) {
                //拦截器设定
                outAnimator = lastFrameItem.transition.transitionOut(lastFrameView)
            }
            //处理当前桢
            if (null != frameItem) {
                if (0 != frameView.width && 0 != frameView.height) {
                    startTransition(frameItem, frameView, outAnimator, lastFrameView, frameId)
                } else {
                    //尚未初始化.
                    frameView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            if (0 != frameView.width && 0 != frameView.height) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                    frameView.viewTreeObserver.removeGlobalOnLayoutListener(this)
                                } else {
                                    frameView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                }
                                startTransition(frameItem, frameView, outAnimator, lastFrameView, frameId)
                            }
                        }
                    })
                }
            }
        }
    }

    /**
     * 开始执行转换
     */
    private fun startTransition(frameItem: FrameItem, frameView: View, outAnimator: Animator?, lastFrameView: View, frameId: Int) {
        frameItem.transition.preAnim(frameView)
        if (null == outAnimator) {
            transitionIn(frameItem, frameView, lastFrameView, frameId)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                outAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        transitionIn(frameItem, frameView, lastFrameView, frameId)
                    }
                })
                outAnimator.start()
            } else {
                transitionIn(frameItem, frameView, lastFrameView, frameId)
            }
        }
    }

    /**
     * 新的展示桢进入
     */
    private fun transitionIn(frameItem: FrameItem, frameView: View, lastFrameView: View, frameId: Int) {
        //上一桢处理
        if (intercept.invoke(frameId, lastFrame)) {
            interceptFrames.add(lastFrame)
        } else {
            //隐藏之前被拦截桢
            hideFrameView(lastFrameView)
            interceptFrames.map { findFrameLayoutById(it) }.forEach(this::hideFrameView)
            interceptFrames.clear()
        }
        val transitionIn = frameItem.transition.transitionIn(frameView)
        if (null == transitionIn) {
            //设置新的桢数
            lastFrame = frameId
            popNextBlockFrame()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                transitionIn.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        //设置新的桢数
                        lastFrame = frameId
                        popNextBlockFrame()
                    }
                })
                transitionIn.start()
            } else {
                //设置新的桢数
                lastFrame = frameId
                popNextBlockFrame()
            }
        }
    }

    private fun popNextBlockFrame() {
        val frameItem = blockFrames.poll()
        if (null == frameItem) {
            startTransition = false
        } else {
            if (0L == frameItem.delayTime)
                setFrameInner(frameItem.id)
            else {
                removeCallbacks(delayAction)
                delayAction = Runnable { setFrameInner(frameItem.id) }
                postDelayed(delayAction, frameItem.delayTime)
            }
        }

    }

    private fun hideFrameView(frame: View?) {
        frame?.visibility = if (contentLayout == frame) View.INVISIBLE else View.GONE
    }

    fun getCurrentFrame() = lastFrame

    fun isFrame(frameId: Int) = lastFrame == frameId

    override fun onDetachedFromWindow() {
        removeCallbacks(delayAction)
        super.onDetachedFromWindow()
    }


    override fun onSaveInstanceState(): Parcelable {
        val state = super.onSaveInstanceState()
        return Bundle().apply {
            putParcelable("state", state)
            putInt("frame", lastFrame)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        val state = bundle.getParcelable<Parcelable>("state")
        setFrame(bundle.getInt("frame", FRAME_CONTENT))
        super.onRestoreInstanceState(state)
    }

}
