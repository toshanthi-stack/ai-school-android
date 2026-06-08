package com.lillytech.aischool.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lillytech.aischool.core.model.Course
import com.lillytech.aischool.core.model.Lesson
import com.lillytech.aischool.core.network.AISchoolApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface SyllabusUiState {
    data object Loading : SyllabusUiState
    data class Ready(val courses: List<Course>) : SyllabusUiState
    data class Error(val message: String) : SyllabusUiState
}

/**
 * Single source of truth for the syllabus tree on the mobile flavor.
 * Requests the full VISUAL payload (videos, code editors, sandboxes).
 */
class SyllabusViewModel : ViewModel() {

    private val apiClient = AISchoolApiClient()

    private val _uiState = MutableStateFlow<SyllabusUiState>(SyllabusUiState.Loading)
    val uiState: StateFlow<SyllabusUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = SyllabusUiState.Loading
            _uiState.value = try {
                val courses = withContext(Dispatchers.IO) {
                    apiClient.fetchSyllabus(AISchoolApiClient.PayloadMode.VISUAL)
                }
                SyllabusUiState.Ready(courses)
            } catch (t: Throwable) {
                SyllabusUiState.Error(t.message ?: "Unable to load the AI School syllabus")
            }
        }
    }

    fun course(courseId: String?): Course? =
        (uiState.value as? SyllabusUiState.Ready)?.courses?.firstOrNull { it.id == courseId }

    fun lesson(lessonId: String?): Pair<Course, Lesson>? {
        val courses = (uiState.value as? SyllabusUiState.Ready)?.courses ?: return null
        for (course in courses) {
            course.lessons.firstOrNull { it.id == lessonId }?.let { return course to it }
        }
        return null
    }

    override fun onCleared() {
        apiClient.close()
    }
}
