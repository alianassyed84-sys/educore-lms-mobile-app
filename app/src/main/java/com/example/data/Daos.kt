package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmailFlow(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRoleFlow(role: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUserByEmail(email: String)

    @Query("UPDATE users SET isApproved = :approved WHERE email = :email")
    suspend fun approveInstructor(email: String, approved: Boolean)

    @Query("UPDATE users SET isActive = :active WHERE email = :email")
    suspend fun setUserActiveState(email: String, active: Boolean)

    @Query("UPDATE users SET isBanned = 1, isActive = 0 WHERE email = :email")
    suspend fun banUser(email: String)

    @Query("UPDATE users SET role = :role WHERE email = :email")
    suspend fun changeUserRole(email: String, role: String)

    @Query("UPDATE users SET subscription = :plan, proExpiryAt = :expiry WHERE email = :email")
    suspend fun setUserSubscription(email: String, plan: String, expiry: Long)

    @Query("UPDATE users SET commissionRate = :rate WHERE email = :email")
    suspend fun setInstructorCommission(email: String, rate: Int)

    @Query("UPDATE users SET isFeatured = :featured WHERE email = :email")
    suspend fun setInstructorFeatured(email: String, featured: Boolean)

    @Query("UPDATE users SET lastActiveAt = :time WHERE email = :email")
    suspend fun updateLastActive(email: String, time: Long)

    @Query("SELECT * FROM users WHERE role = 'Instructor' AND isApproved = 1")
    suspend fun getApprovedInstructors(): List<UserEntity>
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCoursesFlow(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE status = :status")
    fun getCoursesByStatusFlow(status: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE instructorId = :instructorId")
    fun getCoursesByInstructorFlow(instructorId: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Int): CourseEntity?

    @Query("SELECT * FROM courses WHERE id = :id")
    fun getCourseByIdFlow(id: Int): Flow<CourseEntity?>

    @Query("SELECT * FROM courses WHERE isFeatured = 1 ORDER BY featuredOrder ASC")
    fun getFeaturedCoursesFlow(): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: Int)

    @Query("UPDATE courses SET status = :status WHERE id = :id")
    suspend fun updateCourseStatus(id: Int, status: String)

    @Query("UPDATE courses SET enrolledCount = enrolledCount + 1 WHERE id = :id")
    suspend fun incrementEnrollmentCount(id: Int)

    @Query("UPDATE courses SET instructorId = :newId, instructorName = :newName WHERE id = :courseId")
    suspend fun reassignInstructor(courseId: Int, newId: String, newName: String)

    @Query("UPDATE courses SET isFeatured = :featured, featuredOrder = :order WHERE id = :id")
    suspend fun setCourseFeatured(id: Int, featured: Boolean, order: Int)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY sectionOrder ASC, lessonOrder ASC")
    fun getLessonsForCourseFlow(courseId: Int): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY sectionOrder ASC, lessonOrder ASC")
    suspend fun getLessonsForCourse(courseId: Int): List<LessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Delete
    suspend fun deleteLesson(lesson: LessonEntity)

    @Query("DELETE FROM lessons WHERE id = :id")
    suspend fun deleteLessonById(id: Int)

    @Query("DELETE FROM lessons WHERE courseId = :courseId")
    suspend fun deleteLessonsForCourse(courseId: Int)
}

@Dao
interface EnrollmentDao {
    @Query("SELECT * FROM enrollments WHERE userEmail = :email")
    fun getEnrollmentsForUserFlow(email: String): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM enrollments WHERE courseId = :courseId")
    fun getEnrollmentsForCourseFlow(courseId: Int): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM enrollments WHERE userEmail = :email AND courseId = :courseId")
    suspend fun getEnrollment(email: String, courseId: Int): EnrollmentEntity?

    @Query("SELECT * FROM enrollments WHERE userEmail = :email AND courseId = :courseId")
    fun getEnrollmentFlow(email: String, courseId: Int): Flow<EnrollmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: EnrollmentEntity)

    @Update
    suspend fun updateEnrollment(enrollment: EnrollmentEntity)

    @Query("DELETE FROM enrollments WHERE userEmail = :email AND courseId = :courseId")
    suspend fun removeEnrollment(email: String, courseId: Int)

    @Query("UPDATE enrollments SET completedLessonIds = :completedIds, progress = :progress WHERE userEmail = :email AND courseId = :courseId")
    suspend fun updateProgress(email: String, courseId: Int, completedIds: String, progress: Int)

    @Query("UPDATE enrollments SET notes = :notes WHERE userEmail = :email AND courseId = :courseId")
    suspend fun updateNotes(email: String, courseId: Int, notes: String)

    @Query("UPDATE enrollments SET certificateGranted = 1, isCompleted = 1, progress = 100 WHERE userEmail = :email AND courseId = :courseId")
    suspend fun grantCertificate(email: String, courseId: Int)

    @Query("UPDATE enrollments SET progress = 0, completedLessonIds = '', isCompleted = 0 WHERE userEmail = :email AND courseId = :courseId")
    suspend fun resetProgress(email: String, courseId: Int)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE courseId = :courseId ORDER BY createdAt DESC")
    fun getReviewsForCourseFlow(courseId: Int): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviewsFlow(): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Update
    suspend fun updateReview(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteReviewById(id: Int)

    @Query("UPDATE reviews SET status = :status WHERE id = :id")
    suspend fun updateReviewStatus(id: Int, status: String)

    @Query("UPDATE reviews SET adminResponse = :response WHERE id = :id")
    suspend fun setAdminResponse(id: Int, response: String)
}

@Dao
interface LiveSessionDao {
    @Query("SELECT * FROM live_sessions ORDER BY scheduledAt ASC")
    fun getAllSessionsFlow(): Flow<List<LiveSessionEntity>>

    @Query("SELECT * FROM live_sessions WHERE instructorId = :instructorId ORDER BY scheduledAt ASC")
    fun getSessionsForInstructorFlow(instructorId: String): Flow<List<LiveSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: LiveSessionEntity)

    @Update
    suspend fun updateSession(session: LiveSessionEntity)

    @Query("DELETE FROM live_sessions WHERE id = :id")
    suspend fun deleteSession(id: Int)

    @Query("UPDATE live_sessions SET status = 'Cancelled', cancelReason = :reason WHERE id = :id")
    suspend fun cancelSession(id: Int, reason: String)
}

@Dao
interface PayoutDao {
    @Query("SELECT * FROM payouts ORDER BY requestedAt DESC")
    fun getAllPayoutsFlow(): Flow<List<PayoutEntity>>

    @Query("SELECT * FROM payouts WHERE instructorId = :instructorId ORDER BY requestedAt DESC")
    fun getPayoutsForInstructorFlow(instructorId: String): Flow<List<PayoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayout(payout: PayoutEntity)

    @Query("UPDATE payouts SET status = :status, processedAt = :processedAt, transactionId = :transactionId WHERE id = :id")
    suspend fun updatePayoutStatus(id: Int, status: String, processedAt: Long, transactionId: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userEmail = :email ORDER BY createdAt DESC")
    fun getNotificationsForUserFlow(email: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userEmail = :email")
    suspend fun markAllAsRead(email: String)
}

@Dao
interface AdminLogDao {
    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC")
    fun getAllAdminLogsFlow(): Flow<List<AdminLogEntity>>

    @Query("SELECT * FROM admin_logs WHERE targetType = :type ORDER BY timestamp DESC")
    fun getLogsByTargetTypeFlow(type: String): Flow<List<AdminLogEntity>>

    @Query("SELECT * FROM admin_logs WHERE adminEmail = :email ORDER BY timestamp DESC")
    fun getLogsByAdminFlow(email: String): Flow<List<AdminLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminLog(log: AdminLogEntity)
}

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons ORDER BY createdAt DESC")
    fun getAllCouponsFlow(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE courseId = :courseId")
    fun getCouponsForCourseFlow(courseId: Int): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Update
    suspend fun updateCoupon(coupon: CouponEntity)

    @Query("DELETE FROM coupons WHERE code = :code")
    suspend fun deleteCoupon(code: String)

    @Query("UPDATE coupons SET usedCount = usedCount + 1 WHERE code = :code")
    suspend fun incrementCouponUse(code: String)
}

@Dao
interface BannerDao {
    @Query("SELECT * FROM banners ORDER BY displayOrder ASC")
    fun getAllBannersFlow(): Flow<List<BannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: BannerEntity)

    @Update
    suspend fun updateBanner(banner: BannerEntity)

    @Query("DELETE FROM banners WHERE id = :id")
    suspend fun deleteBanner(id: Int)

    @Query("UPDATE banners SET isEnabled = :enabled WHERE id = :id")
    suspend fun setBannerEnabled(id: Int, enabled: Boolean)
}

@Dao
interface SentNotificationDao {
    @Query("SELECT * FROM sent_notifications ORDER BY sentAt DESC")
    fun getAllSentNotificationsFlow(): Flow<List<SentNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSentNotification(n: SentNotificationEntity)
}

@Dao
interface PlatformSettingDao {
    @Query("SELECT * FROM platform_settings")
    fun getAllSettingsFlow(): Flow<List<PlatformSettingEntity>>

    @Query("SELECT value FROM platform_settings WHERE key = :key")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: PlatformSettingEntity)
}
