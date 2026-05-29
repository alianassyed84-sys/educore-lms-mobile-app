package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * FirebaseSeeder
 * ──────────────
 * Seeds the Firebase Auth + Firestore database with demo data on first run.
 * Checks if the "admin@educore.saas" user already exists before seeding.
 *
 * Demo credentials (all seeded automatically):
 *  Admin      → admin@educore.saas       / admin123
 *  Instructor → instructor@educore.saas  / instructor123
 *  Instructor → sarah@educore.saas       / sarah123
 *  Student    → student@educore.saas     / student123
 */
object FirebaseSeeder {

    private val auth get() = FirebaseAuth.getInstance()
    private val db   get() = FirebaseFirestore.getInstance()

    suspend fun seedIfNeeded() {
        // Guard: only seed if no users collection exists yet
        val existingUsers = db.collection("users").limit(1).get().await()
        if (!existingUsers.isEmpty) return   // Already seeded → skip

        seedUsers()
        seedCourses()
        seedLiveSessions()
        seedPayouts()
        seedNotifications()
        seedPlatformSettings()
        seedCoupons()
        seedBanners()
        seedAdminLogs()
    }

    // ─── USERS ───────────────────────────────────────────────────────────

    private suspend fun seedUsers() {
        val users = listOf(
            mapOf(
                "tempEmail"    to "admin@educore.saas",
                "password"     to "admin123",
                "name"         to "Admin Chief",
                "role"         to "Admin",
                "isVerified"   to true,
                "isApproved"   to true,
                "isActive"     to true,
                "isBanned"     to false,
                "subscription" to "Pro",
                "proExpiryAt"  to 0L,
                "streakDays"   to "7/7",
                "streakCount"  to 7,
                "xp"           to 1000,
                "badges"       to "Admin,Platform Builder",
                "commissionRate" to 30,
                "totalEarnings"  to 0,
                "isFeatured"   to false,
                "phone"        to "",
                "bio"          to "Platform administrator.",
                "photoUrl"     to "",
                "expertiseTags" to "",
                "createdAt"    to System.currentTimeMillis()
            ),
            mapOf(
                "tempEmail"    to "instructor@educore.saas",
                "password"     to "instructor123",
                "name"         to "Dev Kar",
                "role"         to "Instructor",
                "isVerified"   to true,
                "isApproved"   to true,
                "isActive"     to true,
                "isBanned"     to false,
                "subscription" to "Free",
                "proExpiryAt"  to 0L,
                "streakDays"   to "5/7",
                "streakCount"  to 5,
                "xp"           to 750,
                "badges"       to "First Course,Top Instructor",
                "commissionRate" to 70,
                "totalEarnings"  to 19500,
                "isFeatured"   to true,
                "phone"        to "",
                "bio"          to "Python & AI educator with 5+ years of industry experience.",
                "photoUrl"     to "",
                "expertiseTags" to "Python,AI,Machine Learning",
                "createdAt"    to System.currentTimeMillis()
            ),
            mapOf(
                "tempEmail"    to "sarah@educore.saas",
                "password"     to "sarah123",
                "name"         to "Sarah Hughes",
                "role"         to "Instructor",
                "isVerified"   to true,
                "isApproved"   to true,
                "isActive"     to true,
                "isBanned"     to false,
                "subscription" to "Free",
                "proExpiryAt"  to 0L,
                "streakDays"   to "3/7",
                "streakCount"  to 3,
                "xp"           to 500,
                "badges"       to "UI Designer",
                "commissionRate" to 70,
                "totalEarnings"  to 8200,
                "isFeatured"   to false,
                "phone"        to "",
                "bio"          to "Senior UX/UI designer and Figma expert.",
                "photoUrl"     to "",
                "expertiseTags" to "UI Design,Figma,UX",
                "createdAt"    to System.currentTimeMillis()
            ),
            mapOf(
                "tempEmail"    to "alex@educore.saas",
                "password"     to "alex123",
                "name"         to "Alex Mercer",
                "role"         to "Instructor",
                "isVerified"   to true,
                "isApproved"   to false,   // Pending approval
                "isActive"     to true,
                "isBanned"     to false,
                "subscription" to "Free",
                "proExpiryAt"  to 0L,
                "streakDays"   to "0/7",
                "streakCount"  to 0,
                "xp"           to 0,
                "badges"       to "",
                "commissionRate" to 30,
                "totalEarnings"  to 0,
                "isFeatured"   to false,
                "phone"        to "",
                "bio"          to "Full-stack developer applying to teach on EduCore.",
                "photoUrl"     to "",
                "expertiseTags" to "React,Node.js",
                "createdAt"    to System.currentTimeMillis()
            ),
            mapOf(
                "tempEmail"    to "student@educore.saas",
                "password"     to "student123",
                "name"         to "Rohit Sharma",
                "role"         to "Learner",
                "isVerified"   to true,
                "isApproved"   to true,
                "isActive"     to true,
                "isBanned"     to false,
                "subscription" to "Free",
                "proExpiryAt"  to 0L,
                "streakDays"   to "4/7",
                "streakCount"  to 4,
                "xp"           to 350,
                "badges"       to "First Course,7-Day Streak",
                "commissionRate" to 30,
                "totalEarnings"  to 0,
                "isFeatured"   to false,
                "phone"        to "",
                "bio"          to "Aspiring software developer.",
                "photoUrl"     to "",
                "expertiseTags" to "",
                "createdAt"    to System.currentTimeMillis()
            )
        )

        for (userMap in users) {
            val email    = userMap["tempEmail"] as String
            val password = userMap["password"]  as String

            try {
                // 1. Create Firebase Auth account
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid    = result.user?.uid ?: continue

                // 2. Send email verification (auto-verify for demo accounts is not possible
                //    via SDK — they'll need to verify manually or we set isVerified=true in Firestore)

                // 3. Write Firestore profile (exclude temp password fields)
                val profile = userMap.toMutableMap().apply {
                    remove("tempEmail")
                    remove("password")
                    put("uid",   uid)
                    put("email", email)
                    put("lastActiveAt", System.currentTimeMillis())
                }
                db.collection("users").document(uid).set(profile).await()

            } catch (e: Exception) {
                // User might already exist in Auth — skip silently
                if (!e.message.orEmpty().contains("email address is already in use", ignoreCase = true)) {
                    e.printStackTrace()
                }
            }
        }
    }

