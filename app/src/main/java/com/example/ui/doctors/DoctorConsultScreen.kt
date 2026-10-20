package com.example.ui.doctors

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.ml.ParsedFormData
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.DoctorAppointmentEntity
import com.example.model.Doctor
import com.example.model.DoctorSortOption
import com.example.ui.components.OfflineCapabilityBanner
import com.example.ui.components.SahayakSearchBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.StatusAmber
import com.example.viewmodel.SahayakViewModel

@Composable
fun DoctorConsultScreen(
    viewModel: SahayakViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.doctorSearchQuery.collectAsState()
    val specialityFilter by viewModel.doctorSpecialityFilter.collectAsState()
    val doctorRadiusRange by viewModel.doctorRadiusRangeKm.collectAsState()
    val doctorSortOption by viewModel.doctorSortOption.collectAsState()
    val doctorTypeFilter by viewModel.doctorTypeFilter.collectAsState()
    val doctors = viewModel.getFilteredDoctors()
    val appointments by viewModel.doctorAppointments.collectAsState()
    val autoFillData by viewModel.autoFillFormData.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedDoctorForAppointment by remember { mutableStateOf<Doctor?>(null) }

    val specialities = listOf("Cardiologist", "Physician", "Pediatrician", "Gynecologist", "Orthopedic", "Pulmonologist", "Neurosurgery", "Nephrology", "Dermatology", "Oncology", "Gastroenterology", "ENT")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("doctor_consult_screen_container"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Search bar
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SahayakSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setDoctorSearch(it) },
                    placeholder = "Search doctors by name, speciality, hospital...",
                    testTag = "doctor_search_input"
                )
            }
        }

        // Offline Banner
        item {
            OfflineCapabilityBanner()
        }

        // Tabs: Available Doctors (6) / My Appointments
        item {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Delhi Doctors (${doctors.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_doctors")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("My Appointments (${appointments.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_appointments")
                )
            }
        }

        if (selectedTabIndex == 0) {
            // Range Filter (All, 10 km, 20 km) & Sector (Govt / Private)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                    Text(
                        text = "RANGE & SECTOR FILTER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = doctorRadiusRange == null,
                                onClick = { viewModel.setDoctorRadiusRangeKm(null) },
                                label = { Text("All Delhi NCT") },
                                modifier = Modifier.testTag("filter_doc_range_all")
                            )
                        }
                        item {
                            FilterChip(
                                selected = doctorRadiusRange == 10.0,
                                onClick = { viewModel.setDoctorRadiusRangeKm(if (doctorRadiusRange == 10.0) null else 10.0) },
                                label = { Text("Within 10 km") },
                                modifier = Modifier.testTag("filter_doc_range_10km")
                            )
                        }
                        item {
                            FilterChip(
                                selected = doctorRadiusRange == 20.0,
                                onClick = { viewModel.setDoctorRadiusRangeKm(if (doctorRadiusRange == 20.0) null else 20.0) },
                                label = { Text("Within 20 km") },
                                modifier = Modifier.testTag("filter_doc_range_20km")
                            )
                        }
                        item {
                            FilterChip(
                                selected = doctorTypeFilter == null,
                                onClick = { viewModel.setDoctorTypeFilter(null) },
                                label = { Text("All Sectors") },
                                modifier = Modifier.testTag("filter_doc_sector_all")
                            )
                        }
                        item {
                            FilterChip(
                                selected = doctorTypeFilter == "GOVERNMENT",
                                onClick = { viewModel.setDoctorTypeFilter(if (doctorTypeFilter == "GOVERNMENT") null else "GOVERNMENT") },
                                label = { Text("Govt Hospitals (AIIMS / Safdarjung)") },
                                modifier = Modifier.testTag("filter_doc_sector_govt")
                            )
                        }
                        item {
                            FilterChip(
                                selected = doctorTypeFilter == "PRIVATE",
                                onClick = { viewModel.setDoctorTypeFilter(if (doctorTypeFilter == "PRIVATE") null else "PRIVATE") },
                                label = { Text("Private Super Speciality") },
                                modifier = Modifier.testTag("filter_doc_sector_private")
                            )
                        }
                    }
                }
            }

            // Sort Options (Rating: High to Low, Rating: Low to High, Fee, Distance)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                    Text(
                        text = "SORT BY RATING & FEE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = doctorSortOption == DoctorSortOption.RATING_HIGH_TO_LOW,
                                onClick = { viewModel.setDoctorSortOption(DoctorSortOption.RATING_HIGH_TO_LOW) },
                                label = { Text("Rating: High to Low ★") },
                                modifier = Modifier.testTag("sort_doc_rating_desc")
                            )
                        }
                        item {
                            FilterChip(
                                selected = doctorSortOption == DoctorSortOption.RATING_LOW_TO_HIGH,
                                onClick = { viewModel.setDoctorSortOption(DoctorSortOption.RATING_LOW_TO_HIGH) },
                                label = { Text("Rating: Low to High") },
                                modifier = Modifier.testTag("sort_doc_rating_asc")
                            )
                        }
                        item {
                            FilterChip(
                                selected = doctorSortOption == DoctorSortOption.FEE_LOW_TO_HIGH,
                                onClick = { viewModel.setDoctorSortOption(DoctorSortOption.FEE_LOW_TO_HIGH) },
                                label = { Text("Fee: Low to High (Govt ₹10-₹50)") },
                                modifier = Modifier.testTag("sort_doc_fee_asc")
                            )
                        }
                        item {
                            FilterChip(
                                selected = doctorSortOption == DoctorSortOption.FEE_HIGH_TO_LOW,
                                onClick = { viewModel.setDoctorSortOption(DoctorSortOption.FEE_HIGH_TO_LOW) },
                                label = { Text("Fee: High to Low") },
                                modifier = Modifier.testTag("sort_doc_fee_desc")
                            )
                        }
                        item {
                            FilterChip(
                                selected = doctorSortOption == DoctorSortOption.DISTANCE_NEAREST,
                                onClick = { viewModel.setDoctorSortOption(DoctorSortOption.DISTANCE_NEAREST) },
                                label = { Text("Nearest to Me") },
                                modifier = Modifier.testTag("sort_doc_distance")
                            )
                        }
                    }
                }
            }

            // Speciality filter chips
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                    Text(
                        text = "SPECIALITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = specialityFilter == null,
                                onClick = { viewModel.setDoctorSpecialityFilter(null) },
                                label = { Text("All Specialities") },
                                modifier = Modifier.testTag("filter_all_specialities")
                            )
                        }
                        items(specialities) { spec ->
                            FilterChip(
                                selected = specialityFilter == spec,
                                onClick = {
                                    viewModel.setDoctorSpecialityFilter(if (specialityFilter == spec) null else spec)
                                },
                                label = { Text(spec) },
                                modifier = Modifier.testTag("filter_spec_$spec")
                            )
                        }
                    }
                }
            }

            // Doctor list
            items(doctors) { doctor ->
                DoctorCard(
                    doctor = doctor,
                    onBookClick = { selectedDoctorForAppointment = doctor },
                    onCallOpdClick = { viewModel.callEmergencyNumber(context, doctor.phone) },
                    onCallMobileClick = { viewModel.callEmergencyNumber(context, if (doctor.mobileNumber.isNotEmpty()) doctor.mobileNumber else doctor.phone) },
                    onNavigateMapClick = {
                        viewModel.openGoogleMapsTurnByTurnNavigation(
                            context,
                            doctor.latitude,
                            doctor.longitude,
                            "${doctor.name} - ${doctor.hospitalName}"
                        )
                    }
                )
            }
        } else {
            // Appointments Tab
            if (appointments.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "No Appointments",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Doctor Appointments Requested",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select from top Delhi specialists at AIIMS, Max, Safdarjung & Ganga Ram to request OPD or teleconsultation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { selectedTabIndex = 0 },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Browse Doctors")
                        }
                    }
                }
            } else {
                items(appointments) { appointment ->
                    DoctorAppointmentCard(
                        appointment = appointment,
                        onCancel = { viewModel.cancelDoctorAppointment(appointment.id) }
                    )
                }
            }
        }
    }

    // Appointment Request Modal
    selectedDoctorForAppointment?.let { doctor ->
        DoctorAppointmentDialog(
            doctor = doctor,
            autoFillData = autoFillData,
            onTriggerAutoFill = {
                viewModel.parseAndPrepareAutoFill("Need doctor appointment for self in Delhi", "DOCTOR")
            },
            onDismiss = { selectedDoctorForAppointment = null },
            onConfirm = { patientName, age, gender, phone, date, slot, mode, symptoms ->
                viewModel.bookDoctorAppointment(
                    doctor = doctor,
                    patientName = patientName,
                    patientAge = age,
                    patientGender = gender,
                    phone = phone,
                    date = date,
                    slot = slot,
                    consultMode = mode,
                    symptoms = symptoms
                )
                selectedDoctorForAppointment = null
                selectedTabIndex = 1 // Switch to appointments tab
            }
        )
    }
}

