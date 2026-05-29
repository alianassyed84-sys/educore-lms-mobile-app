package com.example.ui.admin

import android.widget.Toast
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminCourseControlScreen(viewModel: MainViewModel) {
    val courses by viewModel.allCoursesList.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }
    var selectedCourse by remember { mutableStateOf<CourseEntity?>(null) }

    val filtered = remember(courses, searchQuery, statusFilter) {
        courses.filter { c ->
            (statusFilter == "All" || c.status == statusFilter) &&
            (searchQuery.isEmpty() || c.title.contains(searchQuery, true) || c.instructorName.contains(searchQuery, true))
        }
    }

    if (selectedCourse != null) {
        AdminCourseEditSheet(course = selectedCourse!!, viewModel = viewModel, onDismiss = { selectedCourse = null })
        return
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Course Control", "${courses.size} courses on platform")

        OutlinedTextField(
            value = searchQuery, onValueChange = { searchQuery = it },
            placeholder = { Text("Search courses or instructors…") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = BodyText) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor)
        )
        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("All","Pending","Published","Unpublished","Rejected","Draft","Archived")) { s ->
                FilterChip(s, statusFilter == s) { statusFilter = s }
            }
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { course ->
                CourseAdminRow(course = course, onEdit = { selectedCourse = course }, onDelete = { viewModel.adminDeleteCourse(course.id) }, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CourseAdminRow(course: CourseEntity, onEdit: () -> Unit, onDelete: () -> Unit, viewModel: MainViewModel) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        ConfirmDialog("Delete Course", "Delete '${course.title}'? This cannot be undone.", "Delete", RedDanger,
            onConfirm = { onDelete(); showDeleteConfirm = false }, onDismiss = { showDeleteConfirm = false })
    }

    Card(modifier = Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp, 42.dp).clip(RoundedCornerShape(8.dp)).background(
                when(course.category) { "Coding" -> IndigoPrimary; "Design" -> EmeraldSecondary; else -> AmberWarning }
            ), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Book, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(course.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HeadingText, maxLines = 1)
                Text("${course.instructorName} • ${course.category} • ${if(course.price==0)"Free" else "₹${course.price}"}", fontSize = 11.sp, color = BodyText)
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusBadge(course.status)
                    if (course.isFeatured) Box(Modifier.clip(RoundedCornerShape(50.dp)).background(AmberWarning.copy(0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("Featured", color = AmberWarning, fontSize = 9.sp) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, tint = IndigoPrimary, modifier = Modifier.size(16.dp)) }
                IconButton(onClick = { viewModel.adminSetCourseFeatured(course.id, !course.isFeatured); context.showToast(if(!course.isFeatured) "Featured!" else "Unfeatured") }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Star, null, tint = if(course.isFeatured) AmberWarning else MutedText, modifier = Modifier.size(16.dp)) }
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = RedDanger, modifier = Modifier.size(16.dp)) }
            }
        }
    }
}

