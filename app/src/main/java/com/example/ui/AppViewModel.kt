package com.example.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import android.content.Context

data class TotalsData(
    val netBalance: Double = 0.0,
    val totalMandatory: Double = 0.0,
    val totalVoluntary: Double = 0.0,
    val totalOther: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val mandatoryThisMonth: Double = 0.0,
    val voluntaryThisMonth: Double = 0.0,
    val expensesThisMonth: Double = 0.0
)

enum class AppTab {
    BERANDA, ANGGOTA, KEUANGAN, LAPORAN, LAINNYA
}

enum class SubScreen {
    NONE,
    ADD_MEMBER, EDIT_MEMBER, MEMBER_PROFILE, ADD_MEMBER_PAYMENT,
    ADD_MANDATORY, EDIT_MANDATORY,
    ADD_VOLUNTARY, EDIT_VOLUNTARY,
    ADD_OTHER_INCOME, EDIT_OTHER_INCOME,
    ADD_EXPENSE, EDIT_EXPENSE,
    ADD_AGENDA, EDIT_AGENDA,
    SETTINGS,
    BACKUP_DATA
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = Repository(db)

    // Security & Login
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    // Navigation state
    private val _currentTab = MutableStateFlow(AppTab.BERANDA)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _subScreen = MutableStateFlow(SubScreen.NONE)
    val subScreen: StateFlow<SubScreen> = _subScreen.asStateFlow()

    // Selection states
    private val _selectedMemberId = MutableStateFlow<Int?>(null)
    val selectedMemberId: StateFlow<Int?> = _selectedMemberId.asStateFlow()

    private val _selectedVoluntaryPayment = MutableStateFlow<VoluntaryDuesPaymentEntity?>(null)
    val selectedVoluntaryPayment: StateFlow<VoluntaryDuesPaymentEntity?> = _selectedVoluntaryPayment.asStateFlow()

    fun selectVoluntaryPayment(payment: VoluntaryDuesPaymentEntity?) {
        _selectedVoluntaryPayment.value = payment
    }

    private val _selectedExpense = MutableStateFlow<ExpenseEntity?>(null)
    val selectedExpense: StateFlow<ExpenseEntity?> = _selectedExpense.asStateFlow()

    fun selectExpense(expense: ExpenseEntity?) {
        _selectedExpense.value = expense
    }

    // Dynamic filtering periods for reports and lists
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-12

    private val _selectedMonth = MutableStateFlow(currentMonth)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(currentYear)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // Report specific search & filter states
    private val _reportFilterType = MutableStateFlow("Semua Pembayar") // "Semua Pembayar", "Iuran Wajib", "Iuran Sukarela", "Keduanya"
    val reportFilterType: StateFlow<String> = _reportFilterType.asStateFlow()

    private val _reportSearchQuery = MutableStateFlow("")
    val reportSearchQuery: StateFlow<String> = _reportSearchQuery.asStateFlow()

