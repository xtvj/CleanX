package github.xtvj.cleanx.utils.ImageLoader

import android.content.Context
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
open class ProvideModule {
    @Singleton
    @Provides
   fun provideFM(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }

    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context): FileCache {
        return FileCache(context)
    }

    @Singleton
    @Provides
    fun provideImageLoaderX(pm : PackageManager,fileCache: FileCache) : ImageLoaderX{
        return ImageLoaderX(pm,fileCache)
    }
}