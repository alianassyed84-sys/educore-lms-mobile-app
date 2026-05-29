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

@Composable
fun AdminUserControlScreen(viewModel: MainViewModel) {
    val allUsers by viewModel.allUsersList.collectAsState()
    var search by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf("All") }
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }

    val filtered = remember(allUsers, search, roleFilter) {
        allUsers.filter { u ->
            val matchSearch = search.isEmpty() || u.name.contains(search, true) || u.email.contains(search, true)
            val matchRole = roleFilter == "All" || u.role == roleFilter ||
                (roleFilter == "Suspended" && !u.isActive) || (roleFilter == "Banned" && u.isBanned) ||
                (roleFilter == "Pro" && u.subscription == "Pro") || (roleFilter == "Free" && u.subscription == "Free")
            matchSearch && matchRole
        }
    }

    if (selectedUser != null) {
        AdminUserDetailSheet(user = selectedUser!!, viewModel = viewModel, onDismiss = { selectedUser = null })
        return
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("User Control", "${allUsers.size} registered users")
        EduTextField("Search by name or email…", search) { search = it }
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("All","Learner","Instructor","Admin","Pro","Free","Suspended","Banned")) { f ->
                FilterChip(f, roleFilter == f) { roleFilter = f }
            }
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { user ->
                UserAdminRow(user = user, onClick = { selectedUser = user })
            }
        }
    }
}

@Composable
fun UserAdminRow(user: UserEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkCardBg)
            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarBox(user.name)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HeadingText)
            Text(user.email, fontSize = 11.sp, color = BodyText)
            Text("Joined ${formatTimestamp(user.createdAt)}", fontSize = 10.sp, color = MutedText)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RoleBadge(user.role)
            StatusBadge(if (user.isBanned) "Banned" else if (!user.isActive) "Suspended" else "Active")
        }
    }
}

