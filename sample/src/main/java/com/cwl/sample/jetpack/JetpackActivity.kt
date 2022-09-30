package com.cwl.sample.jetpack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.cwl.sample.R
import com.cwl.sample.util.logi

class JetpackActivity : AppCompatActivity() {
    private val vm by viewModels<JetpackViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jetpack)

        vm.testAsLiveData().observeForever {
            logi(javaClass.simpleName,it.toString())
        }
    }

}