package com.eromn.microfintracker.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire DTO for a transaction as exchanged with the server. Never used directly by the UI;
 * local [com.eromn.microfintracker.data.Transaction] entities are mapped to this before sending.
 */
@Serializable
data class TransactionDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("user_id") val userId: Long,
    @SerialName("date") val date: String,
    @SerialName("timezone") val timezone: String,
    @SerialName("amount") val amount: Double,
    @SerialName("amount_currency_id") val amountCurrencyId: Long,
    @SerialName("amount_in_local_currency") val amountInLocalCurrency: Double? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("account_id") val accountId: Long? = null,
    @SerialName("category_id") val categoryId: Long? = null,
)