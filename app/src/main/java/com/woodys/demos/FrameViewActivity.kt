package com.woodys.demos

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_frame_view.*

class FrameViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frame_view)
        title=intent.getStringExtra("title")
        toolBar.title = title
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolBar.setNavigationOnClickListener{ finish() }

        /**
        //设定拦截器,在切换桢时,强制让某些桢不隐藏,做类似电商商品详情页,有加载框,但也显示内容的界面
        frameView.intercept { frame,lastFrame-> lastFrame== FrameView.CONTAINER&&frame==  FrameView.PROGRESS }
        frameView.setFrame(FrameView.PROGRESS)
        recyclerView.layoutManager= LinearLayoutManager(this)
        recyclerView.adapter= SimpleAdapter(this,DataProvider.ITEMS)
        radioLayout.setOnCheckedListener(object :RadioLayout.OnCheckedListener{
            override fun onChecked(v: View, position: Int, isChecked: Boolean) {
                frameView.setFrame(position)
            }
        })
        **/
    }
}
