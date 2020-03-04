package green.sailor.qdrio

/**
 * An AsyncWith is an object that needs to be opened with an [asyncWith] statement.
 *
 * This is modeled after the `async with` statement from Python.
 */
interface AsyncWith<T> {
    /**
     * Asynchronously enters an object.
     */
    suspend fun asyncEnter(): T

    /**
     * Asynchronously exits an object.
     */
    suspend fun asyncExit(error: Throwable?)

}

/**
 * Opens something using asyncWith.
 */
suspend fun <T, R> asyncWith(item: AsyncWith<T>, block: suspend (T) -> R): R {
    try {
        val obb = item.asyncEnter()
        val result = block(obb)
        item.asyncExit(null)
        return result
    } catch (e: Throwable) {
        item.asyncExit(e)
        throw e
    }
}
