package com.example.rfscanlib

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import com.example.rfscan.PERMISSION_ID
import com.example.rfscan.checkPermissions
import com.example.rfscan.requestPermissions
import com.example.rfscan.service.BackgroundService

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onResume() {
        super.onResume()
        if (checkPermissions(this)){
            if(isGPSEnabled()){
                if(!BackgroundService().isServiceRunning){
                    startForegroundService(Intent(this, BackgroundService::class.java))
                }else{
//                    Log.e("MainActivity", BackgroundService().rfModel.carrierName.toString())
                }
            }else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }else{
            requestPermissions(this)
        }
    }
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                startLocationUpdate()
            }
        }
    }
    private fun isGPSEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


}