@Composable
fun RoleBadge(role: String) {
    val color = when(role) { "Admin" -> RedDanger; "Instructor" -> AmberWarning; else -> IndigoPrimary }
    Box(Modifier.clip(RoundedCornerShape(50.dp)).background(color.copy(0.15f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
        Text(role, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AdminUserDetailSheet(user: UserEntity, viewModel: MainViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Profile","Activity","Admin Actions")
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showBanConfirm by remember { mutableStateOf(false) }
    var proGrantDays by remember { mutableStateOf("30") }
    var commissionRate by remember { mutableStateOf(user.commissionRate.toString()) }

    val currentUserState by viewModel.currentUser.collectAsState()
    val enrollments by viewModel.enrollmentDao.getEnrollmentsForUserFlow(user.email).collectAsState(emptyList())
    val allCourses by viewModel.allCoursesList.collectAsState()

    if (showDeleteConfirm) {
        ConfirmDialog("Delete Account", "Permanently delete ${user.name}? Type confirms.", "Delete Forever", RedDanger,
            onConfirm = { viewModel.adminDeleteUser(user.email); showDeleteConfirm = false; onDismiss() },
            onDismiss = { showDeleteConfirm = false })
    }
    if (showBanConfirm) {
        ConfirmDialog("Ban Account", "Permanently ban ${user.name}? They cannot login again.", "Ban Permanently", RedDanger,
            onConfirm = { viewModel.adminBanUser(user.email); showBanConfirm = false; onDismiss() },
            onDismiss = { showBanConfirm = false })
    }

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().background(DarkCardBg).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, null, tint = HeadingText) }
            Spacer(Modifier.width(8.dp))
            AvatarBox(user.name, 40.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) { Text(user.name, fontWeight = FontWeight.Bold, color = HeadingText); Text(user.role, fontSize = 12.sp, color = BodyText) }
        }

        TabRow(selectedTabIndex = selectedTab, containerColor = DarkCardBg, contentColor = IndigoPrimary) {
            tabs.forEachIndexed { i, t -> Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(t, fontSize = 12.sp) }) }
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            when (selectedTab) {
                // TAB 0 — Profile
                0 -> {
                    InfoRow("Name", user.name)
                    InfoRow("Email", user.email)
                    InfoRow("Role", user.role)
                    InfoRow("Plan", "${user.subscription}${if(user.subscription=="Pro") " (expires ${formatTimestamp(user.proExpiryAt)})" else ""}")
                    InfoRow("Status", if(user.isBanned) "BANNED" else if(!user.isActive) "Suspended" else "Active")
                    InfoRow("Joined", formatTimestamp(user.createdAt))
                    InfoRow("Last Active", formatTimestamp(user.lastActiveAt))
                    InfoRow("XP", "${user.xp} XP • ${user.streakCount} day streak")
                    InfoRow("Badges", if(user.badges.isEmpty()) "None" else user.badges.replace(",", ", "))
                    if (user.role == "Instructor") {
                        Spacer(Modifier.height(16.dp))
                        Text("Commission Rate Override", fontWeight = FontWeight.Bold, color = HeadingText)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            EduTextField("Commission %", commissionRate, isNumber = true, modifier = Modifier.weight(1f)) { commissionRate = it }
                            Button(onClick = { viewModel.adminSetCommissionRate(user.email, commissionRate.toIntOrNull() ?: 30); context.showToast("Commission set to $commissionRate%") }, colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)) { Text("Set") }
                        }
                    }
                }
                // TAB 1 — Activity
                1 -> {
                    Text("Enrolled Courses (${enrollments.size})", fontWeight = FontWeight.Bold, color = HeadingText)
                    Spacer(Modifier.height(8.dp))
                    if (enrollments.isEmpty()) Text("No enrollments.", color = MutedText, fontSize = 13.sp)
                    enrollments.forEach { enrollment ->
                        val course = allCourses.firstOrNull { it.id == enrollment.courseId }
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(10.dp)).background(DarkCardBg).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Book, null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(course?.title ?: "Course #${enrollment.courseId}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("${enrollment.progress}% • ${if(enrollment.isCompleted)"Completed" else "In Progress"}", fontSize = 10.sp, color = BodyText)
                            }
                            if (enrollment.certificateGranted) Icon(Icons.Default.Verified, null, tint = EmeraldSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Login History", fontWeight = FontWeight.Bold, color = HeadingText)
                    Spacer(Modifier.height(8.dp))
                    listOf("Android App • 192.168.1.1 • Today","Android App • 10.0.0.2 • Yesterday","Android App • 192.168.1.1 • 3 days ago").forEach {
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Icon(Icons.Default.PhoneAndroid, null, tint = MutedText, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(it, fontSize = 11.sp, color = BodyText)
                        }
                    }
                }
                // TAB 2 — Admin Actions
                2 -> {
                    Text("Access Control", fontWeight = FontWeight.Bold, color = HeadingText)
                    Spacer(Modifier.height(10.dp))
                    AdminActionButton("Grant Pro Access ($proGrantDays days)", EmeraldSecondary, Icons.Default.Star) { viewModel.adminGrantPro(user.email, proGrantDays.toIntOrNull() ?: 30); context.showToast("Pro granted!") }
                    EduTextField("Pro Grant Days", proGrantDays, isNumber = true) { proGrantDays = it }
                    Spacer(Modifier.height(6.dp))
                    AdminActionButton("Revoke Pro Access", AmberWarning, Icons.Default.RemoveCircle) { viewModel.adminRevokePro(user.email); context.showToast("Pro revoked") }
                    Spacer(Modifier.height(6.dp))
                    EduDropdown("Change Role", user.role, listOf("Learner","Instructor","Admin")) { newRole ->
                        viewModel.adminChangeUserRole(user.email, newRole); context.showToast("Role changed to $newRole")
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Account Actions", fontWeight = FontWeight.Bold, color = HeadingText)
                    Spacer(Modifier.height(10.dp))
                    AdminActionButton(if (user.isActive) "Suspend Account" else "Unsuspend Account", if (user.isActive) AmberWarning else EmeraldSecondary, Icons.Default.Block) {
                        viewModel.adminSuspendUser(user.email, user.isActive); context.showToast(if(user.isActive) "Account suspended" else "Account restored")
                    }
                    Spacer(Modifier.height(6.dp))
                    AdminActionButton("Impersonate User", IndigoPrimary, Icons.Default.Visibility) { viewModel.adminImpersonateUser(user.email); context.showToast("Now viewing as ${user.name}"); onDismiss() }
                    Spacer(Modifier.height(6.dp))
                    AdminActionButton("Send Direct Notification", IndigoPrimary, Icons.Default.Send) { viewModel.adminSendNotification("Admin Message", "Message from admin.", user.email, "In-App", listOf(user)); context.showToast("Message sent") }
                    Spacer(Modifier.height(16.dp))
                    Text("Danger Zone", fontWeight = FontWeight.Bold, color = RedDanger)
                    Spacer(Modifier.height(10.dp))
                    AdminActionButton("Ban Account Permanently", RedDanger, Icons.Default.GppBad) { showBanConfirm = true }
                    Spacer(Modifier.height(6.dp))
                    AdminActionButton("Delete Account Forever", RedDanger, Icons.Default.DeleteForever) { showDeleteConfirm = true }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = BodyText)
        Text(value, fontSize = 13.sp, color = HeadingText, fontWeight = FontWeight.Medium)
    }
    Divider(color = CardBorderColor)
}

@Composable
fun AdminActionButton(label: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(46.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, color)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
    Spacer(Modifier.height(2.dp))
}
