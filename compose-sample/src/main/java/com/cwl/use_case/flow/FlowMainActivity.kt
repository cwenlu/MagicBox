package com.cwl.use_case.flow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.cwl.use_case.R
import com.cwl.use_case.databinding.ActivityFlowMainBinding
import com.cwl.use_case.flow.with_lifecycle.CorrectUseFlowActivity
import com.cwl.use_case.flow.with_lifecycle.LifecycleFlowActivity

class FlowMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityFlowMainBinding>(this, R.layout.activity_flow_main)
            .run {
                lifecycleOwner = this@FlowMainActivity
                clickProxy = this@FlowMainActivity
            }
    }

    fun jumpLifecycleFlowActivity() {
        //startActivity(Intent(this, LifecycleFlowActivity::class.java))
        startActivity(Intent(this, CorrectUseFlowActivity::class.java))
    }
}