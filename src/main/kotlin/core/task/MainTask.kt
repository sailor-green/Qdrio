package green.sailor.qdrio.core.task

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

/**
 * Represents the main task. This task has no associated nursery.
 */
class MainTask<T>(
    override val context: CoroutineContext, continuation: Continuation<Unit>
) : Task<T>(context) {
    override var nextContinuation: Continuation<Unit> = continuation
}
