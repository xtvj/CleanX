package github.xtvj.cleanx.di

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
open class PMModule {

    @Singleton
    @Provides
    fun providePM(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }


}