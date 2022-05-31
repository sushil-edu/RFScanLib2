package com.example.rfscanlib

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.rfscanlib.model.RFModel
import java.time.LocalDateTime
import java.util.*

class RFScanUtil {

    private var RSRQ: Double = 0.0
    private var RSRP: Double = 0.0
    private var PCI: Int = 0
    private var SNR: Long = 0
    private var lteBand: String = ""

    private val lstRFData: MutableList<RFModel> = ArrayList()

    companion object {
        val instanceRFScan = RFScanUtil()
    }

    @SuppressLint("MissingPermission", "NewApi", "HardwareIds")
    fun getRFInfo(context: Context, latitude: Double, longitude: Double): RFModel {
        //device info
//        val version =
//            "${Build.VERSION_CODES::class.java.fields[Build.VERSION.SDK_INT].name} ${Build.VERSION.RELEASE} API ${Build.VERSION.SDK_INT}"

        val version = Build.VERSION.RELEASE
        val androidID =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)


        //RF Scan data
        val tm: TelephonyManager =
            context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
        val data = tm.allCellInfo
        try {
            for (info in data) {

                when (info) {
                    is CellInfoGsm -> {
                        val gsm = info.cellSignalStrength
                        RSRP = 0.0
                        RSRQ = 0.0
                        SNR = 0
                        lteBand = gsm.level.toString()
                        PCI = 0
                    }
                    /*is CellInfoCdma -> {
                        val cdma = info.cellSignalStrength.cdmaDbm
                    }*/

                    is CellInfoLte -> {
                        var lte = info.cellSignalStrength
                        RSRP = lte.rsrp.toDouble()
                        RSRQ = lte.rsrq.toDouble()
                        SNR = lte.rssnr.toLong()
                        lteBand = lte.level.toString()
                        PCI = info.cellIdentity.pci

                    }
                    else -> {
                        throw Exception("Unknown type of cell signal!")
                    }
                }
                break
            }

            //RF Data
            var rm = RFModel(
                /* assetTag = "AST00101",
                 os = "Android",
                 osVersion = version,
                 serialNo = getDeviceSerial(),
                 manufacture = Build.MANUFACTURER,
                 modelNo = Build.MODEL,
                 imei = getIMEI(tm),
                 iccid = getICCID(context),
                 phoneNumber = tm.line1Number.ifEmpty { "unknown" },
                 androidID = androidID,*/
                carrierName = tm.networkOperatorName,
//                isHomeNetwork = if (tm.isNetworkRoaming) "Roam" else "Home",
                isHomeNetwork = !tm.isNetworkRoaming,
                rsrp = RSRP,
                rsrq = RSRQ,
                sinr = SNR,
                pci = PCI,
                networkType = getNetwork(context),
                lteBand = lteBand,
                longitude = longitude,
                latitude = latitude,
                timestamp = Calendar.getInstance().timeInMillis,
                localTime = LocalDateTime.now().toString(),
                timeZone = Calendar.getInstance().time.toString().split(" ")[4],

            )

            lstRFData.addAll(listOf(rm))
            Log.e(context.packageName, "RF Scan finished")
            return rm
        } catch (e: Exception) {
            throw e
        }
    }
}

