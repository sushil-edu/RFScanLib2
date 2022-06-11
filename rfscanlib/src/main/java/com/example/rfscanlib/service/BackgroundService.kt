package com.example.rfscanlib.service

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.rfscanlib.RFScanLib
import com.example.rfscanlib.model.RFModel
import com.google.android.gms.location.*


class BackgroundService : LifecycleService() {
    private var isFirstRun = true
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var bgFusedLocationClient: FusedLocationProviderClient? = null
    private var bgLocationRequest: LocationRequest? = null

    companion object {
        var rfLiveData = MutableLiveData<RFModel>()
        var backgroundServiceInterval: Int = 0
        var foregroundServiceInterval: Int = 0
        var rfUpdateLocation = MutableLiveData<RFModel>()
        const val ACTION_STOP_LISTEN = "action_stop_listen"


        fun startService(
            context: Context?,
            isBackgroundService: Boolean,
        ) {
            if (isBackgroundService) {
                context?.startForegroundService(Intent(context, BackgroundService::class.java))
            }
        }

        fun stopService(context: Context?) {
            val intent = Intent(context, BackgroundService::class.java)
            intent.action = ACTION_STOP_LISTEN
            context?.startService(intent)
        }
    }

    private fun initData() {
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = (foregroundServiceInterval * 1000).toLong()
        locationRequest!!.fastestInterval = (foregroundServiceInterval * 1000).toLong()
        locationRequest!!.maxWaitTime = (foregroundServiceInterval * 1000).toLong()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        //for background service
        bgLocationRequest = LocationRequest.create()
        bgLocationRequest!!.interval = (backgroundServiceInterval * 1000).toLong()
        bgLocationRequest!!.fastestInterval = (backgroundServiceInterval * 1000).toLong()
        bgLocationRequest!!.maxWaitTime = (backgroundServiceInterval * 1000).toLong()
        bgLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        bgFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        if (isFirstRun) {
            fnStartService()
        }

        //to stop service
        if (intent?.action != null && intent.action.equals(
                ACTION_STOP_LISTEN, ignoreCase = true)
        ) {
            stopForeground(true)
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun fnStartService() {
        startLocationUpdates()
        startLocationUpdatesInBG()
        notification()
    }

    private fun notification() {
        val channelID = "1234"
        val notificationChannel = NotificationChannel(
            channelID, channelID, IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            notificationChannel
        )
        val notificationBuilder = Notification.Builder(this, channelID)
            .setContentText("App running in background")
            .setContentTitle("RFScanLib")
//            .setSmallIcon(R.mipmap.ic_launcher)
        startForeground(1001, notificationBuilder.build())
    }

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations

            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                latitude = location.latitude
                longitude = location.longitude
                rfUpdateLocation.postValue(RFScanLib.getRFInfo(applicationContext,
                    longitude,
                    latitude))

            }
        }
    }

    private var locationCallbackBG: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations

            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                latitude = location.latitude
                longitude = location.longitude
                rfLiveData.postValue(RFScanLib.getRFInfo(applicationContext,
                    longitude,
                    latitude))

            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        initData()

    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mFusedLocationClient!!.requestLocationUpdates(
            this.locationRequest!!,
            this.locationCallback, Looper.myLooper()!!
        )
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdatesInBG() {
        bgFusedLocationClient!!.requestLocationUpdates(
            this.bgLocationRequest!!,
            this.locationCallbackBG, Looper.myLooper()!!
        )
    }

    fun isServiceRunning(): Boolean {
        val serviceClass = BackgroundService::class.java
        try {
            val manager =
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(
                Int.MAX_VALUE
            )) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

}