    // ─── COURSES ─────────────────────────────────────────────────────────

    private suspend fun seedCourses() {
        // Look up instructor UIDs from Firestore (we just wrote them above)
        val instructorUid = getUidByEmail("instructor@educore.saas")
        val sarahUid      = getUidByEmail("sarah@educore.saas")

        val courses = listOf(
            mapOf(
                "title"          to "Mastering Python & AI Development",
                "description"    to "Learn Python programming from scratch and build robust AI models. Topics include loops, modules, neural networks, and model deployment.",
                "longDescription" to "This comprehensive course covers every aspect of Python programming and AI development. You will start from basics and progress to building complete machine learning pipelines.",
                "instructorId"   to (instructorUid ?: "instructor@educore.saas"),
                "instructorName" to "Dev Kar",
                "category"       to "Coding",
                "subCategory"    to "Python",
                "difficulty"     to "Beginner",
                "language"       to "English",
                "tags"           to "python,ai,machine learning,beginner",
                "price"          to 0L,
                "discountPrice"  to 0L,
                "discountExpiry" to 0L,
                "enrollmentCap"  to 0L,
                "accessDuration" to "Lifetime",
                "status"         to "Published",
                "thumbnail"      to "python_ai",
                "introVideoUrl"  to "",
                "certificateTemplate" to 1L,
                "rating"         to 4.8,
                "reviewCount"    to 5L,
                "enrolledCount"  to 280L,
                "isFeatured"     to true,
                "featuredOrder"  to 1L,
                "lessons"        to listOf(
                    mapOf("sectionName" to "Section 1: Setup",      "lessonOrder" to 1L, "sectionOrder" to 1L, "title" to "Python Basics & Hello World",              "type" to "Video",   "duration" to "08:45", "videoUrl" to "https://example.com/py1",    "isPreview" to true),
                    mapOf("sectionName" to "Section 1: Setup",      "lessonOrder" to 2L, "sectionOrder" to 1L, "title" to "Variables and Data Types",                  "type" to "Video",   "duration" to "12:15", "videoUrl" to "https://example.com/py2",    "isPreview" to false),
                    mapOf("sectionName" to "Section 2: Core Flow",   "lessonOrder" to 1L, "sectionOrder" to 2L, "title" to "Writing Your First Loop",                   "type" to "Quiz",    "duration" to "05:00", "videoUrl" to "",                           "isPreview" to false),
                    mapOf("sectionName" to "Section 2: Core Flow",   "lessonOrder" to 2L, "sectionOrder" to 2L, "title" to "Introduction to NumPy Data Structures",     "type" to "Video",   "duration" to "14:20", "videoUrl" to "https://example.com/py3",    "isPreview" to false),
                    mapOf("sectionName" to "Section 3: AI Models",   "lessonOrder" to 1L, "sectionOrder" to 3L, "title" to "Building & Testing a Simple Gradient Model","type" to "Video",   "duration" to "22:10", "videoUrl" to "https://example.com/py4",    "isPreview" to false)
                )
            ),
            mapOf(
                "title"          to "SaaS Product & UI Design Masterclass",
                "description"    to "Design award-winning interfaces with proper spatial layout, visual contrast, typography hierarchies, and component library setups in Figma.",
                "longDescription" to "A deep-dive into professional SaaS product design. Covers everything from wireframes to high-fidelity prototypes using Figma.",
                "instructorId"   to (sarahUid ?: "sarah@educore.saas"),
                "instructorName" to "Sarah Hughes",
                "category"       to "Design",
                "subCategory"    to "UI/UX",
                "difficulty"     to "Intermediate",
                "language"       to "English",
                "tags"           to "ui,ux,figma,saas,design",
                "price"          to 1499L,
                "discountPrice"  to 999L,
                "discountExpiry" to (System.currentTimeMillis() + 7L * 86400000),
                "enrollmentCap"  to 0L,
                "accessDuration" to "Lifetime",
                "status"         to "Published",
                "thumbnail"      to "ui_design",
                "introVideoUrl"  to "",
                "certificateTemplate" to 2L,
                "rating"         to 4.9,
                "reviewCount"    to 3L,
                "enrolledCount"  to 120L,
                "isFeatured"     to true,
                "featuredOrder"  to 2L,
                "lessons"        to listOf(
                    mapOf("sectionName" to "Section 1: Foundations",       "lessonOrder" to 1L, "sectionOrder" to 1L, "title" to "Introduction to Figma Design Interfaces",       "type" to "Video",   "duration" to "10:30", "videoUrl" to "https://example.com/fg1", "isPreview" to true),
                    mapOf("sectionName" to "Section 1: Foundations",       "lessonOrder" to 2L, "sectionOrder" to 1L, "title" to "Visual Hierarchy, Alignment & Contrast Rules",  "type" to "Video",   "duration" to "15:45", "videoUrl" to "https://example.com/fg2", "isPreview" to false),
                    mapOf("sectionName" to "Section 2: Implementation",    "lessonOrder" to 1L, "sectionOrder" to 2L, "title" to "Color Theory & Contrast Accessibility",          "type" to "Article", "duration" to "08:00", "videoUrl" to "",                        "isPreview" to false),
                    mapOf("sectionName" to "Section 2: Implementation",    "lessonOrder" to 2L, "sectionOrder" to 2L, "title" to "Designing a SaaS Landing Page Step-by-Step",    "type" to "Video",   "duration" to "25:30", "videoUrl" to "https://example.com/fg3", "isPreview" to false)
                )
            ),
            mapOf(
                "title"          to "DevOps Pipeline Crash Course (Docker & AWS)",
                "description"    to "Master continuous deployment pipelines with Docker, Kubernetes, and AWS EC2 cloud setups. Practice with simulated YAML files.",
                "longDescription" to "A hands-on DevOps course covering the complete CI/CD pipeline setup using Docker, Kubernetes, and AWS services.",
                "instructorId"   to (instructorUid ?: "instructor@educore.saas"),
                "instructorName" to "Dev Kar",
                "category"       to "DevOps",
                "subCategory"    to "Cloud",
                "difficulty"     to "Advanced",
                "language"       to "English",
                "tags"           to "devops,docker,aws,kubernetes,cicd",
                "price"          to 2499L,
                "discountPrice"  to 0L,
                "discountExpiry" to 0L,
                "enrollmentCap"  to 200L,
                "accessDuration" to "1 Year",
                "status"         to "Pending",
                "thumbnail"      to "devops",
                "introVideoUrl"  to "",
                "certificateTemplate" to 1L,
                "rating"         to 4.5,
                "reviewCount"    to 0L,
                "enrolledCount"  to 0L,
                "isFeatured"     to false,
                "featuredOrder"  to 0L,
                "lessons"        to listOf(
                    mapOf("sectionName" to "Section 1: Docker Basics", "lessonOrder" to 1L, "sectionOrder" to 1L, "title" to "Setting Up Docker Desktop", "type" to "Video", "duration" to "09:30", "videoUrl" to "https://example.com/do1", "isPreview" to true)
                )
            )
        )

        for (course in courses) {
            val lessons = course["lessons"] as? List<*>
            val courseData = course.toMutableMap().apply { remove("lessons") }
            val courseRef = db.collection("courses").add(courseData).await()

            // Seed lessons as a sub-collection
            lessons?.forEach { lesson ->
                @Suppress("UNCHECKED_CAST")
                val l = lesson as? Map<String, Any?> ?: return@forEach
                courseRef.collection("lessons").add(l).await()
            }
        }
    }

