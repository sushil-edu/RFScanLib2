package com.example.rfscan

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.io.IOException



@SuppressLint("MissingPermission", "SwitchIntDef")
fun getNetwork(context: Context): String {
//    val tag = context::class.java.simpleName
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
                TelephonyManager.NETWORK_TYPE_GSM -> return "2G"
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_TD_SCDMA -> return "3G"
                TelephonyManager.NETWORK_TYPE_LTE,
                TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> return "4G"
                TelephonyManager.NETWORK_TYPE_NR -> return "5G"
                else -> return "?"
            }
        }
        else -> return "?"
    }
}

@SuppressLint("MissingPermission")
fun getICCID(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val tm2 = context.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager
        val phoneAccounts: Iterator<PhoneAccountHandle> =
            tm2.callCapablePhoneAccounts.listIterator()
        val phoneAccountHandle = phoneAccounts.next()
        phoneAccountHandle.id.substring(0, 19)
    } else {
        val sm = context.getSystemService(SubscriptionManager::class.java)

        val sis = sm.activeSubscriptionInfoList
        /* LogUtil.DEBUG("iccid", sis.toString())*/
        val si = sis[0]
        si.iccId
    }
}

fun getIMEI(tm: TelephonyManager): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "unknown" else tm.imei.toString()
}

fun writeLogsToFile(msg: String, context: Context) {
    val tag = context::class.java.simpleName
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Logs.txt")
    val fileSize = (file.length() / 1024).toString().toInt()
//    log(tag, "$file: file length: : $fileSize", Level.DEBUG)
    if (file.exists() && fileSize >= 10) {
        file.delete()
    } else {
        Toast.makeText(context, "$file", Toast.LENGTH_LONG).show()
        try {
            val fw = FileWriter(file, true)
            fw.write(msg)
            fw.close()
        } catch (e: IOException) {
        }
    }
}
