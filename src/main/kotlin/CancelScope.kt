package green.sailor.qdrio

/**
 * A cancel scope is a scope over a block of code that can be individually cancelled.
 *
 * In some frameworks, cancellation propagates over an entire task, meaning you cannot finely
 * control what gets cancelled where. Cancel scopes allow you to designate one block of code to
 * be cancelled independent of other blocks in the task.
 *
 * They are also used within nurseries to allow cancelling an entire task tree at once.
 */
interface CancelScope {
    /**
     * Cancels this cancel scope. The code currently running under this scope will be terminated,
     * and the code will resume outside of the cancel scope.
     */
    fun cancel()
}
