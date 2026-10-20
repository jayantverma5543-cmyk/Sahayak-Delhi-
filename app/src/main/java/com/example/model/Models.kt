package com.example.model

enum class ScreenTab(val title: String) {
    EMERGENCY("Emergency"),
    HOSPITALS("Hospitals"),
    WATER_TANKER("Water"),
    DOCTORS("Doctors"),
    COUNSELLOR("Counsellor"),
    FIRST_AID("First Aid")
}

enum class DoctorSortOption(val label: String) {
    RATING_HIGH_TO_LOW("Rating: High to Low"),
    RATING_LOW_TO_HIGH("Rating: Low to High"),
    FEE_LOW_TO_HIGH("Fee: Low to High"),
    FEE_HIGH_TO_LOW("Fee: High to Low"),
    DISTANCE_NEAREST("Distance: Nearest")
}

enum class WaterSortOption(val label: String) {
    RATING_HIGH_TO_LOW("Rating: High to Low"),
    RATING_LOW_TO_HIGH("Rating: Low to High"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    DISTANCE_NEAREST("Distance: Nearest")
}

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String = "Delhi Center (Connaught Place)",
    val isRealGps: Boolean = false
)

data class Hospital(
    val id: String,
    val name: String,
    val hindiName: String = "",
    val area: String,
    val fullAddress: String,
    val phone: String,
    val mobileNumber: String = "",
    val emergencyPhone: String,
    val icuTotal: Int,
    val icuAvailable: Int,
    val oxygenBedsAvailable: Int,
    val ventilatorBedsAvailable: Int,
    val distanceKm: Double,
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val hospitalType: HospitalType, // GOVT, PRIVATE_TRUST, SUPER_SPECIALITY
    val is24x7Emergency: Boolean = true,
    val googleMapsUrl: String,
    val websiteUrl: String = "",
    val landmark: String,
    val nearestMetroStation: String = "",
    val nearestMetroLine: String = ""
)

enum class HospitalType(val label: String) {
    GOVERNMENT("Delhi Govt / Central"),
    SUPER_SPECIALITY("Super Speciality (Private)"),
    PRIVATE_TRUST("Trust / Non-Profit")
}

data class Clinic(
    val id: String,
    val name: String,
    val hindiName: String = "",
    val area: String,
    val fullAddress: String,
    val phone: String,
    val mobileNumber: String,
    val clinicType: ClinicType,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double,
    val openHours: String,
    val servicesAvailable: String,
    val googleMapsUrl: String,
    val landmark: String,
    val nearestMetroStation: String = ""
)

enum class ClinicType(val label: String) {
    URGENT_CARE_24X7("24x7 Urgent Care Clinic"),
    MOHALLA_CLINIC("Aam Aadmi Mohalla Clinic"),
    GOVT_DISPENSARY("Govt Health Dispensary"),
    POLYCLINIC("Delhi Govt Polyclinic")
}

data class MetroStation(
    val id: String,
    val name: String,
    val lines: List<String>,
    val latitude: Double,
    val longitude: Double,
    val connectedHospital: String? = null,
    val isInterchange: Boolean = false
)

data class WaterSupplier(
    val id: String,
    val name: String,
    val areaServed: String,
    val baseRatePer5000L: Int,
    val baseRatePer3000L: Int,
    val baseRatePer10000L: Int,
    val distanceKm: Double,
    val rating: Double,
    val totalReviews: Int,
    val deliveryTimeMinutes: String,
    val phone: String,
    val isGovtSubsidized: Boolean = false,
    val isGovernment: Boolean = false,
    val authorityName: String = "Delhi Jal Board (Govt)",
    val waterType: String = "Potable Drinking (TDS < 150)",
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val availableSlots: List<String> = listOf("Morning 06:00 - 09:00", "Noon 11:00 - 02:00", "Evening 04:00 - 07:00", "Night 08:00 - 11:00")
)

data class Doctor(
    val id: String,
    val name: String,
    val speciality: String,
    val qualification: String,
    val hospitalName: String,
    val consultationFee: Int,
    val experienceYears: Int,
    val rating: Double,
    val reviewsCount: Int,
    val phone: String,
    val mobileNumber: String = "",
    val availableDays: String,
    val nextAvailableSlot: String,
    val languages: List<String> = listOf("Hindi", "English"),
    val isVerified: Boolean = true,
    val isGovernment: Boolean = false,
    val institutionType: String = "Private", // "Government (AIIMS / Safdarjung)" or "Private Super Speciality"
    val clinicAddress: String = "",
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val googleMapsUrl: String = "",
    val distanceKm: Double = 0.0
)

data class ExamCounsellor(
    val id: String,
    val name: String,
    val exams: List<String>, // "UPSC", "NEET", "JEE", "SSC", "CAT", "CUET"
    val background: String,
    val designation: String,
    val rating: Double,
    val studentsMentored: Int,
    val isVerified: Boolean = true,
    val availableMode: String = "1-on-1 Call / Chat",
    val feeDescription: String = "Free Initial Civic Guidance",
    val phone: String
)

data class EmergencyContact(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val number: String,
    val category: String,
    val description: String,
    val isTollFree: Boolean = true
)

data class FirstAidGuide(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val urgencyLevel: String, // "CRITICAL", "HIGH", "MODERATE"
    val immediateActionSummary: String,
    val steps: List<String>,
    val whatNotToDo: List<String>,
    val helplineToCall: String = "102 / 112"
)
