package com.woodys.demos.widget

import android.content.Context
import android.util.AttributeSet
import com.woodys.demos.R
import com.woodys.frame.TemplateView
import com.woodys.frame.transition.DefaultTransition
import cz.widget.frame.transition.AlphaFrameTransition

/**
 * Created by cz on 2017/7/3.
 */
class FrameView(context: Context, attrs: AttributeSet?=null, defStyleAttr: Int=0) : TemplateView(context, attrs, defStyleAttr) {
    companion object {
        val CONTAINER=0
        val PROGRESS=1
        val EMPTY=2
        val ERROR=3
        val FRAME1=4
        val FRAME2=5
        val FRAME3=6
        val FRAME4=7
        init {
            //不同桢配置
            frame {
                id= PROGRESS
                layout= R.layout.frame_progress
                transition= AlphaFrameTransition()
            }
            frame {
                id= EMPTY
                layout= R.layout.frame_load_empty
                transition= AlphaFrameTransition()
            }
            frame {
                id= ERROR
                layout= R.layout.frame_load_error
                transition= AlphaFrameTransition()
            }
            frame {
                id= FRAME1
                layout= R.layout.frame_layout1
                //此转换器,里面设定了固定的元素转换,所以除了frame1-4以外,其他的frame不能用
                transition= DefaultTransition()
            }
            frame {
                id= FRAME2
                layout= R.layout.frame_layout2
                transition= DefaultTransition()
            }
            frame {
                id= FRAME3
                layout= R.layout.frame_layout3
                transition= DefaultTransition()
            }
            frame {
                id= FRAME4
                layout= R.layout.frame_layout4
                transition= DefaultTransition()
            }
        }
    }
    constructor(context: Context):this(context,null,0)
    constructor(context: Context, attrs: AttributeSet?=null):this(context,attrs,0)

}