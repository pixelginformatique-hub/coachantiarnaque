package com.coachantiarnaque.utils

import android.content.Context
import android.provider.Telephony

/**
 * Représente un SMS lu depuis la boîte de réception.
 */
data class SmsMessage(
    val id: Long,
    val sender: String,
    val body: String,
    val date: Long
)

/**
 * Lit les derniers SMS de la boîte de réception.
 */
object SmsReader {

    fun getInboxMessages(context: Context, limit: Int = 30): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )
            cursor?.use {
                val idIdx = it.getColumnIndex(Telephony.Sms._ID)
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)

                var count = 0
                while (it.moveToNext() && count < limit) {
                    val id = it.getLong(idIdx)
                    val address = it.getString(addressIdx) ?: "Inconnu"
                    val body = it.getString(bodyIdx) ?: ""
                    val date = it.getLong(dateIdx)

                    if (body.isNotBlank()) {
                        messages.add(SmsMessage(id, address, body, date))
                        count++
                    }
                }
            }
        } catch (_: SecurityException) {
            // Permission non accordée
        }
        return messages
    }
}
