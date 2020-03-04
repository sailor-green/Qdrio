package green.sailor.qdrio.annotations

/**
 * Marks a suspend function as one that will always checkpoint, i.e. yield to the event loop to
 * allow other coroutines to run.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class AlwaysCheckpoints
