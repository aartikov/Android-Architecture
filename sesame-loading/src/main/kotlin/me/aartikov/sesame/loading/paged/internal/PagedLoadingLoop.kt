package me.aartikov.sesame.loading.paged.internal

import me.aartikov.sesame.loading.paged.PagedLoading.Event
import me.aartikov.sesame.loading.paged.PagingInfo
import me.aartikov.sesame.loop.*

internal sealed class State<out T> {
    object Empty : State<Nothing>()
    object Loading : State<Nothing>()
    data class Error(val throwable: Throwable) : State<Nothing>()
    data class Data<T>(val pageCount: Int, val data: List<T>) : State<T>()
    data class Refresh<T>(val pageCount: Int, val data: List<T>) : State<T>()
    data class LoadingMore<T>(val pageCount: Int, val data: List<T>) : State<T>()
    data class FullData<T>(val pageCount: Int, val data: List<T>) : State<T>()
}

internal sealed class Action<out T> {
    data class LoadFirstPage(val fresh: Boolean, val reset: Boolean) : Action<Nothing>()
    object LoadMore : Action<Nothing>()
    data class Cancel(val reset: Boolean) : Action<Nothing>()

    data class NewPageLoaded<T>(val data: List<T>) : Action<T>()
    object EmptyPageLoaded : Action<Nothing>()
    data class LoadingError(val throwable: Throwable) : Action<Nothing>()
}

internal sealed class Effect<out T> {
    data class LoadFirstPage(val fresh: Boolean) : Effect<Nothing>()
    data class LoadNextPage<T>(val pagingInfo: PagingInfo<T>) : Effect<T>()
    object CancelLoading: Effect<Nothing>()
    data class EmitEvent<T>(val event: Event<T>) : Effect<T>()
}

internal typealias PagedLoadingLoop<T> = Loop<State<T>, Action<T>, Effect<T>>

internal class PagedLoadingReducer<T> : Reducer<State<T>, Action<T>, Effect<T>> {

    override fun reduce(state: State<T>, action: Action<T>): Next<State<T>, Effect<T>> = when (action) {

        is Action.LoadFirstPage -> {
            if (action.reset) {
                next(
                    State.Loading,
                    Effect.LoadFirstPage(action.fresh)
                )
            } else {
                when (state) {
                    is State.Empty -> next(
                        State.Loading,
                        Effect.LoadFirstPage(action.fresh)
                    )
                    is State.Error -> next(
                        State.Loading,
                        Effect.LoadFirstPage(action.fresh)
                    )
                    is State.Data -> next(
                        State.Refresh(state.pageCount, state.data),
                        Effect.LoadFirstPage(action.fresh)
                    )
                    is State.LoadingMore -> next(
                        State.Refresh(state.pageCount, state.data),
                        Effect.LoadFirstPage(action.fresh)
                    )
                    is State.FullData -> next(
                        State.Refresh(state.pageCount, state.data),
                        Effect.LoadFirstPage(action.fresh)
                    )
                    else -> nothing()
                }
            }
        }

        is Action.LoadMore -> {
            when (state) {
                is State.Data -> next(
                    State.LoadingMore(state.pageCount, state.data),
                    Effect.LoadNextPage(PagingInfo(state.pageCount, state.data))
                )
                else -> nothing()
            }
        }

        is Action.Cancel -> {
            if (action.reset) {
                next(
                    State.Empty,
                    Effect.CancelLoading
                )
            } else {
                when (state) {
                    is State.Loading -> next(
                        State.Empty,
                        Effect.CancelLoading
                    )
                    is State.Refresh -> next(
                        State.Data(state.pageCount, state.data),
                        Effect.CancelLoading
                    )
                    is State.LoadingMore -> next(
                        State.Data(state.pageCount, state.data),
                        Effect.CancelLoading
                    )
                    else -> nothing()
                }
            }
        }

        is Action.NewPageLoaded -> {
            when (state) {
                is State.Loading -> next(State.Data(1, action.data))
                is State.Refresh -> next(State.Data(1, action.data))
                is State.LoadingMore -> next(
                    State.Data(
                        state.pageCount + 1,
                        state.data + action.data
                    )
                )
                else -> nothing()
            }
        }

        is Action.EmptyPageLoaded -> {
            when (state) {
                is State.Loading -> next(State.Empty)
                is State.Refresh -> next(State.Empty)
                is State.LoadingMore -> next(State.FullData(state.pageCount, state.data))
                else -> nothing()
            }
        }

        is Action.LoadingError -> {
            when (state) {
                is State.Loading -> next(
                    State.Error(action.throwable),
                    Effect.EmitEvent(Event.Error(action.throwable, state.toPublicState()))
                )
                is State.Refresh -> next(
                    State.Data(state.pageCount, state.data),
                    Effect.EmitEvent(Event.Error(action.throwable, state.toPublicState()))
                )
                is State.LoadingMore -> next(
                    State.Data(state.pageCount, state.data),
                    Effect.EmitEvent(Event.Error(action.throwable, state.toPublicState()))
                )
                else -> nothing()
            }
        }
    }
}