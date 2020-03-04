package green.sailor.qdrio.core.task

import green.sailor.qdrio.CancelScope
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.createCoroutine

/**
 * Represents the task spawned from a cancel scope.
 */
class CancelScopeTask(
    context: CoroutineContext,
    scope: CancelScope,
    fn: suspend (CancelScope) -> Unit
) : CancellableTask<Unit>(context) {
    override var nextContinuation: Continuation<Unit> = fn.createCoroutine(scope, this)
}
