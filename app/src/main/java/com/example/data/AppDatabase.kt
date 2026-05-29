package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        CourseEntity::class,
        LessonEntity::class,
        EnrollmentEntity::class,
        ReviewEntity::class,
        LiveSessionEntity::class,
        PayoutEntity::class,
        NotificationEntity::class,
        AdminLogEntity::class,
        CouponEntity::class,
        BannerEntity::class,
        SentNotificationEntity::class,
        PlatformSettingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun enrollmentDao(): EnrollmentDao
    abstract fun reviewDao(): ReviewDao
    abstract fun liveSessionDao(): LiveSessionDao
    abstract fun payoutDao(): PayoutDao
    abstract fun notificationDao(): NotificationDao
    abstract fun adminLogDao(): AdminLogDao
    abstract fun couponDao(): CouponDao
    abstract fun bannerDao(): BannerDao
    abstract fun sentNotificationDao(): SentNotificationDao
    abstract fun platformSettingDao(): PlatformSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "educore_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
