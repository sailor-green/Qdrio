package green.sailor.qdrio.core

import green.sailor.qdrio.core.task.Task

/**
 * Represents a deadline. These are used to track timeouts (cancel scopes) and sleeps.
 */
data class Deadline(val task: Task<*>, val at: Long, val type: DeadlineType)
    : Comparable<Deadline> {

    enum class DeadlineType {
        SLEEP,
        CANCEL
    }

    override fun compareTo(other: Deadline): Int {
        return at.compareTo(other.at)
    }
}
