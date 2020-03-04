package green.sailor.qdrio

import green.sailor.qdrio.Qdrio
import green.sailor.qdrio.core.task.SpawnedTask
import green.sailor.qdrio.core.task.Task

/**
 * A nursery is a task supervisor that can be ran within a function to spawn child tasks.
 *
 * The nursery provides several guarantees:
 *  1. A nursery will never close until all children have finished.
 *  2. A nursery will stop all children if one child errors.
 *  3. A nursery cannot be created outside of a suspend function.
 *
 * This means that children tasks spawned from a task that opens a nursery will never outlive it.
 */
@Suppress("MemberVisibilityCanBePrivate")
interface Nursery {
    /**
     * Starts a task soon. This task will be scheduled to run at some point in the future. This
     * function will immediately return.
     */
    fun startSoon(fn: suspend () -> Unit)
}
