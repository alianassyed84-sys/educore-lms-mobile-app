package com.example.ui.admin

import android.widget.Toast
import com.example.util.showToast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// ━━━━━━━ SECTIONS ROOT CONTROLLER ━━━━━━━
@Composable
fun AdminMainScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf("Overview") }
    val currentUserState by viewModel.currentUser.collectAsState()
    val admin = currentUserState ?: return

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Column(modifier = Modifier.background(DarkCardBg)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = RedDanger, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Command Center", fontWeight = FontWeight.Bold, color = HeadingText, fontSize = 16.sp)
                    }

                    IconButton(onClick = onLogout) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Exit", tint = RedDanger)
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = when (activeTab) {
                        "Overview" -> 0
                        "Users" -> 1
                        "Instructors" -> 2
                        "Courses" -> 3
                        "Revenue" -> 4
                        else -> 5
                    },
                    containerColor = DarkCardBg,
                    contentColor = IndigoPrimary,
                    edgePadding = 12.dp
                ) {
                    val tabs = listOf("Overview", "Users", "Instructors", "Courses", "Revenue", "Settings")
                    tabs.forEach { tabName ->
                        Tab(
                            selected = activeTab == tabName,
                            onClick = { activeTab = tabName },
                            text = { Text(tabName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (activeTab == tabName) IndigoPrimary else BodyText) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "Overview" -> PlatformOverviewScreen(viewModel)
                "Users" -> UserManagementScreen(viewModel)
                "Instructors" -> InstructorApprovalScreen(viewModel)
                "Courses" -> CourseModerationScreen(viewModel)
                "Revenue" -> RevenuePayoutsScreen(viewModel)
                "Settings" -> PlatformSettingsScreen(viewModel)
            }
        }
    }
}

// ━━━━━━━ SCREEN 1: PLATFORM OVERVIEW ━━━━━━━
@Composable
fun PlatformOverviewScreen(
    viewModel: MainViewModel
) {
    val users by viewModel.allUsersList.collectAsState()
    val courses by viewModel.allCoursesList.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Admin Analytics Ledger", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        // KPI Box Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KPIContainer(label = "Total Users", value = "${users.size * 3 + 12000}", color = IndigoPrimary, modifier = Modifier.weight(1f))
                    KPIContainer(label = "Revenues Ledger", value = "₹18,40,000", color = EmeraldSecondary, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KPIContainer(label = "Active Courses", value = "${courses.size}", color = AmberWarning, modifier = Modifier.weight(1f))
                    KPIContainer(label = "Active Monthly", value = "8,200 MAU", color = HeadingText, modifier = Modifier.weight(1f))
                }
            }
        }

        // Simulated Line Chart
        item {
            Text(text = "Revenues Platform Ledger (6-Month Summary)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, size.height * 0.9f)
                            lineTo(size.width * 0.2f, size.height * 0.8f)
                            lineTo(size.width * 0.4f, size.height * 0.4f)
                            lineTo(size.width * 0.6f, size.height * 0.62f)
                            lineTo(size.width * 0.8f, size.height * 0.2f)
                            lineTo(size.width, size.height * 0.05f)
                        }
                        drawPath(path = path, color = EmeraldSecondary, style = Stroke(width = 8f))
                    }
                }
            }
        }

        // Active Flags alerts
        item {
            Text(text = "Unattended Action Alerts", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AlertDetailItem(msg = "Instructor Alex Mercer is pending profile approval", isAlert = true)
                    AlertDetailItem(msg = "Course 'DevOps Pipeline Crash Course' requires moderation", isAlert = true)
                    AlertDetailItem(msg = "System is online with stable memory pools", isAlert = false)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun KPIContainer(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, CardBorderColor, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, color = BodyText, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
        }
    }
}

@Composable
fun AlertDetailItem(msg: String, isAlert: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isAlert) AmberWarning else EmeraldSecondary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = msg, fontSize = 12.sp, color = HeadingText)
    }
}

