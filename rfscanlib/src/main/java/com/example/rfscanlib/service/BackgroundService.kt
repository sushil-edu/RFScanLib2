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
import androidx.lifecycle.MutableLiveData
import com.example.rfscanlib.TAG
import com.example.rfscan.checkPermissions
import com.example.rfscanlib.RFScanLib
import com.example.rfscanlib.level
import com.example.rfscanlib.log
import com.example.rfscanlib.model.RFModel
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BackgroundService : Service() {

    lateinit var mainHandler: Handler
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

   companion object{
       var isServiceRunning = false
       lateinit var rfModel: RFModel
       var rfLiveData = MutableLiveData<RFModel>()
       var interval:Int=0
   }

    private val scheduleRFScan = object : Runnable {
        override fun run() {
            CoroutineScope(Dispatchers.IO).launch {
                if (checkPermissions(applicationContext)) {
                    if (latitude != 0.0) {
                        rfModel = RFScanLib.getRFInfo(applicationContext, longitude, latitude)
                        rfLiveData.postValue(rfModel)
                        sendMessageToActivity(rfModel)
                    }

                }
            }
            mainHandler.postDelayed(this, (interval*1000).toLong())
        }
    }

    private fun sendMessageToActivity(newData: RFModel) {
        val broadcastIntent = Intent()
        broadcastIntent.action = "ServiceToActivityAction"
        broadcastIntent.putExtra("RF", newData.toString())
        sendBroadcast(broadcastIntent)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun initData() {
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = (interval*1000).toLong()
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
            .setContentTitle("RFScanLib")
//            .setSmallIcon(R.mipmap.ic_launcher)
        startForeground(1001, notificationBuilder.build())
        return super.onStartCommand(intent, flags, startId)
    }

    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations

            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                latitude = location.latitude
                longitude = location.longitude
                Log.e(TAG, "Location: $latitude::$longitude" )
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


    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning= false

    }

}