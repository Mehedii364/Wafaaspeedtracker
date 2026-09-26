package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.db.dao.FuelDao
import com.example.data.db.dao.LocationPointDao
import com.example.data.db.dao.MaintenanceDao
import com.example.data.db.dao.SpeedEventDao
import com.example.data.db.dao.TripDao
import com.example.data.db.dao.VehicleDao
import com.example.data.db.dao.WarningAlertDao
import com.example.data.db.entities.FuelRecordEntity
import com.example.data.db.entities.LocationPointEntity
import com.example.data.db.entities.MaintenanceEntity
import com.example.data.db.entities.SpeedEventEntity
import com.example.data.db.entities.TripEntity
import com.example.data.db.entities.VehicleEntity
import com.example.data.db.entities.WarningAlertEntity

@Database(
    entities = [
        VehicleEntity::class,
        TripEntity::class,
        LocationPointEntity::class,
        SpeedEventEntity::class,
        FuelRecordEntity::class,
        MaintenanceEntity::class,
        WarningAlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WafaDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun tripDao(): TripDao
    abstract fun locationPointDao(): LocationPointDao
    abstract fun speedEventDao(): SpeedEventDao
    abstract fun fuelDao(): FuelDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun warningAlertDao(): WarningAlertDao

    companion object {
        @Volatile
        private var INSTANCE: WafaDatabase? = null

        fun getInstance(context: Context): WafaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WafaDatabase::class.java,
                    "wafa_speed_tracker.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
