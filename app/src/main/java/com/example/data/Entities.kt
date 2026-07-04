package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class ConfigEntity(
    @PrimaryKey val id: Int = 1,
    val pin: String = "123456",
    val mandatoryDuesAmount: Double = 10000.0
)

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val whatsApp: String,
    val status: String, // "Menunggu Persetujuan", "Aktif", "Tidak Aktif"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mandatory_dues")
data class MandatoryDuesPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,
    val memberName: String,
    val month: Int, // 1 - 12
    val year: Int,
    val amountPaid: Double,
    val paymentDate: Long,
    val note: String,
    val isCancelled: Boolean = false
)

@Entity(tableName = "voluntary_dues")
data class VoluntaryDuesPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int = 0,
    val donorName: String,
    val amountPaid: Double,
    val paymentDate: Long,
    val paymentTime: String = "", // Optional e.g. "14:30"
    val note: String,
    val isCancelled: Boolean = false,
    val reportMonth: Int = 0,
    val reportYear: Int = 0
)

@Entity(tableName = "other_income")
data class OtherIncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val source: String,
    val amount: Double,
    val paymentDate: Long,
    val note: String,
    val isCancelled: Boolean = false
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "Bantuan Sosial", "Kegiatan", "Konsumsi", "Operasional", "Lainnya"
    val amount: Double,
    val expenseDate: Long,
    val recipient: String,
    val note: String,
    val isCancelled: Boolean = false,
    val expenseTime: String = "",
    val reportMonth: Int = 0,
    val reportYear: Int = 0,
    val recipientName: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val memberId: Int = 0
)

@Entity(tableName = "agendas")
data class AgendaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: Long,
    val time: String,
    val location: String,
    val description: String,
    val meetingMinutes: String = "",
    val status: String // "Akan Datang", "Selesai"
)
