package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first

class Repository(private val db: AppDatabase) {

    val configFlow: Flow<ConfigEntity?> = db.configDao().getConfigFlow()
    val membersFlow: Flow<List<MemberEntity>> = db.memberDao().getAllMembersFlow()
    val mandatoryPaymentsFlow: Flow<List<MandatoryDuesPaymentEntity>> = db.mandatoryDuesDao().getAllPaymentsFlow()
    val voluntaryPaymentsFlow: Flow<List<VoluntaryDuesPaymentEntity>> = db.voluntaryDuesDao().getAllPaymentsFlow()
    val otherIncomesFlow: Flow<List<OtherIncomeEntity>> = db.otherIncomeDao().getAllIncomesFlow()
    val expensesFlow: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpensesFlow()
    val agendasFlow: Flow<List<AgendaEntity>> = db.agendaDao().getAllAgendasFlow()

    suspend fun saveConfig(config: ConfigEntity) {
        db.configDao().saveConfig(config)
    }

    suspend fun getConfig(): ConfigEntity? {
        return db.configDao().getConfig()
    }

    suspend fun insertMember(member: MemberEntity): Long {
        return db.memberDao().insertMember(member)
    }

    suspend fun updateMember(member: MemberEntity) {
        db.memberDao().updateMember(member)
    }

    suspend fun insertMandatoryPayment(payment: MandatoryDuesPaymentEntity): Long {
        return db.mandatoryDuesDao().insertPayment(payment)
    }

    suspend fun updateMandatoryPayment(payment: MandatoryDuesPaymentEntity) {
        db.mandatoryDuesDao().updatePayment(payment)
    }

    suspend fun insertVoluntaryPayment(payment: VoluntaryDuesPaymentEntity): Long {
        return db.voluntaryDuesDao().insertPayment(payment)
    }

    suspend fun updateVoluntaryPayment(payment: VoluntaryDuesPaymentEntity) {
        db.voluntaryDuesDao().updatePayment(payment)
    }

    suspend fun insertOtherIncome(income: OtherIncomeEntity): Long {
        return db.otherIncomeDao().insertIncome(income)
    }

    suspend fun updateOtherIncome(income: OtherIncomeEntity) {
        db.otherIncomeDao().updateIncome(income)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return db.expenseDao().insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        db.expenseDao().updateExpense(expense)
    }

    suspend fun insertAgenda(agenda: AgendaEntity): Long {
        return db.agendaDao().insertAgenda(agenda)
    }

    suspend fun updateAgenda(agenda: AgendaEntity) {
        db.agendaDao().updateAgenda(agenda)
    }

    suspend fun seedInitialDataIfEmpty() {
        // 1. Seed Config if empty
        val existingConfig = db.configDao().getConfig()
        if (existingConfig == null) {
            db.configDao().saveConfig(ConfigEntity(pin = "123456", mandatoryDuesAmount = 10000.0))
        }

        // 2. Seed Members if empty
        val membersList = db.memberDao().getAllMembersFlow().first()
        if (membersList.isEmpty()) {
            val initialMembers = listOf(
                "Lukman Hanafi", "Dole DAT", "Kaje", "Erionan", "Gultom",
                "Yobby R", "Saeman", "Ardiansyah", "Arik", "Bagas",
                "Ibnu", "Santo", "Daus", "Legan", "Daeng", "Kris",
                "Pali", "Toton"
            )
            for (name in initialMembers) {
                db.memberDao().insertMember(
                    MemberEntity(
                        name = name,
                        whatsApp = "08123456789",
                        status = "Aktif"
                    )
                )
            }
        }
    }

