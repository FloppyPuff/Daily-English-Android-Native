package com.dailyenglish.nativeapp.di

import com.dailyenglish.nativeapp.data.repository.NoopSyncTrigger
import com.dailyenglish.nativeapp.data.repository.SyncTrigger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
  @Binds
  @Singleton
  abstract fun bindSyncTrigger(impl: NoopSyncTrigger): SyncTrigger
}
