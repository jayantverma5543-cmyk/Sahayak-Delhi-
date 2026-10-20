package com.example.ui.counsellor

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
import com.example.data.local.CounsellorQueryEntity
import com.example.model.ExamCounsellor
import com.example.ui.components.OfflineCapabilityBanner
import com.example.ui.components.SahayakSearchBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.StatusAmber
import com.example.viewmodel.SahayakViewModel

@Composable
fun ExamCounsellorScreen(
    viewModel: SahayakViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.counsellorSearchQuery.collectAsState()
    val examFilter by viewModel.counsellorExamFilter.collectAsState()
    val counsellors = viewModel.getFilteredCounsellors()
    val queries by viewModel.counsellorQueries.collectAsState()
    val autoFillData by viewModel.autoFillFormData.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCounsellorForQuery by remember { mutableStateOf<ExamCounsellor?>(null) }

    val examCategories = listOf("UPSC", "NEET", "JEE", "SSC", "CAT", "CUET")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("exam_counsellor_screen_container"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Search bar
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SahayakSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setCounsellorSearch(it) },
                    placeholder = "Search counsellors by exam (UPSC, NEET...), name, hub...",
                    testTag = "counsellor_search_input"
                )
            }
        }

        // Offline Banner
        item {
            OfflineCapabilityBanner()
        }

        // Tab Row: Mentors / My Queries
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
                    text = { Text("Delhi Exam Mentors (${counsellors.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_mentors")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("My Queries (${queries.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_queries")
                )
            }
        }

        if (selectedTabIndex == 0) {
            // Exam Filter Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = examFilter == null,
                            onClick = { viewModel.setCounsellorExamFilter(null) },
                            label = { Text("All Exams") },
                            modifier = Modifier.testTag("filter_all_exams")
                        )
                    }
                    items(examCategories) { exam ->
                        FilterChip(
                            selected = examFilter == exam,
                            onClick = {
                                viewModel.setCounsellorExamFilter(if (examFilter == exam) null else exam)
                            },
                            label = { Text(exam) },
                            modifier = Modifier.testTag("filter_exam_$exam")
                        )
                    }
                }
            }

            // Counsellors list
            items(counsellors) { counsellor ->
                ExamCounsellorCard(
                    counsellor = counsellor,
                    onQueryClick = { selectedCounsellorForQuery = counsellor },
                    onCallClick = { viewModel.callEmergencyNumber(context, counsellor.phone) }
                )
            }
        } else {
            // My Queries Tab
            if (queries.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "No Queries",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Counselling Queries Submitted",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Connect with top mentors for UPSC, NEET, JEE, SSC, CAT & CUET in Delhi hubs (Old Rajinder Nagar, Mukherjee Nagar, Kalu Sarai).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { selectedTabIndex = 0 },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Find Mentors")
                        }
                    }
                }
            } else {
                items(queries) { query ->
                    CounsellorQueryCard(
                        query = query,
                        onCancel = { viewModel.cancelCounsellorQuery(query.id) }
                    )
                }
            }
        }
    }

    // Query Submission Modal
    selectedCounsellorForQuery?.let { counsellor ->
        CounsellorQueryDialog(
            counsellor = counsellor,
            autoFillData = autoFillData,
            onTriggerAutoFill = {
                viewModel.parseAndPrepareAutoFill("Need guidance for UPSC prelims", "COUNSELLOR")
            },
            onDismiss = { selectedCounsellorForQuery = null },
            onConfirm = { name, phone, exam, stage, queryText, slot ->
                viewModel.submitCounsellorQuery(
                    counsellor = counsellor,
                    examCategory = exam,
                    studentName = name,
                    phone = phone,
                    stage = stage,
                    queryText = queryText,
                    preferredSlot = slot
                )
                selectedCounsellorForQuery = null
                selectedTabIndex = 1 // Switch to queries tab
            }
        )
    }
}

