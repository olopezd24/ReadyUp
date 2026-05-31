package com.example.readyup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readyup.data.model.FollowUser
import com.example.readyup.data.model.MyReviewItem
import com.example.readyup.data.remote.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UsersSearchState(
    val results: List<FollowUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = ""
)

data class UserDetailState(
    val user: FollowUser? = null,
    val reviews: List<MyReviewItem> = emptyList(),
    val followers: List<FollowUser> = emptyList(),
    val following: List<FollowUser> = emptyList(),
    val isFollowing: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val toastMsg: String? = null
)

class UsersViewModel : ViewModel() {

    private val _searchState = MutableStateFlow(UsersSearchState())
    val searchState: StateFlow<UsersSearchState> = _searchState

    private val _detailState = MutableStateFlow(UserDetailState())
    val detailState: StateFlow<UserDetailState> = _detailState

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchState.value = UsersSearchState()
            return
        }
        _searchState.value = UsersSearchState(isLoading = true, query = query)
        viewModelScope.launch {
            try {
                val res = Api.service.searchUsers(query)
                _searchState.value = UsersSearchState(
                    results = res.results,
                    query = query,
                    isLoading = false
                )
            } catch (e: Exception) {
                _searchState.value = UsersSearchState(
                    error = "Error al buscar usuarios",
                    query = query,
                    isLoading = false
                )
            }
        }
    }

    fun loadUserDetail(userId: Int, currentUserId: Int) {
        _detailState.value = UserDetailState(isLoading = true)
        viewModelScope.launch {
            try {
                val reviews = Api.service.getUserReviews(userId)
                val followers = Api.service.getFollowers(userId)
                val following = Api.service.getFollowing(userId)
                val isFollowing = followers.results.any { it.id == currentUserId }
                val foundUser = _searchState.value.results.find { it.id == userId }

                _detailState.value = UserDetailState(
                    user = foundUser,
                    reviews = reviews.results,
                    followers = followers.results,
                    following = following.results,
                    isFollowing = isFollowing,
                    isLoading = false
                )
            } catch (e: Exception) {
                _detailState.value = UserDetailState(
                    error = "Error al cargar el perfil",
                    isLoading = false
                )
            }
        }
    }

    fun toggleFollow(userId: Int, currentUserId: Int) {
        viewModelScope.launch {
            try {
                val isFollowing = _detailState.value.isFollowing
                if (isFollowing) {
                    Api.service.unfollow(userId)
                    _detailState.value = _detailState.value.copy(
                        isFollowing = false,
                        followers = _detailState.value.followers.filter { it.id != currentUserId },
                        toastMsg = "Has dejado de seguir a este usuario"
                    )
                } else {
                    Api.service.follow(userId)
                    val followers = Api.service.getFollowers(userId)
                    _detailState.value = _detailState.value.copy(
                        isFollowing = true,
                        followers = followers.results,
                        toastMsg = "Ahora sigues a este usuario"
                    )
                }
            } catch (e: Exception) {
                _detailState.value = _detailState.value.copy(
                    toastMsg = "Error al actualizar seguimiento"
                )
            }
        }
    }

    fun clearToast() { _detailState.value = _detailState.value.copy(toastMsg = null) }
    fun clearDetail() { _detailState.value = UserDetailState() }
    fun clearSearch() { _searchState.value = UsersSearchState() }
}