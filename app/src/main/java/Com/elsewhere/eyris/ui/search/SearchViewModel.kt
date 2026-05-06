package Com.elsewhere.eyris.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import Com.elsewhere.eyris.domain.models.Lead
import Com.elsewhere.eyris.domain.usecases.SearchBusinessesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

import Com.elsewhere.eyris.data.repositories.LeadsRepository
import Com.elsewhere.eyris.data.repositories.ContactedRepository

enum class SortType {
    NONE, NAME, SCORE
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchBusinessesUseCase: SearchBusinessesUseCase,
    private val leadsRepository: LeadsRepository,
    private val contactedRepository: ContactedRepository
) : ViewModel() {

    private val _rawLeads = MutableStateFlow<List<Lead>>(emptyList())
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _sortType = MutableStateFlow(SortType.NONE)
    val sortType = _sortType.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory = _filterCategory.asStateFlow()

    fun saveLead(lead: Lead) {
        viewModelScope.launch {
            leadsRepository.saveLead(lead)
        }
    }

    init {
        viewModelScope.launch {
            combine(_rawLeads, _sortType, _filterCategory) { leads, sort, category ->
                var filtered = if (category.isNullOrBlank()) {
                    leads
                } else {
                    leads.filter { it.category.equals(category, ignoreCase = true) }
                }

                when (sort) {
                    SortType.NAME -> filtered = filtered.sortedBy { it.businessName }
                    SortType.SCORE -> filtered = filtered.sortedByDescending { it.weightedScore }
                    SortType.NONE -> {}
                }
                filtered
            }.collect { filteredLeads ->
                val currentState = _uiState.value
                if (currentState is SearchUiState.Success || currentState is SearchUiState.Idle) {
                    _uiState.value = SearchUiState.Success(filteredLeads)
                }
            }
        }
    }

    fun search(location: String, category: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val existingLeads = leadsRepository.getLeads().map { it.businessName }
                val contactedLeads = contactedRepository.getContactedLeads().map { it.businessName }
                val excludedNames = (existingLeads + contactedLeads).toSet()

                val results = searchBusinessesUseCase(location, category)
                val filteredResults = results.filter { it.businessName !in excludedNames }
                
                _rawLeads.value = filteredResults
                _uiState.value = SearchUiState.Success(filteredResults)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown error")
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
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val leads: List<Lead>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
