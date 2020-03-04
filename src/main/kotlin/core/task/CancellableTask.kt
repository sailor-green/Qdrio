package green.sailor.qdrio.core.task

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.createCoroutine

/**
 * Represents a cancellable task (i.e. one spawned from a nursery or a cancel scope block).
 */
abstract class CancellableTask<T>(
    context: CoroutineContext
) : Task<T>(context) {

}
