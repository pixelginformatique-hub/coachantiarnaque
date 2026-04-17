package com.coachantiarnaque.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.coachantiarnaque.data.local.AppDatabase
import com.coachantiarnaque.data.repository.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver qui intercepte les SMS entrants
 * et lance automatiquement l'analyse.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Regrouper les parties du SMS
        val senderNumber = messages[0].displayOriginatingAddress
        val fullMessage = messages.joinToString("") { it.displayMessageBody ?: "" }

        if (fullMessage.isBlank()) return

        // Analyser en arrière-plan
        val db = AppDatabase.getInstance(context)
        val repository = MessageRepository(db.analyzedMessageDao(), context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.analyzeMessage(fullMessage, senderNumber)
            } catch (_: Exception) {
                // Silencieux — ne pas crasher le receiver
            }
        }
    }
}
