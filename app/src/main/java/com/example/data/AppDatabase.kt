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
    version = 1,
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "steker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
