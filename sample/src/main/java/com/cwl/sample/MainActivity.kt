package com.cwl.sample

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cwl.sample.customview.LitePagerActivity
import com.cwl.sample.customview.TouchMoveTopViewActivity
import com.cwl.sample.jetpack.JetpackActivity
import com.cwl.sample.layoutmanager.BannerActivity
import com.cwl.sample.layoutmanager.SimpleActivity
import com.cwl.sample.util.jumpActivity

class MainActivity : AppCompatActivity() {
    private var rv: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv = findViewById<RecyclerView>(R.id.rv)
        rv?.layoutManager = LinearLayoutManager(this)
        rv?.adapter = SimpleAdapter(
            this,
            arrayListOf(
                "SimpleLayoutManager",
                "BannerLayoutManager",
                "TouchMoveTopView",
                "LitePager",
                "jetpack",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10",
                "11",
                "12",
                "13",
                "14",
                "15",
                "16",
                "17",
                "18",
            )
        )

    }

    class SimpleAdapter(private val context: Context, var data: List<String>) :
        RecyclerView.Adapter<SimpleAdapter.ViewHolder>() {

        inner class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(TextView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
                gravity = Gravity.CENTER
            })
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val text = data.getOrElse(position) { "" }
            (holder.itemView as TextView).text = text
            holder.itemView.setOnClickListener {
                when (text) {
                    "SimpleLayoutManager" -> {
                        context.jumpActivity<SimpleActivity>()
                    }
                    "BannerLayoutManager" -> {
                        context.jumpActivity<BannerActivity>()
                    }
                    "TouchMoveTopView" -> {
                        context.jumpActivity<TouchMoveTopViewActivity>()
                    }
                    "LitePager" -> {
                        context.jumpActivity<LitePagerActivity>()
                    }
                    "jetpack" -> {
                        context.jumpActivity<JetpackActivity>()
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}

