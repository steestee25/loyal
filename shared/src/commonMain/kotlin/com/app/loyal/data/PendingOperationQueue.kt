package com.app.loyal.data

import com.app.loyal.util.KeyValueStore
import kotlinx.serialization.json.Json

/**
 * Coda FIFO persistente delle scritture non ancora sincronizzate (outbox).
 * Sopravvive alla chiusura dell'app: le modifiche fatte offline non si perdono.
 *
 * Non è thread-safe: chi la usa deve serializzare gli accessi
 * (il repository lo fa con un Mutex).
 */
class PendingOperationQueue(
    private val store: KeyValueStore,
    userId: String
) {
    private val key = "pending_$userId"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "op"
    }

    private val operations: MutableList<PendingOperation> = load()

    val size: Int get() = operations.size

    internal fun first(): PendingOperation? = operations.firstOrNull()

    internal fun removeFirst() {
        if (operations.isNotEmpty()) {
            operations.removeAt(0)
            persist()
        }
    }

    /**
     * Accoda [operation] accorpandola con quelle già presenti sulla stessa
     * tessera, così una sessione offline lunga non genera una coda infinita:
     * conta solo lo stato finale, non ogni singolo passaggio.
     */
    internal fun enqueue(operation: PendingOperation) {
        when (operation) {
            is PendingOperation.Add -> operations += operation

            is PendingOperation.Update -> {
                // Se la tessera non è ancora stata creata sul server, aggiorniamo
                // direttamente l'Add in coda: partirà già con i dati nuovi.
                val addIndex = operations.indexOfFirst {
                    it is PendingOperation.Add && it.cardId == operation.cardId
                }
                if (addIndex >= 0) {
                    operations[addIndex] = PendingOperation.Add(operation.card)
                } else {
                    operations.removeAll {
                        it is PendingOperation.Update && it.cardId == operation.cardId
                    }
                    operations += operation
                }
            }

            is PendingOperation.Delete -> {
                // Una tessera creata e cancellata da offline non è mai esistita
                // per il server: si annullano a vicenda.
                val neverSynced = operations.any {
                    it is PendingOperation.Add && it.cardId == operation.cardId
                }
                operations.removeAll { it.cardId == operation.cardId }
                if (!neverSynced) operations += operation
            }

            is PendingOperation.SetFavorite -> {
                operations.removeAll {
                    it is PendingOperation.SetFavorite && it.cardId == operation.cardId
                }
                operations += operation
            }

            is PendingOperation.RecordView -> {
                // Conta solo l'ultimo conteggio letto.
                operations.removeAll {
                    it is PendingOperation.RecordView && it.cardId == operation.cardId
                }
                operations += operation
            }
        }
        persist()
    }

    private fun load(): MutableList<PendingOperation> {
        val raw = store.getString(key) ?: return mutableListOf()
        // Una coda illeggibile viene scartata: meglio perdere le modifiche
        // offline che bloccare per sempre la sincronizzazione.
        return runCatching {
            json.decodeFromString<List<PendingOperation>>(raw).toMutableList()
        }.getOrElse { mutableListOf() }
    }

    private fun persist() {
        runCatching {
            store.putString(key, json.encodeToString(operations.toList()))
        }
    }
}
