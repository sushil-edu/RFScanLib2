package com.example.rfscanlib.model

data class RFModel(
    var carrierName: String,
    var isHomeNetwork: Boolean,
    var rsrp: Double,
    var rsrq: Double,
    var sinr: Long,
    var pci: Int,
    var networkType: String,
    var lteBand: String,
    var longitude: Double,
    var latitude: Double,
    var timestamp: Long,
    val localTime: String,
    val timeZone: String
)