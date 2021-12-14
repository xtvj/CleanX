package github.xtvj.cleanx.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import github.xtvj.cleanx.data.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
open class DatabaseModule {

//    @Singleton // Tell Dagger-Hilt to create a singleton accessible everywhere in ApplicationCompenent (i.e. everywhere in the application)
//    @Provides
//    fun provideDatabase(
//        @ApplicationContext context: Context
//    ) = Room.databaseBuilder(
//        context,
//        AppDatabase::class.java,
//        "cleanx"
//    ).build() // The reason we can construct a database for the repo

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideAppItemDao(db: AppDatabase) = db.appItemDao() // The reason we can implement a Dao for the database


    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context) : Context{
        return context
    }


}