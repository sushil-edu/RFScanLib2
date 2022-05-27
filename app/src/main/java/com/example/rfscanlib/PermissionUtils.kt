package com.example.rfscan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

fun checkPermissions(context: Context): Boolean {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_NUMBERS
        ) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED /*&&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED*/
    ) {
        return true
    }
    return false
}

const val PERMISSION_ID = 42

fun requestPermissions(context: AppCompatActivity) {
    ActivityCompat.requestPermissions(
        context,
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_NUMBERS,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_EXTERNAL_STORAGE
        ),
        PERMISSION_ID
    )
}