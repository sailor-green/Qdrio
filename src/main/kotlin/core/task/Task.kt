package green.sailor.qdrio.core.task

import green.sailor.qdrio.EventLoop
import green.sailor.qdrio.annotations.AlwaysCheckpoints
import kotlin.coroutines.*

// continuations kind of... lose their type safety. a lot.
// to say it's annoying would be an understatement.
// task is a bit of a wrapper around that nonsense.

/**
 * Represents a single task created within Qdrio.
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class Task<T>(context: CoroutineContext) : Continuation<T> {
    /** The real coroutine for this task. */
    protected abstract var nextContinuation: Continuation<Unit>

    override val context: CoroutineContext = context

    protected var isCompleted: Boolean = false
    protected var _result: T? = null
    protected var _exception: Throwable? = null

    override fun toString(): String {
        return "<Task continuation=$nextContinuation>"
    }

    /**
     * Checkpoints this task. This simply reschedules this task then waits.
     */
    @AlwaysCheckpoints
    internal suspend fun checkpoint(loop: EventLoop) {
        loop.reschedule(this)
        waitTaskRescheduled()
    }

    /**
     * Waits for this task to be rescheduled.
     *
     * Another function must call `EventLoop.reschedule(Task)` in order for this task to run again.
     */
    @AlwaysCheckpoints
    suspend fun waitTaskRescheduled() {
        suspendCoroutine<Unit> { continuation ->
            nextContinuation = continuation
        }
    }

    /**
     * Called when this task is completed by the inner coroutine.
     *
     * Not to be called by the event loop or anything else!
     */
    override fun resumeWith(result: Result<T>) {
        isCompleted = true
        _exception = result.exceptionOrNull()
        _result = result.getOrNull()
    }

    /**
     * Runs the next iteration of this task.
     */
    internal fun next() {
        nextContinuation.resume(Unit)
    }
}
