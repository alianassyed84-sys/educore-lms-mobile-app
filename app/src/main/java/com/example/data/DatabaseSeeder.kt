package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {
    private fun hashPassword(password: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun seedIfNeeded(context: Context, database: AppDatabase) {
        withContext(Dispatchers.IO) {
            val userDao = database.userDao()
            val existingAdmin = userDao.getUserByEmail("admin@educore.saas")
            if (existingAdmin == null) {
                // 1. Seed Users (Admin, Instructor, Student, Pending Instructor)
                userDao.insertUser(
                    UserEntity(
                        email = "admin@educore.saas",
                        name = "Admin Chief",
                        passwordHash = hashPassword("admin123"),
                        role = "Admin",
                        isVerified = true,
                        isApproved = true,
                        subscription = "Pro"
                    )
                )

                userDao.insertUser(
                    UserEntity(
                        email = "instructor@educore.saas",
                        name = "Dev Kar",
                        passwordHash = hashPassword("instructor123"),
                        role = "Instructor",
                        isVerified = true,
                        isApproved = true,
                        subscription = "Free"
                    )
                )

                userDao.insertUser(
                    UserEntity(
                        email = "student@educore.saas",
                        name = "Rohit Sharma",
                        passwordHash = hashPassword("student123"),
                        role = "Learner",
                        isVerified = true,
                        isApproved = false,
                        subscription = "Free",
                        streakDays = "4/7",
                        streakCount = 4,
                        xp = 350
                    )
                )

                // Pending Instructor for Admin Approvals panel
                userDao.insertUser(
                    UserEntity(
                        email = "alex@educore.saas",
                        name = "Alex Mercer",
                        passwordHash = hashPassword("alex123"),
                        role = "Instructor",
                        isVerified = true,
                        isApproved = false,
                        subscription = "Free"
                    )
                )

                userDao.insertUser(
                    UserEntity(
                        email = "sarah@educore.saas",
                        name = "Sarah Hughes",
                        passwordHash = hashPassword("sarah123"),
                        role = "Instructor",
                        isVerified = true,
                        isApproved = true,
                        subscription = "Free"
                    )
                )

                // 2. Seed Courses
                val courseDao = database.courseDao()
                val course1Id = courseDao.insertCourse(
                    CourseEntity(
                        id = 1,
                        title = "Mastering Python & AI Development",
                        description = "Learn Python programming from scratch and build robust AI models. Topics include loops, modules, neural networks, and model deployment.",
                        instructorId = "instructor@educore.saas",
                        instructorName = "Dev Kar",
                        category = "Coding",
                        price = 0, // Free
                        status = "Approved",
                        thumbnail = "python_ai",
                        rating = 4.8f,
                        reviewCount = 5,
                        enrolledCount = 280
                    )
                ).toInt()

                val course2Id = courseDao.insertCourse(
                    CourseEntity(
                        id = 2,
                        title = "SaaS Product & UI Design Masterclass",
                        description = "Design award-winning interfaces with proper spatial layout, visual contrast, typography hierarchies, and component library setups in Figma.",
                        instructorId = "sarah@educore.saas",
                        instructorName = "Sarah Hughes",
                        category = "Design",
                        price = 1499, // ₹1,499
                        status = "Approved",
                        thumbnail = "ui_design",
                        rating = 4.9f,
                        reviewCount = 3,
                        enrolledCount = 120
                    )
                ).toInt()

                val course3Id = courseDao.insertCourse(
                    CourseEntity(
                        id = 3,
                        title = "DevOps Pipeline Crash Course (Docker & AWS)",
                        description = "Master continuous deployment pipelines with Docker, Kubernetes, and AWS EC2 cloud setups. Practice with simulated YAML files.",
                        instructorId = "instructor@educore.saas",
                        instructorName = "Dev Kar",
                        category = "DevOps",
                        price = 2499, // ₹2,499
                        status = "Pending",
                        thumbnail = "devops",
                        rating = 4.5f,
                        reviewCount = 0,
                        enrolledCount = 0
                    )
                ).toInt()

                // 3. Seed Lessons for Courses
                val lessonDao = database.lessonDao()
                lessonDao.insertLessons(
                    listOf(
                        LessonEntity(
                            courseId = course1Id,
                            sectionName = "Section 1: Setup",
                            title = "Python Basics & Hello World",
                            type = "Video",
                            duration = "08:45",
                            videoUrl = "https://example.com/python1",
                            isPreview = true
                        ),
                        LessonEntity(
                            courseId = course1Id,
                            sectionName = "Section 1: Setup",
                            title = "Variables and Data Types",
                            type = "Video",
                            duration = "12:15",
                            videoUrl = "https://example.com/python2",
                            isPreview = false
                        ),
                        LessonEntity(
                            courseId = course1Id,
                            sectionName = "Section 2: Core Flow",
                            title = "Writing Your First Loop",
                            type = "Quiz",
                            duration = "05:00",
                            videoUrl = "",
                            isPreview = false
                        ),
                        LessonEntity(
                            courseId = course1Id,
                            sectionName = "Section 2: Core Flow",
                            title = "Introduction to NumPy Data Structures",
                            type = "Video",
                            duration = "14:20",
                            videoUrl = "https://example.com/python3",
                            isPreview = false
                        ),
                        LessonEntity(
                            courseId = course1Id,
                            sectionName = "Section 3: AI Models",
                            title = "Building & Testing a Simple Gradient Model",
                            type = "Video",
                            duration = "22:10",
                            videoUrl = "https://example.com/python4",
                            isPreview = false
                        )
                    )
                )

                lessonDao.insertLessons(
                    listOf(
                        LessonEntity(
                            courseId = course2Id,
                            sectionName = "Section 1: Foundations",
                            title = "Introduction to Figma Design Interfaces",
                            type = "Video",
                            duration = "10:30",
                            videoUrl = "https://example.com/figma1",
                            isPreview = true
                        ),
                        LessonEntity(
                            courseId = course2Id,
                            sectionName = "Section 1: Foundations",
                            title = "Visual Hierarchy, Alignment & Contrast Rules",
                            type = "Video",
                            duration = "15:45",
                            videoUrl = "https://example.com/figma2",
                            isPreview = false
                        ),
                        LessonEntity(
                            courseId = course2Id,
                            sectionName = "Section 2: Implementation",
                            title = "Color Theory & Contrast Accessibility",
                            type = "Article",
                            duration = "08:00",
                            videoUrl = "",
                            isPreview = false
                        ),
                        LessonEntity(
                            courseId = course2Id,
                            sectionName = "Section 2: Implementation",
                            title = "Designing a SaaS Landing Page Step-by-Step",
                            type = "Video",
                            duration = "25:30",
                            videoUrl = "https://example.com/figma3",
                            isPreview = false
                        )
                    )
                )

                // 4. Seed Reviews
                val reviewDao = database.reviewDao()
                reviewDao.insertReview(
                    ReviewEntity(
                        courseId = course1Id,
                        userEmail = "student@educore.saas",
                        userName = "Rohit Sharma",
                        rating = 5,
                        comment = "Outstanding content! It starts from the absolute fundamentals and goes into building complete neural networks step-by-step. Love the interactive exercises."
                    )
                )
                reviewDao.insertReview(
                    ReviewEntity(
                        courseId = course1Id,
                        userEmail = "sarah@educore.saas",
                        userName = "Sarah Hughes",
                        rating = 4,
                        comment = "Very well structured. The explanation of arrays and dataset vectorizations is highly accessible."
                    )
                )

                // 5. Seed Live Sessions
                val liveSessionDao = database.liveSessionDao()
                liveSessionDao.insertSession(
                    LiveSessionEntity(
                        topic = "AI & Machine Learning Q&A Session",
                        description = "Bring all your questions on linear regressions, multi-layer nodes, and building production-ready pipelines in Python.",
                        instructorId = "instructor@educore.saas",
                        instructorName = "Dev Kar",
                        scheduledAt = "2026-05-28 15:30",
                        duration = "1 Hour",
                        maxParticipants = 150,
                        status = "Upcoming",
                        enrolledCount = 45
                    )
                )
                liveSessionDao.insertSession(
                    LiveSessionEntity(
                        topic = "Designing Delightful Micro-Interactions",
                        description = "Learn how to build beautiful layout transitions and micro-animations to satisfy users.",
                        instructorId = "sarah@educore.saas",
                        instructorName = "Sarah Hughes",
                        scheduledAt = "2026-05-29 18:00",
                        duration = "1.5 Hours",
                        maxParticipants = 100,
                        status = "Upcoming",
                        enrolledCount = 76
                    )
                )

                // 6. Seed Notifications
                val notificationDao = database.notificationDao()
                notificationDao.insertNotification(
                    NotificationEntity(
                        userEmail = "student@educore.saas",
                        message = "Welcome to EduCore! Upgrade to Pro for unlimited course access and verified certificates.",
                        type = "Alert"
                    )
                )

                // 7. Seed Instructor Payouts
                val payoutDao = database.payoutDao()
                payoutDao.insertPayout(
                    PayoutEntity(
                        instructorId = "instructor@educore.saas",
                        amount = 4500,
                        status = "Paid",
                        requestedAt = System.currentTimeMillis() - 172800000,
                        processedAt = System.currentTimeMillis() - 86400000,
                        transactionId = "TXN_8462058372"
                    )
                )
                payoutDao.insertPayout(
                    PayoutEntity(
                        instructorId = "instructor@educore.saas",
                        amount = 15000,
                        status = "Pending",
                        requestedAt = System.currentTimeMillis() - 3600000
                    )
                )
            }
        }
    }
}
