package com.example.rfscanlib

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import com.example.rfscan.TAG
import com.example.rfscan.checkPermissions
import com.example.rfscan.getNetwork
import com.example.rfscan.requestPermissions
import com.example.rfscanlib.model.RFModel
import com.example.rfscanlib.service.BackgroundService
import java.time.LocalDateTime
import java.util.*

class RFScanLib {


    companion object {
        private var carrierName: String = ""
        private var isHomeNetwork: Boolean = false
        private var rsrp: Double = 0.0
        private var rsrq: Double = 0.0
        private var sinr: Long = 0
        private var pci: Int = 0
        private var networkType: String = ""
        private var lteBand: String = ""

        fun startService(context: Context?, isBackgroundService: Boolean, interval: Int) {
            if (!BackgroundService.isServiceRunning && isBackgroundService) {
                BackgroundService.interval = interval
                context?.startForegroundService(Intent(context, BackgroundService::class.java))
                log(TAG, "Services started", level.INFO)
            } else {
                stopService(context)
            }
        }

        fun stopService(context: Context?) {
            context?.stopService(Intent(context, BackgroundService::class.java))
            log(TAG, "Service Stopped", level.INFO)
        }

        @SuppressLint("MissingPermission")
        fun getRFInfo(context: Context, longitude: Double, latitude: Double): RFModel {
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
                                    rsrp = lte.rsrp.toDouble()
                                    rsrq = lte.rsrq.toDouble()
                                    sinr = lte.rssnr.toLong()
                                    lteBand = lte.level.toString()
                                    pci = info.cellIdentity.pci

                                }
                                else -> {
                                    log(TAG, "Unknown type of cell signal!", level.ERROR)
                                    throw Exception("Unknown type of cell signal!")
                                }
                            }
                            break
                        }

                    } catch (e: Exception) {
                        log(TAG, e.message.toString(), level.ERROR)
                        throw e
                    }
                } else {
                    requestPermissions(context as AppCompatActivity)
                }
            } catch (e: Exception) {
                log(TAG, e.message.toString(), level.ERROR)
            }

            return RFModel(
                carrierName = carrierName,
                isHomeNetwork = isHomeNetwork,
                rsrp = rsrp,
                rsrq = rsrq,
                sinr = sinr,
                pci = pci,
                networkType = getNetwork(context),
                lteBand = lteBand,
                longitude = longitude,
                latitude = latitude,
                timestamp = Calendar.getInstance().timeInMillis,
                localTime = LocalDateTime.now().toString(),
                timeZone = Calendar.getInstance().time.toString().split(" ")[4]
            )
        }

    }
}