    // Live Data Streams from Room
    val config = repository.configFlow.stateIn(viewModelScope, SharingStarted.Lazily, ConfigEntity())
    val members = repository.membersFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val mandatoryPayments = repository.mandatoryPaymentsFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val voluntaryPayments = repository.voluntaryPaymentsFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val otherIncomes = repository.otherIncomesFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val expenses = repository.expensesFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val agendas = repository.agendasFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Active Member profiles details computed
    val selectedMemberProfile = combine(selectedMemberId, members, mandatoryPayments, voluntaryPayments) { memberId, memberList, mandatoryList, voluntaryList ->
        if (memberId == null) return@combine null
        val member = memberList.find { it.id == memberId } ?: return@combine null
        val mandatoryPaymentsFiltered = mandatoryList.filter { it.memberId == memberId }
        // Match voluntary payments by memberId, or by donorName if memberId is 0 (fallback for older transactions)
        val voluntaryPaymentsFiltered = voluntaryList.filter {
            it.memberId == memberId || (it.memberId == 0 && it.donorName.equals(member.name, ignoreCase = true))
        }
        
        Triple(member, mandatoryPaymentsFiltered, voluntaryPaymentsFiltered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Dynamic live balances calculation (Saldo Kas Saat Ini)
    val totalsState = combine(
        mandatoryPayments,
        voluntaryPayments,
        otherIncomes,
        expenses
    ) { mandatory, voluntary, other, exps ->
        val totalMandatory = mandatory.filter { !it.isCancelled }.sumOf { it.amountPaid }
        val totalVoluntary = voluntary.filter { !it.isCancelled }.sumOf { it.amountPaid }
        val totalOther = other.filter { !it.isCancelled }.sumOf { it.amount }
        val totalExpenses = exps.filter { !it.isCancelled }.sumOf { it.amount }
        val netBalance = totalMandatory + totalVoluntary + totalOther - totalExpenses

        // Current month calculations
        val cal = Calendar.getInstance()
        val thisMonth = cal.get(Calendar.MONTH) + 1
        val thisYear = cal.get(Calendar.YEAR)

        val mandatoryThisMonth = mandatory.filter { !it.isCancelled && it.month == thisMonth && it.year == thisYear }.sumOf { it.amountPaid }
        val voluntaryThisMonth = voluntary.filter { !it.isCancelled && isDateInPeriod(it.paymentDate, thisMonth, thisYear) }.sumOf { it.amountPaid }
        val expensesThisMonth = exps.filter { !it.isCancelled && isDateInPeriod(it.expenseDate, thisMonth, thisYear) }.sumOf { it.amount }

        TotalsData(
            netBalance = netBalance,
            totalMandatory = totalMandatory,
            totalVoluntary = totalVoluntary,
            totalOther = totalOther,
            totalExpenses = totalExpenses,
            mandatoryThisMonth = mandatoryThisMonth,
            voluntaryThisMonth = voluntaryThisMonth,
            expensesThisMonth = expensesThisMonth
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Helper to check if a timestamp is in a specific month & year
    private fun isDateInPeriod(dateMs: Long, targetMonth: Int, targetYear: Int): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMs
        return (cal.get(Calendar.MONTH) + 1) == targetMonth && cal.get(Calendar.YEAR) == targetYear
    }

    // Authentication Checks
    fun loginWithPin(enteredPin: String) {
        viewModelScope.launch {
            val pinFromConfig = config.value?.pin ?: "123456"
            if (enteredPin == pinFromConfig) {
                _isLoggedIn.value = true
                _pinError.value = null
            } else {
                _pinError.value = "PIN tidak sesuai"
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _pinError.value = null
        _currentTab.value = AppTab.BERANDA
        _subScreen.value = SubScreen.NONE
    }

    // Navigation Mutators
    fun setTab(tab: AppTab) {
        _currentTab.value = tab
        _subScreen.value = SubScreen.NONE
    }

    fun setSubScreen(sub: SubScreen) {
        _subScreen.value = sub
        if (sub == SubScreen.NONE) {
            _selectedMemberId.value = null
        }
    }

    fun selectMember(memberId: Int, destinationSubScreen: SubScreen = SubScreen.MEMBER_PROFILE) {
        _selectedMemberId.value = memberId
        _subScreen.value = destinationSubScreen
    }

    fun setPeriod(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun setReportSearch(query: String) {
        _reportSearchQuery.value = query
    }

    fun setReportFilterType(type: String) {
        _reportFilterType.value = type
    }

    // CRUD - CONFIG
    fun updatePin(oldPin: String, newPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val cur = config.value?.pin ?: "123456"
            if (cur != oldPin) {
                onError("PIN lama tidak sesuai")
                return@launch
            }
            if (newPin.length != 6 || newPin.any { !it.isDigit() }) {
                onError("PIN baru harus 6 digit angka")
                return@launch
            }
            val currentConf = config.value ?: ConfigEntity()
            repository.saveConfig(currentConf.copy(pin = newPin))
            onSuccess()
        }
    }

    fun updateMandatoryDuesAmount(newAmount: Double) {
        viewModelScope.launch {
            val currentConf = config.value ?: ConfigEntity()
            repository.saveConfig(currentConf.copy(mandatoryDuesAmount = newAmount))
        }
    }

    // CRUD - MEMBERS
    fun addNewMember(name: String, whatsApp: String, status: String = "Menunggu Persetujuan") {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            repository.insertMember(MemberEntity(name = name.trim(), whatsApp = whatsApp.trim(), status = status))
            setSubScreen(SubScreen.NONE)
        }
    }

    fun updateMemberProfile(member: MemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member)
            setSubScreen(SubScreen.NONE)
        }
    }

    fun approveMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member.copy(status = "Aktif"))
        }
    }

    fun deactivateMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member.copy(status = "Tidak Aktif"))
        }
    }

    // CRUD - MANDATORY PAYMENTS
    fun addMandatoryPayment(memberId: Int, month: Int, year: Int, amount: Double, dateMs: Long, note: String) {
        viewModelScope.launch {
            val member = members.value.find { it.id == memberId } ?: return@launch
            repository.insertMandatoryPayment(
                MandatoryDuesPaymentEntity(
                    memberId = memberId,
                    memberName = member.name,
                    month = month,
                    year = year,
                    amountPaid = amount,
                    paymentDate = dateMs,
                    note = note
                )
            )
            setSubScreen(SubScreen.NONE)
        }
    }

    fun addCombinedPayment(
        memberId: Int,
        dateMs: Long,
        isMandatorySelected: Boolean,
        mandatoryMonth: Int,
        mandatoryYear: Int,
        mandatoryAmount: Double,
        mandatoryNote: String,
        isVoluntarySelected: Boolean,
        voluntaryAmount: Double,
        voluntaryNote: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val member = members.value.find { it.id == memberId } ?: return@launch
            
            if (isMandatorySelected && mandatoryAmount > 0.0) {
                repository.insertMandatoryPayment(
                    MandatoryDuesPaymentEntity(
                        memberId = memberId,
                        memberName = member.name,
                        month = mandatoryMonth,
                        year = mandatoryYear,
                        amountPaid = mandatoryAmount,
                        paymentDate = dateMs,
                        note = mandatoryNote
                    )
                )
            }
            
            if (isVoluntarySelected && voluntaryAmount > 0.0) {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = dateMs }
                val rMonth = cal.get(java.util.Calendar.MONTH) + 1
                val rYear = cal.get(java.util.Calendar.YEAR)
                val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(dateMs))
                repository.insertVoluntaryPayment(
                    VoluntaryDuesPaymentEntity(
                        memberId = memberId,
                        donorName = member.name,
                        amountPaid = voluntaryAmount,
                        paymentDate = dateMs,
                        paymentTime = timeStr,
                        note = voluntaryNote,
                        isCancelled = false,
                        reportMonth = rMonth,
                        reportYear = rYear
                    )
                )
            }
            onSuccess()
        }
    }

    fun editMandatoryPayment(id: Int, memberId: Int, month: Int, year: Int, amount: Double, dateMs: Long, note: String, isCancelled: Boolean = false) {
        viewModelScope.launch {
            val member = members.value.find { it.id == memberId } ?: return@launch
            repository.updateMandatoryPayment(
                MandatoryDuesPaymentEntity(
                    id = id,
                    memberId = memberId,
                    memberName = member.name,
                    month = month,
                    year = year,
                    amountPaid = amount,
                    paymentDate = dateMs,
                    note = note,
                    isCancelled = isCancelled
                )
            )
            setSubScreen(SubScreen.NONE)
        }
    }

    // CRUD - VOLUNTARY PAYMENTS
    fun addVoluntaryPayment(memberId: Int, donorName: String, amount: Double, dateMs: Long, timeStr: String, note: String) {
        viewModelScope.launch {
            if (donorName.isBlank()) return@launch
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = dateMs }
            val rMonth = cal.get(java.util.Calendar.MONTH) + 1
            val rYear = cal.get(java.util.Calendar.YEAR)
            repository.insertVoluntaryPayment(
                VoluntaryDuesPaymentEntity(
                    memberId = memberId,
                    donorName = donorName.trim(),
                    amountPaid = amount,
                    paymentDate = dateMs,
                    paymentTime = timeStr.trim(),
                    note = note.trim(),
                    isCancelled = false,
                    reportMonth = rMonth,
                    reportYear = rYear
                )
            )
            setSubScreen(SubScreen.NONE)
        }
    }

    fun editVoluntaryPayment(id: Int, memberId: Int, donorName: String, amount: Double, dateMs: Long, timeStr: String, note: String, isCancelled: Boolean = false) {
        viewModelScope.launch {
            if (donorName.isBlank()) return@launch
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = dateMs }
            val rMonth = cal.get(java.util.Calendar.MONTH) + 1
            val rYear = cal.get(java.util.Calendar.YEAR)
            repository.updateVoluntaryPayment(
                VoluntaryDuesPaymentEntity(
                    id = id,
                    memberId = memberId,
                    donorName = donorName.trim(),
                    amountPaid = amount,
                    paymentDate = dateMs,
                    paymentTime = timeStr.trim(),
                    note = note.trim(),
                    isCancelled = isCancelled,
                    reportMonth = rMonth,
                    reportYear = rYear
                )
            )
            setSubScreen(SubScreen.NONE)
        }
    }

    fun deleteVoluntaryPayment(payment: VoluntaryDuesPaymentEntity) {
        viewModelScope.launch {
            repository.deleteVoluntaryPayment(payment)
            setSubScreen(SubScreen.NONE)
        }
    }

    // CRUD - OTHER INCOMES
    fun addOtherIncome(source: String, amount: Double, dateMs: Long, note: String) {
        viewModelScope.launch {
            if (source.isBlank()) return@launch
            repository.insertOtherIncome(
                OtherIncomeEntity(
                    source = source.trim(),
                    amount = amount,
                    paymentDate = dateMs,
                    note = note.trim()
                )
            )
            setSubScreen(SubScreen.NONE)
        }
    }

    fun editOtherIncome(id: Int, source: String, amount: Double, dateMs: Long, note: String, isCancelled: Boolean = false) {
        viewModelScope.launch {
            if (source.isBlank()) return@launch
            repository.updateOtherIncome(
                OtherIncomeEntity(
                    id = id,
                    source = source.trim(),
                    amount = amount,
                    paymentDate = dateMs,
                    note = note.trim(),
                    isCancelled = isCancelled
                )
            )
            setSubScreen(SubScreen.NONE)
        }
    }

    // CRUD - EXPENSES
    fun addExpense(
        category: String,
        amount: Double,
        dateMs: Long,
        recipient: String,
        note: String,
        time: String = "",
        memberId: Int = 0
    ) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply { timeInMillis = dateMs }
            val rMonth = cal.get(Calendar.MONTH) + 1
            val rYear = cal.get(Calendar.YEAR)
            repository.insertExpense(
                ExpenseEntity(
                    category = category,
                    amount = amount,
                    expenseDate = dateMs,
                    recipient = recipient.trim(),
                    note = note.trim(),
                    isCancelled = false,
                    expenseTime = time.trim(),
                    reportMonth = rMonth,
                    reportYear = rYear,
                    recipientName = recipient.trim(),
                    notes = note.trim(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    memberId = memberId
                )
            )
            setSubScreen(SubScreen.NONE)
        }
    }

    fun editExpense(
        id: Int,
        category: String,
        amount: Double,
        dateMs: Long,
        recipient: String,
        note: String,
        isCancelled: Boolean = false,
        time: String = "",
        memberId: Int = 0,
        createdAt: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply { timeInMillis = dateMs }
            val rMonth = cal.get(Calendar.MONTH) + 1
            val rYear = cal.get(Calendar.YEAR)
            repository.updateExpense(
                ExpenseEntity(
                    id = id,
                    category = category,
                    amount = amount,
                    expenseDate = dateMs,
                    recipient = recipient.trim(),
                    note = note.trim(),
                    isCancelled = isCancelled,
                    expenseTime = time.trim(),
                    reportMonth = rMonth,
                    reportYear = rYear,
                    recipientName = recipient.trim(),
                    notes = note.trim(),
                    createdAt = createdAt,
                    updatedAt = System.currentTimeMillis(),
                    memberId = memberId
                )
            )
            setSubScreen(SubScreen.NONE)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // CRUD - AGENDAS
    fun addAgenda(title: String, dateMs: Long, timeStr: String, location: String, description: String, status: String) {
        viewModelScope.launch {
            if (title.isBlank()) return@launch
            repository.insertAgenda(
                AgendaEntity(
                    title = title.trim(),
                    date = dateMs,
                    time = timeStr.trim(),
                    location = location.trim(),
                    description = description.trim(),
                    status = status
                )
            )
            setSubScreen(SubScreen.NONE)
        }
    }

    fun editAgenda(id: Int, title: String, dateMs: Long, timeStr: String, location: String, description: String, minutes: String, status: String) {
        viewModelScope.launch {
            if (title.isBlank()) return@launch
            repository.updateAgenda(
                AgendaEntity(
                    id = id,
                    title = title.trim(),
                    date = dateMs,
                    time = timeStr.trim(),
                    location = location.trim(),
                    description = description.trim(),
                    meetingMinutes = minutes.trim(),
                    status = status
                )
            )
            setSubScreen(SubScreen.NONE)
        }
    }

    // Backup & Restore handlers
    fun triggerLocalBackup(onFinished: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repository.backupDataToJson()
                onFinished(data)
            } catch (e: Exception) {
                onFinished(null)
            }
        }
    }

    fun triggerLocalRestore(jsonString: String, overwrite: Boolean = true, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.restoreDataFromJson(jsonString, overwrite)
            if (result) {
                refreshAllData()
            }
            onFinished(result)
        }
    }

    private val _refreshTrigger = MutableStateFlow(0L)
    val refreshTrigger: StateFlow<Long> = _refreshTrigger.asStateFlow()

    fun refreshReports() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun refreshAllData() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    // Combined live data computation for Special Payment Report (Anggota Sudah Membayar)
    val specialPaymentReportState: StateFlow<Pair<List<PaymentReportItem>, SpecialReportSummary>> = combine(
        selectedMonth,
        selectedYear,
        reportSearchQuery,
        reportFilterType,
        members,
        mandatoryPayments,
        voluntaryPayments,
        refreshTrigger
    ) { flows ->
        val month = flows[0] as Int
        val year = flows[1] as Int
        val search = flows[2] as String
        val filter = flows[3] as String
        @Suppress("UNCHECKED_CAST")
        val memberList = flows[4] as List<MemberEntity>
        @Suppress("UNCHECKED_CAST")
        val mandatoryList = flows[5] as List<MandatoryDuesPaymentEntity>
        @Suppress("UNCHECKED_CAST")
        val voluntaryList = flows[6] as List<VoluntaryDuesPaymentEntity>
        // flows[7] is the refresh trigger, which forces recompute of the state flow when updated
        
        // Month name helper
        val monthName = getIndonesianMonthName(month)
        
        // Filter elements in this specific month/year
        val currentPeriodMandatory = mandatoryList.filter { !it.isCancelled && it.month == month && it.year == year }
        val currentPeriodVoluntary = voluntaryList.filter { !it.isCancelled && isDateInPeriod(it.paymentDate, month, year) }

        // Compute items per member / donor
        val reportItems = mutableListOf<PaymentReportItem>()

        // 1. Collect members who are active OR paid in this period
        val activeOrPaidMembers = memberList.filter { it.status == "Aktif" || currentPeriodMandatory.any { p -> p.memberId == it.id } }

        // 2. Group voluntary payments in this period by donor name (trimmed, case-insensitive)
        val voluntaryByDonor = currentPeriodVoluntary.groupBy { it.donorName.trim().lowercase() }

        // 3. Collect all unique names (combining member names and voluntary donor names)
        val memberNamesLower = activeOrPaidMembers.map { it.name.trim().lowercase() }
        val allNamesLower = (memberNamesLower + voluntaryByDonor.keys).distinct()

        for (nameKey in allNamesLower) {
            // Find member if exists
            val member = activeOrPaidMembers.find { it.name.trim().lowercase() == nameKey }
            
            // Determine display name
            val displayName = member?.name ?: (voluntaryByDonor[nameKey]?.firstOrNull()?.donorName ?: nameKey)

            // Check if matches search query
            if (search.isNotBlank() && !displayName.contains(search, ignoreCase = true)) {
                continue
            }

            // Calculate mandatory dues
            val memberMandatoryPayments = if (member != null) {
                currentPeriodMandatory.filter { it.memberId == member.id }
            } else {
                emptyList()
            }
            val memberMandatoryTotal = memberMandatoryPayments.sumOf { it.amountPaid }

            // Calculate voluntary dues
            val memberVoluntaryPayments = voluntaryByDonor[nameKey] ?: emptyList()
            val memberVoluntaryTotal = memberVoluntaryPayments.sumOf { it.amountPaid }

            // Skip if no payments occurred in this period
            if (memberMandatoryTotal == 0.0 && memberVoluntaryTotal == 0.0) {
                continue
            }

            // Status check
            val hasMandatory = memberMandatoryTotal > 0.0
            val hasVoluntary = memberVoluntaryTotal > 0.0
            val statusStr = when {
                hasMandatory && hasVoluntary -> "Keduanya"
                hasMandatory -> "Iuran Wajib"
                else -> "Sukarela Saja"
            }

            // Filter on billing status selection
            if (filter == "Iuran Wajib" && !hasMandatory) continue
            if (filter == "Iuran Sukarela" && !hasVoluntary) continue
            if (filter == "Keduanya" && (!hasMandatory || !hasVoluntary)) continue

            // Last payment date calculation
            val lastDateMs = listOf(
                memberMandatoryPayments.map { it.paymentDate }.maxOrNull(),
                memberVoluntaryPayments.map { it.paymentDate }.maxOrNull()
            ).filterNotNull().maxOrNull() ?: 0L

            val lastDateStr = if (lastDateMs > 0L) {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(lastDateMs))
            } else {
                "-"
            }

            reportItems.add(
                PaymentReportItem(
                    nomor = 0, // Assigned later after filtering is completed
                    name = displayName,
                    mandatoryPaid = memberMandatoryTotal,
                    voluntaryPaid = memberVoluntaryTotal,
                    totalPaid = memberMandatoryTotal + memberVoluntaryTotal,
                    lastPaymentDateStr = lastDateStr,
                    statusStr = statusStr
                )
            )
        }

        // Apply dynamic re-numbering
        reportItems.forEachIndexed { index, item ->
            reportItems[index] = item.copy(nomor = index + 1)
        }

        // Compute summary metrics for report box
        val numMandatoryPayers = reportItems.count { it.mandatoryPaid > 0.0 }
        val numVoluntaryPayers = reportItems.count { it.voluntaryPaid > 0.0 }
        val totalMandatoryCollected = reportItems.sumOf { it.mandatoryPaid }
        val totalVoluntaryCollected = reportItems.sumOf { it.voluntaryPaid }
        val totalFundsReceived = totalMandatoryCollected + totalVoluntaryCollected

        val summary = SpecialReportSummary(
            numMandatoryPayers = numMandatoryPayers,
            numVoluntaryPayers = numVoluntaryPayers,
            totalMandatoryCollected = totalMandatoryCollected,
            totalVoluntaryCollected = totalVoluntaryCollected,
            totalFundsReceived = totalFundsReceived
        )

        Pair(reportItems.toList(), summary)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(emptyList<PaymentReportItem>(), SpecialReportSummary(0, 0, 0.0, 0.0, 0.0)))

    // Export Trigger
    fun exportReportToPdf(context: Context, shareToWhatsApp: Boolean = false) {
        viewModelScope.launch {
            val (list, summary) = specialPaymentReportState.value
            val monthName = getIndonesianMonthName(selectedMonth.value)
            ExportHelper.exportSpecialReportToPdf(
                context = context,
                month = selectedMonth.value,
                year = selectedYear.value,
                monthName = monthName,
                reportItems = list,
                summary = summary,
                mandatoryPayments = mandatoryPayments.value,
                voluntaryPayments = voluntaryPayments.value,
                otherIncomes = otherIncomes.value,
                expenses = expenses.value,
                shareToWhatsApp = shareToWhatsApp
            )
        }
    }

    fun exportReportToExcel(context: Context, shareToWhatsApp: Boolean = false) {
        viewModelScope.launch {
            val (list, summary) = specialPaymentReportState.value
            val monthName = getIndonesianMonthName(selectedMonth.value)
            ExportHelper.exportSpecialReportToExcel(
                context = context,
                monthName = monthName,
                year = selectedYear.value,
                reportItems = list,
                summary = summary,
                shareToWhatsApp = shareToWhatsApp
            )
        }
    }

    fun getIndonesianMonthName(month: Int): String {
        return when (month) {
            1 -> "Januari"
            2 -> "Februari"
            3 -> "Maret"
            4 -> "April"
            5 -> "Mei"
            6 -> "Juni"
            7 -> "Juli"
            8 -> "Agustus"
            9 -> "September"
            10 -> "Oktober"
            11 -> "November"
            12 -> "Desember"
            else -> ""
        }
    }
}
