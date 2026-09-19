package com.ascend75.feature.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.ScienceCardDao
import com.ascend75.core.database.entities.ScienceCardEntity
import com.ascend75.core.datastore.AscendPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScienceLibraryUiState(
    val currentDay: Int = 1,
    val searchQuery: String = "",
    val selectedCategory: String = "ALL",
    val unlockedCards: List<ScienceCardEntity> = emptyList(),
    val filteredCards: List<ScienceCardEntity> = emptyList(),
    val selectedCard: ScienceCardEntity? = null
)

@HiltViewModel
class ScienceLibraryViewModel @Inject constructor(
    private val scienceCardDao: ScienceCardDao,
    private val dailyRecordDao: DailyRecordDao,
    private val preferencesDataSource: AscendPreferencesDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScienceLibraryUiState())
    val uiState: StateFlow<ScienceLibraryUiState> = _uiState.asStateFlow()

    init {
        observeUnlockedCards()
    }

    private fun observeUnlockedCards() {
        viewModelScope.launch {
            preferencesDataSource.userPreferencesFlow.collect { prefs ->
                val challengeId = prefs.activeChallengeId
                val latestDay = if (challengeId != null) dailyRecordDao.getLatestDayNumber(challengeId) ?: 1 else 1

                scienceCardDao.observeUnlockedCards(latestDay).collect { cards ->
                    _uiState.update { state ->
                        state.copy(
                            currentDay = latestDay,
                            unlockedCards = cards,
                            filteredCards = filterCards(cards, state.searchQuery, state.selectedCategory)
                        )
                    }
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredCards = filterCards(state.unlockedCards, query, state.selectedCategory)
            )
        }
    }

    fun setSelectedCategory(category: String) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                filteredCards = filterCards(state.unlockedCards, state.searchQuery, category)
            )
        }
    }

    fun selectCard(card: ScienceCardEntity?) {
        _uiState.update { it.copy(selectedCard = card) }
    }

    fun toggleBookmark(card: ScienceCardEntity) {
        viewModelScope.launch {
            scienceCardDao.updateBookmark(card.dayNumber, !card.isBookmarked)
        }
    }

    private fun filterCards(cards: List<ScienceCardEntity>, query: String, category: String): List<ScienceCardEntity> {
        return cards.filter { card ->
            val matchesCategory = if (category == "ALL") true else card.category.equals(category, ignoreCase = true)
            val matchesQuery = if (query.isBlank()) true else {
                card.title.contains(query, ignoreCase = true) ||
                card.summary.contains(query, ignoreCase = true) ||
                card.actionItem.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }
}
