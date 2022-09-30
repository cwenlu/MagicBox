package com.cwl.sample.layoutmanager

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.cwl.sample.R
import com.cwl.sample.layoutmanager.SimpleLayoutManager
import com.cwl.sample.layoutmanager.SimpleLayoutManager2

class SimpleActivity : AppCompatActivity() {
    private var rv: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        rv = findViewById<RecyclerView>(R.id.rv)
        //rv?.layoutManager = StackLayoutManager(LinearLayoutManager.VERTICAL)
        rv?.layoutManager = SimpleLayoutManager()
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

        rv?.postDelayed({
            rv?.smoothScrollToPosition(12)
        }, 3000)
    }
}

class SimpleAdapter(var data: List<String>) : RecyclerView.Adapter<SimpleAdapter.ViewHolder>() {

    inner class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(TextView(parent.context).apply {
            width = 300
            height = 300
            background = resources.getDrawable(R.drawable.shape_oval)
            gravity = Gravity.CENTER_VERTICAL
        })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder.itemView as TextView).text = data.getOrElse(position) { "" }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
