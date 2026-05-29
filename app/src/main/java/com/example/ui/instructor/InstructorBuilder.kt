package com.example.ui.instructor

import com.example.util.showToast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import kotlinx.coroutines.delay

@Composable
fun InstructorCourseBuilderScreen(viewModel: MainViewModel, editCourse: CourseEntity?, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(1) } // 1-6
    var isSaving by remember { mutableStateOf(false) }

    // Step 1: Basics
    var title by remember { mutableStateOf(editCourse?.title ?: "") }
    var subtitle by remember { mutableStateOf(editCourse?.subtitle ?: "") }
    var category by remember { mutableStateOf(editCourse?.category ?: "Python") }
    var difficulty by remember { mutableStateOf(editCourse?.difficulty ?: "Beginner") }
    var language by remember { mutableStateOf(editCourse?.language ?: "English") }
    var tags by remember { mutableStateOf(editCourse?.tags ?: "") }

    // Step 2: Thumbnail & Intro
    var thumbnailUrl by remember { mutableStateOf(editCourse?.thumbnailUrl ?: "") }
    var introVideoUrl by remember { mutableStateOf(editCourse?.introVideoUrl ?: "") }

    // Step 3: Curriculum (simplified for UI logic)
    val lessons = remember { mutableStateListOf<LessonEntity>() }
    var draftLessonTitle by remember { mutableStateOf("") }
    var draftLessonType by remember { mutableStateOf("Video") }
    LaunchedEffect(editCourse) {
        if (editCourse != null) {
            viewModel.lessonDao.getLessonsForCourseFlow(editCourse.id).collect { lessons.clear(); lessons.addAll(it) }
        }
    }

    // Step 4: Pricing
    var isFree by remember { mutableStateOf(editCourse?.price == 0) }
    var priceStr by remember { mutableStateOf(if(editCourse?.price == 0) "" else editCourse?.price.toString()) }
    var discountActive by remember { mutableStateOf(editCourse?.discountedPrice != null) }
    var discountPriceStr by remember { mutableStateOf(editCourse?.discountedPrice?.toString() ?: "") }

    // Step 5: Settings
    var welcomeMsg by remember { mutableStateOf(editCourse?.welcomeMessage ?: "") }
    var congratsMsg by remember { mutableStateOf(editCourse?.congratulationsMessage ?: "") }
    var certEnabled by remember { mutableStateOf(editCourse?.certificateEnabled ?: true) }

    // Autosave Simulation
    LaunchedEffect(title, subtitle, category, difficulty, priceStr) {
        if (title.isNotEmpty()) {
            delay(30000) // 30 sec autosave
            isSaving = true; delay(1000); isSaving = false
            context.showToast("Draft saved")
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(Modifier.fillMaxWidth().statusBarsPadding().background(DarkCardBg).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (step > 1) step-- else onDismiss() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = HeadingText) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Step $step of 6", fontWeight = FontWeight.Bold, color = IndigoPrimary, fontSize = 14.sp)
                    Text(when(step){1->"Basics"; 2->"Media"; 3->"Curriculum"; 4->"Pricing"; 5->"Settings"; else->"Review"}, fontSize = 11.sp, color = BodyText)
                }
                if (isSaving) {
                    CircularProgressIndicator(color = IndigoPrimary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    TextButton(onClick = { onDismiss(); context.showToast("Draft saved") }) { Text("Save & Exit", color = BodyText, fontSize = 12.sp) }
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Progress Bar
            LinearProgressIndicator(progress = step / 6f, color = IndigoPrimary, trackColor = DarkCardBg, modifier = Modifier.fillMaxWidth().height(2.dp))
            
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (step) {
                    1 -> {
                        Text("Course Basics", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = HeadingText)
                        EduTextField("Course Title (max 100 chars)", title) { if (it.length <= 100) title = it }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (title.length < 10) "Too short" else if (title.length < 40) "Good" else "Perfect", color = if (title.length < 10) AmberWarning else EmeraldSecondary, fontSize = 10.sp)
                            Text("${title.length}/100", color = BodyText, fontSize = 10.sp)
                        }
                        EduTextField("Subtitle (max 200 chars)", subtitle, maxLines = 3) { if (it.length <= 200) subtitle = it }
                        EduDropdown("Category", category, listOf("Python", "Web Development", "Data Science", "Design", "Business", "Marketing")) { category = it }
                        Text("Difficulty Level", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Beginner", "Intermediate", "Advanced", "Expert").forEach { d ->
                                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if(difficulty==d) IndigoPrimary else DarkCardBg).border(1.dp, CardBorderColor, RoundedCornerShape(8.dp)).clickable { difficulty = d }.padding(horizontal=12.dp, vertical=8.dp)) { Text(d, color=Color.White, fontSize=12.sp) }
                            }
                        }
                        EduDropdown("Language", language, listOf("English", "Hindi", "Telugu", "Tamil", "Kannada", "Marathi")) { language = it }
                        EduTextField("Tags (comma separated)", tags) { tags = it }
                    }
                    2 -> {
                        Text("Course Media", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = HeadingText)
                        Text("Course Thumbnail (16:9)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        if (thumbnailUrl.isEmpty()) {
                            Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)).background(DarkCardBg).border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)).clickable { thumbnailUrl = "https://images.unsplash.com/photo-1498050108023-c5249f4df085" }, contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Image, null, tint = MutedText, modifier = Modifier.size(32.dp)); Spacer(Modifier.height(8.dp)); Text("Tap to Search Unsplash", color = BodyText, fontSize = 12.sp) }
                            }
                        } else {
                            Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)).background(EmeraldSecondary.copy(0.1f)), contentAlignment = Alignment.Center) { Text(thumbnailUrl, fontSize = 10.sp, color = EmeraldSecondary, modifier = Modifier.padding(16.dp)) }
                            TextButton(onClick = { thumbnailUrl = "" }) { Text("Remove Image", color = RedDanger) }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Promo Video URL (YouTube/Vimeo)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        EduTextField("e.g. https://youtu.be/...", introVideoUrl) { introVideoUrl = it }
                    }
                    3 -> {
                        Text("Build Curriculum", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = HeadingText)
                        Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                            Column(Modifier.padding(14.dp)) {
                                Text("Add Lesson", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                EduTextField("Lesson Title", draftLessonTitle) { draftLessonTitle = it }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Video", "Article", "Quiz").forEach { t -> Box(Modifier.clip(RoundedCornerShape(50.dp)).background(if(draftLessonType==t) IndigoPrimary else DarkBg).clickable{draftLessonType=t}.padding(horizontal=12.dp,vertical=6.dp)){Text(t,fontSize=11.sp,color=Color.White)} }
                                }
                                Spacer(Modifier.height(10.dp))
                                Button(onClick = { if(draftLessonTitle.isNotEmpty()) { lessons.add(LessonEntity(courseId = editCourse?.id ?: 0, sectionName = "Section 1", title = draftLessonTitle, type = draftLessonType, duration = "10:00")); draftLessonTitle = ""; context.showToast("Lesson added") } }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)) { Text("Add Lesson") }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Lessons (${lessons.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        lessons.forEachIndexed { i, l ->
                            Row(Modifier.fillMaxWidth().padding(vertical=4.dp).clip(RoundedCornerShape(8.dp)).background(DarkCardBg).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if(l.type=="Video") Icons.Default.PlayCircle else if(l.type=="Quiz") Icons.Default.Quiz else Icons.Default.Article, null, tint = IndigoPrimary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("${i+1}. ${l.title}", fontSize = 12.sp, color = HeadingText, modifier = Modifier.weight(1f))
                                IconButton(onClick = { lessons.removeAt(i) }, modifier=Modifier.size(20.dp)) { Icon(Icons.Default.Delete, null, tint = RedDanger, modifier=Modifier.size(14.dp)) }
                            }
                        }
                    }
                    4 -> {
                        Text("Pricing", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = HeadingText)
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkCardBg).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Make this course Free", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Switch(checked = isFree, onCheckedChange = { isFree = it }, colors = SwitchDefaults.colors(checkedThumbColor = EmeraldSecondary))
                        }
                        if (!isFree) {
                            Spacer(Modifier.height(16.dp))
                            EduTextField("Price (₹)", priceStr, isNumber = true) { priceStr = it }
                            Text("Suggested: ₹799 - ₹2,999", fontSize = 11.sp, color = MutedText)
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkCardBg).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Enable Discount", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Switch(checked = discountActive, onCheckedChange = { discountActive = it }, colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary))
                            }
                            if (discountActive) {
                                Spacer(Modifier.height(8.dp))
                                EduTextField("Discounted Price (₹)", discountPriceStr, isNumber = true) { discountPriceStr = it }
                            }
                            Spacer(Modifier.height(16.dp))
                            val p = (priceStr.toIntOrNull() ?: 0)
                            val finalPrice = if (discountActive && discountPriceStr.isNotEmpty()) discountPriceStr.toIntOrNull() ?: p else p
                            if (finalPrice > 0) {
                                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = EmeraldSecondary.copy(0.1f))) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text("Earnings Preview (per 100 students)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldSecondary)
                                        Spacer(Modifier.height(8.dp))
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Gross Revenue:", fontSize=12.sp, color=BodyText); Text("₹${finalPrice * 100}", fontWeight=FontWeight.Bold, color=HeadingText) }
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Platform Fee (30%):", fontSize=12.sp, color=BodyText); Text("₹${(finalPrice * 100 * 0.3).toInt()}", fontWeight=FontWeight.Bold, color=RedDanger) }
                                        Divider(Modifier.padding(vertical = 8.dp), color = CardBorderColor)
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Your Share (70%):", fontSize=12.sp, color=HeadingText); Text("₹${(finalPrice * 100 * 0.7).toInt()}", fontWeight=FontWeight.Bold, color=EmeraldSecondary, fontSize = 16.sp) }
                                    }
                                }
                            }
                        }
                    }
                    5 -> {
                        Text("Course Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = HeadingText)
                        EduTextField("Welcome Message (sent to students upon enrollment)", welcomeMsg, maxLines = 3) { welcomeMsg = it }
                        Spacer(Modifier.height(12.dp))
                        EduTextField("Congratulations Message (upon completion)", congratsMsg, maxLines = 3) { congratsMsg = it }
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkCardBg).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Enable Completion Certificate", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Switch(checked = certEnabled, onCheckedChange = { certEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = EmeraldSecondary))
                        }
                    }
                    6 -> {
                        Text("Review & Submit", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = HeadingText)
                        Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("$category • $difficulty • $language", fontSize = 12.sp, color = BodyText)
                                Text("Price: ${if(isFree) "Free" else "₹$priceStr"}", fontSize = 14.sp, color = EmeraldSecondary, fontWeight = FontWeight.Bold)
                                Text("${lessons.size} Lessons included", fontSize = 12.sp, color = BodyText)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Publishing Checklist", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        ChecklistIndicator("Title & category added", title.isNotEmpty() && category.isNotEmpty())
                        ChecklistIndicator("Thumbnail provided", thumbnailUrl.isNotEmpty())
                        ChecklistIndicator("At least 1 lesson added", lessons.isNotEmpty())
                        ChecklistIndicator("Price configured", isFree || priceStr.isNotEmpty())
                    }
                }
            }
            
            // Bottom Bar
            Row(Modifier.fillMaxWidth().background(DarkCardBg).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                if (step > 1) {
                    OutlinedButton(onClick = { step-- }, border = BorderStroke(1.dp, CardBorderColor)) { Text("Back", color = HeadingText) }
                } else { Spacer(Modifier.width(1.dp)) }

                val canProceed = when(step) { 1 -> title.isNotEmpty(); 2 -> true; 3 -> lessons.isNotEmpty(); 4 -> isFree || priceStr.isNotEmpty(); else -> true }

                Button(onClick = {
                    if (step < 6) {
                        step++
                    } else {
                        val p = if (isFree) 0 else priceStr.toIntOrNull() ?: 0
                        val dp = if (!isFree && discountActive) discountPriceStr.toIntOrNull() else null
                        viewModel.publishCourseByInstructor(title, subtitle, category, difficulty, p) // Simplified for prototype
                        context.showToast("Course submitted for review!")
                        onDismiss()
                    }
                }, enabled = canProceed, colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)) { Text(if (step == 6) "Submit for Review" else "Next Step") }
            }
        }
    }
}
