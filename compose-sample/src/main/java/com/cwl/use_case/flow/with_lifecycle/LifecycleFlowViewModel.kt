package com.cwl.use_case.flow.with_lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * @Author cwl
 * @Date 2022/3/8 9:14 上午
 * @Description
 */
class LifecycleFlowViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(1)
    val uiState: StateFlow<Int> = _uiState

    private val _uiState2 = MutableStateFlow(1)
    val uiState2: StateFlow<Int> = _uiState2

    private val _uiStateLd = MutableLiveData<Int>(1)
    val uiStateLd: LiveData<Int> = _uiStateLd


    init {
        viewModelScope.launch {
            delay(2000)
            _uiState.value = _uiState.value + 1
            _uiState2.value = _uiState2.value + 1
            _uiStateLd.value = 2
        }
    }

    //convert to StateFlow
    val result = flow<Int> {
        emit(Random.nextInt())
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 1)

    //convert to SharedFlow
    val result2 = flow {
        emit(Random.nextInt())
    }.shareIn(scope = viewModelScope,started = SharingStarted.WhileSubscribed())
}