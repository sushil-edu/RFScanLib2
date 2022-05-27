package com.example.rfscanlib

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import com.example.rfscanlib.model.RFModel
import java.util.*

class RFScan {

    private lateinit var rfModel: RFModel

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
                        rfModel.rsrp = 0.0
                        rfModel.rsrq = 0.0
                        rfModel.sinr = 0.0
                        rfModel.lteBand = gsm.level.toString()
                        rfModel.pci = 0
                    }
                    /*is CellInfoCdma -> {
                        val cdma = info.cellSignalStrength.cdmaDbm
                    }*/

                    is CellInfoLte -> {
                        val lte = info.cellSignalStrength
                        rfModel.rsrp = lte.rsrp.toDouble()
                        rfModel.rsrq = lte.rsrq.toDouble()
                        rfModel.sinr = lte.rssnr.toDouble()
                        rfModel.lteBand = lte.level.toString()
                        rfModel.pci = info.cellIdentity.pci

                    }
                    else -> {
                        throw Exception("Unknown type of cell signal!")
                    }
                }
                break
            }
            rfModel.timestamp = Calendar.getInstance().timeInMillis

            return rfModel
        } catch (e: Exception) {
            throw e
        }
    }
}