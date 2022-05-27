package com.example.rfscanlib

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import com.example.rfscanlib.model.RFModel
import java.time.LocalDateTime
import java.util.*

class RFScan {

    private lateinit var carrierName: String
    private  var isHomeNetwork: Boolean = false
    private var rsrp: Double=0.0
    private var rsrq: Double=0.0
    private var sinr: Long=0
    private var pci: Int=0
    private lateinit var networkType: String
    private lateinit var lteBand: String
    private lateinit var longitude: String
    private lateinit var latitude: String
    private var timestamp: Long=0
    private var localTime: String=""
    private var timeZone: String=""


    @SuppressLint("MissingPermission")
    fun getRFInfo(context: Context): RFModel {
        val tm: TelephonyManager =
            context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
        val data = tm.allCellInfo
        try {
            for (info in data) {

                when (info) {
                    is CellInfoGsm -> {
                        val gsm = info.cellSignalStrength
                       rsrp = 0.0
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
                        throw Exception("Unknown type of cell signal!")
                    }
                }
                break
            }
            timestamp = Calendar.getInstance().timeInMillis
            localTime = LocalDateTime.now().toString()
            timeZone =  Calendar.getInstance().time.toString().split(" ")[4]
            return RFModel(carrierName, isHomeNetwork,rsrp, rsrq, sinr, pci, networkType, lteBand,longitude, latitude, timestamp, localTime, timeZone )
        } catch (e: Exception) {
            throw e
        }
    }
}