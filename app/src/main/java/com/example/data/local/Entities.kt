package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tanker_bookings")
data class TankerBookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplierId: String,
    val supplierName: String,
    val capacityLiters: Int,
    val totalCost: Int,
    val deliveryDate: String,
    val deliverySlot: String,
    val customerName: String,
    val phone: String,
    val deliveryAddress: String,
    val landmark: String,
    val bookingTimeMillis: Long = System.currentTimeMillis(),
    val status: String = "Confirmed (Dispatch Scheduled)"
)

@Entity(tableName = "doctor_appointments")
data class DoctorAppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val doctorId: String,
    val doctorName: String,
    val speciality: String,
    val hospitalName: String,
    val consultationFee: Int,
    val patientName: String,
    val patientAge: Int,
    val patientGender: String,
    val phone: String,
    val appointmentDate: String,
    val appointmentSlot: String,
    val consultMode: String, // "In-Person OPD" or "Teleconsultation"
    val symptoms: String,
    val bookingTimeMillis: Long = System.currentTimeMillis(),
    val status: String = "Requested (Slot Reserved)"
)

@Entity(tableName = "counsellor_queries")
data class CounsellorQueryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val counsellorId: String,
    val counsellorName: String,
    val examCategory: String,
    val studentName: String,
    val phone: String,
    val preparationStage: String, // "Beginner / 1st Attempt", "Dropper / Repeat", "Mains / Interview", "Query"
    val queryText: String,
    val preferredSlot: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val status: String = "Assigned (Counsellor Call within 4 hrs)"
)

@Entity(tableName = "weather_signals")
data class WeatherSignal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val temperatureCelsius: Float,
    val humidityPercent: Float,
    val heatRiskLevel: String = "ELEVATED", // "NORMAL", "ELEVATED", "EXTREME_HEATWAVE"
    val aqiIndex: Int = 320,
    val delhiRegion: String = "Delhi Central & NCR",
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "hospital_bed_trends")
data class HospitalBedTrend(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hospitalId: String,
    val hospitalName: String,
    val icuAvailable: Int,
    val icuTotal: Int,
    val oxygenBeds: Int,
    val ventilatorBeds: Int,
    val occupancyPercent: Float,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "tanker_demands")
data class TankerDemand(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val zone: String, // e.g., "South Delhi (Mehrauli/Vasant Kunj)", "Dwarka", "Rohini"
    val demandLevel: String = "HIGH", // "NORMAL", "HIGH", "CRITICAL_DEFICIT"
    val activeRequestsCount: Int = 42,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_query_logs")
data class UserQueryLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val queryText: String,
    val moduleAccessed: String, // "HOSPITALS", "WATER_TANKER", "DOCTORS", "COUNSELLOR", "FIRST_AID"
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_module_usage_stats")
data class UserModuleUsageStats(
    @PrimaryKey
    val moduleKey: String, // "WATER_TANKER", "HOSPITALS", "DOCTORS", "COUNSELLOR", "FIRST_AID"
    val visitCount: Int = 1,
    val lastVisitedMillis: Long = System.currentTimeMillis()
)

