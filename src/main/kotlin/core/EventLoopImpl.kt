package green.sailor.qdrio.core

import green.sailor.qdrio.EventLoop
import green.sailor.qdrio.annotations.AlwaysCheckpoints
import green.sailor.qdrio.core.task.MainTask
import green.sailor.qdrio.core.task.Task
import java.nio.channels.spi.SelectorProvider
import java.util.*
import kotlin.coroutines.createCoroutine

/**
 * Represents an event loop for a thread.
 */
internal class EventLoopImpl<T>(val token: QdrioToken<T>) : EventLoop {
    /** The selector provider for this thread. */
    val provider = SelectorProvider.provider()

    /** The selector for this thread. */
    val selector = provider.openSelector()

    // == COROUTINE QUEUES AND SETS == //
    /**
     * The currently running task.
     */
    internal lateinit var currentTask: Task<out Any?>

    /**
     * The list of immediately runnable tasks.
     */
    private val immediatelyRunnable = ArrayDeque<Task<out Any?>>()

    /**
     * The list of checkpointed tasks. This is stored separately so that checkpointed tasks will
     * not stop I/O from happening before they resume running.
     */
    private val checkpointed = ArrayDeque<Task<out Any?>>()

    /**
     * The deadline priority queue.
     */
    private val deadlineQueue = PriorityQueue<Deadline>()

    /**
     * Runs the actual event loop.
     */
    fun runEventLoop(main: suspend () -> T): T {
        val coro = main.createCoroutine(token)
        val task = MainTask<T>(coro.context, coro)

        immediatelyRunnable.add(task)

        return runLoopImpl()
    }

    /**
     * Implementation of running the event loop.
     */
    private fun runLoopImpl(): T {
        // == ACTUAL LOOP IMPL == //
        while (true) {
            // Step 1: Process all immediately runnable coroutines.
            while (!immediatelyRunnable.isEmpty()) {
                val nextTask = immediatelyRunnable.pop()
                currentTask = nextTask
                nextTask.next()
            }

            // Step 2: Check if we're done, as a result of the token completing.
            if (token.isDone) {
                return token.getResult()
            }

            // Step 3: I/O
            processIO()

            // Step 4: Process deadlines
            val copy = deadlineQueue.toTypedArray()
            for (deadline in copy) {
                val current = System.nanoTime()
                if (deadline.at < current) {
                    processDeadline(deadline)
                    deadlineQueue.remove(deadline)
                } else {
                    // not reached deadline, stop processing
                    break
                }
            }

            // Step 4: Copy checkpointed tasks to the end of immediately runnable tasks.
            immediatelyRunnable.addAll(checkpointed)
            checkpointed.clear()
        }
    }

    /**
     * Processes a deadline.
     */
    fun processDeadline(deadline: Deadline) {
        val task = deadline.task
        // sleeps just wake up normally
        if (deadline.type == Deadline.DeadlineType.SLEEP) {
            reschedule(task)
        } else {
            TODO("Cancellations")
        }
    }

    /**
     * Processes all I/O events.
     */
    fun processIO() {
        val currentTime = System.nanoTime()
        val deadline = deadlineQueue.peek()?.at ?: 0L

        val selectTime = if (deadline != 0L) {
            (deadline - currentTime) / 1_000_000
        } else {
            0L
        }

        val selected = if (!checkpointed.isEmpty() || selectTime <= 0) {
            // If there are checkpointed tasks waiting to be ran, do a non-blocking select.
            // This ensures that anything waiting for I/O will be woken up first.
            selector.selectNow()
        } else {
            // No non-checkpointed tasks, just select according to the next deadline.
            //val nextDeadline = deadlineQueues.poll() ?: 0L
            selector.select(selectTime)
        }
        // TODO: I/O, I guess
    }

    // == sleepy functions == //
    // these functions can (and will) suspend when called.
    @AlwaysCheckpoints
    override suspend fun sleep(time: Long) {
        if (time < 0L) checkpoint()
        else {
            val ns = System.nanoTime() + (time * 1_000_000)
            val deadline = Deadline(currentTask, ns, Deadline.DeadlineType.SLEEP)
            deadlineQueue.add(deadline)
            currentTask.waitTaskRescheduled()
        }
    }

    @AlwaysCheckpoints
    override suspend fun checkpoint() {
        checkpointed.add(currentTask)
        currentTask.waitTaskRescheduled()
    }

    override fun reschedule(task: Task<*>) {
        // this just adds a task straight onto the runnable list
        immediatelyRunnable.addLast(task)
    }


}
