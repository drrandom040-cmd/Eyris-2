package com.elsewhere.eyris.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsewhere.eyris.domain.models.Lead
import com.elsewhere.eyris.domain.usecases.SearchBusinessesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.elsewhere.eyris.data.repositories.LeadsRepository
import com.elsewhere.eyris.data.repositories.ContactedRepository
import android.util.Log

enum class SortType {
    NONE, NAME, SCORE
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchBusinessesUseCase: SearchBusinessesUseCase,
    private val leadsRepository: LeadsRepository,
    private val contactedRepository: ContactedRepository
) : ViewModel() {

    private val _rawResults = MutableStateFlow<List<Lead>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _sortType = MutableStateFlow(SortType.NONE)
    val sortType = _sortType.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory = _filterCategory.asStateFlow()

    val uiState: StateFlow<SearchUiState> = combine(
        _rawResults, _isLoading, _error, _sortType, _filterCategory
    ) { results, loading, error, sort, category ->
        if (loading) return@combine SearchUiState.Loading
        if (error != null) return@combine SearchUiState.Error(error)

        var filtered = if (category.isNullOrBlank()) {
            results
        } else {
            results.filter { it.category.equals(category, ignoreCase = true) }
        }

        when (sort) {
            SortType.NAME -> filtered = filtered.sortedBy { it.businessName }
            SortType.SCORE -> filtered = filtered.sortedByDescending { it.weightedScore }
            SortType.NONE -> {}
        }

        SearchUiState.Success(filtered)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState.Idle
    )

    fun saveLead(lead: Lead) {
        viewModelScope.launch {
            leadsRepository.saveLead(lead)
        }
    }

    fun search(location: String, category: String) {
        if (location.isBlank() || category.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Get existing data for exclusion
                val existingLeads = leadsRepository.getLeadsSync().map { it.businessName }
                val contactedLeads = contactedRepository.getContactedLeadsSync().map { it.businessName }
                val excludedNames = (existingLeads + contactedLeads).toSet()

                val results = searchBusinessesUseCase(location, category)
                val filteredResults = results.filter { it.businessName !in excludedNames }
                
                _rawResults.value = filteredResults
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search failed", e)
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSortType(type: SortType) {
        _sortType.value = type
    }

    fun setFilterCategory(category: String?) {
        _filterCategory.value = category
    }
}

sealed class SearchUiState {
    data object Idle : SearchUiState()
    data object Loading : SearchUiState()
    data class Success(val leads: List<Lead>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