    // ─── LIVE SESSIONS ────────────────────────────────────────────────────

    private suspend fun seedLiveSessions() {
        val instructorUid = getUidByEmail("instructor@educore.saas")
        val sarahUid      = getUidByEmail("sarah@educore.saas")

        val sessions = listOf(
            mapOf(
                "instructorId"    to (instructorUid ?: "instructor@educore.saas"),
                "instructorName"  to "Dev Kar",
                "topic"           to "AI & Machine Learning Q&A Session",
                "description"     to "Bring all your questions on linear regressions, multi-layer nodes, and building production-ready pipelines in Python.",
                "scheduledAt"     to "2026-05-28 15:30",
                "duration"        to "1 Hour",
                "maxParticipants" to 150L,
                "status"          to "Upcoming",
                "enrolledCount"   to 45L,
                "cancelReason"    to "",
                "createdByAdmin"  to false,
                "createdAt"       to System.currentTimeMillis()
            ),
            mapOf(
                "instructorId"    to (sarahUid ?: "sarah@educore.saas"),
                "instructorName"  to "Sarah Hughes",
                "topic"           to "Designing Delightful Micro-Interactions",
                "description"     to "Learn how to build beautiful layout transitions and micro-animations to satisfy users.",
                "scheduledAt"     to "2026-05-29 18:00",
                "duration"        to "1.5 Hours",
                "maxParticipants" to 100L,
                "status"          to "Upcoming",
                "enrolledCount"   to 76L,
                "cancelReason"    to "",
                "createdByAdmin"  to false,
                "createdAt"       to System.currentTimeMillis()
            )
        )

        sessions.forEach { db.collection("live_sessions").add(it).await() }
    }

