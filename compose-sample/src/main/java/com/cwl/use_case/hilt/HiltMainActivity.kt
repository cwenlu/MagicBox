package com.cwl.use_case.hilt

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.cwl.use_case.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltMainActivity : AppCompatActivity() {
    private val vm by viewModels<HiltViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hilt_main)
        vm.printNamedMagic()
    }
}