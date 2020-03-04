
package green.sailor.qdrio

import green.sailor.qdrio.annotations.AlwaysCheckpoints
import green.sailor.qdrio.core.*
import green.sailor.qdrio.core.EventLoopImpl
import green.sailor.qdrio.core.task.Task
import green.sailor.qdrio.core.openNursery as internalOpenNursery

/**
 * Namespaced object for all operations.
 */
@Suppress("MemberVisibilityCanBePrivate")
object Qdrio : EventLoop {
    /** The token for this thread. */
    internal val token = ThreadLocal<QdrioToken<*>>()

    /** The event loop thread local. */
    internal val eventLoops = ThreadLocal<EventLoop>()

    internal val eventLoop get() = eventLoops.get() ?: error("There is no running event loop")

    /**
     * Scary unsafe operators, but ones that are needed.
     */
    object ScaryUnsafe {
        /**
         * Gets the current task.
         */
        fun currentTask(): Task<*> {
            val loop = eventLoop as EventLoopImpl<*>
            return loop.currentTask
        }

        /**
         * Waits until the current task is rescheduled.
         */
        @AlwaysCheckpoints
        suspend fun waitUntilRescheduled() {
            val loop = eventLoop as EventLoopImpl<*>
            return loop.currentTask.waitTaskRescheduled()
        }
    }

    /**
     * Runs the initial suspend function.
     */
    fun <T> run(fn: suspend () -> T): T {
        if (token.get() != null) {
            error("Attempted to call run() whilst the Qdrio event loop is already running")
        }

        val token = QdrioToken<T>()
        this.token.set(token)

        val loop = EventLoopImpl(token)
        eventLoops.set(loop)
        try {
            return loop.runEventLoop(fn)
        } finally {
            this.token.remove()
            eventLoops.remove()
        }
    }

    /**
     * Reschedules a task to be ran at a later date. This will resume the task on the next
     * iteration of the event loop.
     */
    override fun reschedule(task: Task<*>) = eventLoop.reschedule(task)

    /**
     * Causes a checkpoint - a chance for another coroutine to run.
     *
     * A checkpoint causes the coroutine that checkpointed to be placed at the back of the
     * coroutine queue, and it will be ran once all other runnable coroutines are dealt with.
     */
    @AlwaysCheckpoints
    override suspend fun checkpoint() = eventLoop.checkpoint()

    /**
     * Sleeps for [time] milliseconds. A time of 0 is equivalent to calling [checkpoint].
     */
    @AlwaysCheckpoints
    override suspend fun sleep(time: Long) = eventLoop.sleep(time)

    /**
     * Opens a new nursery for spawning child tasks.
     */
    suspend inline fun openNursery(noinline block: (Nursery) -> Unit) =
        internalOpenNursery(block)

}

fun main() {
    val result = Qdrio.run {
        Qdrio.openNursery {
            it.startSoon {
                Qdrio.sleep(3000L)
                println("Task 1: Awoken")
            }

            it.startSoon {
                Qdrio.sleep(1000L)
                println("Task 2: Awoken")
            }
        }
        println("Nursery closed")
    }
    println("Result: $result")
}

/*
fun main(args: Array<String>) {
    val continuation = Continuation<Any>(EmptyCoroutineContext) {
        println("result: $it")
    }

    var outerContinuation: Continuation<Any>? = null

    val fn = suspend {
        println("Start!")
        val got1 = suspendCoroutineUninterceptedOrReturn<Int> { it ->
            println("Suspend 1: $it")
            outerContinuation = it as Continuation<Any>
            COROUTINE_SUSPENDED
        }
        println("Resume 1! $got1")

        val got2 = suspendCoroutineUninterceptedOrReturn<String> {
            println("Suspend 2: $it")
            COROUTINE_SUSPENDED
        }
        println("Resume 2! $got2")

        123
    }
    /* Oh! */ println("Create!")
    val coro = fn.createCoroutineUnintercepted(continuation)

    println("Outer Resume! (1)")
    coro.resume(Unit)
    println("Outer Resume! (2)")
    outerContinuation!!.resume(123)
    println("Outer Resume! (3)")
    coro.resume(Unit)
    println("Done!")
}
*/