@Composable
fun AdminCourseEditSheet(course: CourseEntity, viewModel: MainViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Basic Info","Instructor","Pricing","Curriculum","Reviews","Enrollments")

    var title by remember { mutableStateOf(course.title) }
    var description by remember { mutableStateOf(course.description) }
    var category by remember { mutableStateOf(course.category) }
    var difficulty by remember { mutableStateOf(course.difficulty) }
    var status by remember { mutableStateOf(course.status) }
    var price by remember { mutableStateOf(course.price.toString()) }
    var isFeatured by remember { mutableStateOf(course.isFeatured) }

    val lessons by viewModel.lessonDao.getLessonsForCourseFlow(course.id).collectAsState(emptyList())
    val reviews by viewModel.reviewDao.getReviewsForCourseFlow(course.id).collectAsState(emptyList())
    val enrollments by viewModel.enrollmentDao.getEnrollmentsForCourseFlow(course.id).collectAsState(emptyList())
    val allUsers by viewModel.allUsersList.collectAsState()
    val instructors = remember(allUsers) { allUsers.filter { it.role == "Instructor" && it.isApproved } }

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        // Top bar
        Row(Modifier.fillMaxWidth().statusBarsPadding().background(DarkCardBg).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, null, tint = HeadingText) }
            Text("Edit: ${course.title}", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
            Button(onClick = {
                val updated = course.copy(title = title, description = description, category = category, difficulty = difficulty, status = status, price = price.toIntOrNull() ?: course.price, isFeatured = isFeatured)
                scope.launch { viewModel.adminUpdateCourse(updated, course.status); context.showToast("Course saved!") }
            }, colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary), modifier = Modifier.height(36.dp)) { Text("Save", fontSize = 13.sp) }
        }

        ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = DarkCardBg, contentColor = IndigoPrimary, edgePadding = 8.dp) {
            tabs.forEachIndexed { i, t -> Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(t, fontSize = 12.sp) }) }
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            when (selectedTab) {
                // TAB 0 — Basic Info
                0 -> {
                    EduTextField("Course Title", title) { title = it }
                    Spacer(Modifier.height(12.dp))
                    EduTextField("Description", description, maxLines = 4) { description = it }
                    Spacer(Modifier.height(12.dp))
                    EduDropdown("Category", category, listOf("Coding","Design","Business","Data Science","Cloud","DevOps","Marketing")) { category = it }
                    Spacer(Modifier.height(12.dp))
                    EduDropdown("Difficulty", difficulty, listOf("Beginner","Intermediate","Advanced","Expert")) { difficulty = it }
                    Spacer(Modifier.height(12.dp))
                    EduDropdown("Status (Force Set)", status, listOf("Draft","Pending","Published","Unpublished","Archived","Rejected")) { status = it }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkCardBg).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column { Text("Feature on Homepage", fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("Shown in Featured section", color = BodyText, fontSize = 11.sp) }
                        Switch(checked = isFeatured, onCheckedChange = { isFeatured = it }, colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary))
                    }
                }
                // TAB 1 — Instructor
                1 -> {
                    Text("Current Instructor", fontWeight = FontWeight.Bold, color = HeadingText)
                    Spacer(Modifier.height(8.dp))
                    InstructorRow(course.instructorName, course.instructorId)
                    Spacer(Modifier.height(16.dp))
                    Text("Reassign to Another Instructor", fontWeight = FontWeight.Bold, color = HeadingText)
                    Spacer(Modifier.height(8.dp))
                    instructors.filter { it.email != course.instructorId }.forEach { inst ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(10.dp)).background(DarkCardBg).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AvatarBox(inst.name)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) { Text(inst.name, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(inst.email, fontSize = 11.sp, color = BodyText) }
                            OutlinedButton(onClick = { viewModel.adminReassignInstructor(course.id, inst.email, inst.name, course.instructorId); context.showToast("Reassigned to ${inst.name}") }, border = BorderStroke(1.dp, IndigoPrimary)) { Text("Assign", color = IndigoPrimary, fontSize = 12.sp) }
                        }
                    }
                }
                // TAB 2 — Pricing
                2 -> {
                    EduTextField("Price (₹)", price, isNumber = true) { price = it }
                    Spacer(Modifier.height(12.dp))
                    Text("Quick Actions", fontWeight = FontWeight.Bold, color = HeadingText)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { price = "0"; context.showToast("Set to Free") }, colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary), modifier = Modifier.fillMaxWidth()) { Text("Make Free") }
                    Spacer(Modifier.height(8.dp))
                    Text("Coupons", fontWeight = FontWeight.Bold, color = HeadingText, modifier = Modifier.padding(top = 8.dp))
                    Spacer(Modifier.height(8.dp))
                    val allCoupons by viewModel.couponDao.getCouponsForCourseFlow(course.id).collectAsState(emptyList())
                    var newCode by remember { mutableStateOf("") }
                    var newDiscount by remember { mutableStateOf("") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EduTextField("Code", newCode, modifier = Modifier.weight(1f)) { newCode = it }
                        EduTextField("% Off", newDiscount, isNumber = true, modifier = Modifier.width(80.dp)) { newDiscount = it }
                        Button(onClick = { if(newCode.isNotEmpty()) { viewModel.adminCreateCoupon(newCode.uppercase(), newDiscount.toIntOrNull()?:10, 100, course.id); newCode=""; newDiscount="" } }, colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)) { Text("Add") }
                    }
                    Spacer(Modifier.height(8.dp))
                    allCoupons.forEach { c ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp).clip(RoundedCornerShape(8.dp)).background(DarkCardBg).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(c.code, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("${c.discountPercent}% • Used ${c.usedCount}/${c.maxUses}", fontSize = 11.sp, color = BodyText)
                            StatusBadge(if(c.isActive) "Active" else "Inactive")
                        }
                    }
                }
                // TAB 3 — Curriculum
                3 -> {
                    val sections = lessons.groupBy { it.sectionName }
                    sections.forEach { (sectionName, sectionLessons) ->
                        Text(sectionName, fontWeight = FontWeight.Bold, color = IndigoPrimary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 6.dp))
                        sectionLessons.forEach { lesson ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp).clip(RoundedCornerShape(10.dp)).background(DarkCardBg).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(when(lesson.type){"Video"->Icons.Default.PlayCircle;"Quiz"->Icons.Default.Quiz;else->Icons.Default.Article}, null, tint = IndigoPrimary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) { Text(lesson.title, fontSize = 12.sp, fontWeight = FontWeight.Medium); Text("${lesson.type} • ${lesson.duration}", fontSize = 10.sp, color = BodyText) }
                                if (lesson.isPreview) Box(Modifier.clip(RoundedCornerShape(50.dp)).background(EmeraldSecondary.copy(0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("Preview", color = EmeraldSecondary, fontSize = 9.sp) }
                                IconButton(onClick = { viewModel.adminDeleteLesson(lesson.id, course.id) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, null, tint = RedDanger, modifier = Modifier.size(14.dp)) }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    var newLessonTitle by remember { mutableStateOf("") }
                    var newLessonSection by remember { mutableStateOf("New Section") }
                    Text("Add New Lesson", fontWeight = FontWeight.Bold, color = HeadingText)
                    Spacer(Modifier.height(8.dp))
                    EduTextField("Section Name", newLessonSection) { newLessonSection = it }
                    Spacer(Modifier.height(8.dp))
                    EduTextField("Lesson Title", newLessonTitle) { newLessonTitle = it }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { if(newLessonTitle.isNotEmpty()) { viewModel.adminAddLesson(LessonEntity(courseId = course.id, sectionName = newLessonSection, title = newLessonTitle, type = "Video", duration = "10:00")); newLessonTitle="" } }, colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary), modifier = Modifier.fillMaxWidth()) { Text("+ Add Lesson") }
                }
                // TAB 4 — Reviews
                4 -> {
                    if (reviews.isEmpty()) { Text("No reviews yet.", color = MutedText, fontSize = 13.sp) }
                    reviews.forEach { review ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AvatarBox(review.userName, size = 30.dp)
                                        Spacer(Modifier.width(8.dp))
                                        Column { Text(review.userName, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text("⭐".repeat(review.rating), fontSize = 11.sp) }
                                    }
                                    StatusBadge(review.status)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(review.comment, fontSize = 12.sp, color = BodyText)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SmallButton("Approve", EmeraldSecondary) { viewModel.adminModerateReview(review.id, "Approved") }
                                    SmallButton("Hide", AmberWarning) { viewModel.adminModerateReview(review.id, "Hidden") }
                                    SmallButton("Delete", RedDanger) { viewModel.adminModerateReview(review.id, "Flagged") }
                                }
                            }
                        }
                    }
                }
                // TAB 5 — Enrollments
                5 -> {
                    Text("${enrollments.size} enrolled students", fontWeight = FontWeight.Bold, color = HeadingText)
                    Spacer(Modifier.height(12.dp))
                    var searchStudent by remember { mutableStateOf("") }
                    EduTextField("Search & Enroll Student by Email", searchStudent) { searchStudent = it }
                    Spacer(Modifier.height(8.dp))
                    val matchedUser = remember(searchStudent, allUsers) { allUsers.firstOrNull { it.email.contains(searchStudent, true) && searchStudent.length > 3 } }
                    if (matchedUser != null) {
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(DarkCardBg).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AvatarBox(matchedUser.name); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(matchedUser.name, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(matchedUser.email, fontSize = 11.sp, color = BodyText) }
                            Button(onClick = { viewModel.adminEnrollStudent(matchedUser.email, course.id); context.showToast("Enrolled ${matchedUser.name}") }, colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary), modifier = Modifier.height(34.dp)) { Text("Enroll", fontSize = 12.sp) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    enrollments.forEach { enrollment ->
                        val user = allUsers.firstOrNull { it.email == enrollment.userEmail }
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(10.dp)).background(DarkCardBg).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AvatarBox(user?.name ?: "?"); Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(user?.name ?: enrollment.userEmail, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                LinearProgressIndicator(progress = { enrollment.progress / 100f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).padding(top = 4.dp), color = IndigoPrimary, trackColor = CardBorderColor)
                                Text("${enrollment.progress}%", fontSize = 10.sp, color = BodyText)
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                SmallButton("Certificate", EmeraldSecondary) { viewModel.adminGrantCertificate(enrollment.userEmail, course.id) }
                                SmallButton("Remove", RedDanger) { viewModel.adminRemoveEnrollment(enrollment.userEmail, course.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstructorRow(name: String, email: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkCardBg).border(1.dp, IndigoPrimary, RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        AvatarBox(name); Spacer(Modifier.width(12.dp))
        Column { Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text(email, fontSize = 11.sp, color = BodyText) }
        Spacer(Modifier.weight(1f))
        StatusBadge("Current")
    }
}

@Composable
fun AvatarBox(name: String, size: Dp = 36.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(IndigoPrimary), contentAlignment = Alignment.Center) {
        Text(name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.4f).sp)
    }
}

@Composable
fun SmallButton(label: String, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), modifier = Modifier.height(28.dp)) { Text(label, fontSize = 10.sp) }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(50.dp)).background(if (selected) IndigoPrimary else DarkCardBg).border(1.dp, if (selected) IndigoPrimary else CardBorderColor, RoundedCornerShape(50.dp)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 6.dp)
    ) { Text(label, color = if (selected) Color.White else BodyText, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
}

@Composable
fun EduTextField(label: String, value: String, maxLines: Int = 1, isNumber: Boolean = false, modifier: Modifier = Modifier.fillMaxWidth(), onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, singleLine = maxLines == 1, maxLines = maxLines,
        modifier = modifier, shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor, focusedLabelColor = IndigoPrimary, unfocusedLabelColor = BodyText))
}

@Composable
fun EduDropdown(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(value = selected, onValueChange = {}, label = { Text(label) }, readOnly = true,
            trailingIcon = { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, modifier = Modifier.clickable { expanded = true }) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor))
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt -> DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(opt); expanded = false }) }
        }
    }
}
