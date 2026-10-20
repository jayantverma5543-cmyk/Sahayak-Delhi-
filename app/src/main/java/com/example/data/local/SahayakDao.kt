package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SahayakDao {
    // Tanker Bookings
    @Query("SELECT * FROM tanker_bookings ORDER BY bookingTimeMillis DESC")
    fun getAllTankerBookings(): Flow<List<TankerBookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTankerBooking(booking: TankerBookingEntity): Long

    @Query("DELETE FROM tanker_bookings WHERE id = :id")
    suspend fun deleteTankerBooking(id: Long)

    // Doctor Appointments
    @Query("SELECT * FROM doctor_appointments ORDER BY bookingTimeMillis DESC")
    fun getAllDoctorAppointments(): Flow<List<DoctorAppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctorAppointment(appointment: DoctorAppointmentEntity): Long

    @Query("DELETE FROM doctor_appointments WHERE id = :id")
    suspend fun deleteDoctorAppointment(id: Long)

    // Counsellor Queries
    @Query("SELECT * FROM counsellor_queries ORDER BY timestampMillis DESC")
    fun getAllCounsellorQueries(): Flow<List<CounsellorQueryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounsellorQuery(query: CounsellorQueryEntity): Long

    @Query("DELETE FROM counsellor_queries WHERE id = :id")
    suspend fun deleteCounsellorQuery(id: Long)

    // Weather Signals
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherSignal(signal: WeatherSignal): Long

    @Query("SELECT * FROM weather_signals ORDER BY timestampMillis DESC LIMIT 1")
    fun getLastWeatherSignal(): Flow<WeatherSignal?>

    @Query("SELECT * FROM weather_signals ORDER BY timestampMillis DESC LIMIT 1")
    suspend fun getLastWeatherSignalSync(): WeatherSignal?

    // Hospital Bed Trends
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospitalBedTrend(trend: HospitalBedTrend): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospitalBedTrends(trends: List<HospitalBedTrend>)

    @Query("SELECT * FROM hospital_bed_trends ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentHospitalTrends(limit: Int = 10): Flow<List<HospitalBedTrend>>

    @Query("SELECT * FROM hospital_bed_trends ORDER BY timestampMillis DESC LIMIT :limit")
    suspend fun getRecentHospitalTrendsList(limit: Int = 10): List<HospitalBedTrend>

    // Tanker Demands
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTankerDemand(demand: TankerDemand): Long

    @Query("SELECT * FROM tanker_demands ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentTankerDemands(limit: Int = 10): Flow<List<TankerDemand>>

    @Query("SELECT * FROM tanker_demands ORDER BY timestampMillis DESC LIMIT :limit")
    suspend fun getRecentTankerDemandsList(limit: Int = 10): List<TankerDemand>

    // User Query Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserQueryLog(log: UserQueryLog): Long

    @Query("SELECT * FROM user_query_logs ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentUserQueries(limit: Int = 10): Flow<List<UserQueryLog>>

    @Query("SELECT * FROM user_query_logs ORDER BY timestampMillis DESC LIMIT :limit")
    suspend fun getRecentUserQueriesList(limit: Int = 10): List<UserQueryLog>

    // Module Usage Stats (for Personalization)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateModuleUsage(stats: UserModuleUsageStats)

    @Query("SELECT * FROM user_module_usage_stats ORDER BY visitCount DESC")
    fun getAllModuleUsageStats(): Flow<List<UserModuleUsageStats>>

    @Query("SELECT * FROM user_module_usage_stats ORDER BY visitCount DESC")
    suspend fun getAllModuleUsageStatsSync(): List<UserModuleUsageStats>

    @Query("SELECT * FROM user_module_usage_stats WHERE moduleKey = :moduleKey LIMIT 1")
    suspend fun getModuleUsage(moduleKey: String): UserModuleUsageStats?
}
