package com.cwl.magicbox.di.qa

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

/**
 * @Author cwl
 * @Date 2022/3/11 7:13 下午
 * @Description
 */
@InstallIn(SingletonComponent::class)
@Module
object QaNetworkModule {
    @Provides
    @Singleton
    @IntoSet
    fun provideHttpLoggingInterceptor(): Interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    @IntoSet
    fun provideChuckerInterceptor(@ApplicationContext context: Context): Interceptor =
        ChuckerInterceptor.Builder(context).redactHeaders(
            "trakt-api-key",
            "Authorization",
        ).build()
}