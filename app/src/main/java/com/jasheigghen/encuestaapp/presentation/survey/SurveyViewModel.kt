package com.jasheigghen.encuestaapp.presentation.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jasheigghen.encuestaapp.domain.model.Survey
import com.jasheigghen.encuestaapp.domain.model.Vote
import com.jasheigghen.encuestaapp.domain.repository.SurveyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SurveyUiState(
    val isLoading: Boolean = false,
    val surveys: List<Survey> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val repository: SurveyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SurveyUiState())
    val uiState: StateFlow<SurveyUiState> = _uiState.asStateFlow()

    init {
        loadSurveys()
    }

    fun loadSurveys() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val surveys = repository.getActiveSurveys()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    surveys = surveys,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    fun vote(surveyId: String, optionIndex: Int) {
        viewModelScope.launch {
            try {
                repository.submitVote(Vote(surveyId, optionIndex))
                // Opcional: Recargar o actualizar estado local
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
