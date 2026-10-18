package com.dailyenglish.nativeapp.data.repository

import javax.inject.Inject
import javax.inject.Singleton

interface SyncTrigger {
  fun markDirty()
}

@Singleton
class NoopSyncTrigger @Inject constructor() : SyncTrigger {
  override fun markDirty() = Unit
}
