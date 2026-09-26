package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.MaintenanceCategory
import com.example.data.model.TripStatus
import com.example.data.model.VehicleType
import com.example.data.model.WarningLevel
import com.example.data.model.WarningType

class Converters {
    @TypeConverter
    fun fromTripStatus(status: TripStatus?): String? = status?.name

    @TypeConverter
    fun toTripStatus(value: String?): TripStatus? =
        value?.let { runCatching { TripStatus.valueOf(it) }.getOrDefault(TripStatus.IDLE) }

    @TypeConverter
    fun fromVehicleType(type: VehicleType?): String? = type?.name

    @TypeConverter
    fun toVehicleType(value: String?): VehicleType? =
        value?.let { runCatching { VehicleType.valueOf(it) }.getOrDefault(VehicleType.CAR) }

    @TypeConverter
    fun fromMaintenanceCategory(cat: MaintenanceCategory?): String? = cat?.name

    @TypeConverter
    fun toMaintenanceCategory(value: String?): MaintenanceCategory? =
        value?.let { runCatching { MaintenanceCategory.valueOf(it) }.getOrDefault(MaintenanceCategory.OTHER) }

    @TypeConverter
    fun fromWarningLevel(level: WarningLevel?): String? = level?.name

    @TypeConverter
    fun toWarningLevel(value: String?): WarningLevel? =
        value?.let { runCatching { WarningLevel.valueOf(it) }.getOrDefault(WarningLevel.INFO) }

    @TypeConverter
    fun fromWarningType(type: WarningType?): String? = type?.name

    @TypeConverter
    fun toWarningType(value: String?): WarningType? =
        value?.let { runCatching { WarningType.valueOf(it) }.getOrDefault(WarningType.SPEED_LIMIT) }
}
