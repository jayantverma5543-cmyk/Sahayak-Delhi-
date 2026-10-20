package com.example.ui.tanker

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.data.local.TankerBookingEntity
import com.example.model.WaterSortOption
import com.example.model.WaterSupplier
import com.example.ui.components.OfflineCapabilityBanner
import com.example.ui.components.SahayakSearchBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.viewmodel.SahayakViewModel

@Composable
fun WaterTankerScreen(
    viewModel: SahayakViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.tankerSearchQuery.collectAsState()
    val tankerRadiusRange by viewModel.tankerRadiusRangeKm.collectAsState()
    val tankerSortOption by viewModel.tankerSortOption.collectAsState()
    val tankerTypeFilter by viewModel.tankerTypeFilter.collectAsState()
    val tankers = viewModel.getFilteredTankers()
    val bookings by viewModel.tankerBookings.collectAsState()
    val autoFillData by viewModel.autoFillFormData.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var bookingSupplier by remember { mutableStateOf<WaterSupplier?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("water_tanker_screen_container"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Search bar on top
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SahayakSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setTankerSearch(it) },
                    placeholder = "Search suppliers by area (Dwarka, Rohini, Saket...)",
                    testTag = "tanker_search_input"
                )
            }
        }

        // Offline Banner
        item {
            OfflineCapabilityBanner()
        }

        // Tabs: Available Suppliers / My Bookings
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
                    text = { Text("Delhi Suppliers (${tankers.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_suppliers")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("My Bookings (${bookings.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_tanker_bookings")
                )
            }
        }

        if (selectedTabIndex == 0) {
            // Range Filter (All, 10 km, 20 km) & Sector (DJB / Private)
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
                                selected = tankerRadiusRange == null,
                                onClick = { viewModel.setTankerRadiusRangeKm(null) },
                                label = { Text("All Delhi & NCR") },
                                modifier = Modifier.testTag("filter_tanker_range_all")
                            )
                        }
                        item {
                            FilterChip(
                                selected = tankerRadiusRange == 10.0,
                                onClick = { viewModel.setTankerRadiusRangeKm(if (tankerRadiusRange == 10.0) null else 10.0) },
                                label = { Text("Within 10 km") },
                                modifier = Modifier.testTag("filter_tanker_range_10km")
                            )
                        }
                        item {
                            FilterChip(
                                selected = tankerRadiusRange == 20.0,
                                onClick = { viewModel.setTankerRadiusRangeKm(if (tankerRadiusRange == 20.0) null else 20.0) },
                                label = { Text("Within 20 km") },
                                modifier = Modifier.testTag("filter_tanker_range_20km")
                            )
                        }
                        item {
                            FilterChip(
                                selected = tankerTypeFilter == null,
                                onClick = { viewModel.setTankerTypeFilter(null) },
                                label = { Text("All Suppliers") },
                                modifier = Modifier.testTag("filter_tanker_type_all")
                            )
                        }
                        item {
                            FilterChip(
                                selected = tankerTypeFilter == "GOVERNMENT",
                                onClick = { viewModel.setTankerTypeFilter(if (tankerTypeFilter == "GOVERNMENT") null else "GOVERNMENT") },
                                label = { Text("Delhi Jal Board (DJB Govt)") },
                                modifier = Modifier.testTag("filter_tanker_type_govt")
                            )
                        }
                        item {
                            FilterChip(
                                selected = tankerTypeFilter == "PRIVATE",
                                onClick = { viewModel.setTankerTypeFilter(if (tankerTypeFilter == "PRIVATE") null else "PRIVATE") },
                                label = { Text("Private Certified Tankers") },
                                modifier = Modifier.testTag("filter_tanker_type_private")
                            )
                        }
                    }
                }
            }

            // Sort Options (Rating: High to Low, Rating: Low to High, Price, Distance)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                    Text(
                        text = "SORT BY RATING & PRICE",
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
                                selected = tankerSortOption == WaterSortOption.RATING_HIGH_TO_LOW,
                                onClick = { viewModel.setTankerSortOption(WaterSortOption.RATING_HIGH_TO_LOW) },
                                label = { Text("Rating: High to Low ★") },
                                modifier = Modifier.testTag("sort_tanker_rating_desc")
                            )
                        }
                        item {
                            FilterChip(
                                selected = tankerSortOption == WaterSortOption.RATING_LOW_TO_HIGH,
                                onClick = { viewModel.setTankerSortOption(WaterSortOption.RATING_LOW_TO_HIGH) },
                                label = { Text("Rating: Low to High") },
                                modifier = Modifier.testTag("sort_tanker_rating_asc")
                            )
                        }
                        item {
                            FilterChip(
                                selected = tankerSortOption == WaterSortOption.PRICE_LOW_TO_HIGH,
                                onClick = { viewModel.setTankerSortOption(WaterSortOption.PRICE_LOW_TO_HIGH) },
                                label = { Text("Price: Low to High (DJB ₹650)") },
                                modifier = Modifier.testTag("sort_tanker_price_asc")
                            )
                        }
                        item {
                            FilterChip(
                                selected = tankerSortOption == WaterSortOption.PRICE_HIGH_TO_LOW,
                                onClick = { viewModel.setTankerSortOption(WaterSortOption.PRICE_HIGH_TO_LOW) },
                                label = { Text("Price: High to Low") },
                                modifier = Modifier.testTag("sort_tanker_price_desc")
                            )
                        }
                        item {
                            FilterChip(
                                selected = tankerSortOption == WaterSortOption.DISTANCE_NEAREST,
                                onClick = { viewModel.setTankerSortOption(WaterSortOption.DISTANCE_NEAREST) },
                                label = { Text("Nearest to Me") },
                                modifier = Modifier.testTag("sort_tanker_distance")
                            )
                        }
                    }
                }
            }

            // Suppliers Tab
            items(tankers) { supplier ->
                WaterSupplierCard(
                    supplier = supplier,
                    onBookClick = { bookingSupplier = supplier },
                    onCallClick = { viewModel.callEmergencyNumber(context, supplier.phone) }
                )
            }
        } else {
            // My Bookings Tab
            if (bookings.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "No Bookings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Active Water Tanker Bookings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose from the 5 Delhi suppliers to request emergency or scheduled water supply.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { selectedTabIndex = 0 },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Browse Suppliers")
                        }
                    }
                }
            } else {
                items(bookings) { booking ->
                    TankerBookingCard(
                        booking = booking,
                        onCancel = { viewModel.cancelTankerBooking(booking.id) }
                    )
                }
            }
        }
    }

    // Booking Dialog Modal
    bookingSupplier?.let { supplier ->
        TankerBookingDialog(
            supplier = supplier,
            autoFillData = autoFillData,
            onTriggerAutoFill = {
                viewModel.parseAndPrepareAutoFill("Need 5000L water tanker for self in Delhi", "WATER_TANKER")
            },
            onDismiss = { bookingSupplier = null },
            onConfirmBooking = { capacity, cost, date, slot, name, phone, address, landmark ->
                viewModel.bookWaterTanker(
                    supplier = supplier,
                    capacityLiters = capacity,
                    totalCost = cost,
                    deliveryDate = date,
                    deliverySlot = slot,
                    customerName = name,
                    phone = phone,
                    deliveryAddress = address,
                    landmark = landmark
                )
                bookingSupplier = null
                selectedTabIndex = 1 // Switch to bookings tab to see order
            }
        )
    }
}

