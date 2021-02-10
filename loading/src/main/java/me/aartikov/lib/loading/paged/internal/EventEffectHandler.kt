package me.aartikov.lib.loading.paged.internal

import me.aartikov.lib.loading.paged.PagedLoading
import me.aartikov.lib.loop.EffectHandler

internal class EventEffectHandler<T>(private val emitEvent: (PagedLoading.Event) -> Unit) :
    EffectHandler<Effect<T>, Action<T>> {

    override suspend fun handleEffect(effect: Effect<T>, actionConsumer: (Action<T>) -> Unit) {
        when (effect) {
            is Effect.EmitEvent -> emitEvent(effect.event)
        }
    }
}