package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<ConfigEntity?>

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: ConfigEntity)
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembersFlow(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Int): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Update
    suspend fun updateMember(member: MemberEntity)
}

@Dao
interface MandatoryDuesDao {
    @Query("SELECT * FROM mandatory_dues ORDER BY paymentDate DESC")
    fun getAllPaymentsFlow(): Flow<List<MandatoryDuesPaymentEntity>>

    @Query("SELECT * FROM mandatory_dues WHERE month = :month AND year = :year ORDER BY paymentDate DESC")
    fun getPaymentsByPeriodFlow(month: Int, year: Int): Flow<List<MandatoryDuesPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: MandatoryDuesPaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: MandatoryDuesPaymentEntity)
}

@Dao
interface VoluntaryDuesDao {
    @Query("SELECT * FROM voluntary_dues ORDER BY paymentDate DESC")
    fun getAllPaymentsFlow(): Flow<List<VoluntaryDuesPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: VoluntaryDuesPaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: VoluntaryDuesPaymentEntity)

    @Delete
    suspend fun deletePayment(payment: VoluntaryDuesPaymentEntity)
}

@Dao
interface OtherIncomeDao {
    @Query("SELECT * FROM other_income ORDER BY paymentDate DESC")
    fun getAllIncomesFlow(): Flow<List<OtherIncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: OtherIncomeEntity): Long

    @Update
    suspend fun updateIncome(income: OtherIncomeEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC")
    fun getAllExpensesFlow(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
}

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agendas ORDER BY date ASC, time ASC")
    fun getAllAgendasFlow(): Flow<List<AgendaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgenda(agenda: AgendaEntity): Long

    @Update
    suspend fun updateAgenda(agenda: AgendaEntity)
}
