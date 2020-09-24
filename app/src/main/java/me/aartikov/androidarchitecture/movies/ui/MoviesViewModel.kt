package me.aartikov.androidarchitecture.movies.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import me.aartikov.androidarchitecture.base.BaseViewModel
import me.aartikov.androidarchitecture.movies.domain.Movie
import me.aartikov.lib.data_binding.computed
import me.aartikov.lib.data_binding.stateFromFlow
import me.aartikov.lib.loading.paged.PagedLoading
import me.aartikov.lib.loading.paged.handleErrors
import me.aartikov.lib.loading.paged.startIn
import me.aartikov.lib.loading.paged.toUiState


class MoviesViewModel @ViewModelInject constructor(
    private val moviesLoading: PagedLoading<Movie>
) : BaseViewModel() {

    private val moviesState by stateFromFlow(moviesLoading.stateFlow)
    val moviesUiState by computed(::moviesState) { it.toUiState() }

    init {
        moviesLoading.handleErrors(viewModelScope) {error ->
            if (error.hasData)
                showError(error.throwable)
        }
        moviesLoading.startIn(viewModelScope)
    }

    fun onPullToRefresh() = moviesLoading.refresh()

    fun onRetryClicked() = moviesLoading.refresh()

    fun onLoadMore() {
        if (moviesUiState.loadMoreEnabled)
            moviesLoading.loadMore()
    }
}