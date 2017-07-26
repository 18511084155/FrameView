package com.woodys.frame.transition

import android.animation.Animator
import android.view.View

/**
 * Created by woodys on 2017/7/24.
 * 默认的桢转换器
 */

class DefaultTransition : FrameTransition {

    override fun preAnim(v: View) {}

    override fun transitionIn(v: View): Animator? {
        v.visibility = View.VISIBLE
        return null
    }

    override fun transitionOut(v: View): Animator? {
        v.visibility = View.INVISIBLE
        return null
    }
}