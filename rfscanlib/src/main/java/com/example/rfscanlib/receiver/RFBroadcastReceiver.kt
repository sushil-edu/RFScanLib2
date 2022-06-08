package com.example.rfscanlib.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.os.BatteryManager
import com.example.rfscanlib.service.BackgroundService

class RFBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val bm = context?.getSystemService(BATTERY_SERVICE) as BatteryManager

        val batLevel: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        var intentService = Intent(context, BackgroundService::class.java)
        if (intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            intentService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startForegroundService(intentService)
        }

    }
}