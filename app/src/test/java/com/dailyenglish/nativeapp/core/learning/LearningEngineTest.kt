package com.dailyenglish.nativeapp.core.learning

import com.dailyenglish.nativeapp.core.model.PaperRecord
import com.dailyenglish.nativeapp.core.model.Settings
import com.dailyenglish.nativeapp.core.model.StudyEvent
import com.dailyenglish.nativeapp.core.model.createState
import com.dailyenglish.nativeapp.data.content.SentenceDto
import com.dailyenglish.nativeapp.data.content.WordDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningEngineTest {
  private val at = 1_760_000_000_000L

  private fun word(id: String, hits: Int = 0, exam: String = "cet6") = WordDto(
    id = id,
    meaning = id,
    exams = listOf(exam),
    hits = mapOf(exam to hits),
  )

  private fun sentence(id: String) = SentenceDto(
    id = id,
    exams = listOf("cet6"),
    en = "English $id",
    zh = "中文 $id",
  )

  @Test
  fun rateWordFollowsWebIntervals() {
    var state = createState()
    state = LearningEngine.rateWord(state, "abandon", "good", at)
    assertEquals(3, state.records.getValue("abandon").interval)
    assertEquals(at + 3 * DAY_MILLIS, state.records.getValue("abandon").due)
    assertEquals(1, state.records.getValue("abandon").streak)
    assertEquals(true, state.events.last().isNew)

    state = LearningEngine.rateWord(state, "abandon", "good", at + DAY_MILLIS)
    assertEquals(7, state.records.getValue("abandon").interval)
    assertEquals(2, state.records.getValue("abandon").streak)
    assertEquals(false, state.events.last().isNew)

    state = LearningEngine.rateWord(state, "abandon", "easy", at + 2 * DAY_MILLIS)
    assertEquals(20, state.records.getValue("abandon").interval)
    assertEquals(3, state.records.getValue("abandon").streak)
    assertFalse(state.records.getValue("abandon").difficult)
  }

  @Test
  fun againAndHardMarkDifficultAndResetStreak() {
    var state = LearningEngine.rateWord(createState(), "abandon", "good", at)
    state = LearningEngine.rateWord(state, "abandon", "again", at + 1)
    val again = state.records.getValue("abandon")
    assertEquals(0, again.interval)
    assertEquals(0, again.streak)
    assertEquals(1, again.lapses)
    assertTrue(again.difficult)
    assertEquals(at + 1 + 600_000L, again.due)

    state = LearningEngine.rateWord(state, "abandon", "hard", at + 2)
    val hard = state.records.getValue("abandon")
    assertEquals(1, hard.interval)
    assertEquals(0, hard.streak)
    assertEquals(1, hard.lapses)
    assertTrue(hard.difficult)
    assertEquals(at + 2 + DAY_MILLIS, hard.due)
  }

  @Test
  fun intervalIsCappedAtOneYear() {
    var state = createState()
    state = state.copy(records = mapOf("abandon" to com.dailyenglish.nativeapp.core.model.WordRecord(interval = 300)))
    state = LearningEngine.rateWord(state, "abandon", "easy", at)
    assertEquals(365, state.records.getValue("abandon").interval)
    assertEquals(at + 365 * DAY_MILLIS, state.records.getValue("abandon").due)
  }

  @Test
  fun queueOrdersNewByPaperHitsAndReviewByDue() {
    val words = listOf(word("alpha", 1), word("beta", 5), word("gamma", 0))
    var state = createState()
    assertEquals(listOf("beta", "alpha", "gamma"), LearningEngine.buildQueue(words, state, "new", at).map { it.id })

    state = state.copy(
      records = mapOf(
        "alpha" to com.dailyenglish.nativeapp.core.model.WordRecord(due = at + 1000, lastReview = 1),
        "beta" to com.dailyenglish.nativeapp.core.model.WordRecord(due = at - 1000, lastReview = 2),
      ),
    )
    assertEquals(listOf("beta"), LearningEngine.buildQueue(words, state, "review", at).map { it.id })
    assertEquals(listOf("gamma"), LearningEngine.buildQueue(words, state, "new", at).map { it.id })
  }

  @Test
  fun dailyPlanCountsNewAndSentenceTargets() {
    val words = (0 until 25).map { word("w${('a' + it)}") }
    val sentences = (1..5).map { sentence("s00$it") }
    var state = createState()
    var plan = LearningEngine.dailyPlan(state, words, sentences, at)
    assertEquals("new", plan.active)
    assertEquals(0, plan.review.size)
    assertEquals(20, plan.newWords.size)
    assertEquals(3, plan.sentences.size)

    state = LearningEngine.rateWord(state, "wa", "good", at)
    state = LearningEngine.recordSentence(state, "s001", "answer", "mastered", at)
    plan = LearningEngine.dailyPlan(state, words, sentences, at)
    assertEquals(19, plan.newWords.size)
    assertEquals(2, plan.sentences.size)
    assertEquals(1, plan.steps.first { it.id == "new" }.done)
    assertEquals(1, plan.steps.first { it.id == "sentences" }.done)
  }

  @Test
  fun statisticsCountsUniquePracticeAndFocus() {
    val words = listOf(word("alpha"), word("beta"))
    var state = createState()
    state = LearningEngine.rateWord(state, "alpha", "good", at)
    state = LearningEngine.recordSentence(state, "s001", "answer", "mastered", at)
    state = state.copy(
      settings = Settings(exam = "cet6"),
      events = state.events + StudyEvent(type = "focus", seconds = 120, at = at, exam = "cet6"),
    )
    val stats = LearningEngine.statistics(state, words, at)
    assertEquals(1, stats.todayWords)
    assertEquals(1, stats.todayNew)
    assertEquals(1, stats.todaySentences)
    assertEquals(2, stats.todayMinutes)
    assertEquals(1, stats.learned)
    assertEquals(2, stats.total)
    assertEquals(1, stats.streak)
  }

  @Test
  fun reviewStreakSurvivesTodayWithoutStudy() {
    val words = listOf(word("alpha"))
    val yesterday = addDays(at, -1)
    val state = LearningEngine.rateWord(createState(), "alpha", "good", yesterday)
    val stats = LearningEngine.statistics(state, words, at)
    assertEquals(1, stats.streak)
  }

  @Test
  fun spellingIsTrimmedAndCaseInsensitive() {
    assertTrue(LearningEngine.checkSpelling("  Abandon ", "abandon"))
    assertFalse(LearningEngine.checkSpelling("abandons", "abandon"))
  }

  @Test
  fun heatmapKeepsTwelveWeeksOfUniqueExamEvents() {
    val state = createState().copy(
      events = listOf(
        StudyEvent("word", "alpha", at = at, exam = "cet6"),
        StudyEvent("word", "alpha", at = at + 1, exam = "cet6"),
        StudyEvent("sentence", "s001", at = at + 2, exam = "cet6"),
        StudyEvent("focus", seconds = 600, at = at, exam = "cet6"),
        StudyEvent("word", "alpha", at = at, exam = "postgrad"),
      ),
    )
    val stats = LearningEngine.statistics(state, listOf(word("alpha")), at)
    assertEquals(84, stats.heatmap.size)
    assertEquals(localDay(at), stats.heatmap.last().day)
    assertEquals(2, stats.heatmap.last().count)
    assertEquals(1, stats.heatmap.last().level)
    assertTrue(stats.heatmap.all { it.count <= 2 })
  }

  @Test
  fun mistakesQueueIncludesWrongPaperWords() {
    val words = listOf(word("alpha"), word("beta"), word("gamma"))
    val state = createState().copy(
      records = mapOf("alpha" to com.dailyenglish.nativeapp.core.model.WordRecord(difficult = true)),
      papers = mapOf("2025-2#0-0" to PaperRecord(words = listOf("beta"))),
    )
    assertEquals(listOf("alpha", "beta"), LearningEngine.buildQueue(words, state, "mistakes", at).map { it.id })
  }
}
