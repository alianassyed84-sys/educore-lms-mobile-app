package com.example.ui.instructor

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
import kotlinx.coroutines.launch

@Composable
fun InstructorCoursesScreen(viewModel: MainViewModel, user: UserEntity, onEditCourse: (CourseEntity) -> Unit) {
    val context = LocalContext.current
    val courses by viewModel.allCoursesList.collectAsState()
    val myCourses = remember(courses) { courses.filter { it.instructorId == user.email } }
    var statusFilter by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }
    var confirmDeleteId by remember { mutableStateOf<Int?>(null) }

    val filtered = remember(myCourses, statusFilter, search) {
        myCourses.filter { c ->
            (statusFilter == "All" || c.status == statusFilter) &&
            (search.isEmpty() || c.title.contains(search, true))
        }
    }

    if (confirmDeleteId != null) {
        AlertDialog(onDismissRequest = { confirmDeleteId = null }, containerColor = DarkCardBg,
            title = { Text("Delete Course?", color = HeadingText, fontWeight = FontWeight.Bold) },
            text = { Text("This cannot be undone. All enrolled students will lose access.", color = BodyText) },
            confirmButton = { Button(onClick = { viewModel.adminDeleteCourse(confirmDeleteId!!); confirmDeleteId = null; context.showToast("Course deleted") }, colors = ButtonDefaults.buttonColors(containerColor = RedDanger)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDeleteId = null }) { Text("Cancel", color = BodyText) } })
    }

    Column(Modifier.fillMaxSize()) {
        // Search
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = search, onValueChange = { search = it }, placeholder = { Text("Search courses…") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = BodyText) }, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor))
        }
        // Filter tabs
        ScrollableTabRow(selectedTabIndex = listOf("All","Published","Draft","Pending","Unpublished","Archived").indexOf(statusFilter).coerceAtLeast(0),
            containerColor = DarkCardBg, contentColor = IndigoPrimary, edgePadding = 8.dp) {
            listOf("All","Published","Draft","Pending","Unpublished","Archived").forEach { s ->
                Tab(selected = statusFilter == s, onClick = { statusFilter = s }, text = { Text(s, fontSize = 11.sp) })
            }
        }
        Spacer(Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LibraryBooks, null, tint = MutedText, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No courses found", color = MutedText, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered) { course ->
                    CourseInstructorCard(course = course, onEdit = { onEditCourse(course) }, onDelete = { confirmDeleteId = course.id }, onPublish = { viewModel.adminModerateCourse(course.id, true) }, viewModel = viewModel)
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
fun CourseInstructorCard(course: CourseEntity, onEdit: () -> Unit, onDelete: () -> Unit, onPublish: () -> Unit, viewModel: MainViewModel) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
        Column {
            // Banner
            Box(Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)).background(
                when(course.category) { "Coding","Python","Data Science" -> IndigoPrimary; "Design" -> EmeraldSecondary; else -> AmberWarning }
            ), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayCircle, null, tint = Color.White.copy(0.5f), modifier = Modifier.size(48.dp))
                // Status badge
                Box(Modifier.align(Alignment.TopEnd).padding(10.dp).clip(RoundedCornerShape(50.dp)).background(DarkCardBg).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    val statusColor = when(course.status) { "Published" -> EmeraldSecondary; "Pending" -> AmberWarning; "Rejected" -> RedDanger; else -> MutedText }
                    Text(course.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                if (course.isFeatured) {
                    Box(Modifier.align(Alignment.TopStart).padding(10.dp).clip(RoundedCornerShape(50.dp)).background(AmberWarning).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("Featured", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(Modifier.padding(14.dp)) {
                Text(course.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2)
                Text("${course.category} • ${course.difficulty}", fontSize = 11.sp, color = BodyText)
                Spacer(Modifier.height(10.dp))
                // Stats
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatChip(Icons.Default.Group, "${course.enrolledCount} students", IndigoPrimary)
                    StatChip(Icons.Default.Star, "${course.rating} ★", AmberWarning)
                    StatChip(Icons.Default.CurrencyRupee, if(course.price==0) "Free" else "₹${course.price}", EmeraldSecondary)
                }
                Spacer(Modifier.height(12.dp))
                // Action buttons
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, CardBorderColor)) {
                        Icon(Icons.Default.Edit, null, tint = IndigoPrimary, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Edit", fontSize = 12.sp, color = HeadingText)
                    }
                    OutlinedButton(onClick = { context.showToast("Analytics: ${course.enrolledCount} students enrolled") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, CardBorderColor)) {
                        Icon(Icons.Default.BarChart, null, tint = AmberWarning, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Stats", fontSize = 12.sp, color = HeadingText)
                    }
                    Box {
                        OutlinedButton(onClick = { showMenu = true }, modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, CardBorderColor), contentPadding = PaddingValues(0.dp)) {
                            Icon(Icons.Default.MoreVert, null, tint = BodyText, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Preview as Student") }, onClick = { showMenu = false; context.showToast("Preview mode") }, leadingIcon = { Icon(Icons.Default.Visibility, null) })
                            DropdownMenuItem(text = { Text("Duplicate Course") }, onClick = { showMenu = false; context.showToast("Course duplicated!") }, leadingIcon = { Icon(Icons.Default.ContentCopy, null) })
                            if (course.status == "Published") {
                                DropdownMenuItem(text = { Text("Unpublish", color = AmberWarning) }, onClick = { showMenu = false; viewModel.adminUpdateCourse(course.copy(status = "Unpublished"), course.status) }, leadingIcon = { Icon(Icons.Default.VisibilityOff, null, tint = AmberWarning) })
                            }
                            DropdownMenuItem(text = { Text("Archive", color = MutedText) }, onClick = { showMenu = false; viewModel.adminUpdateCourse(course.copy(status = "Archived"), course.status) }, leadingIcon = { Icon(Icons.Default.Archive, null, tint = MutedText) })
                            DropdownMenuItem(text = { Text("Delete", color = RedDanger) }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = RedDanger) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(3.dp))
        Text(label, fontSize = 11.sp, color = BodyText)
    }
}
