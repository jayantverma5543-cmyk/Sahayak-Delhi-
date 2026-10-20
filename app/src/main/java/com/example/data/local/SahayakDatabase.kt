package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TankerBookingEntity::class,
        DoctorAppointmentEntity::class,
        CounsellorQueryEntity::class,
        WeatherSignal::class,
        HospitalBedTrend::class,
        TankerDemand::class,
        UserQueryLog::class,
        UserModuleUsageStats::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SahayakDatabase : RoomDatabase() {
    abstract fun sahayakDao(): SahayakDao

    companion object {
        @Volatile
        private var INSTANCE: SahayakDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `weather_signals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `temperatureCelsius` REAL NOT NULL,
                        `humidityPercent` REAL NOT NULL,
                        `heatRiskLevel` TEXT NOT NULL,
                        `aqiIndex` INTEGER NOT NULL,
                        `delhiRegion` TEXT NOT NULL,
                        `timestampMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `hospital_bed_trends` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `hospitalId` TEXT NOT NULL,
                        `hospitalName` TEXT NOT NULL,
                        `icuAvailable` INTEGER NOT NULL,
                        `icuTotal` INTEGER NOT NULL,
                        `oxygenBeds` INTEGER NOT NULL,
                        `ventilatorBeds` INTEGER NOT NULL,
                        `occupancyPercent` REAL NOT NULL,
                        `timestampMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tanker_demands` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `zone` TEXT NOT NULL,
                        `demandLevel` TEXT NOT NULL,
                        `activeRequestsCount` INTEGER NOT NULL,
                        `timestampMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_query_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `queryText` TEXT NOT NULL,
                        `moduleAccessed` TEXT NOT NULL,
                        `timestampMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_module_usage_stats` (
                        `moduleKey` TEXT PRIMARY KEY NOT NULL,
                        `visitCount` INTEGER NOT NULL,
                        `lastVisitedMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): SahayakDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SahayakDatabase::class.java,
                    "sahayak_delhi.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
