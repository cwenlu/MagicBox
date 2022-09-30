package com.cwl.use_case.flow.with_lifecycle

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

/**
 * @Author cwl
 * @Date 2022/3/8 9:14 上午
 * @Description flow结合lifecycle使用问题分析
 */
class LifecycleFlowActivity : AppCompatActivity() {
    private val vm by viewModels<LifecycleFlowViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            View(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.GRAY)
            }
        )

        //这样pause,stop 了还是能收到消息
        vm.uiState.onEach {
            println("${it}======Flow")
        }.launchIn(lifecycleScope)

        lifecycleScope.launch {
            vm.uiState.collect {
                println("${it}======Flow collect")
            }
        }


        //stop 不能收到，如启动dialog，或透明activity，pause了但是没stop，这个时候还是会走
        //同理 lifecycleScope.launchWhenResumed pause了就不会走了
        //建议使用repeatOnLifecycle，这个api未来可能被移除
        //只在destory的时候取消，其他的只是挂起
        lifecycleScope.launchWhenStarted {
            vm.uiState.collect {
                println("${it}======Flow launchWhenStarted")
            }
        }

        lifecycleScope.launch {
            //达到STARTED时启动一个新的coroutine执行，低于给定值时取消
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.uiState.collect {
                    println("${it}======Flow repeatOnLifecycle")
                }
            }
            //这里除非界面destroy，否则不会执行
        }

        vm.uiStateLd.observe(this) {
            println("${it}======LiveData")
        }

    }
}