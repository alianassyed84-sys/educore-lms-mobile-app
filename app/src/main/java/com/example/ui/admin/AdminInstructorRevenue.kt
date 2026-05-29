package com.example.ui.admin

import com.example.util.showToast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

// ─── INSTRUCTOR CONTROL ───────────────────────────────────

@Composable
fun AdminInstructorControlScreen(viewModel: MainViewModel) {
    val allUsers by viewModel.allUsersList.collectAsState()
    val allCourses by viewModel.allCoursesList.collectAsState()
    var filter by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }
    var selectedInstructor by remember { mutableStateOf<UserEntity?>(null) }

    val instructors = remember(allUsers, filter, search) {
        allUsers.filter { it.role == "Instructor" }.filter { inst ->
            (filter == "All" || (filter == "Pending" && !inst.isApproved && inst.isActive) ||
             (filter == "Approved" && inst.isApproved) || (filter == "Suspended" && !inst.isActive) ||
             (filter == "Featured" && inst.isFeatured)) &&
            (search.isEmpty() || inst.name.contains(search, true) || inst.email.contains(search, true))
        }
    }

    if (selectedInstructor != null) {
        AdminInstructorDetailPage(instructor = selectedInstructor!!, viewModel = viewModel, allCourses = allCourses, onDismiss = { selectedInstructor = null })
        return
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Instructor Control", "${instructors.size} instructors")
        EduTextField("Search instructors…", search) { search = it }
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("All","Pending","Approved","Suspended","Featured")) { f -> FilterChip(f, filter == f) { filter = f } }
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(instructors) { inst ->
                val courseCount = allCourses.count { it.instructorId == inst.email }
                Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)).clickable { selectedInstructor = inst }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AvatarBox(inst.name, 44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(inst.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (inst.isFeatured) Icon(Icons.Default.Star, null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                            }
                            Text(inst.email, fontSize = 11.sp, color = BodyText)
                            Text("$courseCount courses • Commission ${inst.commissionRate}%", fontSize = 11.sp, color = MutedText)
                        }
                        StatusBadge(if (inst.isBanned) "Banned" else if (!inst.isActive) "Suspended" else if (inst.isApproved) "Approved" else "Pending")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminInstructorDetailPage(instructor: UserEntity, viewModel: MainViewModel, allCourses: List<CourseEntity>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var showRejectModal by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    var commRate by remember { mutableStateOf(instructor.commissionRate.toString()) }
    val instructorCourses = remember(allCourses) { allCourses.filter { it.instructorId == instructor.email } }
    val payouts by viewModel.payoutDao.getPayoutsForInstructorFlow(instructor.email).collectAsState(emptyList())
    val totalEarned = payouts.filter { it.status == "Paid" }.sumOf { it.amount }

    if (showRejectModal) {
        AlertDialog(
            onDismissRequest = { showRejectModal = false },
            containerColor = DarkCardBg,
            title = { Text("Reject Instructor", color = HeadingText, fontWeight = FontWeight.Bold) },
            text = { EduTextField("Reason for rejection", rejectReason, maxLines = 3) { rejectReason = it } },
            confirmButton = { Button(onClick = { if (rejectReason.isNotEmpty()) { viewModel.adminRejectInstructor(instructor.email, rejectReason); showRejectModal = false; onDismiss() } }, colors = ButtonDefaults.buttonColors(containerColor = RedDanger)) { Text("Reject") } },
            dismissButton = { TextButton(onClick = { showRejectModal = false }) { Text("Cancel") } }
        )
    }

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().background(DarkCardBg).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, null, tint = HeadingText) }
            AvatarBox(instructor.name, 40.dp); Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) { Text(instructor.name, fontWeight = FontWeight.Bold, color = HeadingText); Text("Instructor", fontSize = 12.sp, color = BodyText) }
            StatusBadge(if (instructor.isApproved) "Approved" else "Pending")
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Stats row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniStat("Courses", "${instructorCourses.size}", Modifier.weight(1f))
                MiniStat("Earned", "₹$totalEarned", Modifier.weight(1f))
                MiniStat("Commission", "${instructor.commissionRate}%", Modifier.weight(1f))
            }
            Spacer(Modifier.height(20.dp))

            // Approval actions
            if (!instructor.isApproved && instructor.isActive) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { viewModel.adminApproveInstructor(instructor.email); context.showToast("Approved!") }, colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary), modifier = Modifier.weight(1f)) { Text("✓ Approve") }
                    Button(onClick = { showRejectModal = true }, colors = ButtonDefaults.buttonColors(containerColor = RedDanger), modifier = Modifier.weight(1f)) { Text("✗ Reject") }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Commission override
            Text("Commission Rate", fontWeight = FontWeight.Bold, color = HeadingText)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                EduTextField("Rate %", commRate, isNumber = true, modifier = Modifier.weight(1f)) { commRate = it }
                Button(onClick = { viewModel.adminSetCommissionRate(instructor.email, commRate.toIntOrNull() ?: 30); context.showToast("Rate set to $commRate%") }, colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)) { Text("Update") }
            }
            Spacer(Modifier.height(16.dp))

            // Feature toggle
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkCardBg).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Feature on Homepage", fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("Show in Top Instructors section", color = BodyText, fontSize = 11.sp) }
                Switch(checked = instructor.isFeatured, onCheckedChange = { viewModel.adminFeatureInstructor(instructor.email, it) }, colors = SwitchDefaults.colors(checkedThumbColor = AmberWarning))
            }
            Spacer(Modifier.height(16.dp))

            // Courses by this instructor
            Text("Courses (${instructorCourses.size})", fontWeight = FontWeight.Bold, color = HeadingText)
            Spacer(Modifier.height(8.dp))
            instructorCourses.forEach { course ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(10.dp)).background(DarkCardBg).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Book, null, tint = IndigoPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) { Text(course.title, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1); Text("${course.enrolledCount} students", fontSize = 10.sp, color = BodyText) }
                    StatusBadge(course.status)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Danger actions
            Text("Account Actions", fontWeight = FontWeight.Bold, color = RedDanger)
            Spacer(Modifier.height(8.dp))
            AdminActionButton(if (instructor.isActive) "Suspend Instructor" else "Unsuspend Instructor", if (instructor.isActive) AmberWarning else EmeraldSecondary, Icons.Default.Block) {
                viewModel.adminSuspendUser(instructor.email, instructor.isActive)
                context.showToast(if (instructor.isActive) "Instructor suspended" else "Instructor restored")
            }
        }
    }
}

