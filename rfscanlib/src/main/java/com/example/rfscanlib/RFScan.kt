package com.example.rfscanlib

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.rfscan.checkPermissions
import com.example.rfscan.getNetwork
import com.example.rfscan.requestPermissions
import com.example.rfscanlib.model.RFModel
import com.example.rfscanlib.service.BackgroundService
import java.time.LocalDateTime
import java.util.*

class RFScan {


    companion object {
        private lateinit var tm: TelephonyManager
        private var carrierName: String = ""
        private var isHomeNetwork: Boolean = false
        private var rsrp: Double = 0.0
        private var rsrq: Double = 0.0
        private var sinr: Long = 0
        private var pci: Int = 0
        private var networkType: String = ""
        private var lteBand: String = ""
        private var longitude: String = ""
        private var latitude: String = ""
        private var timestamp: Long = 0
        private var localTime: String = ""
        private var timeZone: String = ""

        fun startService(context: Context?): String {
            return if (!BackgroundService().isServiceRunning) {
                context?.startForegroundService(Intent(context, BackgroundService::class.java))
                "Service started"
            } else {
                "Service already running"
            }
        }

        fun stopService(context: Context?): String {
            context?.stopService(Intent(context, BackgroundService::class.java))
            return "Service Stopped"
        }

        @SuppressLint("MissingPermission")
        fun getRFData(context: Context?): RFModel {
            try {
                if (checkPermissions(context!!)) {
                    tm =
                        context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
                    val data = tm.allCellInfo
                    try {

                        for (info in data) {
                            when (info) {
                                is CellInfoGsm -> {
                                    val gsm = info.cellSignalStrength
                                    Log.e("GSM Data", gsm.toString())
                                    rsrp = gsm.dbm.toDouble()
                                    rsrq = 0.0
                                    sinr = 0
                                    lteBand = gsm.level.toString()
                                    pci = 0
                                }
                                /*is CellInfoCdma -> {
                                    val cdma = info.cellSignalStrength.cdmaDbm
                                }*/

                                is CellInfoLte -> {
                                    val lte = info.cellSignalStrength
                                    Log.e("Lte data", lte.toString())
                                    rsrp = lte.rsrp.toDouble()
                                    rsrq = lte.rsrq.toDouble()
                                    sinr = lte.rssnr.toLong()
                                    lteBand = lte.level.toString()
                                    pci = info.cellIdentity.pci

                                }
                                else -> {
                                    throw Exception("Unknown type of cell signal!")
                                }
                            }
                            break
                        }

                    } catch (e: Exception) {
                        throw e
                    }
                } else {
                    requestPermissions(context as AppCompatActivity)
                }
            } catch (e: Exception) {
                Log.e("excep", e.message.toString())
            }

            return RFModel(
                carrierName = tm.networkOperatorName,
                isHomeNetwork = !tm.isNetworkRoaming,
                rsrp = rsrp,
                rsrq = rsrq,
                sinr = sinr,
                pci = pci,
                networkType = getNetwork(context!!),
                lteBand = lteBand,
                longitude = 0.0,
                latitude = 0.0,
                timestamp = Calendar.getInstance().timeInMillis,
                localTime = LocalDateTime.now().toString(),
                timeZone = Calendar.getInstance().time.toString().split(" ")[4]
            )
        }

    }

    @SuppressLint("MissingPermission")
    fun getRFInfo(context: Context): RFModel {
        try {
            if (checkPermissions(context)) {
                val tm: TelephonyManager =
                    context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
                val data = tm.allCellInfo
                try {
                    carrierName = tm.networkOperatorName
                    isHomeNetwork = !tm.isNetworkRoaming
                    networkType = getNetwork(context)
                    for (info in data) {
                        when (info) {
                            is CellInfoGsm -> {
                                val gsm = info.cellSignalStrength
                                Log.e("GSM Data", gsm.toString())
                                rsrp = gsm.dbm.toDouble()
                                rsrq = 0.0
                                sinr = 0
                                lteBand = gsm.level.toString()
                                pci = 0
                            }
                            /*is CellInfoCdma -> {
                                val cdma = info.cellSignalStrength.cdmaDbm
                            }*/

                            is CellInfoLte -> {
                                val lte = info.cellSignalStrength
                                Log.e("Lte data", lte.toString())
                                rsrp = lte.rsrp.toDouble()
                                rsrq = lte.rsrq.toDouble()
                                sinr = lte.rssnr.toLong()
                                lteBand = lte.level.toString()
                                pci = info.cellIdentity.pci

                            }
                            else -> {
                                throw Exception("Unknown type of cell signal!")
                            }
                        }
                        break
                    }
                    timestamp = Calendar.getInstance().timeInMillis
                    localTime = LocalDateTime.now().toString()
                    timeZone = Calendar.getInstance().time.toString().split(" ")[4]

                } catch (e: Exception) {
                    throw e
                }
            } else {
                requestPermissions(context as AppCompatActivity)
            }
        } catch (e: Exception) {
            Log.e("excep", e.message.toString())
        }

        Log.e("RFInfo", rsrp.toString())
        return RFModel(
            carrierName = carrierName,
            isHomeNetwork = isHomeNetwork,
            rsrp = rsrp,
            rsrq = rsrq,
            sinr = sinr,
            pci = pci,
            networkType = getNetwork(context),
            lteBand = lteBand,
            longitude = longitude.toDouble(),
            latitude = latitude.toDouble(),
            timestamp = Calendar.getInstance().timeInMillis,
            localTime = LocalDateTime.now().toString(),
            timeZone = Calendar.getInstance().time.toString().split(" ")[4]
        )
    }

    private val PERMISSION_ID = 42

    private fun requestPermissions(context: AppCompatActivity) {
        ActivityCompat.requestPermissions(
            context,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_PHONE_NUMBERS,
            ),
            PERMISSION_ID
        )
    }
}