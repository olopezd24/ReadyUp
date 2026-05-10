package com.example.readyup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readyup.data.model.*
import com.example.readyup.data.remote.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LibraryState(
    val items: List<StatusItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentStatus: String = "PLAYING"
)

data class FeedState(
    val items: List<FeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ProfileState(
    val reviews: List<MyReviewItem> = emptyList(),
    val playingCount: Int = 0,
    val completedCount: Int = 0,
    val backlogCount: Int = 0,
    val droppedCount: Int = 0,
    val isLoading: Boolean = false
)

class LibraryViewModel : ViewModel() {

    private val _libraryState = MutableStateFlow(LibraryState())
    val libraryState: StateFlow<LibraryState> = _libraryState

    private val _feedState = MutableStateFlow(FeedState())
    val feedState: StateFlow<FeedState> = _feedState

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    fun loadLibrary(status: String) {
        _libraryState.value = LibraryState(isLoading = true, currentStatus = status)
        viewModelScope.launch {
            try {
                val res = Api.service.getMyStatus(status, limit = 50)
                _libraryState.value = LibraryState(
                    items = res.results,
                    currentStatus = status,
                    isLoading = false
                )
            } catch (e: Exception) {
                _libraryState.value = LibraryState(
                    error = "Error al cargar biblioteca",
                    currentStatus = status,
                    isLoading = false
                )
            }
        }
    }

    fun loadFeed() {
        _feedState.value = FeedState(isLoading = true)
        viewModelScope.launch {
            try {
                val res = Api.service.getFeed(limit = 30)
                _feedState.value = FeedState(items = res.results, isLoading = false)
            } catch (e: Exception) {
                _feedState.value = FeedState(error = "Error al cargar el feed", isLoading = false)
            }
        }
    }

    fun loadProfile() {
        _profileState.value = ProfileState(isLoading = true)
        viewModelScope.launch {
            try {
                val reviews = Api.service.getMyReviews(limit = 50)
                val playing = Api.service.getMyStatus("PLAYING", limit = 1)
                val completed = Api.service.getMyStatus("COMPLETED", limit = 1)
                val backlog = Api.service.getMyStatus("BACKLOG", limit = 1)
                val dropped = Api.service.getMyStatus("DROPPED", limit = 1)
                _profileState.value = ProfileState(
                    reviews = reviews.results,
                    playingCount = playing.count,
                    completedCount = completed.count,
                    backlogCount = backlog.count,
                    droppedCount = dropped.count,
                    isLoading = false
                )
            } catch (e: Exception) {
                _profileState.value = ProfileState(isLoading = false)
            }
        }
    }
}
