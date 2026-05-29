package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val passwordHash: String,
    val role: String, // "Learner", "Instructor", "Admin", "SuperAdmin"
    val isVerified: Boolean = false,
    val isApproved: Boolean = false, // Required for instructors
    val isActive: Boolean = true, // Account suspension
    val isBanned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val subscription: String = "Free", // "Free", "Pro"
    val proExpiryAt: Long = 0L,
    val streakDays: String = "4/7",
    val streakCount: Int = 4,
    val xp: Int = 250,
    val badges: String = "First Course,7-Day Streak",
    val phone: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val expertiseTags: String = "", // comma-separated
    val commissionRate: Int = 30, // per-instructor override
    val totalEarnings: Int = 0,
    val suspensionReason: String = "",
    val isFeatured: Boolean = false
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val longDescription: String = "",
    val instructorId: String,
    val instructorName: String,
    val coInstructorId: String = "",
    val coInstructorName: String = "",
    val revenueSharePrimary: Int = 100, // % to primary instructor
    val category: String,
    val subCategory: String = "",
    val difficulty: String = "Beginner",
    val language: String = "English",
    val tags: String = "", // comma-separated
    val price: Int, // in Indian Rupees (₹)
    val discountPrice: Int = 0,
    val discountExpiry: Long = 0L,
    val enrollmentCap: Int = 0, // 0 = unlimited
    val accessDuration: String = "Lifetime", // "Lifetime","30 Days","90 Days","1 Year"
    val status: String = "Pending", // "Draft","Pending","Published","Unpublished","Archived","Rejected"
    val thumbnail: String = "coding_placeholder",
    val introVideoUrl: String = "",
    val certificateTemplate: Int = 1, // 1, 2, 3
    val rating: Float = 4.5f,
    val reviewCount: Int = 12,
    val enrolledCount: Int = 142,
    val isFeatured: Boolean = false,
    val featuredOrder: Int = 0
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val sectionName: String,
    val sectionOrder: Int = 0,
    val lessonOrder: Int = 0,
    val title: String,
    val type: String, // "Video", "Article", "Quiz"
    val duration: String, // "10:15"
    val videoUrl: String = "",
    val articleContent: String = "",
    val isPreview: Boolean = false
)

@Entity(tableName = "enrollments")
data class EnrollmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val courseId: Int,
    val progress: Int = 0,
    val completedLessonIds: String = "",
    val notes: String = "",
    val wishlist: Boolean = false,
    val isCompleted: Boolean = false,
    val certificateGranted: Boolean = false,
    val enrolledAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val userEmail: String,
    val userName: String,
    val rating: Int, // 1 to 5 stars
    val comment: String,
    val status: String = "Approved", // "Approved", "Hidden", "Flagged"
    val adminResponse: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_sessions")
data class LiveSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val instructorId: String,
    val instructorName: String,
    val topic: String,
    val description: String,
    val scheduledAt: String,
    val duration: String,
    val maxParticipants: Int = 100,
    val status: String = "Upcoming", // "Upcoming", "Live", "Past", "Cancelled"
    val enrolledCount: Int = 0,
    val cancelReason: String = "",
    val createdByAdmin: Boolean = false
)

@Entity(tableName = "payouts")
data class PayoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val instructorId: String,
    val amount: Int,
    val status: String = "Pending",
    val requestedAt: Long = System.currentTimeMillis(),
    val processedAt: Long = 0L,
    val transactionId: String = ""
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val message: String,
    val type: String, // Course, Live, Alert, Payout
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_logs")
data class AdminLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val adminEmail: String,
    val action: String,
    val targetId: String = "",
    val targetType: String = "", // USER, COURSE, PAYOUT, SYSTEM, NOTIFICATION, SESSION
    val oldValue: String = "",
    val newValue: String = "",
    val ipAddress: String = "192.168.1.1",
    val deviceInfo: String = "Android App",
    val timestamp: Long = System.currentTimeMillis()
)

// ─── NEW ENTITIES ───────────────────────────────────────────

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val code: String,
    val courseId: Int = 0, // 0 = platform-wide
    val discountPercent: Int,
    val expiryDate: Long,
    val maxUses: Int = 100,
    val usedCount: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUrl: String,
    val title: String,
    val subtitle: String = "",
    val buttonLabel: String = "Explore",
    val buttonLink: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val isEnabled: Boolean = true,
    val displayOrder: Int = 0
)

@Entity(tableName = "sent_notifications")
data class SentNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val target: String, // "All Users", "All Students", etc.
    val notificationType: String, // "Push", "In-App", "Email", "All"
    val sentAt: Long = System.currentTimeMillis(),
    val deliveryCount: Int = 0,
    val openRate: Float = 0f
)

@Entity(tableName = "platform_settings")
data class PlatformSettingEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)
