package com.example.rfscanlib

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("MissingPermission", "SwitchIntDef")
fun getNetwork(context: Context): String {
    val connectivityManager =
        context.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
    val nw = connectivityManager.activeNetwork ?: return "-"
    val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return "-"
    when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return "WIFI"
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return "ETHERNET"
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
            val tm =
                context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
            when (tm.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN,
                TelephonyManager.NETWORK_TYPE_GSM,
                -> return "2G"
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_TD_SCDMA,
                -> return "3G"
                TelephonyManager.NETWORK_TYPE_LTE,
                TelephonyManager.NETWORK_TYPE_IWLAN, 19,
                -> return "4G"
                TelephonyManager.NETWORK_TYPE_NR -> return "5G"
                else -> return "?"
            }
        }
        else -> return "?"
    }
}
