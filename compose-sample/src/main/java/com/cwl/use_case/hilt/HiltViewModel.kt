package com.cwl.use_case.hilt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

/**
 * @Author cwl
 * @Date 2022/3/8 3:44 下午
 * @Description
 */

//利用@Magic 注解注入基础变量
//利用Named 注入基础变量
@HiltViewModel
class HiltViewModel @Inject constructor(@Magic private val magic: String) : ViewModel() {
    @Inject
    @Named(HiltModule.NAMED_MAGIC)
    lateinit var namedMagic: String

    init {
        println(magic + "=====")
    }

    fun printNamedMagic() {
        println(namedMagic + "=====")
    }
}