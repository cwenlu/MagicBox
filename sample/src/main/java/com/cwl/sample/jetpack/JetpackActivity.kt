package com.cwl.sample.jetpack

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.cwl.sample.R
import com.cwl.sample.util.logi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JetpackActivity : AppCompatActivity() {
    private val vm by viewModels<JetpackViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jetpack)

        vm.testAsLiveData().observeForever {
            logi(javaClass.simpleName, it.toString())
        }
    }

}