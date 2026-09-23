package com.ascend75.feature.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.domain.model.ScienceCard
import com.ascend75.core.domain.repository.ChallengeRepository
import com.ascend75.core.domain.repository.DailyRecordRepository
import com.ascend75.core.domain.repository.ScienceRepository
import com.ascend75.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScienceLibraryUiState(
    val isLoading: Boolean = true,
    val currentDay: Int = 1,
    val searchQuery: String = "",
    val selectedCategory: String = "ALL",
    val unlockedCards: List<ScienceCard> = emptyList(),
    val filteredCards: List<ScienceCard> = emptyList(),
    val selectedCard: ScienceCard? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScienceLibraryViewModel @Inject constructor(
    private val scienceRepository: ScienceRepository,
    private val challengeRepository: ChallengeRepository,
    private val dailyRecordRepository: DailyRecordRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScienceLibraryUiState())
    val uiState: StateFlow<ScienceLibraryUiState> = _uiState.asStateFlow()

    init {
        observeUnlockedCards()
    }

    /**
     * Keys the card query on the active challenge's latest day. A single `flatMapLatest` chain
     * replaces the previous nested collect, which leaked one live Room observer per emission.
     */
    private fun observeUnlockedCards() {
        viewModelScope.launch {
            combine(
                challengeRepository.observeActiveChallenge(),
                settingsRepository.preferences
            ) { challenge, prefs -> challenge?.id ?: prefs.activeChallengeId }
                .distinctUntilChanged()
                .flatMapLatest { challengeId ->
                    if (challengeId == null) {
                        scienceRepository.observeUnlockedCards(1).map { cards -> 1 to cards }
                    } else {
                        dailyRecordRepository.observeRecordsForChallenge(challengeId)
                            .map { records -> records.maxOfOrNull { it.dayNumber } ?: 1 }
                            .distinctUntilChanged()
                            .flatMapLatest { day ->
                                scienceRepository.observeUnlockedCards(day).map { cards -> day to cards }
                            }
                    }
                }
                .collect { (day, cards) ->
                    _uiState.update { state ->
                        state.copy(
                            // The seeded catalog always covers days 1..75, so an empty result means
                            // the seeder has not finished yet rather than a genuinely empty library.
                            isLoading = cards.isEmpty(),
                            currentDay = day,
                            unlockedCards = cards,
                            filteredCards = filterCards(cards, state.searchQuery, state.selectedCategory),
                            selectedCard = state.selectedCard?.let { selected ->
                                cards.firstOrNull { it.dayNumber == selected.dayNumber }
                            }
                        )
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

    fun selectCard(card: ScienceCard?) {
        _uiState.update { it.copy(selectedCard = card) }
    }

    fun toggleBookmark(card: ScienceCard) {
        viewModelScope.launch {
            scienceRepository.setBookmarked(card.dayNumber, !card.isBookmarked)
        }
    }

    private fun filterCards(cards: List<ScienceCard>, query: String, category: String): List<ScienceCard> {
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
