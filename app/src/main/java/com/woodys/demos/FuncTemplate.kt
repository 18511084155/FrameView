package com.woodys.demos

import android.app.Activity
import com.woodys.demos.model.SampleItem

/**
 * Created by woodys on 2017/7/24.
 */

class FuncTemplate {
    companion object {
        var items = mutableListOf<SampleItem<Activity>>()
        val groupItems = mutableMapOf<Int, List<SampleItem<Activity>>>()
        fun item(closure: SampleItem<Activity>.() -> Unit) {
            items.add(SampleItem<Activity>().apply(closure))
        }

        fun group(closure: () -> Unit) {
            closure.invoke()
            groupItems += items.groupBy { it.pid }
        }

        operator fun get(id: Int) = groupItems[id]

        operator fun contains(id: Int?): Boolean = groupItems.any { it.key == id }

        init {
            group {
                item {
                    id = 1
                    title = "FrameView"
                    desc = "状态选择器"

                    item {
                        pid = 1
                        title = "Frame各桢演示"
                        desc = "动态切换任一桢,并附带转换器动画效果"
                        clazz = FrameViewActivity::class.java
                    }

                    item {
                        pid = 1
                        title = "Frame嵌套Frame"
                        desc = "FrameView嵌套FrameView,并演示动态嵌套大小问题"
                        clazz = FrameViewActivity::class.java
                    }
                }
            }
        }
    }
}