@Composable
fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HeadingText)
            Text(label, fontSize = 11.sp, color = BodyText)
        }
    }
}

// ─── REVENUE & PAYOUTS ───────────────────────────────────

@Composable
fun AdminRevenueScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val payouts by viewModel.allPayoutsList.collectAsState()
    val pending = payouts.filter { it.status == "Pending" }
    val paid = payouts.filter { it.status == "Paid" }
    val totalPaid = paid.sumOf { it.amount }
    val totalPending = pending.sumOf { it.amount }

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionHeader("Revenue & Payouts", "Financial control center") }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KPICard("Total Paid Out", "₹${totalPaid.formatNum()}", EmeraldSecondary, Icons.Default.CheckCircle, Modifier.weight(1f))
                KPICard("Pending Payouts", "₹${totalPending.formatNum()}", AmberWarning, Icons.Default.Pending, Modifier.weight(1f))
            }
        }

        item { Text("Commission: 30% platform | 70% instructor", fontSize = 12.sp, color = BodyText) }

        item {
            Text("Pending Requests", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
            Spacer(Modifier.height(8.dp))
            if (pending.isEmpty()) Text("No pending payouts.", color = MutedText, fontSize = 13.sp)
        }

        items(pending) { payout ->
            Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(payout.instructorId, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Requested: ₹${payout.amount}", color = AmberWarning, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(formatTimestamp(payout.requestedAt), fontSize = 10.sp, color = MutedText)
                    }
                    Button(onClick = { viewModel.adminProcessPayout(payout.id); context.showToast("Payout processed!") }, colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary)) { Text("Pay ₹${payout.amount}") }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("Payout History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
            Spacer(Modifier.height(8.dp))
        }

        items(paid) { payout ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(DarkCardBg).border(1.dp, CardBorderColor, RoundedCornerShape(10.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(payout.instructorId, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("₹${payout.amount} • ${payout.transactionId}", fontSize = 11.sp, color = BodyText)
                    Text(formatTimestamp(payout.processedAt), fontSize = 10.sp, color = MutedText)
                }
                StatusBadge("Paid")
            }
        }
    }
}
