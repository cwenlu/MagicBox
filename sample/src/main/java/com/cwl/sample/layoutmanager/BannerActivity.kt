package com.cwl.sample.layoutmanager

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.cwl.sample.R

class BannerActivity : AppCompatActivity() {
    private var rv: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banner)
        rv = findViewById<RecyclerView>(R.id.rv)
        PagerSnapHelper().attachToRecyclerView(rv)
        rv?.layoutManager = BannerLayoutManager()
        rv?.adapter = SimpleAdapter(
            arrayListOf(
                "0",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
            )
        )
    }

    class SimpleAdapter(var data: List<String>) : RecyclerView.Adapter<SimpleAdapter.ViewHolder>() {

        inner class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(TextView(parent.context).apply {
                height=300
                setBackgroundColor(Color.CYAN)
                gravity = Gravity.CENTER
            })
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            (holder.itemView as TextView).text = data.getOrElse(position) { "" }
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}


