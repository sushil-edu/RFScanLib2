package com.example.rfscanlib

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.*
import android.telephony.AccessNetworkConstants.EutranBand
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.rfscanlib.model.RFModel
import com.example.rfscanlib.service.BackgroundService
import java.text.SimpleDateFormat
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



        fun startService(
            context: Context?,
            isBackgroundService: Boolean,
            interval: Int, locationInterval: Int,
        ) {
            if (!BackgroundService.isServiceRunning && isBackgroundService) {
                BackgroundService.interval = interval
                BackgroundService.locationInterval = locationInterval
                context?.startForegroundService(Intent(context, BackgroundService::class.java))
                log(TAG, "Services started", Level.INFO)
            }

        }

        fun stopService(context: Context?) {
            context?.stopService(Intent(context, BackgroundService::class.java))
            log(TAG, "Service Stopped", Level.INFO)
        }

        @SuppressLint("MissingPermission", "NewApi")
        fun getRFInfo(context: Context, longitude: Double, latitude: Double): RFModel {
            try {
                if (checkPermissions(context)) {
                    val tm: TelephonyManager =
                        context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
                    val data= tm.allCellInfo
                    val cc = tm.signalStrength
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val listCC = cc!!.cellSignalStrengths
                        for (css in listCC) {
                            when (css) {
                                is CellSignalStrengthGsm -> {
                                    val rssi = css.rssi
                                    // Log.e("Gsm Rssnr::", "$rssi")
                                }
                                is CellSignalStrengthLte -> {
                                    rsrp = css.rsrp.toDouble()
                                    rsrq = css.rsrq.toDouble()
                                    sinr = css.rssnr.toLong()

                                }
                                is CellSignalStrengthNr -> {
                                    rsrp = css.csiRsrp.toDouble()
                                    rsrq = css.csiRsrq.toDouble()
                                    sinr = css.csiSinr.toLong()
                                }
                            }
                            break

                        }
                    }else{
                        sinr = (cc.toString().split(" ")[11]).toLong()
                    }
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
                                    sinr = gsm.rssi.toLong()
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
                                   /* sinr = lte.rssnr.toLong()*/
                                    lteBand = getBandFromEarfnc(info.cellIdentity.earfcn).toString()
                                    pci = info.cellIdentity.pci
                                }


                                else -> {
                                    log(TAG, "Unknown type of cell signal!", Level.ERROR)
                                    throw Exception("Unknown type of cell signal!")
                                }
                            }
                            break
                        }

                    } catch (e: Exception) {
                        log(TAG, e.message.toString(), Level.ERROR)
                        throw e
                    }
                } else {
                    rqstPermissions(context as AppCompatActivity)
                }
            } catch (e: Exception) {
                log(TAG, e.message.toString(), Level.ERROR)
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
                localTime = SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss", Locale.getDefault()).format(
                    Date()),
                timeZone = Calendar.getInstance().time.toString().split(" ")[4]
            )
        }

        private const val INVALID_BAND = -1
        @SuppressLint("InlinedApi")
        fun getBandFromEarfnc(earfcn: Int): Int {
            when {
                earfcn > 70645 -> {
                    return INVALID_BAND
                }
                earfcn >= 70596 -> {
                    return EutranBand.BAND_88
                }
                earfcn >= 70546 -> {
                    return EutranBand.BAND_87
                }
                earfcn >= 70366 -> {
                    return EutranBand.BAND_85
                }
                earfcn > 69465 -> {
                    return INVALID_BAND
                }
                earfcn >= 69036 -> {
                    return EutranBand.BAND_74
                }
                earfcn >= 68986 -> {
                    return EutranBand.BAND_73
                }
                earfcn >= 68936 -> {
                    return EutranBand.BAND_72
                }
                earfcn >= 68586 -> {
                    return EutranBand.BAND_71
                }
                earfcn >= 68336 -> {
                    return EutranBand.BAND_70
                }
                earfcn > 67835 -> {
                    return INVALID_BAND
                }
                earfcn >= 67536 -> {
                    return EutranBand.BAND_68
                }
                earfcn >= 67366 -> {
                    return INVALID_BAND // band 67 only for CarrierAgg
                }
                earfcn >= 66436 -> {
                    return EutranBand.BAND_66
                }
                earfcn >= 65536 -> {
                    return EutranBand.BAND_65
                }
                earfcn > 60254 -> {
                    return INVALID_BAND
                }
                earfcn >= 60140 -> {
                    return EutranBand.BAND_53
                }
                earfcn >= 59140 -> {
                    return EutranBand.BAND_52
                }
                earfcn >= 59090 -> {
                    return EutranBand.BAND_51
                }
                earfcn >= 58240 -> {
                    return EutranBand.BAND_50
                }
                earfcn >= 56740 -> {
                    return EutranBand.BAND_49
                }
                earfcn >= 55240 -> {
                    return EutranBand.BAND_48
                }
                earfcn >= 54540 -> {
                    return EutranBand.BAND_47
                }
                earfcn >= 46790 -> {
                    return EutranBand.BAND_46
                }
                earfcn >= 46590 -> {
                    return EutranBand.BAND_45
                }
                earfcn >= 45590 -> {
                    return EutranBand.BAND_44
                }
                earfcn >= 43590 -> {
                    return EutranBand.BAND_43
                }
                earfcn >= 41590 -> {
                    return EutranBand.BAND_42
                }
                earfcn >= 39650 -> {
                    return EutranBand.BAND_41
                }
                earfcn >= 38650 -> {
                    return EutranBand.BAND_40
                }
                earfcn >= 38250 -> {
                    return EutranBand.BAND_39
                }
                earfcn >= 37750 -> {
                    return EutranBand.BAND_38
                }
                earfcn >= 37550 -> {
                    return EutranBand.BAND_37
                }
                earfcn >= 36950 -> {
                    return EutranBand.BAND_36
                }
                earfcn >= 36350 -> {
                    return EutranBand.BAND_35
                }
                earfcn >= 36200 -> {
                    return EutranBand.BAND_34
                }
                earfcn >= 36000 -> {
                    return EutranBand.BAND_33
                }
                earfcn > 10359 -> {
                    return INVALID_BAND
                }
                earfcn >= 9920 -> {
                    return INVALID_BAND // band 32 only for CarrierAgg
                }
                earfcn >= 9870 -> {
                    return EutranBand.BAND_31
                }
                earfcn >= 9770 -> {
                    return EutranBand.BAND_30
                }
                earfcn >= 9660 -> {
                    return INVALID_BAND // band 29 only for CarrierAgg
                }
                earfcn >= 9210 -> {
                    return EutranBand.BAND_28
                }
                earfcn >= 9040 -> {
                    return EutranBand.BAND_27
                }
                earfcn >= 8690 -> {
                    return EutranBand.BAND_26
                }
                earfcn >= 8040 -> {
                    return EutranBand.BAND_25
                }
                earfcn >= 7700 -> {
                    return EutranBand.BAND_24
                }
                earfcn >= 7500 -> {
                    return EutranBand.BAND_23
                }
                earfcn >= 6600 -> {
                    return EutranBand.BAND_22
                }
                earfcn >= 6450 -> {
                    return EutranBand.BAND_21
                }
                earfcn >= 6150 -> {
                    return EutranBand.BAND_20
                }
                earfcn >= 6000 -> {
                    return EutranBand.BAND_19
                }
                earfcn >= 5850 -> {
                    return EutranBand.BAND_18
                }
                earfcn >= 5730 -> {
                    return EutranBand.BAND_17
                }
                earfcn > 5379 -> {
                    return INVALID_BAND
                }
                earfcn >= 5280 -> {
                    return EutranBand.BAND_14
                }
                earfcn >= 5180 -> {
                    return EutranBand.BAND_13
                }
                earfcn >= 5010 -> {
                    return EutranBand.BAND_12
                }
                earfcn >= 4750 -> {
                    return EutranBand.BAND_11
                }
                earfcn >= 4150 -> {
                    return EutranBand.BAND_10
                }
                earfcn >= 3800 -> {
                    return EutranBand.BAND_9
                }
                earfcn >= 3450 -> {
                    return EutranBand.BAND_8
                }
                earfcn >= 2750 -> {
                    return EutranBand.BAND_7
                }
                earfcn >= 2650 -> {
                    return EutranBand.BAND_6
                }
                earfcn >= 2400 -> {
                    return EutranBand.BAND_5
                }
                earfcn >= 1950 -> {
                    return EutranBand.BAND_4
                }
                earfcn >= 1200 -> {
                    return EutranBand.BAND_3
                }
                earfcn >= 600 -> {
                    return EutranBand.BAND_2
                }
                earfcn >= 0 -> {
                    return EutranBand.BAND_1
                }
                else -> return INVALID_BAND
            }
        }

    }
}
