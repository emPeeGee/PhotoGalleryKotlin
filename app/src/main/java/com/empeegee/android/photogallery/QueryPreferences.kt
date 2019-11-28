package com.empeegee.android.photogallery

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit

private const val PREF_SEACH_QUERY = "searchQuery"
private const val PREF_LAST_RESULT = "lastResultId"

object QueryPreferences {

    fun getStoredQuery(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        return prefs.getString(PREF_SEACH_QUERY, "")!!
    }

    fun setStoredQuery(context: Context, query: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(PREF_SEACH_QUERY, query)
            }
    }

    fun getLastResultId(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LAST_RESULT, "")!!
    }

    fun setLastResultId(context: Context, lastResultId: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit{
            putString(PREF_LAST_RESULT, lastResultId)
        }
    }
}