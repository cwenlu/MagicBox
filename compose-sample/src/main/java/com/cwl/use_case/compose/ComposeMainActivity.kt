package com.cwl.use_case.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text

/**
 * @Author cwl
 * @Date 2022/3/9 2:11 下午
 * @Description
 */
class ComposeMainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Text("hello world")
        }
    }
}