package github.xtvj.cleanx.module

import android.content.Context
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import github.xtvj.cleanx.utils.ImageLoader.ImageLoaderX
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
open class ProvideModule {
    @Singleton
    @Provides
   fun providePM(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }

    @Singleton
    @Provides
   fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Singleton
    @Provides
    fun provideImageLoaderX(pm : PackageManager,@ApplicationContext context: Context) : ImageLoaderX {
        return ImageLoaderX(pm,context)
    }
}