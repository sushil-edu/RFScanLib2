package com.example.rfscanlib.model

data class RFModel(
    var carrierName: String,
    var isHomeNetwork: Boolean,
    var rsrp: Double,
    var rsrq: Double,
    var sinr: Double,
    var pci: Int,
    var networkType: String,
    var lteBand: String,
    var longitude: String,
    var latitude: String,
    var timestamp: Long,
    val localTime: String,
    val timeZone: String,
    val lastDataSync: Long
)