package com.cwl

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cwl.use_case.flow.FlowMainActivity

/**
 * @Author cwl
 * @Date 2022/10/9 4:51 下午
 * @Description
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, FlowMainActivity::class.java))
    }
}