package green.sailor.qdrio

import green.sailor.qdrio.annotations.AlwaysCheckpoints
import green.sailor.qdrio.core.task.Task

/**
 * A looping construct that listens to events happening inside user code and dispatches the
 * appropriate responses.
 *
 * The Event Loop is at the core of your program, and is responsible for rescheduling coroutines,
 * waiting for network I/O, and dealing with other concurrency primitives.
 */
interface EventLoop {
    /**
     * Reschedules a task to be ran at a later date. This will resume the task on the next
     * iteration of the event loop.
     */
    fun reschedule(task: Task<*>)

    /**
     * Causes a checkpoint - a chance for another coroutine to run.
     *
     * A checkpoint causes the coroutine that checkpointed to be placed at the back of the
     * coroutine queue, and it will be ran once all other runnable coroutines are dealt with.
     */
    @AlwaysCheckpoints
    suspend fun checkpoint(): Unit

    /**
     * Sleeps for [time] milliseconds. A time of 0 is equivalent to calling [checkpoint].
     */
    @AlwaysCheckpoints
    suspend fun sleep(time: Long)

}
