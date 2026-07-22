package com.app.loyal

import com.app.loyal.data.CachedCard
import com.app.loyal.data.PendingOperation
import com.app.loyal.data.PendingOperationQueue
import com.app.loyal.util.KeyValueStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Archivio in memoria, condiviso fra più code per verificarne la persistenza. */
private class FakeKeyValueStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()
    override fun getString(key: String): String? = values[key]
    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }
}

private fun card(id: String, favorite: Boolean = false) = CachedCard(
    id = id,
    brandName = "Coop",
    domain = "coop.it",
    code = "12345",
    format = "EAN13",
    colorArgb = 0xFF00FF00,
    createdAt = "2026-07-22T10:00:00Z",
    isFavorite = favorite
)

private fun queue(store: KeyValueStore = FakeKeyValueStore()) =
    PendingOperationQueue(store, userId = "user-1")

class PendingOperationQueueTest {

    @Test
    fun operationsAreReplayedInOrder() {
        val queue = queue()
        queue.enqueue(PendingOperation.Add(card("a")))
        queue.enqueue(PendingOperation.Delete("b"))

        assertEquals(PendingOperation.Add(card("a")), queue.first())
        queue.removeFirst()
        assertEquals(PendingOperation.Delete("b"), queue.first())
        queue.removeFirst()
        assertNull(queue.first())
        assertEquals(0, queue.size)
    }

    @Test
    fun deletingANeverSyncedCardCancelsItsAdd() {
        val queue = queue()
        queue.enqueue(PendingOperation.Add(card("a")))
        queue.enqueue(PendingOperation.Delete("a"))

        // La tessera non è mai arrivata al server: niente da inviare.
        assertEquals(0, queue.size)
        assertNull(queue.first())
    }

    @Test
    fun deletingASyncedCardKeepsOnlyTheDelete() {
        val queue = queue()
        queue.enqueue(PendingOperation.SetFavorite("a", favorite = true))
        queue.enqueue(PendingOperation.Delete("a"))

        assertEquals(1, queue.size)
        assertEquals(PendingOperation.Delete("a"), queue.first())
    }

    @Test
    fun updatingAPendingAddRewritesTheAdd() {
        val queue = queue()
        queue.enqueue(PendingOperation.Add(card("a")))
        queue.enqueue(PendingOperation.Update(card("a", favorite = true)))

        assertEquals(1, queue.size)
        assertEquals(PendingOperation.Add(card("a", favorite = true)), queue.first())
    }

    @Test
    fun repeatedOperationsOnTheSameCardCollapse() {
        val queue = queue()
        repeat(5) { queue.enqueue(PendingOperation.SetFavorite("a", favorite = true)) }
        repeat(5) { index ->
            queue.enqueue(PendingOperation.RecordView("a", usageCount = index, lastViewedAt = "t$index"))
        }

        // Conta solo lo stato finale: un preferito e una visualizzazione.
        assertEquals(2, queue.size)
        assertEquals(PendingOperation.SetFavorite("a", favorite = true), queue.first())
    }

    @Test
    fun operationsOnDifferentCardsAreKeptSeparate() {
        val queue = queue()
        queue.enqueue(PendingOperation.SetFavorite("a", favorite = true))
        queue.enqueue(PendingOperation.SetFavorite("b", favorite = true))

        assertEquals(2, queue.size)
    }

    @Test
    fun queueSurvivesAppRestart() {
        val store = FakeKeyValueStore()
        queue(store).enqueue(PendingOperation.Add(card("a")))

        // Nuova istanza sullo stesso archivio: simula il riavvio dell'app.
        val restarted = queue(store)
        assertEquals(1, restarted.size)
        assertEquals(PendingOperation.Add(card("a")), restarted.first())
    }

    @Test
    fun removalIsPersistedAcrossRestart() {
        val store = FakeKeyValueStore()
        val queue = queue(store)
        queue.enqueue(PendingOperation.Add(card("a")))
        queue.removeFirst()

        assertEquals(0, queue(store).size)
    }

    @Test
    fun corruptedQueueIsDiscardedInsteadOfCrashing() {
        val store = FakeKeyValueStore()
        store.putString("pending_user-1", "{not json at all")

        assertEquals(0, queue(store).size)
    }

    @Test
    fun removingFromAnEmptyQueueIsSafe() {
        val queue = queue()
        queue.removeFirst()
        assertTrue(queue.size == 0)
    }
}
