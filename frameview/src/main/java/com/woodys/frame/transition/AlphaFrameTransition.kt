package cz.widget.frame.transition

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.woodys.frame.transition.FrameTransition

/**
 * Created by cz on 2017/7/3.
 */
class AlphaFrameTransition: FrameTransition {

    override fun preAnim(v: View) {
        v.visibility=View.VISIBLE
        v.alpha=0f
    }

    override fun transitionIn(v: View): Animator? {
        val objectAnimator = ObjectAnimator.ofFloat(v, "alpha", 1.0f)
        objectAnimator.duration=600
        return objectAnimator
    }

    override fun transitionOut(v: View): Animator? {
        val objectAnimator = ObjectAnimator.ofFloat(v, "alpha", 0f)
        objectAnimator.duration=600
        return objectAnimator
    }

}