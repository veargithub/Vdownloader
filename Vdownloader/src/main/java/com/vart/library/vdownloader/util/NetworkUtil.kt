package com.vart.library.vdownloader.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkUtil {

    companion object {
        fun isWifi(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = cm.activeNetwork ?: return false
                val nwc = cm.getNetworkCapabilities(network) ?: return false
                return nwc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            } else {
                val networkInfo = cm.activeNetworkInfo ?: return false
                return networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI
            }
        }

        fun isCellular(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = cm.activeNetwork ?: return false
                val nwc = cm.getNetworkCapabilities(network) ?: return false
                return nwc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            } else {
                val networkInfo = cm.activeNetworkInfo ?: return false
                return networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_MOBILE
            }
        }
    }


}