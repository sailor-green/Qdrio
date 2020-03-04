package green.sailor.qdrio.core

import green.sailor.qdrio.AsyncWith
import green.sailor.qdrio.Nursery

/**
 * Represents a nursery manager (something that opens and closes a nursery).
 */
class NurseryManagerImpl : AsyncWith<Nursery> {
    private lateinit var ourNursery: NurseryImpl

    override suspend fun asyncEnter(): Nursery {
        ourNursery = NurseryImpl()
        return ourNursery
    }

    override suspend fun asyncExit(error: Throwable?) {
        ourNursery.waitClosed()
    }
}
