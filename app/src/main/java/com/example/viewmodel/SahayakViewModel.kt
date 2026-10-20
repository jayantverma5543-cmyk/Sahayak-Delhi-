package com.example.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DelhiCivicData
import com.example.data.local.CounsellorQueryEntity
import com.example.data.local.DoctorAppointmentEntity
import com.example.data.local.SahayakDatabase
import com.example.data.local.TankerBookingEntity
import com.example.model.Clinic
import com.example.model.Doctor
import com.example.model.DoctorSortOption
import com.example.model.ExamCounsellor
import com.example.model.FirstAidGuide
import com.example.model.Hospital
import com.example.model.HospitalType
import com.example.model.ScreenTab
import com.example.model.UserLocation
import com.example.model.WaterSortOption
import com.example.model.WaterSupplier
import com.example.ml.AlertFrequency
import com.example.ml.ParsedFormData
import com.example.ml.PredictiveAlert
import com.example.ml.SymptomClassificationResult
import com.example.ml.TinyFishFormParser
import com.example.ml.TinyFishPredictiveEngine
import com.example.ml.TinyFishSymptomClassifier
import com.example.ml.TinyFishUniversalSearch
import com.example.ml.TinyFishVoiceListenerManager
import com.example.ml.UniversalSearchResult
import com.example.ml.UserProfile
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SahayakViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SahayakDatabase.getDatabase(application)
    private val dao = db.sahayakDao()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // User GPS Location State
    private val _userLocation = MutableStateFlow(
        UserLocation(
            latitude = DelhiCivicData.DEFAULT_DELHI_LAT,
            longitude = DelhiCivicData.DEFAULT_DELHI_LNG,
            label = "Delhi Center (Connaught Place)",
            isRealGps = false
        )
    )
    val userLocation: StateFlow<UserLocation> = _userLocation.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    // Range Filter: null = All, 10.0 = Within 10 km, 20.0 = Within 20 km
    private val _radiusRangeKm = MutableStateFlow<Double?>(null)
    val radiusRangeKm: StateFlow<Double?> = _radiusRangeKm.asStateFlow()

    // Inbuilt Map View Toggle
    private val _isMapViewActive = MutableStateFlow(false)
    val isMapViewActive: StateFlow<Boolean> = _isMapViewActive.asStateFlow()

    // Selected Pin on Inbuilt Map
    private val _selectedMapHospital = MutableStateFlow<Hospital?>(null)
    val selectedMapHospital: StateFlow<Hospital?> = _selectedMapHospital.asStateFlow()

    private val _selectedMapClinic = MutableStateFlow<Clinic?>(null)
    val selectedMapClinic: StateFlow<Clinic?> = _selectedMapClinic.asStateFlow()

    // Manual/Fallback Clinics view
    private val _showClinicsView = MutableStateFlow(false)
    val showClinicsView: StateFlow<Boolean> = _showClinicsView.asStateFlow()

    // Metro Lines Overlay on Map
    private val _showMetroLinesOnMap = MutableStateFlow(true)
    val showMetroLinesOnMap: StateFlow<Boolean> = _showMetroLinesOnMap.asStateFlow()

    // Screen Navigation
    private val _currentTab = MutableStateFlow(ScreenTab.EMERGENCY)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Emergency Dark Mode
    private val _isEmergencyDarkMode = MutableStateFlow(false)
    val isEmergencyDarkMode: StateFlow<Boolean> = _isEmergencyDarkMode.asStateFlow()

    // Emergency SOS Triggered Sheet/Dialog
    private val _showSosDialog = MutableStateFlow(false)
    val showSosDialog: StateFlow<Boolean> = _showSosDialog.asStateFlow()

    // Search queries for each section
    private val _hospitalSearchQuery = MutableStateFlow("")
    val hospitalSearchQuery: StateFlow<String> = _hospitalSearchQuery.asStateFlow()

    private val _hospitalTypeFilter = MutableStateFlow<HospitalType?>(null)
    val hospitalTypeFilter: StateFlow<HospitalType?> = _hospitalTypeFilter.asStateFlow()

    private val _onlyAvailableIcu = MutableStateFlow(false)
    val onlyAvailableIcu: StateFlow<Boolean> = _onlyAvailableIcu.asStateFlow()

    private val _tankerSearchQuery = MutableStateFlow("")
    val tankerSearchQuery: StateFlow<String> = _tankerSearchQuery.asStateFlow()

    // Water Tanker Range, Sort & Sector Type Filter
    private val _tankerRadiusRangeKm = MutableStateFlow<Double?>(null) // null = All, 10.0, 20.0
    val tankerRadiusRangeKm: StateFlow<Double?> = _tankerRadiusRangeKm.asStateFlow()

    private val _tankerSortOption = MutableStateFlow(WaterSortOption.RATING_HIGH_TO_LOW)
    val tankerSortOption: StateFlow<WaterSortOption> = _tankerSortOption.asStateFlow()

    private val _tankerTypeFilter = MutableStateFlow<String?>(null) // null = All, "GOVERNMENT", "PRIVATE"
    val tankerTypeFilter: StateFlow<String?> = _tankerTypeFilter.asStateFlow()

    private val _doctorSearchQuery = MutableStateFlow("")
    val doctorSearchQuery: StateFlow<String> = _doctorSearchQuery.asStateFlow()

    private val _doctorSpecialityFilter = MutableStateFlow<String?>(null)
    val doctorSpecialityFilter: StateFlow<String?> = _doctorSpecialityFilter.asStateFlow()

    // Doctor Range, Sort & Sector Type Filter
    private val _doctorRadiusRangeKm = MutableStateFlow<Double?>(null) // null = All, 10.0, 20.0
    val doctorRadiusRangeKm: StateFlow<Double?> = _doctorRadiusRangeKm.asStateFlow()

    private val _doctorSortOption = MutableStateFlow(DoctorSortOption.RATING_HIGH_TO_LOW)
    val doctorSortOption: StateFlow<DoctorSortOption> = _doctorSortOption.asStateFlow()

    private val _doctorTypeFilter = MutableStateFlow<String?>(null) // null = All, "GOVERNMENT", "PRIVATE"
    val doctorTypeFilter: StateFlow<String?> = _doctorTypeFilter.asStateFlow()

    private val _counsellorSearchQuery = MutableStateFlow("")
    val counsellorSearchQuery: StateFlow<String> = _counsellorSearchQuery.asStateFlow()

    private val _counsellorExamFilter = MutableStateFlow<String?>(null)
    val counsellorExamFilter: StateFlow<String?> = _counsellorExamFilter.asStateFlow()

    private val _firstAidSearchQuery = MutableStateFlow("")
    val firstAidSearchQuery: StateFlow<String> = _firstAidSearchQuery.asStateFlow()

    // Room Database Flows
    val tankerBookings: StateFlow<List<TankerBookingEntity>> = dao.getAllTankerBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctorAppointments: StateFlow<List<DoctorAppointmentEntity>> = dao.getAllDoctorAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val counsellorQueries: StateFlow<List<CounsellorQueryEntity>> = dao.getAllCounsellorQueries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // 1. Smart Auto-Fill for Forms States
    private val _savedProfiles = MutableStateFlow<List<UserProfile>>(TinyFishFormParser.DEFAULT_PROFILES)
    val savedProfiles: StateFlow<List<UserProfile>> = _savedProfiles.asStateFlow()

    private val _selectedProfile = MutableStateFlow<UserProfile?>(TinyFishFormParser.DEFAULT_PROFILES.firstOrNull())
    val selectedProfile: StateFlow<UserProfile?> = _selectedProfile.asStateFlow()

    private val _autoFillFormData = MutableStateFlow<ParsedFormData?>(null)
    val autoFillFormData: StateFlow<ParsedFormData?> = _autoFillFormData.asStateFlow()

    private val _showAutoFillConfirmDialog = MutableStateFlow(false)
    val showAutoFillConfirmDialog: StateFlow<Boolean> = _showAutoFillConfirmDialog.asStateFlow()

    // 2. Voice-Triggered SOS & Hardware Permissions State
    private val _isVoiceSosEnabled = MutableStateFlow(false)
    val isVoiceSosEnabled: StateFlow<Boolean> = _isVoiceSosEnabled.asStateFlow()

    private val _hasMicrophonePermission = MutableStateFlow(false)
    val hasMicrophonePermission: StateFlow<Boolean> = _hasMicrophonePermission.asStateFlow()

    private val _isGpsTrackingEnabled = MutableStateFlow(true)
    val isGpsTrackingEnabled: StateFlow<Boolean> = _isGpsTrackingEnabled.asStateFlow()

    private var voiceListenerManager: TinyFishVoiceListenerManager? = null

    // 3. Personalized First-Aid Guidance States
    private val _symptomSearchQuery = MutableStateFlow("")
    val symptomSearchQuery: StateFlow<String> = _symptomSearchQuery.asStateFlow()

    private val _symptomClassificationResult = MutableStateFlow<SymptomClassificationResult?>(null)
    val symptomClassificationResult: StateFlow<SymptomClassificationResult?> = _symptomClassificationResult.asStateFlow()

    private val _isClassifyingSymptom = MutableStateFlow(false)
    val isClassifyingSymptom: StateFlow<Boolean> = _isClassifyingSymptom.asStateFlow()

    // 4. Smart Universal Search Across Modules States
    private val _universalSearchQuery = MutableStateFlow("")
    val universalSearchQuery: StateFlow<String> = _universalSearchQuery.asStateFlow()

    private val _universalSearchResults = MutableStateFlow<List<UniversalSearchResult>>(emptyList())
    val universalSearchResults: StateFlow<List<UniversalSearchResult>> = _universalSearchResults.asStateFlow()

    private val _isUniversalSearchActive = MutableStateFlow(false)
    val isUniversalSearchActive: StateFlow<Boolean> = _isUniversalSearchActive.asStateFlow()

    // 5. Predictive Alerts States
    private val _predictiveAlerts = MutableStateFlow<List<PredictiveAlert>>(TinyFishPredictiveEngine.generatePredictiveAlerts())
    val predictiveAlerts: StateFlow<List<PredictiveAlert>> = _predictiveAlerts.asStateFlow()

    private val _alertFrequency = MutableStateFlow(AlertFrequency.REAL_TIME)
    val alertFrequency: StateFlow<AlertFrequency> = _alertFrequency.asStateFlow()

    init {
        checkLocationPermissionState()
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun selectTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun toggleEmergencyDarkMode() {
        _isEmergencyDarkMode.value = !_isEmergencyDarkMode.value
    }

    fun setEmergencyDarkMode(enabled: Boolean) {
        _isEmergencyDarkMode.value = enabled
    }

    fun triggerSos() {
        _showSosDialog.value = true
        vibrateEmergencyPattern()
    }

    fun dismissSosDialog() {
        _showSosDialog.value = false
    }

    // Location Permission & GPS handling
    fun checkLocationPermissionState() {
        val fineGranted = ContextCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _hasLocationPermission.value = fineGranted || coarseGranted
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _hasLocationPermission.value = granted
        if (granted) {
            refreshUserLocation()
        } else {
            _snackbarMessage.value = "Location permission denied. Using central Delhi (Connaught Place)."
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshUserLocation() {
        if (!_hasLocationPermission.value) return

        _isLocating.value = true
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    _isLocating.value = false
                    if (location != null) {
                        _userLocation.value = UserLocation(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            label = "Your Current GPS Location (${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)})",
                            isRealGps = true
                        )
                        _snackbarMessage.value = "GPS Location pinpointed! Distances updated."
                    } else {
                        // Try last known location fallback
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                _userLocation.value = UserLocation(
                                    latitude = lastLoc.latitude,
                                    longitude = lastLoc.longitude,
                                    label = "Last Known GPS Location (${String.format("%.4f", lastLoc.latitude)}, ${String.format("%.4f", lastLoc.longitude)})",
                                    isRealGps = true
                                )
                            } else {
                                _snackbarMessage.value = "Could not fetch GPS. Defaulting to Delhi Center."
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    _isLocating.value = false
                    _snackbarMessage.value = "GPS fetch failed. Using Delhi center."
                }
        } catch (e: Exception) {
            _isLocating.value = false
        }
    }

    // Range Radius Filter setters
    fun setRadiusRangeKm(radius: Double?) {
        _radiusRangeKm.value = radius
    }

    // Doctor filter setters
    fun setDoctorRadiusRangeKm(radius: Double?) {
        _doctorRadiusRangeKm.value = radius
    }

    fun setDoctorSortOption(option: DoctorSortOption) {
        _doctorSortOption.value = option
    }

    fun setDoctorTypeFilter(type: String?) {
        _doctorTypeFilter.value = type
    }

    // Water Tanker filter setters
    fun setTankerRadiusRangeKm(radius: Double?) {
        _tankerRadiusRangeKm.value = radius
    }

    fun setTankerSortOption(option: WaterSortOption) {
        _tankerSortOption.value = option
    }

    fun setTankerTypeFilter(type: String?) {
        _tankerTypeFilter.value = type
    }

    fun toggleMapView() {
        _isMapViewActive.value = !_isMapViewActive.value
    }

    fun setMapViewActive(active: Boolean) {
        _isMapViewActive.value = active
    }

    fun selectMapHospital(hospital: Hospital?) {
        _selectedMapHospital.value = hospital
        _selectedMapClinic.value = null
    }

    fun selectMapClinic(clinic: Clinic?) {
        _selectedMapClinic.value = clinic
        _selectedMapHospital.value = null
    }

    fun toggleShowClinicsView() {
        _showClinicsView.value = !_showClinicsView.value
    }

    fun toggleShowMetroLines() {
        _showMetroLinesOnMap.value = !_showMetroLinesOnMap.value
    }

    fun downloadDelhiOfflineMapInGoogleMaps(context: Context) {
        try {
            // Intent to open Delhi NCR area directly inside the Google Maps app for offline download
            val gmmIntentUri = Uri.parse("geo:28.6139,77.2090?z=11")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mapIntent)
            Toast.makeText(context, "Opening Delhi in Google Maps. Tap your profile > 'Offline maps' to save for offline use.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            try {
                val fallbackUri = Uri.parse("https://maps.google.com/?q=28.6139,77.2090&z=11")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "Could not open Google Maps", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun vibrateEmergencyPattern() {
        try {
            val context = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 200, 100, 200, 100, 400)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    it.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(longArrayOf(0, 200, 100, 200, 100, 400), -1)
                }
            }
        } catch (_: Exception) {
        }
    }

    // Search query setters
    fun setHospitalSearch(query: String) { _hospitalSearchQuery.value = query }
    fun setHospitalTypeFilter(type: HospitalType?) { _hospitalTypeFilter.value = type }
    fun toggleOnlyAvailableIcu() { _onlyAvailableIcu.value = !_onlyAvailableIcu.value }

    fun setTankerSearch(query: String) { _tankerSearchQuery.value = query }
    fun setDoctorSearch(query: String) { _doctorSearchQuery.value = query }
    fun setDoctorSpecialityFilter(speciality: String?) { _doctorSpecialityFilter.value = speciality }

    fun setCounsellorSearch(query: String) { _counsellorSearchQuery.value = query }
    fun setCounsellorExamFilter(exam: String?) { _counsellorExamFilter.value = exam }

    fun setFirstAidSearch(query: String) { _firstAidSearchQuery.value = query }

    // Dynamic Distances from User's Current GPS Location
    fun getFilteredHospitalsWithDynamicDistance(): List<Hospital> {
        val query = _hospitalSearchQuery.value.trim().lowercase()
        val type = _hospitalTypeFilter.value
        val onlyIcu = _onlyAvailableIcu.value
        val userLoc = _userLocation.value
        val radius = _radiusRangeKm.value

        return DelhiCivicData.HOSPITALS
            .map { hospital ->
                val dynamicDist = DelhiCivicData.calculateDistanceKm(
                    userLoc.latitude,
                    userLoc.longitude,
                    hospital.latitude,
                    hospital.longitude
                )
                hospital.copy(distanceKm = dynamicDist)
            }
            .filter { hospital ->
                val matchesQuery = query.isEmpty() ||
                    hospital.name.lowercase().contains(query) ||
                    hospital.area.lowercase().contains(query) ||
                    hospital.fullAddress.lowercase().contains(query) ||
                    hospital.landmark.lowercase().contains(query)

                val matchesType = type == null || hospital.hospitalType == type
                val matchesIcu = !onlyIcu || hospital.icuAvailable > 0
                val matchesRadius = radius == null || hospital.distanceKm <= radius

                matchesQuery && matchesType && matchesIcu && matchesRadius
            }
            .sortedBy { it.distanceKm }
    }

    fun getFilteredClinicsWithDynamicDistance(): List<Clinic> {
        val query = _hospitalSearchQuery.value.trim().lowercase()
        val userLoc = _userLocation.value
        val radius = _radiusRangeKm.value

        return DelhiCivicData.CLINICS
            .map { clinic ->
                val dynamicDist = DelhiCivicData.calculateDistanceKm(
                    userLoc.latitude,
                    userLoc.longitude,
                    clinic.latitude,
                    clinic.longitude
                )
                clinic.copy(distanceKm = dynamicDist)
            }
            .filter { clinic ->
                val matchesQuery = query.isEmpty() ||
                    clinic.name.lowercase().contains(query) ||
                    clinic.area.lowercase().contains(query) ||
                    clinic.fullAddress.lowercase().contains(query) ||
                    clinic.servicesAvailable.lowercase().contains(query)

                val matchesRadius = radius == null || clinic.distanceKm <= radius

                matchesQuery && matchesRadius
            }
            .sortedBy { it.distanceKm }
    }

    fun getFilteredTankers(): List<WaterSupplier> {
        val query = _tankerSearchQuery.value.trim().lowercase()
        val radius = _tankerRadiusRangeKm.value
        val sortOption = _tankerSortOption.value
        val typeFilter = _tankerTypeFilter.value
        val userLoc = _userLocation.value

        return DelhiCivicData.WATER_SUPPLIERS
            .map { supplier ->
                val dist = DelhiCivicData.calculateDistanceKm(
                    userLoc.latitude, userLoc.longitude,
                    supplier.latitude, supplier.longitude
                )
                supplier.copy(distanceKm = dist)
            }
            .filter { supplier ->
                val matchesQuery = query.isEmpty() ||
                    supplier.name.lowercase().contains(query) ||
                    supplier.areaServed.lowercase().contains(query) ||
                    supplier.waterType.lowercase().contains(query) ||
                    supplier.authorityName.lowercase().contains(query)

                val matchesRadius = radius == null || supplier.distanceKm <= radius

                val matchesType = when (typeFilter) {
                    "GOVERNMENT" -> supplier.isGovernment
                    "PRIVATE" -> !supplier.isGovernment
                    else -> true
                }

                matchesQuery && matchesRadius && matchesType
            }
            .let { list ->
                when (sortOption) {
                    WaterSortOption.RATING_HIGH_TO_LOW -> list.sortedByDescending { it.rating }
                    WaterSortOption.RATING_LOW_TO_HIGH -> list.sortedBy { it.rating }
                    WaterSortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.baseRatePer5000L }
                    WaterSortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.baseRatePer5000L }
                    WaterSortOption.DISTANCE_NEAREST -> list.sortedBy { it.distanceKm }
                }
            }
    }

    fun getFilteredDoctors(): List<Doctor> {
        val query = _doctorSearchQuery.value.trim().lowercase()
        val spec = _doctorSpecialityFilter.value
        val radius = _doctorRadiusRangeKm.value
        val sortOption = _doctorSortOption.value
        val typeFilter = _doctorTypeFilter.value
        val userLoc = _userLocation.value

        return DelhiCivicData.DOCTORS
            .map { doc ->
                val dist = DelhiCivicData.calculateDistanceKm(
                    userLoc.latitude, userLoc.longitude,
                    doc.latitude, doc.longitude
                )
                doc.copy(distanceKm = dist)
            }
            .filter { doc ->
                val matchesQuery = query.isEmpty() ||
                    doc.name.lowercase().contains(query) ||
                    doc.speciality.lowercase().contains(query) ||
                    doc.hospitalName.lowercase().contains(query) ||
                    doc.institutionType.lowercase().contains(query) ||
                    doc.clinicAddress.lowercase().contains(query)

                val matchesSpec = spec == null || doc.speciality.contains(spec, ignoreCase = true)

                val matchesRadius = radius == null || doc.distanceKm <= radius

                val matchesType = when (typeFilter) {
                    "GOVERNMENT" -> doc.isGovernment
                    "PRIVATE" -> !doc.isGovernment
                    else -> true
                }

                matchesQuery && matchesSpec && matchesRadius && matchesType
            }
            .let { list ->
                when (sortOption) {
                    DoctorSortOption.RATING_HIGH_TO_LOW -> list.sortedByDescending { it.rating }
                    DoctorSortOption.RATING_LOW_TO_HIGH -> list.sortedBy { it.rating }
                    DoctorSortOption.FEE_LOW_TO_HIGH -> list.sortedBy { it.consultationFee }
                    DoctorSortOption.FEE_HIGH_TO_LOW -> list.sortedByDescending { it.consultationFee }
                    DoctorSortOption.DISTANCE_NEAREST -> list.sortedBy { it.distanceKm }
                }
            }
    }

    fun getFilteredCounsellors(): List<ExamCounsellor> {
        val query = _counsellorSearchQuery.value.trim().lowercase()
        val exam = _counsellorExamFilter.value
        return DelhiCivicData.EXAM_COUNSELLORS.filter { counsellor ->
            val matchesQuery = query.isEmpty() ||
                counsellor.name.lowercase().contains(query) ||
                counsellor.background.lowercase().contains(query) ||
                counsellor.designation.lowercase().contains(query) ||
                counsellor.exams.any { it.contains(query, ignoreCase = true) }
            val matchesExam = exam == null || counsellor.exams.contains(exam)
            matchesQuery && matchesExam
        }
    }

    fun getFilteredFirstAid(): List<FirstAidGuide> {
        val query = _firstAidSearchQuery.value.trim().lowercase()
        return DelhiCivicData.FIRST_AID_GUIDELINES.filter { guide ->
            query.isEmpty() ||
                guide.title.lowercase().contains(query) ||
                guide.hindiTitle.lowercase().contains(query) ||
                guide.immediateActionSummary.lowercase().contains(query) ||
                guide.steps.any { it.lowercase().contains(query) }
        }
    }

    // Actions & Persistence
    fun bookWaterTanker(
        supplier: WaterSupplier,
        capacityLiters: Int,
        totalCost: Int,
        deliveryDate: String,
        deliverySlot: String,
        customerName: String,
        phone: String,
        deliveryAddress: String,
        landmark: String
    ) {
        viewModelScope.launch {
            val booking = TankerBookingEntity(
                supplierId = supplier.id,
                supplierName = supplier.name,
                capacityLiters = capacityLiters,
                totalCost = totalCost,
                deliveryDate = deliveryDate,
                deliverySlot = deliverySlot,
                customerName = customerName,
                phone = phone,
                deliveryAddress = deliveryAddress,
                landmark = landmark
            )
            dao.insertTankerBooking(booking)
            _snackbarMessage.value = "Water Tanker booking confirmed for $deliveryDate ($deliverySlot)!"
        }
    }

    fun cancelTankerBooking(id: Long) {
        viewModelScope.launch {
            dao.deleteTankerBooking(id)
            _snackbarMessage.value = "Tanker booking cancelled."
        }
    }

    fun bookDoctorAppointment(
        doctor: Doctor,
        patientName: String,
        patientAge: Int,
        patientGender: String,
        phone: String,
        date: String,
        slot: String,
        consultMode: String,
        symptoms: String
    ) {
        viewModelScope.launch {
            val appointment = DoctorAppointmentEntity(
                doctorId = doctor.id,
                doctorName = doctor.name,
                speciality = doctor.speciality,
                hospitalName = doctor.hospitalName,
                consultationFee = doctor.consultationFee,
                patientName = patientName,
                patientAge = patientAge,
                patientGender = patientGender,
                phone = phone,
                appointmentDate = date,
                appointmentSlot = slot,
                consultMode = consultMode,
                symptoms = symptoms
            )
            dao.insertDoctorAppointment(appointment)
            _snackbarMessage.value = "Appointment requested with ${doctor.name} for $date!"
        }
    }

    fun cancelDoctorAppointment(id: Long) {
        viewModelScope.launch {
            dao.deleteDoctorAppointment(id)
            _snackbarMessage.value = "Appointment cancelled."
        }
    }

    fun submitCounsellorQuery(
        counsellor: ExamCounsellor,
        examCategory: String,
        studentName: String,
        phone: String,
        stage: String,
        queryText: String,
        preferredSlot: String
    ) {
        viewModelScope.launch {
            val queryEntity = CounsellorQueryEntity(
                counsellorId = counsellor.id,
                counsellorName = counsellor.name,
                examCategory = examCategory,
                studentName = studentName,
                phone = phone,
                preparationStage = stage,
                queryText = queryText,
                preferredSlot = preferredSlot
            )
            dao.insertCounsellorQuery(queryEntity)
            _snackbarMessage.value = "Counselling query submitted! ${counsellor.name} will call within 4 hours."
        }
    }

    fun cancelCounsellorQuery(id: Long) {
        viewModelScope.launch {
            dao.deleteCounsellorQuery(id)
            _snackbarMessage.value = "Query removed."
        }
    }

    // Call, Navigation and Share Handlers
    fun callEmergencyNumber(context: Context, number: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${number.replace("-", "").replace(" ", "")}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer for $number", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareLiveEmergencyLocation(context: Context, facilityName: String? = null) {
        try {
            val lat = _userLocation.value.latitude
            val lng = _userLocation.value.longitude
            val locationText = if (facilityName != null) {
                """
                🚨 SAHAYAK DELHI EMERGENCY ALERT!
                I am heading to or need emergency support for $facilityName.
                Current Delhi GPS Location: https://maps.google.com/?q=$lat,$lng
                Please alert emergency services (112 / 102 CATS Ambulance) immediately!
                """.trimIndent()
            } else {
                """
                🚨 SAHAYAK DELHI CRITICAL SOS!
                I need immediate emergency assistance in Delhi!
                Current Delhi GPS Location: https://maps.google.com/?q=$lat,$lng
                Please alert Delhi Police (112) or Ambulance (102 CATS) immediately!
                """.trimIndent()
            }

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, locationText)
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Emergency SOS Location")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share location", Toast.LENGTH_SHORT).show()
        }
    }

    // Turn-by-Turn Google Maps Navigation from User Location to Destination
    fun openGoogleMapsTurnByTurnNavigation(context: Context, destLat: Double, destLng: Double, destTitle: String) {
        try {
            val userLat = _userLocation.value.latitude
            val userLng = _userLocation.value.longitude

            // Intent to Google Maps turn-by-turn navigation route from user's current GPS location
            val navUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$userLat,$userLng&destination=$destLat,$destLng&travelmode=driving")
            val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback to simple geo view if browser/maps handles it
            val fallbackUri = Uri.parse("geo:$destLat,$destLng?q=$destLat,$destLng($destTitle)")
            val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
        }
    }

    fun openWebsite(context: Context, url: String) {
        if (url.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open website", Toast.LENGTH_SHORT).show()
        }
    }

    fun openDirections(context: Context, mapsUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open Google Maps", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================
    // 1. SMART AUTO-FILL FOR FORMS LOGIC
    // ==========================================
    fun parseAndPrepareAutoFill(input: String, serviceType: String, customProfileTitle: String? = null) {
        viewModelScope.launch {
            val parsed = TinyFishFormParser.parseInput(input, serviceType, customProfileTitle)
            _autoFillFormData.value = parsed
            _showAutoFillConfirmDialog.value = true
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        val currentList = _savedProfiles.value.filter { it.id != profile.id }
        _savedProfiles.value = currentList + profile
        _selectedProfile.value = profile
        _snackbarMessage.value = "Saved profile for ${profile.title}"
    }

    fun selectProfile(profile: UserProfile) {
        _selectedProfile.value = profile
        _autoFillFormData.value = ParsedFormData(
            profileTitle = profile.title,
            fullName = profile.fullName,
            age = profile.age,
            phone = profile.phone,
            address = profile.address,
            notes = "Auto-filled from saved profile ${profile.title}",
            confidence = 1.0f
        )
    }

    fun updateAutoFillFormData(updated: ParsedFormData) {
        _autoFillFormData.value = updated
    }

    fun dismissAutoFillDialog() {
        _showAutoFillConfirmDialog.value = false
    }

    // ==========================================
    // 2. VOICE-TRIGGERED SOS & HARDWARE LIFECYCLE
    // ==========================================
    fun setVoiceSosEnabled(enabled: Boolean, context: Context? = null) {
        _isVoiceSosEnabled.value = enabled
        if (enabled && context != null) {
            initVoiceListener(context)
        } else if (!enabled) {
            stopVoiceListener()
        }
    }

    fun setMicrophonePermission(granted: Boolean) {
        _hasMicrophonePermission.value = granted
    }

    fun setGpsTrackingEnabled(enabled: Boolean) {
        _isGpsTrackingEnabled.value = enabled
        if (enabled) {
            refreshUserLocation()
        } else {
            // Revert to central Delhi fallback when GPS turned off
            _userLocation.value = UserLocation(
                latitude = DelhiCivicData.DEFAULT_DELHI_LAT,
                longitude = DelhiCivicData.DEFAULT_DELHI_LNG,
                label = "Delhi Center (GPS disabled by user)",
                isRealGps = false
            )
        }
    }

    private fun initVoiceListener(context: Context) {
        if (voiceListenerManager == null) {
            voiceListenerManager = TinyFishVoiceListenerManager(context.applicationContext) { detectedPhrase ->
                triggerVoicePanicDetection(detectedPhrase)
            }
        }
        voiceListenerManager?.startListening()
    }

    private fun stopVoiceListener() {
        voiceListenerManager?.stopListening()
    }

    fun triggerVoicePanicDetection(phrase: String) {
        viewModelScope.launch {
            _snackbarMessage.value = "🚨 TinyFish Voice SOS: Panic phrase '$phrase' detected! Activating emergency."
            triggerSos()
        }
    }

    // ==========================================
    // 3. PERSONALIZED FIRST-AID GUIDANCE
    // ==========================================
    fun classifySymptom(query: String) {
        _symptomSearchQuery.value = query
        if (query.isBlank()) {
            _symptomClassificationResult.value = null
            return
        }
        _isClassifyingSymptom.value = true
        viewModelScope.launch {
            val result = TinyFishSymptomClassifier.classifySymptom(query)
            _symptomClassificationResult.value = result
            _isClassifyingSymptom.value = false
        }
    }

    fun clearSymptomClassification() {
        _symptomSearchQuery.value = ""
        _symptomClassificationResult.value = null
    }

    // ==========================================
    // 4. SMART UNIVERSAL SEARCH ACROSS MODULES
    // ==========================================
    fun setUniversalSearchQuery(query: String) {
        _universalSearchQuery.value = query
        if (query.isBlank()) {
            _universalSearchResults.value = emptyList()
            _isUniversalSearchActive.value = false
        } else {
            _isUniversalSearchActive.value = true
            _universalSearchResults.value = TinyFishUniversalSearch.search(query)
        }
    }

    fun setUniversalSearchActive(active: Boolean) {
        _isUniversalSearchActive.value = active
        if (!active) {
            _universalSearchQuery.value = ""
            _universalSearchResults.value = emptyList()
        }
    }

    fun clearUniversalSearch() {
        _universalSearchQuery.value = ""
        _universalSearchResults.value = emptyList()
        _isUniversalSearchActive.value = false
    }

    // ==========================================
    // 5. PREDICTIVE CIVIC ALERTS
    // ==========================================
    fun setAlertFrequency(frequency: AlertFrequency) {
        _alertFrequency.value = frequency
        _snackbarMessage.value = "Predictive alerts frequency set to: ${frequency.label}"
    }

    fun dismissPredictiveAlert(alertId: String) {
        _predictiveAlerts.value = _predictiveAlerts.value.filter { it.id != alertId }
    }

    // ==========================================
    // CLEAN LIFECYCLE SHUTDOWN ON APP CLOSE
    // ==========================================
    fun cleanUpResourcesOnAppClose() {
        try {
            voiceListenerManager?.release()
            voiceListenerManager = null
            _isVoiceSosEnabled.value = false
            _isLocating.value = false
        } catch (e: Exception) {
            // Safe teardown
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanUpResourcesOnAppClose()
    }
}