// ━━━━━━━ SCREEN 2: USER MANAGEMENT ━━━━━━━
@Composable
fun UserManagementScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val users by viewModel.allUsersList.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf("All") } // "All", "Learner", "Instructor"

    val filteredList = remember(users, searchQuery, roleFilter) {
        users.filter { user ->
            val matchQuery = user.name.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true)
            val matchRole = roleFilter == "All" || user.role == roleFilter
            matchQuery && matchRole
        }
    }

    var selectedUserForDetails by remember { mutableStateOf<UserEntity?>(null) }

    if (selectedUserForDetails != null) {
        val u = selectedUserForDetails!!
        AlertDialog(
            onDismissRequest = { selectedUserForDetails = null },
            title = { Text(text = "User Operations sheet", color = HeadingText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Name: ${u.name}", fontWeight = FontWeight.Bold)
                    Text("Email: ${u.email}", color = BodyText)
                    Text("Current subscription: ${u.subscription}")
                    Text("Status: ${if (u.isActive) "Active" else "Suspended"}", color = if (u.isActive) EmeraldSecondary else RedDanger)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminSuspendUser(u.email, u.isActive)
                        selectedUserForDetails = null
                        context.showToast("User suspension state updated successfully!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (u.isActive) RedDanger else EmeraldSecondary)
                ) {
                    Text(text = if (u.isActive) "Suspend User" else "Activate User")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForDetails = null }) { Text("Dismiss") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(text = "Manage Users profiles", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name email or roles...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Simple row chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Learner", "Instructor", "Admin").forEach { filter ->
                val active = roleFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (active) IndigoPrimary else DarkCardBg)
                        .clickable { roleFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = filter, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredList) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCardBg)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                        .clickable { selectedUserForDetails = user }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(IndigoPrimary), contentAlignment = Alignment.Center) {
                            Text(user.name.take(1), color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = user.email, fontSize = 10.sp, color = BodyText)
                        }
                    }

                    // Suspension badge status
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (user.isActive) EmeraldSecondary.copy(alpha = 0.2f) else RedDanger.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = if (user.isActive) "Active" else "Suspended", color = if (user.isActive) EmeraldSecondary else RedDanger, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ━━━━━━━ SCREEN 3: INSTRUCTOR APPROVAL QUEUE ━━━━━━━
@Composable
fun InstructorApprovalScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val users by viewModel.allUsersList.collectAsState()

    // Filter unapproved instructors
    val pendingInstructors = remember(users) {
        users.filter { it.role == "Instructor" && !it.isApproved }
    }

    var showRejectionModal by remember { mutableStateOf<String?>(null) } // Email to reject
    var rejectionReason by remember { mutableStateOf("") }

    if (showRejectionModal != null) {
        val targetEmail = showRejectionModal!!
        AlertDialog(
            onDismissRequest = { showRejectionModal = null },
            title = { Text("Reject Profile Request", color = HeadingText) },
            text = {
                OutlinedTextField(
                    value = rejectionReason,
                    onValueChange = { rejectionReason = it },
                    label = { Text("Reason for rejection") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionReason.isBlank()) return@Button
                        viewModel.adminRejectInstructor(targetEmail, rejectionReason)
                        rejectionReason = ""
                        showRejectionModal = null
                        context.showToast("Instructor declined securely.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
                ) {
                    Text("Reject Instructor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectionModal = null }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(text = "Instructor Approval Board", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (pendingInstructors.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MutedText, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No instructor approvals currently await processing.", color = BodyText, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(pendingInstructors) { inst ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(IndigoPrimary), contentAlignment = Alignment.Center) {
                                    Text(inst.name.take(1), color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = inst.name, fontWeight = FontWeight.Bold)
                                    Text(text = inst.email, fontSize = 11.sp, color = BodyText)
                                }
                            }

                            Text(
                                text = "Draft Course catalog status: Pending\nPortfolio Links: https://github.com/simulated-educore-portfolio-doc",
                                fontSize = 11.sp,
                                color = BodyText
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.adminApproveInstructor(inst.email)
                                        context.showToast("Instructor profile approved!")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Approve profile")
                                }

                                Button(
                                    onClick = { showRejectionModal = inst.email },
                                    colors = ButtonDefaults.buttonColors(containerColor = RedDanger),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ━━━━━━━ SCREEN 4: COURSE MODERATION QUEUE ━━━━━━━
@Composable
fun CourseModerationScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val courses by viewModel.allCoursesList.collectAsState()

    val pendingReviewCourses = remember(courses) {
        courses.filter { it.status == "Pending" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(text = "Course Moderation board", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (pendingReviewCourses.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Task, contentDescription = null, tint = MutedText, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("All course listings approved. No pending review files found in the queue.", color = BodyText, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(pendingReviewCourses) { course ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = course.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Instructor: ${course.instructorName}\nCategory: ${course.category} •   Price tag: ${if (course.price == 0) "Free" else "₹${course.price}"}", fontSize = 11.sp, color = BodyText)
                            Text(text = "Description details: ${course.description}", fontSize = 11.sp, color = BodyText, maxLines = 3)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.adminModerateCourse(course.id, true)
                                        context.showToast("Course approved to public!")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Approve listing")
                                }

                                Button(
                                    onClick = {
                                        viewModel.adminModerateCourse(course.id, false, "Price point/content violates publishing metrics.")
                                        context.showToast("Course declined successfully.")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RedDanger),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Moderate Decline")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ━━━━━━━ SCREEN 5: REVENUE LEDGER & PAYOUTS ━━━━━━━
@Composable
fun RevenuePayoutsScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val corporatePayouts by viewModel.allPayoutsList.collectAsState()

    val pendingPayoutsList = remember(corporatePayouts) {
        corporatePayouts.filter { it.status == "Pending" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "Revenue Ledger Controls", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        // General Stats
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Brush.horizontalGradient(listOf(EmeraldSecondary, IndigoPrimary)), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Total Collected Platform Revenues", fontSize = 11.sp, color = BodyText)
                Text(text = "₹18,40,000", style = MaterialTheme.typography.displayMedium, color = EmeraldSecondary, fontSize = 28.sp)
                Divider(color = CardBorderColor, modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("EduCore Commission Split (30%)", fontSize = 11.sp, color = BodyText)
                    Text("₹5,52,000", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Action panel or Payout requests queue
        Text(text = "Instructor Payout Requests Queue", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        if (pendingPayoutsList.isEmpty()) {
            Text("No pending payouts requests received currently.", color = MutedText, fontSize = 12.sp)
        } else {
            pendingPayoutsList.forEach { payout ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCardBg)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Instructor: ${payout.instructorId}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "Royalty Payout Requested: ₹${payout.amount}", color = EmeraldSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.adminProcessPayout(payout.id)
                            context.showToast("Revenues payout dispatched safely via bank wire!", Toast.LENGTH_LONG)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary)
                    ) {
                        Text("Disburse ₹${payout.amount}", fontSize = 11.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ━━━━━━━ SCREEN 6: PLATFORM SETTINGS ━━━━━━━
@Composable
fun PlatformSettingsScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var isMaintenanceMode by remember { mutableStateOf(false) }
    var scaleCommissionVal by remember { mutableStateOf("30") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Core Platform Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        // General settings input
        OutlinedTextField(
            value = "EduCore",
            onValueChange = {},
            label = { Text("Application Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor)
        )

        OutlinedTextField(
            value = scaleCommissionVal,
            onValueChange = { scaleCommissionVal = it },
            label = { Text("General Commission split percentage (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor)
        )

        // Maintenance Mode Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkCardBg)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Lock Platform / Maintenance Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Restrict access to admins only. General students see maintenance alert screens.", color = BodyText, fontSize = 10.sp)
            }
            Switch(
                checked = isMaintenanceMode,
                onCheckedChange = {
                    isMaintenanceMode = it
                    context.showToast(if (it) "Maintenance mode locked!" else "Platform online draft saved.")
                },
                colors = SwitchDefaults.colors(checkedThumbColor = RedDanger)
            )
        }

        Divider(color = CardBorderColor)

        Button(
            onClick = {
                context.showToast("E-learning core credentials updated successfully!")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
        ) {
            Text("Update settings draft")
        }
    }
}