    // ─── PAYOUTS ──────────────────────────────────────────────────────────

    private suspend fun seedPayouts() {
        val instructorUid = getUidByEmail("instructor@educore.saas") ?: "instructor@educore.saas"

        val payouts = listOf(
            mapOf(
                "instructorId"  to instructorUid,
                "amount"        to 4500L,
                "status"        to "Paid",
                "requestedAt"   to (System.currentTimeMillis() - 172800000L),
                "processedAt"   to (System.currentTimeMillis() - 86400000L),
                "transactionId" to "TXN_8462058372"
            ),
            mapOf(
                "instructorId"  to instructorUid,
                "amount"        to 15000L,
                "status"        to "Pending",
                "requestedAt"   to (System.currentTimeMillis() - 3600000L),
                "processedAt"   to 0L,
                "transactionId" to ""
            )
        )

        payouts.forEach { db.collection("payouts").add(it).await() }
    }

    // ─── NOTIFICATIONS ────────────────────────────────────────────────────

    private suspend fun seedNotifications() {
        val studentUid = getUidByEmail("student@educore.saas") ?: return
        db.collection("notifications").add(
            mapOf(
                "userId"    to studentUid,
                "message"   to "Welcome to EduCore! Upgrade to Pro for unlimited course access and verified certificates.",
                "type"      to "Alert",
                "isRead"    to false,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }

    // ─── PLATFORM SETTINGS ────────────────────────────────────────────────

    private suspend fun seedPlatformSettings() {
        val settings = mapOf(
            "app_name"              to "EduCore",
            "commission_rate"       to "30",
            "min_payout"            to "1000",
            "refund_window_days"    to "7",
            "free_enrollment_limit" to "5",
            "maintenance_mode"      to "false",
            "live_enabled"          to "true",
            "certs_enabled"         to "true",
            "announcement_enabled"  to "false",
            "announcement_text"     to ""
        )
        settings.forEach { (key, value) ->
            db.collection("platform_settings").document(key).set(
                mapOf("key" to key, "value" to value, "updatedAt" to System.currentTimeMillis())
            ).await()
        }
    }

    // ─── COUPONS ─────────────────────────────────────────────────────────

    private suspend fun seedCoupons() {
        val coupons = listOf(
            mapOf("code" to "LAUNCH50",  "discountPercent" to 50L, "expiryDate" to (System.currentTimeMillis() + 30L * 86400000), "maxUses" to 500L, "usedCount" to 213L, "isActive" to true, "createdAt" to System.currentTimeMillis()),
            mapOf("code" to "WELCOME20", "discountPercent" to 20L, "expiryDate" to (System.currentTimeMillis() + 90L * 86400000), "maxUses" to 1000L, "usedCount" to 87L,  "isActive" to true, "createdAt" to System.currentTimeMillis())
        )
        coupons.forEach {
            db.collection("coupons").document(it["code"] as String).set(it).await()
        }
    }

    // ─── BANNERS ─────────────────────────────────────────────────────────

    private suspend fun seedBanners() {
        val banners = listOf(
            mapOf("imageUrl" to "https://images.unsplash.com/photo-1516321318423-f06f85e504b3", "title" to "🎉 Learn Anything, Anytime",  "subtitle" to "50,000+ students trust EduCore", "buttonLabel" to "Explore Courses", "isEnabled" to true, "displayOrder" to 0L, "createdAt" to System.currentTimeMillis()),
            mapOf("imageUrl" to "https://images.unsplash.com/photo-1498050108023-c5249f4df085", "title" to "New Year Offer — 40% Off",    "subtitle" to "Pro Plan at just ₹299/month",    "buttonLabel" to "Upgrade Now",     "isEnabled" to true, "displayOrder" to 1L, "createdAt" to System.currentTimeMillis())
        )
        banners.forEach { db.collection("banners").add(it).await() }
    }

    // ─── ADMIN LOGS ──────────────────────────────────────────────────────

    private suspend fun seedAdminLogs() {
        val adminUid = getUidByEmail("admin@educore.saas") ?: return
        val instructorUid = getUidByEmail("instructor@educore.saas") ?: "instructor@educore.saas"
        val logs = listOf(
            mapOf("adminUid" to adminUid, "action" to "APPROVED_INSTRUCTOR", "targetId" to instructorUid, "targetType" to "USER",         "oldValue" to "Pending",  "newValue" to "Approved",   "timestamp" to (System.currentTimeMillis() - 3 * 86400000L)),
            mapOf("adminUid" to adminUid, "action" to "CHANGED_COMMISSION",  "targetId" to instructorUid, "targetType" to "USER",         "oldValue" to "30%",      "newValue" to "70%",        "timestamp" to (System.currentTimeMillis() - 86400000L)),
            mapOf("adminUid" to adminUid, "action" to "SENT_NOTIFICATION",   "targetId" to "All Users",   "targetType" to "NOTIFICATION", "oldValue" to "",         "newValue" to "Welcome msg","timestamp" to (System.currentTimeMillis() - 43200000L))
        )
        logs.forEach { db.collection("admin_logs").add(it).await() }
    }

    // ─── HELPER: Get UID by email ─────────────────────────────────────────

    private suspend fun getUidByEmail(email: String): String? {
        val result = db.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()
        return result.documents.firstOrNull()?.id
    }
}
