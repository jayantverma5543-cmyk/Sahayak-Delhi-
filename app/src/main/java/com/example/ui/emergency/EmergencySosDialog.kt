package com.example.ui.emergency

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright
import com.example.viewmodel.SahayakViewModel

@Composable
fun EmergencySosDialog(
    viewModel: SahayakViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, EmergencyRedBright, RoundedCornerShape(24.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(EmergencyRedBright)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DELHI EMERGENCY DISPATCH",
                            color = EmergencyRedBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dismiss_sos_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Urgent Headline
                Text(
                    text = "Critical Emergency Assistance",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Tap to call immediately or broadcast live GPS coordinates to family & services.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Primary 112 SOS Button
                Button(
                    onClick = {
                        viewModel.callEmergencyNumber(context, "112")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("sos_dialog_call_112_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call 112",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "CALL 112 NOW (NATIONAL SOS)",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Police • Ambulance • Fire Combined Dispatch",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CATS Ambulance 102 Button
                Button(
                    onClick = {
                        viewModel.callEmergencyNumber(context, "102")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        .testTag("sos_dialog_call_102_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Call 102",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Call 102 CATS Ambulance (Delhi Govt)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Free Delhi GPS-tracked trauma ambulance",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Location Share Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.shareLiveEmergencyLocation(context)
                        }
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .testTag("sos_dialog_share_location_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0369A1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShareLocation,
                                contentDescription = "Share Location",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Share Live GPS Location & SOS",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Sends coordinates via WhatsApp, SMS, or Emergency contacts",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section Title for Quick Hotlines
                Text(
                    text = "OFFICIAL 24x7 GOVERNMENT LIFELINES",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Quick Grid Row 1: Childline, Women, Police
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EmergencyQuickChip(
                        label = "Child Helpline",
                        number = "1098",
                        icon = Icons.Default.ChildCare,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.callEmergencyNumber(context, "1098")
                    }

                    EmergencyQuickChip(
                        label = "Women Safety",
                        number = "1091",
                        icon = Icons.Default.Call,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.callEmergencyNumber(context, "1091")
                    }

                    EmergencyQuickChip(
                        label = "Delhi Police",
                        number = "100",
                        icon = Icons.Default.LocalPolice,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.callEmergencyNumber(context, "100")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Grid Row 2: Fire, Cyber Crime, Disaster DDMA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EmergencyQuickChip(
                        label = "Fire Dept",
                        number = "101",
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.callEmergencyNumber(context, "101")
                    }

                    EmergencyQuickChip(
                        label = "Cyber Crime",
                        number = "1930",
                        icon = Icons.Default.Security,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.callEmergencyNumber(context, "1930")
                    }

                    EmergencyQuickChip(
                        label = "Disaster DDMA",
                        number = "1077",
                        icon = Icons.Default.Shield,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.callEmergencyNumber(context, "1077")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Grid Row 3: Delhi Jal Board Water, Senior Citizen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EmergencyQuickChip(
                        label = "DJB Water Helpline",
                        number = "1916",
                        icon = Icons.Default.WaterDrop,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.callEmergencyNumber(context, "1916")
                    }

                    EmergencyQuickChip(
                        label = "Elderline Senior",
                        number = "14567",
                        icon = Icons.Default.Call,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.callEmergencyNumber(context, "14567")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Emergency Hub", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun EmergencyQuickChip(
    label: String,
    number: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .testTag("emergency_chip_$number")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = number,
                fontWeight = FontWeight.Black,
                color = EmergencyRedBright,
                fontSize = 14.sp
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
