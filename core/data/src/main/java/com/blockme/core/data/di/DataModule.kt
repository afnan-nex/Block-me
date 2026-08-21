package com.blockme.core.data.di

import android.content.Context
import androidx.room.Room
import com.blockme.core.common.Constants
import com.blockme.core.data.local.database.BlockMeDatabase
import com.blockme.core.data.local.database.GoalDao
import com.blockme.core.data.local.database.ScheduleDao
import com.blockme.core.data.local.database.SessionDao
import com.blockme.core.data.repository.GoalRepositoryImpl
import com.blockme.core.data.repository.ScheduleRepositoryImpl
import com.blockme.core.data.repository.SessionRepositoryImpl
import com.blockme.core.domain.repository.GoalRepository
import com.blockme.core.domain.repository.ScheduleRepository
import com.blockme.core.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for data layer dependencies.
 * SPDX-License-Identifier: MIT
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BlockMeDatabase =
        Room.databaseBuilder(context, BlockMeDatabase::class.java, Constants.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSessionDao(db: BlockMeDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideGoalDao(db: BlockMeDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideScheduleDao(db: BlockMeDatabase): ScheduleDao = db.scheduleDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository
}