@Composable
fun DoctorCard(
    doctor: Doctor,
    onBookClick: () -> Unit,
    onCallOpdClick: () -> Unit,
    onCallMobileClick: () -> Unit,
    onNavigateMapClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .testTag("doctor_card_${doctor.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Name, Speciality & Fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = doctor.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Doctor",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = doctor.speciality,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = doctor.qualification,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (doctor.isGovernment) {
                            StatusBadge(text = "Govt Hospital (OPD ₹${doctor.consultationFee})", isPositive = true)
                        } else {
                            StatusBadge(text = "Private Super-Speciality", isPositive = false)
                        }
                        if (doctor.distanceKm > 0) {
                            Text(
                                text = "•  ${doctor.distanceKm} km away",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${doctor.consultationFee}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Consult Fee",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hospital & Experience
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = "Hospital",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${doctor.hospitalName} • ${doctor.experienceYears} Yrs Exp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Real Clinic Address
            if (doctor.clinicAddress.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Address",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = doctor.clinicAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Phone numbers
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "OPD: ${doctor.phone}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                if (doctor.mobileNumber.isNotEmpty()) {
                    Text(
                        text = "Mobile: ${doctor.mobileNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timing & Slot Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Slot",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Next Available Slot",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = doctor.nextAvailableSlot,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = StatusAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${doctor.rating} (${doctor.reviewsCount})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row 1: Book Appointment & Call OPD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBookClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("book_doctor_btn_${doctor.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Book Appointment",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Book OPD", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onCallOpdClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("call_doctor_btn_${doctor.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Hospital Phone",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Call OPD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row 2: Direct Mobile Call & Google Maps Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCallMobileClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("call_doctor_mobile_btn_${doctor.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Direct Mobile",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Call Mobile", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onNavigateMapClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(40.dp)
                        .testTag("map_doctor_btn_${doctor.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Google Maps Location",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Google Maps", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DoctorAppointmentCard(
    appointment: DoctorAppointmentEntity,
    onCancel: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .testTag("appointment_card_${appointment.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${appointment.doctorName} • ${appointment.consultMode}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(text = appointment.status, isPositive = true)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${appointment.speciality} • ${appointment.hospitalName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Patient: ${appointment.patientName} (${appointment.patientAge}y, ${appointment.patientGender})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Slot: ${appointment.appointmentDate} at ${appointment.appointmentSlot}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (appointment.symptoms.isNotBlank()) {
                Text(
                    text = "Symptoms: ${appointment.symptoms}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmergencyRed),
                    modifier = Modifier.testTag("cancel_appointment_btn_${appointment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Cancel Request", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DoctorAppointmentDialog(
    doctor: Doctor,
    autoFillData: ParsedFormData? = null,
    onTriggerAutoFill: () -> Unit = {},
    onDismiss: () -> Unit,
    onConfirm: (name: String, age: Int, gender: String, phone: String, date: String, slot: String, mode: String, symptoms: String) -> Unit
) {
    var patientName by remember { mutableStateOf(autoFillData?.fullName ?: "") }
    var patientAgeText by remember { mutableStateOf(autoFillData?.age ?: "28") }
    var selectedGender by remember { mutableStateOf("Male") }
    var phone by remember { mutableStateOf(autoFillData?.phone ?: "") }
    var preferredDate by remember { mutableStateOf("Today") }
    var preferredSlot by remember { mutableStateOf(doctor.nextAvailableSlot) }
    var consultMode by remember { mutableStateOf("In-Person OPD") }
    var symptoms by remember { mutableStateOf(autoFillData?.notes ?: "") }

    LaunchedEffect(autoFillData) {
        if (autoFillData != null) {
            patientName = autoFillData.fullName
            patientAgeText = autoFillData.age
            phone = autoFillData.phone
            symptoms = autoFillData.notes
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Consult Appointment",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${doctor.name} (${doctor.speciality})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // TinyFish Smart Auto-Fill Banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onTriggerAutoFill)
                        .testTag("doctor_autofill_cta")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "⚡ TinyFish Smart Auto-Fill",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (autoFillData != null) "Active profile: ${autoFillData.profileTitle} (Tap to switch)" else "Tap to choose saved person profile or voice input",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Selector
                Text(
                    text = "Consultation Mode",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = consultMode == "In-Person OPD",
                        onClick = { consultMode = "In-Person OPD" },
                        leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Hospital OPD") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = consultMode == "Teleconsultation",
                        onClick = { consultMode = "Teleconsultation" },
                        leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Video Call") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Patient Details
                Text(
                    text = "Patient Information",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = patientName,
                    onValueChange = { patientName = it },
                    label = { Text("Patient Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = patientAgeText,
                        onValueChange = { patientAgeText = it },
                        label = { Text("Age") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = selectedGender,
                        onValueChange = { selectedGender = it },
                        label = { Text("Gender (M/F/Other)") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Patient Mobile Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = preferredSlot,
                    onValueChange = { preferredSlot = it },
                    label = { Text("Preferred Appointment Date & Slot") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Chief Complaint / Symptoms (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Price Summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Consultation Fee", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "₹${doctor.consultationFee}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Pay at OPD Desk / Online",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val name = if (patientName.isBlank()) "Self" else patientName
                        val age = patientAgeText.toIntOrNull() ?: 28
                        val pPhone = if (phone.isBlank()) "9876543210" else phone
                        onConfirm(
                            name,
                            age,
                            selectedGender,
                            pPhone,
                            preferredDate,
                            preferredSlot,
                            consultMode,
                            symptoms
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_doctor_appointment_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Confirm",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Confirm Appointment Request", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
