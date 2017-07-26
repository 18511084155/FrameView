package com.woodys.frame

import com.woodys.frame.transition.DefaultTransition
import com.woodys.frame.transition.FrameTransition

/**
 * Created by woodys on 2017/7/22.
 */
class FrameItem {
    var id: Int = TemplateView.FRAME_CONTENT
    var layout: Int = TemplateView.INVALID_LAYOUT
    var transition: FrameTransition = DefaultTransition()
    override fun toString(): String = "id:$id layout$layout"
}