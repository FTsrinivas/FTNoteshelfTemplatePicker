package com.fluidtouch.noteshelf.commons.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object FTNetworkConnectionUtil {

    fun getNetworkType(context: Context): NetworkType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return NetworkType.NotConnected
        val actNw = connectivityManager.getNetworkCapabilities(nw)
                ?: return NetworkType.NotConnected
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WiFi
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.Mobile
            else -> NetworkType.NotConnected
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        if (getNetworkType(context) == NetworkType.WiFi || getNetworkType(context) == NetworkType.Mobile)
            return true
        return false
    }

}