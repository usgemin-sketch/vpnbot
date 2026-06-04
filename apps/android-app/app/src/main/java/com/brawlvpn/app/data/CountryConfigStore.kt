package com.brawlvpn.app.data

import android.content.Context

class CountryConfigStore(context: Context) {
    private val preferences = context.getSharedPreferences("country-configs", Context.MODE_PRIVATE)

    fun read(countryId: String): String = preferences.getString(key(countryId), "") ?: ""

    fun write(countryId: String, value: String) {
        preferences.edit().putString(key(countryId), value).apply()
    }

    private fun key(countryId: String): String = "wireguard_config_$countryId"
}
