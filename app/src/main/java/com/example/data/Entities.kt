package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val passwordHash: String,
    val role: String, // "Learner", "Instructor", "Admin"
    val isVerified: Boolean = false,
    val isApproved: Boolean = false, // Required for instructors
    val isActive: Boolean = true, // Account suspension
    val createdAt: Long = System.currentTimeMillis(),
    val subscription: String = "Free", // "Free", "Pro"
    val streakDays: String = "4/7", // Weekly indicator label
    val streakCount: Int = 4,
    val xp: Int = 250,
    val badges: String = "First Course,7-Day Streak" // Comma-separated list of earned badges
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val instructorId: String,
    val instructorName: String,
    val category: String, // Coding, Design, Business, Data Science, Cloud, DevOps
    val price: Int, // in Indian Rupees (₹)
    val discountPrice: Int = 0,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val thumbnail: String = "coding_placeholder",
    val rating: Float = 4.5f,
    val reviewCount: Int = 12,
    val enrolledCount: Int = 142
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val sectionName: String,
    val title: String,
    val type: String, // "Video", "Article", "Quiz"
    val duration: String, // "10:15"
    val videoUrl: String = "",
    val isPreview: Boolean = false
)

@Entity(tableName = "enrollments")
data class EnrollmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val courseId: Int,
    val progress: Int = 0, // 0 to 100
    val completedLessonIds: String = "", // Comma-separated list "1,2,5"
    val notes: String = "", // Notes draft saved by the student for this course
    val wishlist: Boolean = false,
    val isCompleted: Boolean = false,
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
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_sessions")
data class LiveSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val instructorId: String,
    val instructorName: String,
    val topic: String,
    val description: String,
    val scheduledAt: String, // "2026-05-28 14:00"
    val duration: String, // "30 Min", "1 Hour", "1.5 Hours", "2 Hours"
    val maxParticipants: Int = 100,
    val status: String = "Upcoming", // "Upcoming", "Live", "Past"
    val enrolledCount: Int = 0
)

@Entity(tableName = "payouts")
data class PayoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val instructorId: String,
    val amount: Int,
    val status: String = "Pending", // "Pending", "Processing", "Paid"
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
    val targetType: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
