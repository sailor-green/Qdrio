package green.sailor.qdrio.core

import green.sailor.qdrio.Qdrio
import green.sailor.qdrio.core.task.SpawnedTask
import green.sailor.qdrio.core.task.Task
import green.sailor.qdrio.Nursery
import green.sailor.qdrio.annotations.AlwaysCheckpoints
import green.sailor.qdrio.asyncWith

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
open class NurseryImpl internal constructor() : Nursery {
    private val ourTasks = mutableSetOf<SpawnedTask>()

    /** The task waiting for us to close, if any. */
    private var waitingToClose: Task<*>? = null

    /**
     * Waits for the nursery to close.
     */
    @AlwaysCheckpoints
    internal suspend fun waitClosed() {
        if (ourTasks.isEmpty()) {
            Qdrio.checkpoint()
        }

        val task = Qdrio.ScaryUnsafe.currentTask()
        waitingToClose = task
        // wait for ourselves to be rescheduled
        task.waitTaskRescheduled()
    }

    /**
     * Called when a task is completed.
     */
    internal fun taskCompleted(task: SpawnedTask, result: Result<Unit>) {
        if (result.isSuccess) {
            ourTasks.remove(task)
        }

        // notify our waiter
        if (ourTasks.isEmpty()) {
            waitingToClose?.let {
                Qdrio.reschedule(it)
            }
        }
    }

    /**
     * Creates a new [SpawnedTask] from a block.
     */
    protected fun createTask(task: suspend () -> Unit): SpawnedTask {
        val currentContext = Qdrio.ScaryUnsafe.currentTask().context
        return SpawnedTask(context = currentContext, nursery = this, coro = task)
    }

    /**
     * Starts a task soon. This task will be ran
     */
    override fun startSoon(fn: suspend () -> Unit) {
        val task = createTask(fn)
        Qdrio.reschedule(task)
        ourTasks.add(task)
    }
}

@PublishedApi
internal suspend inline fun openNursery(crossinline block: (Nursery) -> Unit) =
    asyncWith(NurseryManagerImpl()) { block(it) }
