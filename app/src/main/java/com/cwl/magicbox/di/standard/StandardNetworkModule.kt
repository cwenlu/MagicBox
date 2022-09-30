package com.cwl.magicbox.di.standard

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import okhttp3.Interceptor

/**
 * @Author cwl
 * @Date 2022/3/11 7:10 下午
 * @Description
 */
@InstallIn(SingletonComponent::class)
@Module
object StandardNetworkModule {

    @Provides
    @ElementsIntoSet
    fun provideInterceptors(): Set<Interceptor> = emptySet()
}