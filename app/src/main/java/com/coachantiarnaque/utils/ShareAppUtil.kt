package com.coachantiarnaque.utils

import android.content.Context
import android.content.Intent
import com.coachantiarnaque.R

object ShareAppUtil {
    fun shareApp(context: Context) {
        try {
            val message = context.getString(R.string.share_app_message)
            val subject = context.getString(R.string.share_app_subject)
            val chooserTitle = context.getString(R.string.share_app_via)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        } catch (_: Exception) {}
    }
}
