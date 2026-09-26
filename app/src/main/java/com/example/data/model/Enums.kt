package com.example.data.model

enum class TripStatus {
    IDLE,
    TRACKING,
    PAUSED,
    COMPLETED,
    DISCARDED
}

enum class SpeedUnit(val label: String, val multiplierFromKmh: Double) {
    KMH("km/h", 1.0),
    MPH("mph", 0.621371)
}

enum class WarningLevel {
    INFO,
    WARNING,
    CRITICAL
}

enum class WarningType(val defaultTitle: String) {
    SPEED_LIMIT("Speed Warning"),
    GPS_ACCURACY("GPS Accuracy Warning"),
    GPS_LOST("GPS Lost"),
    BATTERY_LOW("Battery Warning"),
    BACKGROUND_RESTRICTED("Background Tracking Restriction"),
    DATA_ANOMALY("Data Anomaly"),
    LONG_STOP("Long Stop Notice")
}

enum class VehicleType(val displayName: String) {
    CAR("Car"),
    MOTORCYCLE("Motorcycle"),
    BUS("Bus"),
    TRUCK("Truck"),
    BICYCLE("Bicycle"),
    CUSTOM("Custom Vehicle")
}

enum class MaintenanceCategory(val title: String) {
    ENGINE_OIL("Engine Oil"),
    SERVICE("General Service"),
    TIRES("Tires & Alignment"),
    BRAKES("Brake System"),
    BATTERY("Battery Check"),
    OTHER("Other Maintenance")
}