    // Backup current data as JSON text
    suspend fun backupDataToJson(): String {
        val config = db.configDao().getConfig() ?: ConfigEntity()
        val members = db.memberDao().getAllMembersFlow().first()
        val mandatory = db.mandatoryDuesDao().getAllPaymentsFlow().first()
        val voluntary = db.voluntaryDuesDao().getAllPaymentsFlow().first()
        val other = db.otherIncomeDao().getAllIncomesFlow().first()
        val expenses = db.expenseDao().getAllExpensesFlow().first()
        val agendas = db.agendaDao().getAllAgendasFlow().first()

        // We will build a simple custom JSON string representation
        val sb = java.lang.StringBuilder()
        sb.append("{\n")
        
        // Config
        sb.append("  \"config\": {\"pin\":\"${config.pin}\",\"amount\":${config.mandatoryDuesAmount}},\n")
        
        // Members
        sb.append("  \"members\": [\n")
        members.forEachIndexed { i, m ->
            sb.append("    {\"id\":${m.id},\"name\":\"${escapeJson(m.name)}\",\"whatsApp\":\"${escapeJson(m.whatsApp)}\",\"status\":\"${m.status}\",\"createdAt\":${m.createdAt}}")
            if (i < members.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ],\n")

        // Mandatory dues
        sb.append("  \"mandatory\": [\n")
        mandatory.forEachIndexed { i, p ->
            sb.append("    {\"id\":${p.id},\"memberId\":${p.memberId},\"memberName\":\"${escapeJson(p.memberName)}\",\"month\":${p.month},\"year\":${p.year},\"amount\":${p.amountPaid},\"date\":${p.paymentDate},\"note\":\"${escapeJson(p.note)}\",\"isCancelled\":${p.isCancelled}}")
            if (i < mandatory.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ],\n")

        // Voluntary dues
        sb.append("  \"voluntary\": [\n")
        voluntary.forEachIndexed { i, p ->
            sb.append("    {\"id\":${p.id},\"donorName\":\"${escapeJson(p.donorName)}\",\"amount\":${p.amountPaid},\"date\":${p.paymentDate},\"time\":\"${escapeJson(p.paymentTime)}\",\"note\":\"${escapeJson(p.note)}\",\"isCancelled\":${p.isCancelled}}")
            if (i < voluntary.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ],\n")

        // Other income
        sb.append("  \"other\": [\n")
        other.forEachIndexed { i, p ->
            sb.append("    {\"id\":${p.id},\"source\":\"${escapeJson(p.source)}\",\"amount\":${p.amount},\"date\":${p.paymentDate},\"note\":\"${escapeJson(p.note)}\",\"isCancelled\":${p.isCancelled}}")
            if (i < other.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ],\n")

        // Expenses
        sb.append("  \"expenses\": [\n")
        expenses.forEachIndexed { i, e ->
            sb.append("    {\"id\":${e.id},\"category\":\"${escapeJson(e.category)}\",\"amount\":${e.amount},\"date\":${e.expenseDate},\"recipient\":\"${escapeJson(e.recipient)}\",\"note\":\"${escapeJson(e.note)}\",\"isCancelled\":${e.isCancelled}}")
            if (i < expenses.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ],\n")

        // Agendas
        sb.append("  \"agendas\": [\n")
        agendas.forEachIndexed { i, a ->
            sb.append("    {\"id\":${a.id},\"title\":\"${escapeJson(a.title)}\",\"date\":${a.date},\"time\":\"${escapeJson(a.time)}\",\"location\":\"${escapeJson(a.location)}\",\"description\":\"${escapeJson(a.description)}\",\"meetingMinutes\":\"${escapeJson(a.meetingMinutes)}\",\"status\":\"${escapeJson(a.status)}\"}")
            if (i < agendas.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ]\n")

        sb.append("}")
        return sb.toString()
    }

    // Restore data from JSON
    suspend fun restoreDataFromJson(json: String): Boolean {
        return try {
            // Very simple parser using org.json.JSONObject (built into Android SDK, no external library required!)
            val obj = org.json.JSONObject(json)
            
            // 1. Config
            if (obj.has("config")) {
                val cObj = obj.getJSONObject("config")
                db.configDao().saveConfig(
                    ConfigEntity(
                        pin = cObj.optString("pin", "123456"),
                        mandatoryDuesAmount = cObj.optDouble("amount", 10000.0)
                    )
                )
            }

            // Clear and Restore table contents
            db.runInTransaction {
                // We will clear existing tables in transaction
                db.openHelper.writableDatabase.execSQL("DELETE FROM members")
                db.openHelper.writableDatabase.execSQL("DELETE FROM mandatory_dues")
                db.openHelper.writableDatabase.execSQL("DELETE FROM voluntary_dues")
                db.openHelper.writableDatabase.execSQL("DELETE FROM other_income")
                db.openHelper.writableDatabase.execSQL("DELETE FROM expenses")
                db.openHelper.writableDatabase.execSQL("DELETE FROM agendas")

                // 2. Members
                if (obj.has("members")) {
                    val arr = obj.getJSONArray("members")
                    for (i in 0 until arr.length()) {
                        val m = arr.getJSONObject(i)
                        db.openHelper.writableDatabase.execSQL(
                            "INSERT OR REPLACE INTO members (id, name, whatsApp, status, createdAt) VALUES (?, ?, ?, ?, ?)",
                            arrayOf(m.getInt("id"), m.getString("name"), m.getString("whatsApp"), m.getString("status"), m.getLong("createdAt"))
                        )
                    }
                }

                // 3. Mandatory
                if (obj.has("mandatory")) {
                    val arr = obj.getJSONArray("mandatory")
                    for (i in 0 until arr.length()) {
                        val p = arr.getJSONObject(i)
                        db.openHelper.writableDatabase.execSQL(
                            "INSERT OR REPLACE INTO mandatory_dues (id, memberId, memberName, month, year, amountPaid, paymentDate, note, isCancelled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            arrayOf(p.getInt("id"), p.getInt("memberId"), p.getString("memberName"), p.getInt("month"), p.getInt("year"), p.getDouble("amount"), p.getLong("date"), p.getString("note"), if (p.getBoolean("isCancelled")) 1 else 0)
                        )
                    }
                }

                // 4. Voluntary
                if (obj.has("voluntary")) {
                    val arr = obj.getJSONArray("voluntary")
                    for (i in 0 until arr.length()) {
                        val p = arr.getJSONObject(i)
                        db.openHelper.writableDatabase.execSQL(
                            "INSERT OR REPLACE INTO voluntary_dues (id, donorName, amountPaid, paymentDate, paymentTime, note, isCancelled) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            arrayOf(p.getInt("id"), p.getString("donorName"), p.getDouble("amount"), p.getLong("date"), p.getString("time"), p.getString("note"), if (p.getBoolean("isCancelled")) 1 else 0)
                        )
                    }
                }

                // 5. Other Income
                if (obj.has("other")) {
                    val arr = obj.getJSONArray("other")
                    for (i in 0 until arr.length()) {
                        val p = arr.getJSONObject(i)
                        db.openHelper.writableDatabase.execSQL(
                            "INSERT OR REPLACE INTO other_income (id, source, amount, paymentDate, note, isCancelled) VALUES (?, ?, ?, ?, ?, ?)",
                            arrayOf(p.getInt("id"), p.getString("source"), p.getDouble("amount"), p.getLong("date"), p.getString("note"), if (p.getBoolean("isCancelled")) 1 else 0)
                        )
                    }
                }

                // 6. Expenses
                if (obj.has("expenses")) {
                    val arr = obj.getJSONArray("expenses")
                    for (i in 0 until arr.length()) {
                        val e = arr.getJSONObject(i)
                        db.openHelper.writableDatabase.execSQL(
                            "INSERT OR REPLACE INTO expenses (id, category, amount, expenseDate, recipient, note, isCancelled) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            arrayOf(e.getInt("id"), e.getString("category"), e.getDouble("amount"), e.getLong("date"), e.getString("recipient"), e.getString("note"), if (e.getBoolean("isCancelled")) 1 else 0)
                        )
                    }
                }

                // 7. Agendas
                if (obj.has("agendas")) {
                    val arr = obj.getJSONArray("agendas")
                    for (i in 0 until arr.length()) {
                        val a = arr.getJSONObject(i)
                        db.openHelper.writableDatabase.execSQL(
                            "INSERT OR REPLACE INTO agendas (id, title, date, time, location, description, meetingMinutes, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                            arrayOf(a.getInt("id"), a.getString("title"), a.getLong("date"), a.getString("time"), a.getString("location"), a.getString("description"), a.optString("meetingMinutes", ""), a.getString("status"))
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
    }
}
