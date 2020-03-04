package green.sailor.qdrio.core.task

import green.sailor.qdrio.Nursery
import green.sailor.qdrio.core.NurseryImpl
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.createCoroutine

/**
 * Represents a spawned task. This task comes from a nursery.
 */
class SpawnedTask(
    context: CoroutineContext, nursery: NurseryImpl, coro: suspend () -> Unit
) : CancellableTask<Unit>(context) {
    /** The nursery this task was spawned from. */
    private val nursery: NurseryImpl = nursery
    override var nextContinuation: Continuation<Unit> = coro.createCoroutine(this)

    override fun resumeWith(result: Result<Unit>) {
        super.resumeWith(result)
        nursery.taskCompleted(this, result)
    }
}
