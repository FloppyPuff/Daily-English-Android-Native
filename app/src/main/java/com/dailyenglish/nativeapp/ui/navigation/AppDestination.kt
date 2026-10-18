package com.dailyenglish.nativeapp.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Today : NavKey

@Serializable
data object Practice : NavKey

@Serializable
data object Notebook : NavKey

@Serializable
data object Stats : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object Sentences : NavKey

@Serializable
data object Review : NavKey

@Serializable
data class Words(
  val mode: String = "new",
  val wordId: String? = null,
) : NavKey
