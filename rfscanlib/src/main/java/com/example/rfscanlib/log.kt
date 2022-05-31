package com.example.rfscanlib

import android.util.Log


enum class level {
    DEBUG, ERROR, INFO, WARNING
}

private var isDebug = BuildConfig.DEBUG

fun log(TAG: String, message: String, typ: level) {
    if (isDebug)
        when (typ) {
            level.DEBUG -> Log.d(TAG, message)
            level.ERROR -> Log.e(TAG, message)
            level.INFO -> Log.i(TAG, message)
            level.WARNING -> Log.w(TAG, message)
        }
}