@Composable
fun ExamCounsellorCard(
    counsellor: ExamCounsellor,
    onQueryClick: () -> Unit,
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
            .testTag("counsellor_card_${counsellor.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name, Exams badges & Verified badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = counsellor.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (counsellor.isVerified) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Mentor",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = counsellor.designation,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = counsellor.background,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                        text = "${counsellor.rating}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Exams & Students count row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    counsellor.exams.forEach { exam ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = exam,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text = "${counsellor.studentsMentored}+ Students Guided",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onQueryClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(44.dp)
                        .testTag("ask_counsellor_btn_${counsellor.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = "Ask Query",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Ask Guidance Query", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onCallClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("call_counsellor_btn_${counsellor.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Desk",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Call Desk", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun CounsellorQueryCard(
    query: CounsellorQueryEntity,
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
            .testTag("counsellor_query_card_${query.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${query.examCategory} Mentorship • ${query.counsellorName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(text = "Assigned", isPositive = true)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Student: ${query.studentName} (${query.preparationStage})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Query: \"${query.queryText}\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Callback Slot: ${query.preferredSlot}",
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
                    modifier = Modifier.testTag("cancel_query_btn_${query.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Remove Query", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CounsellorQueryDialog(
    counsellor: ExamCounsellor,
    autoFillData: ParsedFormData? = null,
    onTriggerAutoFill: () -> Unit = {},
    onDismiss: () -> Unit,
    onConfirm: (studentName: String, phone: String, exam: String, stage: String, queryText: String, slot: String) -> Unit
) {
    var studentName by remember { mutableStateOf(autoFillData?.fullName ?: "") }
    var studentPhone by remember { mutableStateOf(autoFillData?.phone ?: "") }
    var selectedExam by remember { mutableStateOf(counsellor.exams.firstOrNull() ?: "UPSC") }
    var prepStage by remember { mutableStateOf("Beginner / 1st Attempt") }
    var queryText by remember { mutableStateOf(autoFillData?.notes ?: "") }
    var preferredSlot by remember { mutableStateOf("Evening 5:00 - 8:00 PM") }

    LaunchedEffect(autoFillData) {
        if (autoFillData != null) {
            studentName = autoFillData.fullName
            studentPhone = autoFillData.phone
            queryText = autoFillData.notes
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ask Exam Counsellor",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${counsellor.name} (${counsellor.designation})",
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
                        .testTag("counsellor_autofill_cta")
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
                                text = if (autoFillData != null) "Active profile: ${autoFillData.profileTitle} (Tap to change)" else "Tap to choose saved student profile or voice input",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Select Exam
                Text(
                    text = "Select Targeted Exam",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    counsellor.exams.forEach { exam ->
                        FilterChip(
                            selected = selectedExam == exam,
                            onClick = { selectedExam = exam },
                            label = { Text(exam) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preparation Stage
                Text(
                    text = "Preparation Stage",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = prepStage == "Beginner",
                        onClick = { prepStage = "Beginner" },
                        label = { Text("Beginner") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = prepStage == "Dropper",
                        onClick = { prepStage = "Dropper" },
                        label = { Text("Dropper") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = prepStage == "Mains/Repeat",
                        onClick = { prepStage = "Mains/Repeat" },
                        label = { Text("Advanced") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Student Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = studentPhone,
                    onValueChange = { studentPhone = it },
                    label = { Text("WhatsApp / Calling Mobile Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    label = { Text("Your Question / Strategy Concern") },
                    placeholder = { Text("e.g., Best test series, GS3 strategy, backlog recovery...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = preferredSlot,
                    onValueChange = { preferredSlot = it },
                    label = { Text("Preferred Callback Window") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val name = if (studentName.isBlank()) "Delhi Aspirant" else studentName
                        val phone = if (studentPhone.isBlank()) "9876543210" else studentPhone
                        val query = if (queryText.isBlank()) "Roadmap guidance requested" else queryText

                        onConfirm(name, phone, selectedExam, prepStage, query, preferredSlot)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_counsellor_query_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Submit",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Submit Mentorship Query", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
