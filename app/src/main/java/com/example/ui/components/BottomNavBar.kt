package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScreenTab
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright

@Composable
fun SahayakBottomNavBar(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    onSosClick: () -> Unit,
    isEmergencyDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bottom_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bottom_pulse_scale"
    )

    Surface(
        color = if (isEmergencyDarkMode) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isEmergencyDarkMode) Color(0xFF1E293B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .testTag("sahayak_bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Hospitals
            BottomNavItem(
                label = "Hospitals",
                icon = Icons.Outlined.LocalHospital,
                selectedIcon = Icons.Filled.LocalHospital,
                isSelected = currentTab == ScreenTab.HOSPITALS,
                isDarkMode = isEmergencyDarkMode,
                onClick = { onTabSelected(ScreenTab.HOSPITALS) },
                testTag = "bottom_nav_hospitals"
            )

            // Tab 2: Water Tanker
            BottomNavItem(
                label = "Water",
                icon = Icons.Outlined.WaterDrop,
                selectedIcon = Icons.Filled.WaterDrop,
                isSelected = currentTab == ScreenTab.WATER_TANKER,
                isDarkMode = isEmergencyDarkMode,
                onClick = { onTabSelected(ScreenTab.WATER_TANKER) },
                testTag = "bottom_nav_water"
            )

            // Center Highlighted SOS Emergency Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = (-6).dp)
                    .size(62.dp)
                    .clickable(onClick = onSosClick)
                    .testTag("bottom_nav_sos_button")
            ) {
                // Pulsing glow aura
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(EmergencyRedBright.copy(alpha = 0.3f))
                )
                // Red SOS circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(EmergencyRed)
                        .border(2.dp, Color.White, CircleShape)
                        .shadow(4.dp, CircleShape)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "SOS",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "112",
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        )
                    }
                }
            }

            // Tab 3: Doctors & OPD
            BottomNavItem(
                label = "Doctors",
                icon = Icons.Outlined.MedicalServices,
                selectedIcon = Icons.Filled.MedicalServices,
                isSelected = currentTab == ScreenTab.DOCTORS,
                isDarkMode = isEmergencyDarkMode,
                onClick = { onTabSelected(ScreenTab.DOCTORS) },
                testTag = "bottom_nav_doctors"
            )

            // Tab 4: Legal & Hospital First Aid Guidelines
            BottomNavItem(
                label = "First Aid",
                icon = Icons.Outlined.Healing,
                selectedIcon = Icons.Filled.Healing,
                isSelected = currentTab == ScreenTab.FIRST_AID,
                isDarkMode = isEmergencyDarkMode,
                onClick = { onTabSelected(ScreenTab.FIRST_AID) },
                testTag = "bottom_nav_first_aid"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = if (isDarkMode) EmergencyRedBright else MaterialTheme.colorScheme.primary
    val inactiveColor = if (isDarkMode) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}
