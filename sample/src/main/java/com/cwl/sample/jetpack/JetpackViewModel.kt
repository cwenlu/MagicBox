package com.cwl.sample.jetpack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * @Author cwl
 * @Date 2022/4/28 11:20 上午
 * @Description
 */
class JetpackViewModel : ViewModel() {
    /**
     * 验证[asLiveData]的[context]参数,[context]是收集上流数据是所用的上下文
     * 不传是[EmptyCoroutineContext]并没有生命周期
     *  vm.testAsLiveData().observeForever 不传在界面关闭的时候还是会回调
     *  平时一般不是强制观察的，所以看着没啥问题
     * @return LiveData<Int>
     */
    fun testAsLiveData()= flow {
        emit(1)
        delay(5000)
        emit(2)
    }.flowOn(Dispatchers.IO).asLiveData(viewModelScope.coroutineContext)
}