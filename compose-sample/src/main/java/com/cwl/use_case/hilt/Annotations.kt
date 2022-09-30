package com.cwl.use_case.hilt

import javax.inject.Qualifier

/**
 * @Author cwl
 * @Date 2022/3/8 3:32 下午
 * @Description
 */

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationId

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Magic