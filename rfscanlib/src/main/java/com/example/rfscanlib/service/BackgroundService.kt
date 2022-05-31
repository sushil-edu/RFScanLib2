package com.example.rfscanlib.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.rfscan.checkPermissions
import com.example.rfscanlib.RFScan
import com.example.rfscanlib.model.RFModel
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


class BackgroundService(backgroundServiceInterval: Int) : Service() {

    lateinit var mainHandler: Handler
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private val locationRefreshInterval: Long = (backgroundServiceInterval * 1000).toLong()
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    var isServiceRunning = false
    lateinit var rfModel: RFModel


    private val scheduleRFScan = object : Runnable {
        override fun run() {
            CoroutineScope(Dispatchers.IO).launch {
                if (checkPermissions(applicationContext)) {

                    RFScan().getRFInfo(applicationContext)

                }
            }
            mainHandler.postDelayed(this, (backgroundServiceInterval * 100).toLong())
        }
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun initData() {
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = locationRefreshInterval
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(scheduleRFScan)

        startLocationUpdates()

        val channelID = "1234"

        val notificationChannel = NotificationChannel(
            channelID, channelID, NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            notificationChannel
        )
        val notificationBuilder = Notification.Builder(this, channelID)
            .setContentText("App running in background")
            .setContentTitle("RFScan")
//            .setSmallIcon(R.mipmap.ic_launcher)
        startForeground(1001, notificationBuilder.build())
        return super.onStartCommand(intent, flags, startId)
    }

    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations

            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                /*Toast.makeText(
                    this@ForegroundService, "Latitude: " + location.latitude.toString() + '\n' +
                            "Longitude: " + location.longitude, Toast.LENGTH_LONG
                ).show()*/
                Log.d("Location d", location.latitude.toString())
                latitude = location.latitude
                longitude = location.longitude
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        isServiceRunning = true

        initData()


    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mFusedLocationClient!!.requestLocationUpdates(
            this.locationRequest!!,
            this.locationCallback, Looper.myLooper()!!
        )
    }


    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = (sin(deg2rad(lat1))
                * sin(deg2rad(lat2))
                + (cos(deg2rad(lat1))
                * cos(deg2rad(lat2))
                * cos(deg2rad(theta))))
        dist = acos(dist)
        dist = rad2deg(dist)
        dist *= 60 * 1.1515 //in meter
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false

    }

}