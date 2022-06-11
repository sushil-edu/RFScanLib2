package com.example.rfscanlib

import android.util.Log

const val TAG = "RFLib"

enum class Level {
    DEBUG, ERROR, INFO, WARNING
}

private var isDebug = BuildConfig.DEBUG

fun log(TAG: String, message: String, typ: Level) {
    if (isDebug)
        when (typ) {
            Level.DEBUG -> Log.d(TAG, message)
            Level.ERROR -> Log.e(TAG, message)
            Level.INFO -> Log.i(TAG, message)
            Level.WARNING -> Log.w(TAG, message)
        }
}

