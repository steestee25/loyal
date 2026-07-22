package com.app.loyal.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Una scrittura applicata in locale ma non ancora arrivata a Supabase.
 * Le operazioni vengono rigiocate in ordine appena la rete torna.
 */
@Serializable
internal sealed interface PendingOperation {

    /** Tessera a cui si riferisce l'operazione, usata per accorpare la coda. */
    val cardId: String

    @Serializable
    @SerialName("add")
    data class Add(val card: CachedCard) : PendingOperation {
        override val cardId: String get() = card.id
    }

    @Serializable
    @SerialName("update")
    data class Update(val card: CachedCard) : PendingOperation {
        override val cardId: String get() = card.id
    }

    @Serializable
    @SerialName("delete")
    data class Delete(override val cardId: String) : PendingOperation

    @Serializable
    @SerialName("favorite")
    data class SetFavorite(
        override val cardId: String,
        val favorite: Boolean
    ) : PendingOperation

    @Serializable
    @SerialName("view")
    data class RecordView(
        override val cardId: String,
        val usageCount: Int,
        val lastViewedAt: String
    ) : PendingOperation
}
