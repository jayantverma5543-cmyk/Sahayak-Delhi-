package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ScreenTab
import com.example.ui.components.AutoFillConfirmDialog
import com.example.ui.components.SahayakBottomNavBar
import com.example.ui.components.UniversalSearchOverlay
import com.example.ui.counsellor.ExamCounsellorScreen
import com.example.ui.doctors.DoctorConsultScreen
import com.example.ui.emergency.EmergencyScreen
import com.example.ui.emergency.EmergencySosDialog
import com.example.ui.firstaid.FirstAidScreen
import com.example.ui.hospitals.HospitalsScreen
import com.example.ui.tanker.WaterTankerScreen
import com.example.ui.theme.EmergencyDarkBg
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright
import com.example.ui.theme.SahayakTheme
import com.example.viewmodel.SahayakViewModel

@Composable
fun SahayakApp(
    viewModel: SahayakViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isEmergencyDarkMode by viewModel.isEmergencyDarkMode.collectAsState()
    val showSosDialog by viewModel.showSosDialog.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    // TinyFish Universal Search & Auto-Fill states
    val isUniversalSearchActive by viewModel.isUniversalSearchActive.collectAsState()
    val universalSearchQuery by viewModel.universalSearchQuery.collectAsState()
    val universalSearchResults by viewModel.universalSearchResults.collectAsState()
    val showAutoFillDialog by viewModel.showAutoFillConfirmDialog.collectAsState()
    val autoFillData by viewModel.autoFillFormData.collectAsState()
    val savedProfiles by viewModel.savedProfiles.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Handle back button: return to EMERGENCY tab if on another screen or dismiss universal search
    BackHandler(enabled = isUniversalSearchActive || currentTab != ScreenTab.EMERGENCY) {
        if (isUniversalSearchActive) {
            viewModel.setUniversalSearchActive(false)
        } else {
            viewModel.selectTab(ScreenTab.EMERGENCY)
        }
    }

    SahayakTheme(emergencyDarkMode = isEmergencyDarkMode) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                SahayakTopBar(
                    currentTab = currentTab,
                    isEmergencyDarkMode = isEmergencyDarkMode,
                    onEmergencyHubClick = { viewModel.selectTab(ScreenTab.EMERGENCY) },
                    onFirstAidClick = { viewModel.selectTab(ScreenTab.FIRST_AID) },
                    onCounsellorClick = { viewModel.selectTab(ScreenTab.COUNSELLOR) },
                    onSearchClick = { viewModel.setUniversalSearchActive(true) },
                    onToggleDarkMode = { viewModel.toggleEmergencyDarkMode() }
                )
            },
            bottomBar = {
                SahayakBottomNavBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    onSosClick = { viewModel.triggerSos() },
                    isEmergencyDarkMode = isEmergencyDarkMode
                )
            },
            containerColor = if (isEmergencyDarkMode) EmergencyDarkBg else MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .testTag("sahayak_app_root")
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    ScreenTab.EMERGENCY -> EmergencyScreen(viewModel = viewModel)
                    ScreenTab.HOSPITALS -> HospitalsScreen(viewModel = viewModel)
                    ScreenTab.WATER_TANKER -> WaterTankerScreen(viewModel = viewModel)
                    ScreenTab.DOCTORS -> DoctorConsultScreen(viewModel = viewModel)
                    ScreenTab.COUNSELLOR -> ExamCounsellorScreen(viewModel = viewModel)
                    ScreenTab.FIRST_AID -> FirstAidScreen(viewModel = viewModel)
                }
            }

            // Quick Emergency SOS Dialog modal
            if (showSosDialog) {
                EmergencySosDialog(
                    viewModel = viewModel,
                    onDismiss = { viewModel.dismissSosDialog() }
                )
            }

            // TinyFish Universal Search Overlay Modal
            if (isUniversalSearchActive) {
                UniversalSearchOverlay(
                    searchQuery = universalSearchQuery,
                    results = universalSearchResults,
                    onQueryChange = { viewModel.setUniversalSearchQuery(it) },
                    onClose = { viewModel.setUniversalSearchActive(false) },
                    onResultSelected = { result ->
                        viewModel.selectTab(result.category)
                        viewModel.setUniversalSearchActive(false)
                    },
                    onCallHelpline = { num ->
                        viewModel.callEmergencyNumber(context, num)
                    },
                    onNavigateLocation = { lat, lng, title ->
                        viewModel.openGoogleMapsTurnByTurnNavigation(context, lat, lng, title)
                    }
                )
            }

            // TinyFish Form Auto-Fill "Confirm or Edit" Dialog Modal
            if (showAutoFillDialog) {
                AutoFillConfirmDialog(
                    initialData = autoFillData,
                    savedProfiles = savedProfiles,
                    onSelectProfile = { profile -> viewModel.selectProfile(profile) },
                    onSaveProfile = { profile -> viewModel.saveUserProfile(profile) },
                    onConfirm = { confirmedData ->
                        viewModel.updateAutoFillFormData(confirmedData)
                        viewModel.dismissAutoFillDialog()
                    },
                    onDismiss = { viewModel.dismissAutoFillDialog() }
                )
            }
        }
    }
}

@Composable
fun SahayakTopBar(
    currentTab: ScreenTab,
    isEmergencyDarkMode: Boolean,
    onEmergencyHubClick: () -> Unit,
    onFirstAidClick: () -> Unit,
    onCounsellorClick: () -> Unit,
    onSearchClick: () -> Unit,
    onToggleDarkMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isEmergencyDarkMode) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .testTag("sahayak_top_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding: Delhi Emblem + Sahayak Delhi
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = onEmergencyHubClick)
                    .testTag("brand_header")
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(EmergencyRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Emblem",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sahayak Delhi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = if (isEmergencyDarkMode) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmergencyRed)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "NCT",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Civic & Emergency Lifeline",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isEmergencyDarkMode) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Top Action buttons: Counsellor, First Aid & Dark Mode Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Counsellor shortcut chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (currentTab == ScreenTab.COUNSELLOR) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable(onClick = onCounsellorClick)
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                        .testTag("top_bar_counsellor_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Counsellor",
                            tint = if (currentTab == ScreenTab.COUNSELLOR) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Exams",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (currentTab == ScreenTab.COUNSELLOR) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // First Aid shortcut chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (currentTab == ScreenTab.FIRST_AID) Color(0xFF16A34A).copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable(onClick = onFirstAidClick)
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                        .testTag("top_bar_first_aid_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "First Aid",
                            tint = if (currentTab == ScreenTab.FIRST_AID) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "First Aid",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (currentTab == ScreenTab.FIRST_AID) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Universal Search button
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .testTag("top_bar_universal_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "TinyFish Universal Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Dark Emergency Mode button
                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isEmergencyDarkMode) Color(0xFF334155) else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("top_bar_dark_mode_btn")
                ) {
                    Icon(
                        imageVector = if (isEmergencyDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Dark Emergency Mode",
                        tint = if (isEmergencyDarkMode) EmergencyRedBright else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
