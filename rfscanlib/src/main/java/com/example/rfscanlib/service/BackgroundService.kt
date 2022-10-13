package com.example.rfscanlib.service

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_MIN
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.rfscanlib.RFScanLib
import com.example.rfscanlib.checkPermissions
import com.example.rfscanlib.model.RFModel
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BackgroundService : LifecycleService() {

    lateinit var mainHandler: Handler
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var accuracy: Float = 0.0f
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null


    companion object {
        var isServiceRunning = false
        lateinit var rfModel: RFModel
        var rfLiveData = MutableLiveData<RFModel>()
        var interval: Int = 0
        var locationInterval: Int = 0
        var rfUpdateLocation = MutableLiveData<RFModel>()
        var notificationIcon: Int = R.drawable.ic_menu_view
    }

    private val scheduleRFScan = object : Runnable {
        override fun run() {
            CoroutineScope(Dispatchers.IO).launch {
                if (checkPermissions(applicationContext)) {
                    if (latitude != 0.0) {
                        rfModel = RFScanLib.getRFInfo(applicationContext,
                            longitude,
                            latitude,
                            accuracy)
                        isServiceRunning = true
                        rfLiveData.postValue(rfModel)
                    }

                }
            }
            mainHandler.postDelayed(this, (interval * 1000).toLong())
        }
    }

    private fun initData() {
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = (locationInterval * 10000).toLong()
        locationRequest!!.fastestInterval = ((locationInterval * 1000) / 2).toLong()
        //locationRequest!!.maxWaitTime = (locationInterval * 1000).toLong()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        //locationRequest!!.isWaitForAccurateLocation = true
        //locationRequest!!.expirationTime = (locationInterval*1000).toLong()
        //locationRequest!!.smallestDisplacement = 1.0f
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(scheduleRFScan)
        startLocationUpdates()

        val channelID = "1234"
        val notificationChannel = NotificationChannel(
            channelID, channelID, IMPORTANCE_MIN
        )

        getSystemService(NotificationManager::class.java).createNotificationChannel(
            notificationChannel
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelID)
            .setContentText("App running in background")
            .setContentTitle("EagleEye")
            .setSmallIcon(notificationIcon)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)

        startForeground(1001, notificationBuilder.build())
        return super.onStartCommand(intent, flags, startId)
    }

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations

            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                accuracy = location.accuracy
                latitude = location.latitude
                longitude = location.longitude
                rfUpdateLocation.postValue(RFScanLib.getRFInfo(applicationContext,
                    longitude,
                    latitude, accuracy))
//                   Log.e(TAG, "Location: $latitude::$longitude" )

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
            this.locationCallback,
            Looper.myLooper()!!
        )
    }

    /*override fun onDestroy() {
        rfLiveData.removeObservers(this)
        rfUpdateLocation.removeObservers(this)
        super.onDestroy()

    }*/

}