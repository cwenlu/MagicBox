package com.cwl.use_case.flow.with_lifecycle

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cwl.use_case.R
import com.cwl.use_case.databinding.ActivityCorrectUseFlowBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 正确使用flow
 */
class CorrectUseFlowActivity : AppCompatActivity() {
    private val vm by viewModels<LifecycleFlowViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityCorrectUseFlowBinding>(
            this,
            R.layout.activity_correct_use_flow
        )
            .run {
                lifecycleOwner = this@CorrectUseFlowActivity
            }

        //收集一个flow
        lifecycleScope.launch {
            vm.uiState.flowWithLifecycle(
                this@CorrectUseFlowActivity.lifecycle,
                Lifecycle.State.STARTED
            )
                .collect {

                }
        }

        //收集多个flow
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.uiState.collect {

                    }
                }

                launch {
                    vm.uiState2.collect {

                    }
                }
            }
        }


    }
}