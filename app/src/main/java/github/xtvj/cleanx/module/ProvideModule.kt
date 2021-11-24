package github.xtvj.cleanx.module

import android.content.Context
import android.content.pm.PackageManager
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import github.xtvj.cleanx.data.AppDatabase
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.data.repository.AppLocalRepository
import github.xtvj.cleanx.data.repository.AppRemoteRepository
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


    @Singleton // Tell Dagger-Hilt to create a singleton accessible everywhere in ApplicationCompenent (i.e. everywhere in the application)
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "cleanx"
    ).build() // The reason we can construct a database for the repo

    @Singleton
    @Provides
    fun provideAppItemDao(db: AppDatabase) = db.appItemDao() // The reason we can implement a Dao for the database


    @Singleton
    @Provides
    fun provideAppLocalRepository(appItemDao: AppItemDao) : AppLocalRepository{
        return AppLocalRepository(appItemDao)
    }

    @Singleton
    @Provides
    fun provideAppRemoteRepository(pm: PackageManager,localRepository: AppLocalRepository) : AppRemoteRepository{
        return AppRemoteRepository(pm,localRepository)
    }
}