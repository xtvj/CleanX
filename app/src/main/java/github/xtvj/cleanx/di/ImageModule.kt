package github.xtvj.cleanx.di

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
open class ImageModule {

    @Singleton
    @Provides
    fun provideImageLoaderX(pm : PackageManager, @ApplicationContext context: Context) : ImageLoaderX {
        return ImageLoaderX(pm,context)
    }

}