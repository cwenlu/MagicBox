package com.cwl.use_case.hilt

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

/**
 * @Author cwl
 * @Date 2022/3/8 3:44 下午
 * @Description
 */

@InstallIn(SingletonComponent::class)
@Module
object HiltModule {
    @ApplicationId
    @Provides
    fun provideApplicationId(application: Application): String = application.packageName

    @Singleton
    @Provides
    fun provideCoroutineDispatchers() = AppCoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main
    )

    //利用注解限定符
    @Magic
    @Provides
    fun provideMagic() = "Magic"

    //利用Named限定
    @Named(NAMED_MAGIC)
    @Provides
    fun provideNamedMagic() = "NamedMagic"


    //指定需要的入参，同时指定返回
    @Named(QUALIFIER_MAGIC)
    @Provides
    fun provideQualifierMagic(@Magic input: String): String = "Qualifier-$input"

    const val NAMED_MAGIC = "named_magic"
    const val QUALIFIER_MAGIC = "qualifier_magic"
}