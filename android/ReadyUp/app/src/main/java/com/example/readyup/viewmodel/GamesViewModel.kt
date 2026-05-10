package com.example.readyup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readyup.data.model.*
import com.example.readyup.data.remote.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GamesState(
    val games: List<GameListItem> = emptyList(),
    val count: Int = 0,
    val offset: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val sort: String = "",
    val genre: String = "",
    val platform: String = ""
)

data class GameDetailState(
    val game: GameDetail? = null,
    val reviews: List<ReviewItem> = emptyList(),
    val myReview: MyReview? = null,
    val myStatus: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val reviewError: String? = null,
    val toastMsg: String? = null
)

class GamesViewModel : ViewModel() {

    private val _gamesState = MutableStateFlow(GamesState())
    val gamesState: StateFlow<GamesState> = _gamesState

    private val _detailState = MutableStateFlow(GameDetailState())
    val detailState: StateFlow<GameDetailState> = _detailState

    companion object { const val PAGE_SIZE = 20 }

    fun loadGames(offset: Int = 0) {
        val s = _gamesState.value
        viewModelScope.launch {
            _gamesState.value = s.copy(isLoading = true, error = null, offset = offset)
            try {
                val res = Api.service.getGames(
                    q = s.query.ifBlank { null },
                    sort = s.sort.ifBlank { null },
                    genres = s.genre.ifBlank { null },
                    platforms = s.platform.ifBlank { null },
                    limit = PAGE_SIZE,
                    offset = offset
                )
                _gamesState.value = _gamesState.value.copy(
                    games = res.results,
                    count = res.count,
                    isLoading = false
                )
            } catch (e: Exception) {
                _gamesState.value = _gamesState.value.copy(isLoading = false, error = "Error al cargar juegos")
            }
        }
    }

    fun setQuery(q: String) { _gamesState.value = _gamesState.value.copy(query = q) }
    fun setSort(s: String) { _gamesState.value = _gamesState.value.copy(sort = s); loadGames() }
    fun setGenre(g: String) { _gamesState.value = _gamesState.value.copy(genre = g) }
    fun setPlatform(p: String) { _gamesState.value = _gamesState.value.copy(platform = p) }
    fun clearFilters() {
        _gamesState.value = GamesState()
        loadGames()
    }

    fun loadGameDetail(id: Int) {
        viewModelScope.launch {
            _detailState.value = GameDetailState(isLoading = true)
            try {
                val game = Api.service.getGameDetail(id)
                val reviews = Api.service.getGameReviews(id, limit = 20)
                val myReviewRes = Api.service.getMyReview(id)
                val statusRes = Api.service.getGameStatus(id)
                _detailState.value = GameDetailState(
                    game = game,
                    reviews = reviews.results,
                    myReview = if (myReviewRes.isSuccessful) myReviewRes.body() else null,
                    myStatus = if (statusRes.isSuccessful) statusRes.body()?.status else null,
                    isLoading = false
                )
            } catch (e: Exception) {
                _detailState.value = GameDetailState(error = "Error al cargar el juego", isLoading = false)
            }
        }
    }

    fun submitReview(gameId: Int, rating: Int, text: String) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(reviewError = null)
            try {
                val body = ReviewRequest(rating, text)
                val review = if (_detailState.value.myReview != null) {
                    Api.service.updateReview(gameId, body)
                } else {
                    Api.service.createReview(gameId, body)
                }
                val reviews = Api.service.getGameReviews(gameId, limit = 20)
                _detailState.value = _detailState.value.copy(
                    myReview = review,
                    reviews = reviews.results,
                    toastMsg = if (_detailState.value.myReview != null) "Reseña actualizada" else "Reseña publicada"
                )
            } catch (e: Exception) {
                _detailState.value = _detailState.value.copy(reviewError = "Error al guardar la reseña")
            }
        }
    }

    fun deleteReview(gameId: Int) {
        viewModelScope.launch {
            try {
                Api.service.deleteReview(gameId)
                val reviews = Api.service.getGameReviews(gameId, limit = 20)
                _detailState.value = _detailState.value.copy(
                    myReview = null,
                    reviews = reviews.results,
                    toastMsg = "Reseña eliminada"
                )
            } catch (e: Exception) {
                _detailState.value = _detailState.value.copy(reviewError = "Error al eliminar")
            }
        }
    }

    fun setStatus(gameId: Int, status: String) {
        viewModelScope.launch {
            try {
                val current = _detailState.value.myStatus
                if (current != null) {
                    Api.service.updateStatus(gameId, UpdateStatusRequest(status))
                } else {
                    Api.service.addStatus(AddStatusRequest(gameId, status))
                }
                _detailState.value = _detailState.value.copy(myStatus = status, toastMsg = "Estado actualizado")
            } catch (e: Exception) {
                _detailState.value = _detailState.value.copy(toastMsg = "Error al actualizar estado")
            }
        }
    }

    fun clearToast() { _detailState.value = _detailState.value.copy(toastMsg = null) }
    fun clearDetail() { _detailState.value = GameDetailState() }
}
