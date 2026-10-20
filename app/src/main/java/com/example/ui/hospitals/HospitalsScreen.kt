package com.example.ui.hospitals

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Clinic
import com.example.model.Hospital
import com.example.model.HospitalType
import com.example.ui.components.OfflineCapabilityBanner
import com.example.ui.components.SahayakSearchBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright
import com.example.ui.theme.StatusGreen
import com.example.viewmodel.SahayakViewModel

@Composable
fun HospitalsScreen(
    viewModel: SahayakViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.hospitalSearchQuery.collectAsState()
    val typeFilter by viewModel.hospitalTypeFilter.collectAsState()
    val onlyAvailableIcu by viewModel.onlyAvailableIcu.collectAsState()
    val radiusRangeKm by viewModel.radiusRangeKm.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()
    val isLocating by viewModel.isLocating.collectAsState()
    val isEmergencyDarkMode by viewModel.isEmergencyDarkMode.collectAsState()
    val showClinicsView by viewModel.showClinicsView.collectAsState()

    val hospitals = viewModel.getFilteredHospitalsWithDynamicDistance()
    val clinics = viewModel.getFilteredClinicsWithDynamicDistance()

    // Permission launcher for Location (FINE & COARSE)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        viewModel.onLocationPermissionResult(fineGranted || coarseGranted)
    }

    Column(modifier = modifier.fillMaxSize().testTag("hospitals_screen_root")) {
        // Top Search Bar
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            SahayakSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setHospitalSearch(it) },
                placeholder = "Search hospitals by name, area, or facility...",
                testTag = "hospital_search_input"
            )
        }

        // Location & Header Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Delhi Hospital Lifeline (${hospitals.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = if (userLocation.isRealGps) "Live GPS: Near ${userLocation.label}" else "Distances relative to Connaught Place",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // GPS Pinpoint Button
            IconButton(
                onClick = {
                    if (hasLocationPermission) {
                        viewModel.refreshUserLocation()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (hasLocationPermission) Color(0xFF0284C7).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                    .size(38.dp)
                    .testTag("gps_locate_button")
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "Pinpoint GPS Location",
                    tint = if (hasLocationPermission) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Location Permission Request Banner (if not yet granted)
        if (!hasLocationPermission) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(12.dp))
                    .testTag("location_permission_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "GPS",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pinpoint Your Location on Google Maps",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "Grant location access to detect hospitals within 10 km and 20 km of your exact location.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1E3A8A).copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("enable_gps_permission_btn")
                    ) {
                        Text("Enable GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Location Status Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Location Pin",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLocating) "Locating your GPS..." else userLocation.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Range Filter & Category Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 10 km Range filter chip
            item {
                FilterChip(
                    selected = radiusRangeKm == 10.0,
                    onClick = {
                        viewModel.setRadiusRangeKm(if (radiusRangeKm == 10.0) null else 10.0)
                    },
                    label = { Text("Within 10 km") },
                    leadingIcon = {
                        if (radiusRangeKm == 10.0) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    },
                    modifier = Modifier.testTag("filter_radius_10km")
                )
            }

            // 20 km Range filter chip
            item {
                FilterChip(
                    selected = radiusRangeKm == 20.0,
                    onClick = {
                        viewModel.setRadiusRangeKm(if (radiusRangeKm == 20.0) null else 20.0)
                    },
                    label = { Text("Within 20 km") },
                    leadingIcon = {
                        if (radiusRangeKm == 20.0) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    },
                    modifier = Modifier.testTag("filter_radius_20km")
                )
            }

            // All Delhi
            item {
                FilterChip(
                    selected = radiusRangeKm == null && typeFilter == null && !onlyAvailableIcu,
                    onClick = {
                        viewModel.setRadiusRangeKm(null)
                        viewModel.setHospitalTypeFilter(null)
                    },
                    label = { Text("All Delhi") },
                    modifier = Modifier.testTag("filter_all_delhi")
                )
            }

            // Govt Only
            item {
                FilterChip(
                    selected = typeFilter == HospitalType.GOVERNMENT,
                    onClick = {
                        viewModel.setHospitalTypeFilter(
                            if (typeFilter == HospitalType.GOVERNMENT) null else HospitalType.GOVERNMENT
                        )
                    },
                    label = { Text("Govt Hospitals") }
                )
            }

            // Super Speciality Only
            item {
                FilterChip(
                    selected = typeFilter == HospitalType.SUPER_SPECIALITY,
                    onClick = {
                        viewModel.setHospitalTypeFilter(
                            if (typeFilter == HospitalType.SUPER_SPECIALITY) null else HospitalType.SUPER_SPECIALITY
                        )
                    },
                    label = { Text("Super Speciality") }
                )
            }

            // Only ICU Available
            item {
                FilterChip(
                    selected = onlyAvailableIcu,
                    onClick = { viewModel.toggleOnlyAvailableIcu() },
                    label = { Text("ICU Vacant") }
                )
            }
        }

        // Hospital & Clinic List View
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
                // If NO HOSPITALS found within selected range: Auto-Fallback to Nearby Clinics
                if (hospitals.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .border(1.dp, EmergencyRedBright.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .testTag("no_hospitals_fallback_banner")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Alert",
                                        tint = EmergencyRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "No Hospital Found within ${radiusRangeKm?.toInt() ?: 10} km",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmergencyRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Searching nearby Delhi Primary Clinics, Mohalla Clinics & 24x7 Urgent Care Dispensaries so the patient gets immediate medical stabilization.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF7F1D1D)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.setRadiusRangeKm(null) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Expand Search to All Delhi")
                                }
                            }
                        }
                    }

                    // Section: Nearby Clinics & Urgent Facilities for Immediate Treatment
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nearby Clinics & Urgent Care Facilities (${clinics.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    items(clinics) { clinic ->
                        ClinicCard(
                            clinic = clinic,
                            onCall = { viewModel.callEmergencyNumber(context, clinic.phone) },
                            onCallMobile = { viewModel.callEmergencyNumber(context, clinic.mobileNumber) },
                            onNavigate = {
                                viewModel.openGoogleMapsTurnByTurnNavigation(
                                    context,
                                    clinic.latitude,
                                    clinic.longitude,
                                    clinic.name
                                )
                            }
                        )
                    }
                } else {
                    // Hospitals Found
                    items(hospitals) { hospital ->
                        HospitalCard(
                            hospital = hospital,
                            onCallEmergency = { viewModel.callEmergencyNumber(context, hospital.emergencyPhone) },
                            onCallMobile = { viewModel.callEmergencyNumber(context, hospital.mobileNumber) },
                            onShareLocation = { viewModel.shareLiveEmergencyLocation(context, hospital.name) },
                            onNavigateInGoogleMaps = {
                                viewModel.openGoogleMapsTurnByTurnNavigation(
                                    context,
                                    hospital.latitude,
                                    hospital.longitude,
                                    hospital.name
                                )
                            },
                            onOpenWebsite = { viewModel.openWebsite(context, hospital.websiteUrl) }
                        )
                    }

                    // Also provide an option to browse Delhi clinics below hospitals
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable { viewModel.toggleShowClinicsView() }
                                .testTag("toggle_clinics_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MedicalServices,
                                        contentDescription = "Clinics",
                                        tint = Color(0xFF0D9488),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Nearby Delhi Clinics & Dispensaries (${clinics.size})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Mohalla clinics, 24x7 Urgent Care & Govt Dispensaries",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = if (showClinicsView) "Hide ▲" else "View ▼",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    if (showClinicsView) {
                        items(clinics) { clinic ->
                            ClinicCard(
                                clinic = clinic,
                                onCall = { viewModel.callEmergencyNumber(context, clinic.phone) },
                                onCallMobile = { viewModel.callEmergencyNumber(context, clinic.mobileNumber) },
                                onNavigate = {
                                    viewModel.openGoogleMapsTurnByTurnNavigation(
                                        context,
                                        clinic.latitude,
                                        clinic.longitude,
                                        clinic.name
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
fun HospitalCard(
    hospital: Hospital,
    onCallEmergency: () -> Unit,
    onCallMobile: () -> Unit,
    onShareLocation: () -> Unit,
    onNavigateInGoogleMaps: () -> Unit,
    onOpenWebsite: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .testTag("hospital_card_${hospital.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name, Distance badge & Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hospital.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (hospital.hindiName.isNotEmpty()) {
                        Text(
                            text = hospital.hindiName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = hospital.hospitalType.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0284C7).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${hospital.distanceKm} km",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Address & Landmark
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Area",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${hospital.area} • ${hospital.landmark}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ICU and Beds status grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "ICU BEDS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${hospital.icuAvailable} / ${hospital.icuTotal} Available",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = if (hospital.icuAvailable > 5) StatusGreen else EmergencyRed
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "OXYGEN BEDS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${hospital.oxygenBedsAvailable} Vacant",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "VENTILATORS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${hospital.ventilatorBedsAvailable} Ready",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Verified Mobile & Website on internet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hospital.mobileNumber.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onCallMobile)
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Mobile", tint = StatusGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mobile: ${hospital.mobileNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusGreen
                        )
                    }
                }

                if (hospital.websiteUrl.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onOpenWebsite)
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Web", tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Official Website",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Call Emergency, Google Maps Navigate, Share SOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCallEmergency,
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("hospital_call_btn_${hospital.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Call (${hospital.emergencyPhone})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onNavigateInGoogleMaps,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                        .testTag("hospital_maps_nav_btn_${hospital.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Google Maps",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Google Maps",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onShareLocation,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(44.dp)
                        .testTag("hospital_share_btn_${hospital.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ShareLocation,
                        contentDescription = "Share",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "SOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ClinicCard(
    clinic: Clinic,
    onCall: () -> Unit,
    onCallMobile: () -> Unit,
    onNavigate: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .border(1.dp, Color(0xFF0D9488).copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .testTag("clinic_card_${clinic.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = clinic.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (clinic.hindiName.isNotEmpty()) {
                        Text(
                            text = clinic.hindiName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${clinic.clinicType.label} • ${clinic.openHours}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0D9488),
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0D9488).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${clinic.distanceKm} km",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D9488)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${clinic.fullAddress} (${clinic.landmark})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Services: ${clinic.servicesAvailable}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(40.dp)
                        .testTag("clinic_call_btn_${clinic.id}")
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Call (${clinic.phone})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigate,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("clinic_nav_btn_${clinic.id}")
                ) {
                    Icon(imageVector = Icons.Default.Navigation, contentDescription = "Navigate", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Navigate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