@Composable
fun WaterSupplierCard(
    supplier: WaterSupplier,
    onBookClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .testTag("tanker_card_${supplier.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Supplier title & Gov badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = supplier.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (supplier.isGovtSubsidized) {
                        StatusBadge(text = "Delhi Govt Subsidized (DJB)", isPositive = true)
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
                        text = "${supplier.rating}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = " (${supplier.totalReviews})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Area served
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Area",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = supplier.areaServed,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rates and Specs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STANDARD 5,000L",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${supplier.baseRatePer5000L}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "EST. DISPATCH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = supplier.deliveryTimeMinutes,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DISTANCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${supplier.distanceKm} km",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBookClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(44.dp)
                        .testTag("book_tanker_btn_${supplier.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "Book",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Book Water Tanker", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onCallClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("call_supplier_btn_${supplier.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Call", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun TankerBookingCard(
    booking: TankerBookingEntity,
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
            .testTag("booking_card_${booking.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${booking.capacityLiters}L Tanker • ₹${booking.totalCost}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(text = booking.status, isPositive = true)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Supplier: ${booking.supplierName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Delivery: ${booking.deliveryDate} (${booking.deliverySlot})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Address: ${booking.deliveryAddress}, ${booking.landmark}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmergencyRed),
                    modifier = Modifier.testTag("cancel_booking_btn_${booking.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Cancel Order", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TankerBookingDialog(
    supplier: WaterSupplier,
    autoFillData: ParsedFormData? = null,
    onTriggerAutoFill: () -> Unit = {},
    onDismiss: () -> Unit,
    onConfirmBooking: (capacity: Int, cost: Int, date: String, slot: String, name: String, phone: String, address: String, landmark: String) -> Unit
) {
    var selectedCapacity by remember { mutableIntStateOf(5000) }
    var deliveryDate by remember { mutableStateOf("Today (Urgent)") }
    var selectedSlot by remember { mutableStateOf(supplier.availableSlots.firstOrNull() ?: "Morning 06:00 - 09:00") }
    var customerName by remember { mutableStateOf(autoFillData?.fullName ?: "") }
    var customerPhone by remember { mutableStateOf(autoFillData?.phone ?: "") }
    var deliveryAddress by remember { mutableStateOf(autoFillData?.address ?: "") }
    var landmark by remember { mutableStateOf("") }

    LaunchedEffect(autoFillData) {
        if (autoFillData != null) {
            customerName = autoFillData.fullName
            customerPhone = autoFillData.phone
            deliveryAddress = autoFillData.address
        }
    }

    val calculatedCost = when (selectedCapacity) {
        3000 -> supplier.baseRatePer3000L
        10000 -> supplier.baseRatePer10000L
        else -> supplier.baseRatePer5000L
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
                            text = "Book Water Tanker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = supplier.name,
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
                        .testTag("tanker_autofill_cta")
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
                                text = if (autoFillData != null) "Active profile: ${autoFillData.profileTitle} (Tap to change)" else "Tap to choose saved profile (Self, Father, etc.) or voice input",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Select Capacity
                Text(
                    text = "Select Tanker Capacity",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCapacity == 3000,
                        onClick = { selectedCapacity = 3000 },
                        label = { Text("3,000 L (₹${supplier.baseRatePer3000L})") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedCapacity == 5000,
                        onClick = { selectedCapacity = 5000 },
                        label = { Text("5,000 L (₹${supplier.baseRatePer5000L})") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedCapacity == 10000,
                        onClick = { selectedCapacity = 10000 },
                        label = { Text("10,000 L (₹${supplier.baseRatePer10000L})") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Time Slot
                OutlinedTextField(
                    value = deliveryDate,
                    onValueChange = { deliveryDate = it },
                    label = { Text("Delivery Date") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Date") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = selectedSlot,
                    onValueChange = { selectedSlot = it },
                    label = { Text("Preferred Time Slot") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Slot") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Customer Details
                Text(
                    text = "Delivery Address in Delhi",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Your Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Mobile Number (for Driver)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("House / Flat No., Colony / Sector") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Nearby Landmark / Metro Station") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Price Summary Box
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
                        Text(text = "Total Payable Amount", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "₹$calculatedCost",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Pay on Delivery (Cash/UPI)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        val name = if (customerName.isBlank()) "Delhi Resident" else customerName
                        val phone = if (customerPhone.isBlank()) "9876543210" else customerPhone
                        val address = if (deliveryAddress.isBlank()) "Block C, Delhi" else deliveryAddress
                        val lmark = if (landmark.isBlank()) "Near Metro Station" else landmark

                        onConfirmBooking(
                            selectedCapacity,
                            calculatedCost,
                            deliveryDate,
                            selectedSlot,
                            name,
                            phone,
                            address,
                            lmark
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_tanker_booking_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Confirm",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Confirm Water Tanker Order", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
