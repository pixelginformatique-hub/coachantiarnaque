package com.coachantiarnaque.utils

import android.content.Context
import androidx.annotation.StringRes

/**
 * Fournit des chaînes localisées aux moteurs de détection
 * qui n'ont pas accès aux Composables.
 */
class StringProvider(private val context: Context) {
    fun getString(@StringRes resId: Int): String = context.getString(resId)
    fun getString(@StringRes resId: Int, vararg args: Any): String = context.getString(resId, *args)
}
