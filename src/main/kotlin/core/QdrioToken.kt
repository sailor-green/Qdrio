package green.sailor.qdrio.core

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Represents a Qdrio token.
 *
 * A token is unique per thread. It encapsulates the result of a single .run() call.
 */
class QdrioToken<T> internal constructor() : Continuation<T> {
    var isDone: Boolean = false
        internal set

    private val finished = CompletableFuture<T>()
    private var _realResult: T? = null
    private var _realException: Throwable? = null

    internal fun getResult(): T {
        _realException?.let { throw it }
        _realResult?.let { return it }

        error("This token has no result?")
    }

    override val context: CoroutineContext = EmptyCoroutineContext

    override fun resumeWith(result: Result<T>) {
        _realException = result.exceptionOrNull()?.also { finished.completeExceptionally(it) }
        if (result.isSuccess) {
            _realResult = result.getOrThrow()
            finished.complete(_realResult)
        }

        isDone = true
    }

    /**
     * Waits for this token to be finished.
     */
    fun blockingWait(): T = finished.get()
}

