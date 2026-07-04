package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConfigEntity::class,
        MemberEntity::class,
        MandatoryDuesPaymentEntity::class,
        VoluntaryDuesPaymentEntity::class,
        OtherIncomeEntity::class,
        ExpenseEntity::class,
        AgendaEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDao
    abstract fun memberDao(): MemberDao
    abstract fun mandatoryDuesDao(): MandatoryDuesDao
    abstract fun voluntaryDuesDao(): VoluntaryDuesDao
    abstract fun otherIncomeDao(): OtherIncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun agendaDao(): AgendaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE voluntary_dues ADD COLUMN memberId INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "steker_database"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
