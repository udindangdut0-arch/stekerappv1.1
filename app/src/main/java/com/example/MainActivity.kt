package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.TextStyle
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppMainScreen(viewModel)
                }
            }
        }
    }
}

// Utility to format currency in Indonesian Rupiah
fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(amount).replace("Rp", "Rp ").replace(",00", "")
}

// Utility to format dates
fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "-"
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

@Composable
fun AppMainScreen(viewModel: AppViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        PinLoginScreen(viewModel)
    } else {
        MainShellScreen(viewModel)
    }
}

// ==========================================
// SCREEN 1: PIN LOGIN SCREEN
// ==========================================
@Composable
fun PinLoginScreen(viewModel: AppViewModel) {
    var pinInput by remember { mutableStateOf("") }
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.steker_logo),
                contentDescription = "Logo Steker Hitam",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("login_logo"),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Steker App v1.5",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.testTag("login_title")
            )

            Text(
                text = "Satu Aplikasi untuk Semua Kebutuhan",
                fontSize = 14.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Steker Hitam",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // PIN Form
            Text(
                text = "Masukkan PIN Pengurus",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 6-digit PIN Input
            OutlinedTextField(
                value = pinInput,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        pinInput = it
                        if (it.length == 6) {
                            viewModel.loginWithPin(it)
                        }
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                placeholder = { Text("••••••", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                isError = pinError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp, letterSpacing = 8.sp),
                modifier = Modifier
                    .width(180.dp)
                    .testTag("pin_input")
            )

            if (pinError != null) {
                Text(
                    text = pinError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .testTag("pin_error_msg")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (pinInput.length == 6) {
                        viewModel.loginWithPin(pinInput)
                    } else {
                        Toast.makeText(context, "PIN harus 6 digit", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(180.dp)
                    .height(48.dp)
                    .testTag("login_button")
            ) {
                Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "PIN Bawaan: 123456",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}

// ==========================================
// CORE SHELL: MAIN SHELL
// ==========================================
@Composable
fun MainShellScreen(viewModel: AppViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val subScreen by viewModel.subScreen.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Back button handling
    BackHandler(enabled = subScreen != SubScreen.NONE) {
        if (subScreen == SubScreen.ADD_MEMBER_PAYMENT) {
            viewModel.setSubScreen(SubScreen.MEMBER_PROFILE)
        } else {
            viewModel.setSubScreen(SubScreen.NONE)
        }
    }

    Scaffold(
        topBar = {
            if (subScreen == SubScreen.NONE) {
                MainHeader(viewModel)
            } else {
                SubHeader(viewModel)
            }
        },
        bottomBar = {
            if (subScreen == SubScreen.NONE) {
                MainBottomBar(viewModel, currentTab)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (subScreen != SubScreen.NONE) {
                // Navigate to forms/subscreens
                when (subScreen) {
                    SubScreen.ADD_MEMBER -> AddMemberScreen(viewModel)
                    SubScreen.EDIT_MEMBER -> EditMemberScreen(viewModel)
                    SubScreen.MEMBER_PROFILE -> MemberProfileScreen(viewModel)
                    SubScreen.ADD_MEMBER_PAYMENT -> AddMemberPaymentScreen(viewModel)
                    SubScreen.ADD_MANDATORY -> AddMandatoryScreen(viewModel)
                    SubScreen.ADD_VOLUNTARY -> AddVoluntaryScreen(viewModel)
                    SubScreen.EDIT_VOLUNTARY -> EditVoluntaryScreen(viewModel)
                    SubScreen.ADD_EXPENSE -> AddExpenseScreen(viewModel)
                    SubScreen.EDIT_EXPENSE -> EditExpenseScreen(viewModel)
                    SubScreen.ADD_OTHER_INCOME -> AddOtherIncomeScreen(viewModel)
                    SubScreen.SETTINGS -> SettingsScreen(viewModel)
                    SubScreen.BACKUP_DATA -> BackupDataScreen(viewModel)
                    else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Halaman tidak ditemukan") }
                }
            } else {
                // Main Tab Routing
                when (currentTab) {
                    AppTab.BERANDA -> BerandaTabScreen(viewModel)
                    AppTab.ANGGOTA -> AnggotaTabScreen(viewModel)
                    AppTab.KEUANGAN -> KeuanganTabScreen(viewModel)
                    AppTab.LAPORAN -> LaporanTabScreen(viewModel)
                    AppTab.LAINNYA -> LainnyaTabScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainHeader(viewModel: AppViewModel) {
    Surface(
        color = CharcoalDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CrimsonPrimary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.steker_logo),
                    contentDescription = "Header Logo",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "STEKER HITAM",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhitePrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "STEKER APP V1.3",
                    fontSize = 9.sp,
                    color = TextWhiteSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }

            IconButton(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CharcoalSurface)
                    .testTag("logout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Keluar",
                    tint = TextWhiteSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SubHeader(viewModel: AppViewModel) {
    val subScreen by viewModel.subScreen.collectAsStateWithLifecycle()
    val title = when (subScreen) {
        SubScreen.ADD_MEMBER -> "Tambah Anggota"
        SubScreen.EDIT_MEMBER -> "Edit Anggota"
        SubScreen.MEMBER_PROFILE -> "Profil Anggota"
        SubScreen.ADD_MEMBER_PAYMENT -> "Tambah Pembayaran"
        SubScreen.ADD_MANDATORY -> "Tambah Iuran Wajib"
        SubScreen.ADD_VOLUNTARY -> "Tambah Iuran Sukarela"
        SubScreen.EDIT_VOLUNTARY -> "Edit Iuran Sukarela"
        SubScreen.ADD_EXPENSE -> "Tambah Pengeluaran"
        SubScreen.EDIT_EXPENSE -> "Edit Pengeluaran"
        SubScreen.ADD_OTHER_INCOME -> "Tambah Pemasukan Lain"
        SubScreen.SETTINGS -> "Pengaturan Aplikasi"
        SubScreen.BACKUP_DATA -> "Backup & Pindah Data"
        else -> "Steker App"
    }

    Surface(
        color = CharcoalDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (subScreen == SubScreen.ADD_MEMBER_PAYMENT) {
                        viewModel.setSubScreen(SubScreen.MEMBER_PROFILE)
                    } else {
                        viewModel.setSubScreen(SubScreen.NONE)
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CharcoalSurface)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = TextWhitePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title.uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = TextWhitePrimary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MainBottomBar(viewModel: AppViewModel, currentTab: AppTab) {
    NavigationBar(
        containerColor = BottomNavBg,
        tonalElevation = 0.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = CharcoalBorder,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        val items = listOf(
            Triple(AppTab.BERANDA, Icons.Default.Home, "Beranda"),
            Triple(AppTab.ANGGOTA, Icons.Default.People, "Anggota"),
            Triple(AppTab.KEUANGAN, Icons.Default.AccountBalanceWallet, "Keuangan"),
            Triple(AppTab.LAPORAN, Icons.Default.Assessment, "Laporan"),
            Triple(AppTab.LAINNYA, Icons.Default.Menu, "Lainnya")
        )

        items.forEach { (tab, icon, label) ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { viewModel.setTab(tab) },
                icon = { Icon(imageVector = icon, contentDescription = label) },
                label = { 
                    Text(
                        text = label, 
                        fontSize = 10.sp, 
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CrimsonPrimary,
                    selectedTextColor = CrimsonPrimary,
                    indicatorColor = CrimsonPrimary.copy(alpha = 0.1f),
                    unselectedIconColor = TextWhiteSecondary,
                    unselectedTextColor = TextWhiteSecondary
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}

// ==========================================
// TAB 1: BERANDA SCREEN
// ==========================================
@Composable
fun BerandaTabScreen(viewModel: AppViewModel) {
    val totals by viewModel.totalsState.collectAsStateWithLifecycle()
    val agendas by viewModel.agendas.collectAsStateWithLifecycle()

    // Find nearest upcoming agenda
    val nearestAgenda = agendas
        .filter { it.status == "Akan Datang" }
        .minByOrNull { it.date }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Halo, Menyadik!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Satu Aplikasi untuk Semua Kebutuhan",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
        }

        // Saldo Kas Card
        item {
            val balance = totals?.netBalance ?: 0.0
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CharcoalSurface, CharcoalCard)
                        )
                    )
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
                    .drawBehind {
                        // Soft red ambient glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(CrimsonPrimary.copy(alpha = 0.08f), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                radius = 120.dp.toPx()
                            ),
                            center = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            radius = 120.dp.toPx()
                        )
                    }
                    .testTag("saldo_kas_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "SALDO KAS SAAT INI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhiteSecondary,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Rp",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary,
                            modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)
                        )
                        val formattedBalanceStr = formatRupiah(balance).replace("Rp ", "").replace("Rp", "")
                        Text(
                            text = formattedBalanceStr,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhitePrimary,
                            letterSpacing = (-1).sp,
                            modifier = Modifier.testTag("saldo_amount")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(StatusSuccess)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Satu Aplikasi untuk Semua Kebutuhan",
                            fontSize = 11.sp,
                            color = StatusSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subtle divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CharcoalBorder)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TOTAL PEMASUKAN", fontSize = 9.sp, color = TextWhiteSecondary, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            val totalIn = (totals?.totalMandatory ?: 0.0) + (totals?.totalVoluntary ?: 0.0) + (totals?.totalOther ?: 0.0)
                            Text(formatRupiah(totalIn), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhitePrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL PENGELUARAN", fontSize = 9.sp, color = TextWhiteSecondary, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Text(formatRupiah(totals?.totalExpenses ?: 0.0), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhitePrimary)
                        }
                    }
                }
            }
        }

        // Ringkasan Bulanan (Grid style)
        item {
            Text(
                text = "Ringkasan Bulan Ini",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mandatory Ringkasan
                Box(modifier = Modifier.weight(1f)) {
                    RingkasanMiniCard(
                        title = "Iuran Wajib",
                        value = formatRupiah(totals?.mandatoryThisMonth ?: 0.0),
                        subValue = "Terbayar",
                        backgroundColor = CharcoalSurface
                    )
                }

                // Voluntary Ringkasan
                Box(modifier = Modifier.weight(1f)) {
                    RingkasanMiniCard(
                        title = "Iuran Sukarela",
                        value = formatRupiah(totals?.voluntaryThisMonth ?: 0.0),
                        subValue = "Terkumpul",
                        backgroundColor = CharcoalSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Expenses Ringkasan
                Box(modifier = Modifier.weight(1f)) {
                    RingkasanMiniCard(
                        title = "Pengeluaran",
                        value = formatRupiah(totals?.expensesThisMonth ?: 0.0),
                        subValue = "Bulan Ini",
                        backgroundColor = CharcoalSurface
                    )
                }

                // Member Ringkasan
                val membersList by viewModel.members.collectAsStateWithLifecycle()
                val activeCount = membersList.count { it.status == "Aktif" }
                Box(modifier = Modifier.weight(1f)) {
                    RingkasanMiniCard(
                        title = "Anggota Aktif",
                        value = "$activeCount Orang",
                        subValue = "Status Aktif",
                        backgroundColor = CharcoalSurface
                    )
                }
            }
        }

        // Tombol Cepat (Quick Actions)
        item {
            Text(
                text = "Aksi Cepat Pengurus",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.AddCard,
                    label = "Tambah Iuran Wajib",
                    onClick = { viewModel.setSubScreen(SubScreen.ADD_MANDATORY) },
                    modifier = Modifier.weight(1f)
                )

                QuickActionButton(
                    icon = Icons.Default.VolunteerActivism,
                    label = "Iuran Sukarela",
                    onClick = { viewModel.setSubScreen(SubScreen.ADD_VOLUNTARY) },
                    modifier = Modifier.weight(1f)
                )

                QuickActionButton(
                    icon = Icons.Default.LocalMall,
                    label = "Pengeluaran Kas",
                    onClick = { viewModel.setSubScreen(SubScreen.ADD_EXPENSE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Agenda Terdekat Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AGENDA TERDEKAT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhiteSecondary,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CrimsonPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "BARU",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (nearestAgenda != null) {
                val sdfMonth = remember(nearestAgenda.date) { SimpleDateFormat("MMM", Locale("id", "ID")) }
                val sdfDay = remember(nearestAgenda.date) { SimpleDateFormat("dd", Locale("id", "ID")) }
                val monthStr = sdfMonth.format(Date(nearestAgenda.date)).uppercase()
                val dayStr = sdfDay.format(Date(nearestAgenda.date))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CharcoalSurface.copy(alpha = 0.4f))
                        .border(
                            width = 1.dp,
                            color = CharcoalBorderMedium,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date Block Card
                        Column(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalSurface)
                                .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp)),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = monthStr,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhiteSecondary
                            )
                            Text(
                                text = dayStr,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhitePrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nearestAgenda.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${nearestAgenda.location} • ${nearestAgenda.time}",
                                fontSize = 11.sp,
                                color = TextWhiteSecondary
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CharcoalSurface.copy(alpha = 0.4f))
                        .border(
                            width = 1.dp,
                            color = CharcoalBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada agenda terdekat saat ini.",
                        fontSize = 12.sp,
                        color = TextWhiteSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun RingkasanMiniCard(title: String, value: String, subValue: String, backgroundColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CharcoalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subValue, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CharcoalBorder),
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

// ==========================================
// TAB 2: ANGGOTA SCREEN & OPERATIONS
// ==========================================
@Composable
fun AnggotaTabScreen(viewModel: AppViewModel) {
    val membersList by viewModel.members.collectAsStateWithLifecycle()
    var searchTxt by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") } // "Semua", "Aktif", "Menunggu Persetujuan", "Tidak Aktif"

    val filteredMembers = membersList.filter {
        val matchesSearch = it.name.contains(searchTxt, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Semua" -> true
            "Aktif" -> it.status == "Aktif"
            "Menunggu Persetujuan" -> it.status == "Menunggu Persetujuan"
            "Tidak Aktif" -> it.status == "Tidak Aktif"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchTxt,
                onValueChange = { searchTxt = it },
                placeholder = { Text("Cari anggota...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("member_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontally scrollable status filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Semua", "Aktif", "Menunggu Persetujuan", "Tidak Aktif").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val chipColor = if (isSelected) MaterialTheme.colorScheme.primary else CharcoalSurface
                    val textColor = if (isSelected) Color.White else Color.LightGray
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipColor)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(text = filter, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Count summary
            Text(
                text = "Jumlah Ditemukan: ${filteredMembers.size} Orang",
                fontSize = 12.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Members list
            if (filteredMembers.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredMembers) { m ->
                        MemberItemCard(
                            member = m,
                            onClick = { viewModel.selectMember(m.id) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Anggota tidak ditemukan", color = Color.Gray)
                }
            }
        }

        // Add member button (FAB)
        FloatingActionButton(
            onClick = { viewModel.setSubScreen(SubScreen.ADD_MEMBER) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_member_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Anggota")
        }
    }
}

@Composable
fun MemberItemCard(member: MemberEntity, onClick: () -> Unit) {
    val initialLetter = if (member.name.isNotBlank()) member.name.first().toString().uppercase() else "S"
    
    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CharcoalBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("member_card_${member.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle avatar representation
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialLetter,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "WA: ${member.whatsApp}",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status badge
            val badgeBg = when (member.status) {
                "Aktif" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                "Tidak Aktif" -> Color(0xFFC62828).copy(alpha = 0.15f)
                else -> Color(0xFF1565C0).copy(alpha = 0.15f)
            }
            val badgeText = when (member.status) {
                "Aktif" -> Color(0xFF81C784)
                "Tidak Aktif" -> Color(0xFFE57373)
                else -> Color(0xFF64B5F6)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = member.status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeText
                )
            }
        }
    }
}

// ==========================================
// SUB SCREEN: MEMBER DETAIL & PROFILE
// ==========================================
@Composable
fun MemberProfileScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val profileData by viewModel.selectedMemberProfile.collectAsStateWithLifecycle()

    if (profileData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val (member, mandatoryHistory, voluntaryHistory) = profileData!!

    // State for Tabs & Filters
    var selectedTab by remember { mutableStateOf(0) } // 0: Semua, 1: Iuran Wajib, 2: Sukarela
    var selectedPeriodFilter by remember { mutableStateOf("Semua Periode") } // "Semua Periode", "Bulan & Tahun"
    var filterMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var filterYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    // State for Delete Confirmation Dialog
    var paymentToDelete by remember { mutableStateOf<VoluntaryDuesPaymentEntity?>(null) }

    val isDateInPeriod = remember {
        { dateMs: Long, targetMonth: Int, targetYear: Int ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = dateMs
            (cal.get(Calendar.MONTH) + 1) == targetMonth && cal.get(Calendar.YEAR) == targetYear
        }
    }

    // Filtered lists
    val filteredMandatory = remember(mandatoryHistory, selectedPeriodFilter, filterMonth, filterYear) {
        if (selectedPeriodFilter == "Semua Periode") {
            mandatoryHistory
        } else {
            mandatoryHistory.filter { it.month == filterMonth && it.year == filterYear }
        }
    }

    val filteredVoluntary = remember(voluntaryHistory, selectedPeriodFilter, filterMonth, filterYear) {
        if (selectedPeriodFilter == "Semua Periode") {
            voluntaryHistory
        } else {
            voluntaryHistory.filter { isDateInPeriod(it.paymentDate, filterMonth, filterYear) }
        }
    }

    // Confirmation Dialog
    if (paymentToDelete != null) {
        val p = paymentToDelete!!
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Konfirmasi Hapus", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Hapus iuran sukarela dari ${member.name} sebesar ${formatRupiah(p.amountPaid)}? Saldo kas, riwayat anggota, dan laporan akan diperbarui.", color = Color.LightGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVoluntaryPayment(p)
                        Toast.makeText(context, "Iuran sukarela berhasil dihapus.", Toast.LENGTH_LONG).show()
                        paymentToDelete = null
                    }
                ) {
                    Text("Hapus", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Batal", color = Color.White)
                }
            },
            containerColor = CharcoalSurface,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profil Badge Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name.firstOrNull()?.uppercase() ?: "S",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = member.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "WhatsApp: ${member.whatsApp}",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status pill
                    val statusColor = when (member.status) {
                        "Aktif" -> Color(0xFF81C784)
                        "Tidak Aktif" -> Color(0xFFE57373)
                        else -> Color(0xFF64B5F6)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .border(1.dp, statusColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = member.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Setujui (Only for pending)
                if (member.status == "Menunggu Persetujuan") {
                    Button(
                        onClick = { viewModel.approveMember(member) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Setujui Anggota", color = Color.White)
                    }
                } else if (member.status == "Aktif") {
                    Button(
                        onClick = { viewModel.setSubScreen(SubScreen.ADD_MEMBER_PAYMENT) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Tambah Pembayaran", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.deactivateMember(member) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("Nonaktifkan", color = Color.White, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = { viewModel.approveMember(member) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aktifkan Kembali", color = Color.White)
                    }
                }
            }
        }

        // Payments statistics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Iuran Wajib", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = formatRupiah(mandatoryHistory.filter { !it.isCancelled }.sumOf { it.amountPaid }),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color.Gray)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Iuran Sukarela", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = formatRupiah(voluntaryHistory.filter { !it.isCancelled }.sumOf { it.amountPaid }),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // TABS FOR SECTIONS (Requirement 5)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalSurface, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Semua Riwayat", "Iuran Wajib", "Iuran Sukarela").forEachIndexed { idx, label ->
                    val isSelected = selectedTab == idx
                    Button(
                        onClick = { selectedTab = idx },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (isSelected) Color.White else Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f),
                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 2.dp)
                    ) {
                        Text(label, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // FILTER PERIODE SECTION (Requirement 6 & 9)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter Periode:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                    
                    var isPeriodFilterExpanded by remember { mutableStateOf(false) }
                    Box {
                        Button(
                            onClick = { isPeriodFilterExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                            border = BorderStroke(1.dp, CharcoalBorder),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(selectedPeriodFilter, fontSize = 11.sp, color = Color.White)
                        }
                        DropdownMenu(
                            expanded = isPeriodFilterExpanded,
                            onDismissRequest = { isPeriodFilterExpanded = false },
                            modifier = Modifier.background(CharcoalSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua Periode", color = Color.White, fontSize = 11.sp) },
                                onClick = {
                                    selectedPeriodFilter = "Semua Periode"
                                    isPeriodFilterExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Bulan & Tahun", color = Color.White, fontSize = 11.sp) },
                                onClick = {
                                    selectedPeriodFilter = "Bulan & Tahun"
                                    isPeriodFilterExpanded = false
                                }
                            )
                        }
                    }

                    // Refresh Button (Requirement 9)
                    Button(
                        onClick = {
                            viewModel.refreshAllData()
                            Toast.makeText(context, "Riwayat diperbarui", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                        border = BorderStroke(1.dp, CharcoalBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Refresh Riwayat", fontSize = 11.sp, color = Color.White)
                    }
                }
                
                if (selectedPeriodFilter == "Bulan & Tahun") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Month Dropdown
                        var isMonthExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { isMonthExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                                border = BorderStroke(1.dp, CharcoalBorder),
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(viewModel.getIndonesianMonthName(filterMonth), fontSize = 11.sp, color = Color.White)
                            }
                            DropdownMenu(
                                expanded = isMonthExpanded,
                                onDismissRequest = { isMonthExpanded = false },
                                modifier = Modifier.background(CharcoalSurface)
                            ) {
                                (1..12).forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(viewModel.getIndonesianMonthName(m), color = Color.White, fontSize = 11.sp) },
                                        onClick = {
                                            filterMonth = m
                                            isMonthExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Year Dropdown
                        var isYearExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { isYearExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                                border = BorderStroke(1.dp, CharcoalBorder),
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(filterYear.toString(), fontSize = 11.sp, color = Color.White)
                            }
                            DropdownMenu(
                                expanded = isYearExpanded,
                                onDismissRequest = { isYearExpanded = false },
                                modifier = Modifier.background(CharcoalSurface)
                            ) {
                                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                                ((currentYear - 2)..(currentYear + 2)).forEach { y ->
                                    DropdownMenuItem(
                                        text = { Text(y.toString(), color = Color.White, fontSize = 11.sp) },
                                        onClick = {
                                            filterYear = y
                                            isYearExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Payments History Section Title
        item {
            Text(
                text = "Daftar Riwayat Transaksi",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Empty state logic (Requirement 8)
        val hasAnyHistory = filteredMandatory.isNotEmpty() || filteredVoluntary.isNotEmpty()
        if (!hasAnyHistory) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada riwayat pembayaran.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            // Render list items based on Tab selection
            if (selectedTab == 0 || selectedTab == 1) {
                // Mandatory Section
                if (filteredMandatory.isNotEmpty()) {
                    item {
                        Text("Iuran Wajib (Bulanan)", fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.SemiBold)
                    }
                    items(filteredMandatory) { p ->
                        TransactionListItem(
                            title = "Iuran Wajib (${viewModel.getIndonesianMonthName(p.month)} ${p.year})",
                            amount = p.amountPaid,
                            dateMs = p.paymentDate,
                            isCancelled = p.isCancelled,
                            note = p.note,
                            onToggleCancel = {
                                viewModel.editMandatoryPayment(
                                    id = p.id,
                                    memberId = p.memberId,
                                    month = p.month,
                                    year = p.year,
                                    amount = p.amountPaid,
                                    dateMs = p.paymentDate,
                                    note = p.note,
                                    isCancelled = !p.isCancelled
                                )
                            }
                        )
                    }
                } else if (selectedTab == 1) {
                    item {
                        Text("Belum ada riwayat pembayaran iuran wajib", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            if (selectedTab == 0 || selectedTab == 2) {
                // Voluntary Section
                if (filteredVoluntary.isNotEmpty()) {
                    item {
                        Text("Iuran Sukarela (Donasi)", fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.SemiBold)
                    }
                    items(filteredVoluntary) { p ->
                        TransactionListItem(
                            title = "Iuran Sukarela ${if (p.paymentTime.isNotBlank()) "- " + p.paymentTime else ""}",
                            amount = p.amountPaid,
                            dateMs = p.paymentDate,
                            isCancelled = p.isCancelled,
                            note = p.note,
                            onToggleCancel = {
                                viewModel.editVoluntaryPayment(
                                    id = p.id,
                                    memberId = p.memberId,
                                    donorName = p.donorName,
                                    amount = p.amountPaid,
                                    dateMs = p.paymentDate,
                                    timeStr = p.paymentTime,
                                    note = p.note,
                                    isCancelled = !p.isCancelled
                                )
                            },
                            onEdit = {
                                viewModel.selectVoluntaryPayment(p)
                                viewModel.setSubScreen(SubScreen.EDIT_VOLUNTARY)
                            },
                            onDelete = {
                                paymentToDelete = p
                            }
                        )
                    }
                } else if (selectedTab == 2) {
                    item {
                        Text("Belum ada riwayat iuran sukarela", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(
    title: String,
    amount: Double,
    dateMs: Long,
    isCancelled: Boolean,
    note: String,
    onToggleCancel: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CharcoalBorder),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onEdit != null) {
                    Modifier.clickable { onEdit() }
                } else {
                    Modifier
                }
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCancelled) Color.Gray else Color.White,
                            style = if (isCancelled) LocalTextStyle.current.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else LocalTextStyle.current
                        )
                        if (onEdit != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = formatDate(dateMs),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = if (isCancelled) "BATAL" else formatRupiah(amount),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCancelled) Color.Gray else MaterialTheme.colorScheme.primary
                )
            }

            if (note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ket: $note",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons row (Toggle Cancel, Edit, and Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onEdit != null) {
                    TextButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Edit",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Hapus",
                            fontSize = 10.sp,
                            color = Color(0xFFE57373)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                TextButton(
                    onClick = onToggleCancel,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (isCancelled) "Pulihkan Transaksi" else "Batalkan Transaksi",
                        fontSize = 10.sp,
                        color = if (isCancelled) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 3: KEUANGAN SCREEN & SUBTABS
// ==========================================
@Composable
fun KeuanganTabScreen(viewModel: AppViewModel) {
    var activeSubTab by remember { mutableStateOf("Iuran Wajib") } // "Iuran Wajib", "Iuran Sukarela", "Kas & Pengeluaran"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Headers Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CharcoalSurface)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("Iuran Wajib", "Iuran Sukarela", "Kas & Pengeluaran").forEach { tab ->
                val isSelected = activeSubTab == tab
                val borderBottomColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                
                Box(
                    modifier = Modifier
                        .clickable { activeSubTab = tab }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            val y = this.size.height - strokeWidth / 2
                            drawLine(
                                color = borderBottomColor,
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(this.size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Gray
                    )
                }
            }
        }

        // Active tab rendering
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                "Iuran Wajib" -> IuranWajibSubTab(viewModel)
                "Iuran Sukarela" -> IuranSukarelaSubTab(viewModel)
                "Kas & Pengeluaran" -> KasPengeluaranSubTab(viewModel)
            }
        }
    }
}

@Composable
fun IuranWajibSubTab(viewModel: AppViewModel) {
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val year by viewModel.selectedYear.collectAsStateWithLifecycle()
    val membersList by viewModel.members.collectAsStateWithLifecycle()
    val mandatoryPayments by viewModel.mandatoryPayments.collectAsStateWithLifecycle()

    val currentPeriodPayments = mandatoryPayments.filter { !it.isCancelled && it.month == month && it.year == year }
    val activeMembers = membersList.filter { it.status == "Aktif" }

    val lunasList = activeMembers.filter { m ->
        val paidAmount = currentPeriodPayments.filter { it.memberId == m.id }.sumOf { it.amountPaid }
        paidAmount >= 10000.0 // Default mandatory target is Rp10.000
    }

    val belumLunasList = activeMembers.filter { !lunasList.contains(it) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Selector of period (Month & Year)
        item {
            PeriodSelectorRow(viewModel)
        }

        // Summary Statistics Box (HILANGKAN FITUR TOTAL TARGET IURAN as requested)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("RINGKASAN IURAN WAJIB PERIODE INI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = "Total Masuk: ${formatRupiah(currentPeriodPayments.sumOf { it.amountPaid })}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Lunas", fontSize = 10.sp, color = Color.Gray)
                            Text("${lunasList.size} Orang", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Belum Lunas", fontSize = 10.sp, color = Color.Gray)
                            Text("${belumLunasList.size} Orang", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Quick Input Link
        item {
            Button(
                onClick = { viewModel.setSubScreen(SubScreen.ADD_MANDATORY) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah Pembayaran Anggota", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Active members payment status listing
        item {
            Text(
                text = "Status Pembayaran Anggota Aktif",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (activeMembers.isNotEmpty()) {
            items(activeMembers) { m ->
                val paid = currentPeriodPayments.filter { it.memberId == m.id }.sumOf { it.amountPaid }
                val isLunas = paid >= 10000.0

                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CharcoalBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectMember(m.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(m.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Terbayar: ${formatRupiah(paid)}", fontSize = 11.sp, color = Color.LightGray)
                        }

                        // Badge status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLunas) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFFC62828).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isLunas) "Lunas" else "Belum Bayar",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLunas) Color(0xFF81C784) else Color(0xFFE57373)
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada anggota aktif.", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PeriodSelectorRow(viewModel: AppViewModel) {
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val year by viewModel.selectedYear.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Pilih Periode:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month navigation
            IconButton(
                onClick = {
                    if (month == 1) {
                        viewModel.setPeriod(12, year - 1)
                    } else {
                        viewModel.setPeriod(month - 1, year)
                    }
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Sebelumnya", tint = Color.White)
            }

            Text(
                text = "${viewModel.getIndonesianMonthName(month)} $year",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            IconButton(
                onClick = {
                    if (month == 12) {
                        viewModel.setPeriod(1, year + 1)
                    } else {
                        viewModel.setPeriod(month + 1, year)
                    }
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Selanjutnya", tint = Color.White)
            }
        }
    }
}

@Composable
fun IuranSukarelaSubTab(viewModel: AppViewModel) {
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val year by viewModel.selectedYear.collectAsStateWithLifecycle()
    val voluntaryPayments by viewModel.voluntaryPayments.collectAsStateWithLifecycle()

    // Calculate sum of voluntary payments in this period
    val currentPeriodVoluntary = voluntaryPayments.filter {
        !it.isCancelled && isDateInPeriod(it.paymentDate, month, year)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PeriodSelectorRow(viewModel)
        }

        // Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TOTAL IURAN SUKARELA TERKUMPUL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatRupiah(currentPeriodVoluntary.sumOf { it.amountPaid }),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Menunjukkan donasi sukarela di luar kewajiban iuran wajib bulanan.", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        // Add voluntary button
        item {
            Button(
                onClick = { viewModel.setSubScreen(SubScreen.ADD_VOLUNTARY) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah Iuran Sukarela", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // History list title
        item {
            Text(
                text = "Riwayat Iuran Sukarela",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Render history items in current period
        val allVoluntaryInPeriod = voluntaryPayments.filter { isDateInPeriod(it.paymentDate, month, year) }
        if (allVoluntaryInPeriod.isNotEmpty()) {
            items(allVoluntaryInPeriod) { p ->
                TransactionListItem(
                    title = p.donorName,
                    amount = p.amountPaid,
                    dateMs = p.paymentDate,
                    isCancelled = p.isCancelled,
                    note = p.note,
                    onToggleCancel = {
                        viewModel.editVoluntaryPayment(
                            id = p.id,
                            memberId = p.memberId,
                            donorName = p.donorName,
                            amount = p.amountPaid,
                            dateMs = p.paymentDate,
                            timeStr = p.paymentTime,
                            note = p.note,
                            isCancelled = !p.isCancelled
                        )
                    },
                    onEdit = {
                        viewModel.selectVoluntaryPayment(p)
                        viewModel.setSubScreen(SubScreen.EDIT_VOLUNTARY)
                    }
                )
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada riwayat iuran sukarela periode ini.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

// Helper checker
private fun isDateInPeriod(dateMs: Long, targetMonth: Int, targetYear: Int): Boolean {
    val cal = Calendar.getInstance()
    cal.timeInMillis = dateMs
    return (cal.get(Calendar.MONTH) + 1) == targetMonth && cal.get(Calendar.YEAR) == targetYear
}

@Composable
fun KasPengeluaranSubTab(viewModel: AppViewModel) {
    val ctx = LocalContext.current
    var expenseToDelete by remember { mutableStateOf<com.example.data.ExpenseEntity?>(null) }
    val totals by viewModel.totalsState.collectAsStateWithLifecycle()
    val mandatoryPayments by viewModel.mandatoryPayments.collectAsStateWithLifecycle()
    val voluntaryPayments by viewModel.voluntaryPayments.collectAsStateWithLifecycle()
    val otherIncomes by viewModel.otherIncomes.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    var activeSubFilter by remember { mutableStateOf("Semua Transaksi") } // "Semua Transaksi", "Pemasukan", "Pengeluaran", "Dibatalkan"

    // Construct full compiled ledger list of transactions
    val compiledLedger = remember(mandatoryPayments, voluntaryPayments, otherIncomes, expenses) {
        val list = mutableListOf<LedgerItem>()
        
        mandatoryPayments.forEach {
            list.add(LedgerItem(id = it.id, type = "Pemasukan", source = "Iuran Wajib: ${it.memberName}", amount = it.amountPaid, date = it.paymentDate, note = it.note, isCancelled = it.isCancelled, tag = "Mandatory"))
        }

        voluntaryPayments.forEach {
            list.add(LedgerItem(id = it.id, type = "Pemasukan", source = "Iuran Sukarela: ${it.donorName}", amount = it.amountPaid, date = it.paymentDate, note = it.note, isCancelled = it.isCancelled, tag = "Voluntary"))
        }

        otherIncomes.forEach {
            list.add(LedgerItem(id = it.id, type = "Pemasukan", source = "Pemasukan Lain: ${it.source}", amount = it.amount, date = it.paymentDate, note = it.note, isCancelled = it.isCancelled, tag = "Other"))
        }

        expenses.forEach {
            list.add(LedgerItem(id = it.id, type = "Pengeluaran", source = "Pengeluaran: ${it.category} (${it.recipient})", amount = it.amount, date = it.expenseDate, note = it.note, isCancelled = it.isCancelled, tag = "Expense"))
        }

        // Sort descending by date
        list.sortByDescending { it.date }
        list
    }

    val filteredLedger = compiledLedger.filter {
        when (activeSubFilter) {
            "Semua Transaksi" -> !it.isCancelled
            "Pemasukan" -> !it.isCancelled && it.type == "Pemasukan"
            "Pengeluaran" -> !it.isCancelled && it.type == "Pengeluaran"
            "Dibatalkan" -> it.isCancelled
            else -> true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cash balance card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SALDO KAS", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(formatRupiah(totals?.netBalance ?: 0.0), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { viewModel.setSubScreen(SubScreen.ADD_OTHER_INCOME) }, modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape).size(36.dp)) {
                            Icon(Icons.Default.TrendingUp, contentDescription = "Tambah Pemasukan", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { viewModel.setSubScreen(SubScreen.ADD_EXPENSE) }, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape).size(36.dp)) {
                            Icon(Icons.Default.TrendingDown, contentDescription = "Tambah Pengeluaran", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // SubFilter tab row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Semua Transaksi", "Pemasukan", "Pengeluaran", "Dibatalkan").forEach { filter ->
                    val isSelected = activeSubFilter == filter
                    val borderBg = if (isSelected) MaterialTheme.colorScheme.primary else CharcoalSurface
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(borderBg)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
                            .clickable { activeSubFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(text = filter, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // History items rendering
        if (filteredLedger.isNotEmpty()) {
            items(filteredLedger) { item ->
                TransactionListItem(
                    title = item.source,
                    amount = item.amount,
                    dateMs = item.date,
                    isCancelled = item.isCancelled,
                    note = item.note,
                    onToggleCancel = {
                        // Dynamically resolve and toggle cancel based on tag
                        when (item.tag) {
                            "Mandatory" -> {
                                val original = mandatoryPayments.find { it.id == item.id }
                                if (original != null) {
                                    viewModel.editMandatoryPayment(original.id, original.memberId, original.month, original.year, original.amountPaid, original.paymentDate, original.note, !original.isCancelled)
                                }
                            }
                            "Voluntary" -> {
                                val original = voluntaryPayments.find { it.id == item.id }
                                if (original != null) {
                                    viewModel.editVoluntaryPayment(original.id, original.memberId, original.donorName, original.amountPaid, original.paymentDate, original.paymentTime, original.note, !original.isCancelled)
                                }
                            }
                            "Other" -> {
                                val original = otherIncomes.find { it.id == item.id }
                                if (original != null) {
                                    viewModel.editOtherIncome(original.id, original.source, original.amount, original.paymentDate, original.note, !original.isCancelled)
                                }
                            }
                            "Expense" -> {
                                val original = expenses.find { it.id == item.id }
                                if (original != null) {
                                    viewModel.editExpense(
                                        id = original.id,
                                        category = original.category,
                                        amount = original.amount,
                                        dateMs = original.expenseDate,
                                        recipient = original.recipient,
                                        note = original.note,
                                        isCancelled = !original.isCancelled,
                                        time = original.expenseTime,
                                        memberId = original.memberId,
                                        createdAt = original.createdAt
                                    )
                                }
                            }
                        }
                    },
                    onEdit = when (item.tag) {
                        "Voluntary" -> {
                            {
                                val original = voluntaryPayments.find { it.id == item.id }
                                if (original != null) {
                                    viewModel.selectVoluntaryPayment(original)
                                    viewModel.setSubScreen(SubScreen.EDIT_VOLUNTARY)
                                }
                            }
                        }
                        "Expense" -> {
                            {
                                val original = expenses.find { it.id == item.id }
                                if (original != null) {
                                    viewModel.selectExpense(original)
                                    viewModel.setSubScreen(SubScreen.EDIT_EXPENSE)
                                }
                            }
                        }
                        else -> null
                    },
                    onDelete = if (item.tag == "Expense") {
                        {
                            val original = expenses.find { it.id == item.id }
                            if (original != null) {
                                expenseToDelete = original
                            }
                        }
                    } else null
                )
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada transaksi pada kategori ini.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }

    if (expenseToDelete != null) {
        val exp = expenseToDelete!!
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Konfirmasi Hapus", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Hapus pengeluaran sebesar ${formatRupiah(exp.amount)} untuk ${exp.recipient}? Saldo kas dan laporan akan diperbarui otomatis.", color = Color.LightGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(exp)
                        expenseToDelete = null
                        Toast.makeText(ctx, "Pengeluaran berhasil dihapus.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Batal", color = Color.White)
                }
            },
            containerColor = CharcoalDark,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

data class LedgerItem(
    val id: Int,
    val type: String, // "Pemasukan", "Pengeluaran"
    val source: String,
    val amount: Double,
    val date: Long,
    val note: String,
    val isCancelled: Boolean,
    val tag: String // "Mandatory", "Voluntary", "Other", "Expense"
)

// ==========================================
// TAB 4: LAPORAN SCREEN & COMPREHENSIVE REPORTS
// ==========================================
@Composable
fun LaporanTabScreen(viewModel: AppViewModel) {
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val year by viewModel.selectedYear.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var isSpecialReportActive by remember { mutableStateOf(true) } // "Anggota Sudah Membayar" vs "Laporan Utama"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Period header
        PeriodSelectorRow(viewModel)

        // Secondary Tabs for Reports
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CharcoalSurface)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("Anggota Sudah Membayar", "Laporan Utama & Grafik").forEach { tab ->
                val isSelected = (tab == "Anggota Sudah Membayar" && isSpecialReportActive) || (tab == "Laporan Utama & Grafik" && !isSpecialReportActive)
                val borderBottomColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                
                Box(
                    modifier = Modifier
                        .clickable { isSpecialReportActive = tab == "Anggota Sudah Membayar" }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            val y = this.size.height - strokeWidth / 2
                            drawLine(
                                color = borderBottomColor,
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(this.size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Gray
                    )
                }
            }
        }

        // Display report content
        Box(modifier = Modifier.weight(1f)) {
            if (isSpecialReportActive) {
                SpecialReportPaidMembersSubTab(viewModel)
            } else {
                MainGeneralReportSubTab(viewModel)
            }
        }
    }
}

@Composable
fun SpecialReportPaidMembersSubTab(viewModel: AppViewModel) {
    val context = LocalContext.current
    val searchQuery by viewModel.reportSearchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.reportFilterType.collectAsStateWithLifecycle()
    val reportData by viewModel.specialPaymentReportState.collectAsStateWithLifecycle()

    val (reportItems, summary) = reportData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Actions buttons: Share WhatsApp & PDF/Excel Exporter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.exportReportToPdf(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cetak PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = { viewModel.exportReportToExcel(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ekspor Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = { viewModel.exportReportToPdf(context, shareToWhatsApp = true) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF075E54)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("WA Share", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search & Filter controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setReportSearch(it) },
                placeholder = { Text("Nama pembayar...", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray) },
                singleLine = true,
                modifier = Modifier
                    .weight(1.3f)
                    .height(52.dp),
                textStyle = TextStyle(fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Select category drop filter
            var isFilterExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { isFilterExpanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    border = BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Text(filterType, fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                DropdownMenu(
                    expanded = isFilterExpanded,
                    onDismissRequest = { isFilterExpanded = false },
                    modifier = Modifier.background(CharcoalSurface)
                ) {
                    listOf("Semua Pembayar", "Iuran Wajib", "Iuran Sukarela", "Keduanya").forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item, color = Color.White, fontSize = 12.sp) },
                            onClick = {
                                viewModel.setReportFilterType(item)
                                isFilterExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Summary Statistics Box in UI
        val voluntaryPayments by viewModel.voluntaryPayments.collectAsStateWithLifecycle()
        val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
        val year by viewModel.selectedYear.collectAsStateWithLifecycle()
        val currentPeriodVoluntaryCount = remember(voluntaryPayments, month, year) {
            voluntaryPayments.count { !it.isCancelled && isDateInPeriod(it.paymentDate, month, year) }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Section 1: Ringkasan Iuran Wajib
                Text("RINGKASAN IURAN WAJIB", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Anggota Pembayar Wajib:", fontSize = 11.sp, color = Color.Gray)
                    Text("${summary.numMandatoryPayers} Orang", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Dana Terkumpul:", fontSize = 11.sp, color = Color.Gray)
                    Text(formatRupiah(summary.totalMandatoryCollected), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Divider(color = Color.Gray.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

                // Section 2: Ringkasan Iuran Sukarela
                Text("RINGKASAN IURAN SUKARELA / DONASI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Anggota/Donatur Pembayar Sukarela:", fontSize = 11.sp, color = Color.Gray)
                    Text("${summary.numVoluntaryPayers} Orang", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Dana Terkumpul:", fontSize = 11.sp, color = Color.Gray)
                    Text(formatRupiah(summary.totalVoluntaryCollected), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                // Section 3: Total Pemasukan Gabungan
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TOTAL GABUNGAN PEMASUKAN:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(formatRupiah(summary.totalFundsReceived), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Refresh and Debug Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.refreshReports() },
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Refresh Laporan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Debug info box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                modifier = Modifier.weight(1.3f)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text("DEBUG INFO (TEMPORER)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    Text("Iuran Sukarela di DB: ${voluntaryPayments.size}", fontSize = 9.sp, color = Color.LightGray)
                    Text("Periode terpilih: $currentPeriodVoluntaryCount data", fontSize = 9.sp, color = Color.LightGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabular list of report items
        if (reportItems.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    // Header table row
                    Surface(color = CharcoalSurface, shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("No", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(28.dp))
                            Text("Nama", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1.2f))
                            Text("Wajib", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            Text("Sukarela", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            Text("Total", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1.1f), textAlign = TextAlign.End)
                        }
                    }
                }

                items(reportItems) { item ->
                    Surface(
                        color = CharcoalSurface.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, CharcoalBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(item.nomor.toString(), fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.width(28.dp))
                            Text(item.name, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(formatRupiah(item.mandatoryPaid).replace("Rp ", ""), fontSize = 11.sp, color = Color.LightGray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            Text(formatRupiah(item.voluntaryPaid).replace("Rp ", ""), fontSize = 11.sp, color = Color.LightGray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            Text(formatRupiah(item.totalPaid).replace("Rp ", ""), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f), textAlign = TextAlign.End)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Tidak ada pembayar dalam pencarian / periode ini.", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MainGeneralReportSubTab(viewModel: AppViewModel) {
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val year by viewModel.selectedYear.collectAsStateWithLifecycle()
    
    val mandatoryPayments by viewModel.mandatoryPayments.collectAsStateWithLifecycle()
    val voluntaryPayments by viewModel.voluntaryPayments.collectAsStateWithLifecycle()
    val otherIncomes by viewModel.otherIncomes.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    // 1. Calculate Saldo Awal (all non-cancelled transactions prior to the selected month/year)
    val saldoAwal = remember(month, year, mandatoryPayments, voluntaryPayments, otherIncomes, expenses) {
        val calLimit = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1) // month - 1 matches month index (0-11)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val limitTime = calLimit.timeInMillis

        val priorMandatory = mandatoryPayments.filter { !it.isCancelled && it.paymentDate < limitTime }.sumOf { it.amountPaid }
        val priorVoluntary = voluntaryPayments.filter { !it.isCancelled && it.paymentDate < limitTime }.sumOf { it.amountPaid }
        val priorOther = otherIncomes.filter { !it.isCancelled && it.paymentDate < limitTime }.sumOf { it.amount }
        val priorExpenses = expenses.filter { !it.isCancelled && it.expenseDate < limitTime }.sumOf { it.amount }

        priorMandatory + priorVoluntary + priorOther - priorExpenses
    }

    // 2. Current Month Items
    val currentMandatory = mandatoryPayments.filter { !it.isCancelled && it.month == month && it.year == year }.sumOf { it.amountPaid }
    val currentVoluntary = voluntaryPayments.filter { !it.isCancelled && isDateInPeriod(it.paymentDate, month, year) }.sumOf { it.amountPaid }
    val currentOther = otherIncomes.filter { !it.isCancelled && isDateInPeriod(it.paymentDate, month, year) }.sumOf { it.amount }
    val currentExpenses = expenses.filter { !it.isCancelled && isDateInPeriod(it.expenseDate, month, year) }.sumOf { it.amount }

    val totalIncome = currentMandatory + currentVoluntary + currentOther
    val saldoAkhir = saldoAwal + totalIncome - currentExpenses

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Table Summary Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("LAPORAN KAS BULANAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    RowReportLine("Saldo Awal", formatRupiah(saldoAwal), Color.LightGray)
                    RowReportLine("Total Iuran Wajib", formatRupiah(currentMandatory), Color.White)
                    RowReportLine("Total Iuran Sukarela", formatRupiah(currentVoluntary), Color.White)
                    RowReportLine("Total Pemasukan Lain", formatRupiah(currentOther), Color.White)
                    
                    Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
                    
                    RowReportLine("Total Pengeluaran", "- " + formatRupiah(currentExpenses), MaterialTheme.colorScheme.primary)
                    
                    Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    RowReportLine("Saldo Akhir", formatRupiah(saldoAkhir), Color.White, isBold = true)
                }
            }
        }

        // Custom Graphics Chart
        item {
            Text(
                text = "Grafik Pemasukan vs Pengeluaran",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Render simple custom drawing bars
                    val maxVal = maxOf(totalIncome, currentExpenses, 1.0)
                    val barInPercent = (totalIncome / maxVal).toFloat()
                    val barOutPercent = (currentExpenses / maxVal).toFloat()

                    // Bar Pemasukan
                    Text("Total Pemasukan: ${formatRupiah(totalIncome)}", fontSize = 11.sp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(barInPercent.coerceAtLeast(0.05f))
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2E7D32))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bar Pengeluaran
                    Text("Total Pengeluaran: ${formatRupiah(currentExpenses)}", fontSize = 11.sp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(barOutPercent.coerceAtLeast(0.05f))
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
fun RowReportLine(label: String, value: String, valueColor: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = Color.LightGray
        )

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}

// ==========================================
// TAB 5: LAINNYA & AGENDA SCREEN
// ==========================================
@Composable
fun LainnyaTabScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    var isAgendaExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Info header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.steker_logo),
                        contentDescription = "Steker Logo",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Steker App v1.5", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Satu Aplikasi untuk Semua Kebutuhan", fontSize = 11.sp, color = Color.LightGray)
                    Text("Steker Hitam", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Agenda Pertemuan menu button
        item {
            LainnyaMenuItemCard(
                title = "Agenda Pertemuan & Kegiatan",
                description = "Kelola rencana rapat, musyawarah, dan dokumentasi rapat.",
                icon = Icons.Default.EventNote,
                onClick = { isAgendaExpanded = !isAgendaExpanded }
            )
        }

        if (isAgendaExpanded) {
            item {
                AgendaSubMenu(viewModel)
            }
        }

        // Settings Button
        item {
            LainnyaMenuItemCard(
                title = "Pengaturan PIN",
                description = "Ubah PIN keamanan pengurus.",
                icon = Icons.Default.Settings,
                onClick = { viewModel.setSubScreen(SubScreen.SETTINGS) }
            )
        }

        // Backup & Pindah Data Button
        item {
            LainnyaMenuItemCard(
                title = "Backup & Pindah Data",
                description = "Ekspor, impor, dan pindah data antar-ponsel secara offline.",
                icon = Icons.Default.Backup,
                onClick = { viewModel.setSubScreen(SubScreen.BACKUP_DATA) }
            )
        }
    }
}

@Composable
fun AgendaSubMenu(viewModel: AppViewModel) {
    val agendas by viewModel.agendas.collectAsStateWithLifecycle()
    var isAddingAgenda by remember { mutableStateOf(false) }

    // Forms fields for adding agenda
    var agendaTitle by remember { mutableStateOf("") }
    var agendaLocation by remember { mutableStateOf("") }
    var agendaDescription by remember { mutableStateOf("") }
    var agendaTime by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalSurface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Daftar Rencana Agenda", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            TextButton(onClick = { isAddingAgenda = !isAddingAgenda }) {
                Text(if (isAddingAgenda) "Batal" else "+ Tambah", color = MaterialTheme.colorScheme.primary)
            }
        }

        if (isAddingAgenda) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = agendaTitle,
                    onValueChange = { agendaTitle = it },
                    placeholder = { Text("Judul Agenda (contoh: Rapat Anggota)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = agendaTime,
                    onValueChange = { agendaTime = it },
                    placeholder = { Text("Waktu (contoh: 19:30 WIB)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = agendaLocation,
                    onValueChange = { agendaLocation = it },
                    placeholder = { Text("Lokasi (contoh: Sekretariat Steker)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = agendaDescription,
                    onValueChange = { agendaDescription = it },
                    placeholder = { Text("Agenda Pembahasan") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Button(
                    onClick = {
                        if (agendaTitle.isNotBlank()) {
                            viewModel.addAgenda(
                                title = agendaTitle,
                                dateMs = System.currentTimeMillis(),
                                timeStr = agendaTime,
                                location = agendaLocation,
                                description = agendaDescription,
                                status = "Akan Datang"
                            )
                            isAddingAgenda = false
                            agendaTitle = ""
                            agendaTime = ""
                            agendaLocation = ""
                            agendaDescription = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Simpan", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Render current list of agendas
        if (agendas.isNotEmpty()) {
            agendas.forEach { a ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(a.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            
                            // Toggle status button
                            TextButton(
                                onClick = {
                                    val newStatus = if (a.status == "Akan Datang") "Selesai" else "Akan Datang"
                                    viewModel.editAgenda(a.id, a.title, a.date, a.time, a.location, a.description, a.meetingMinutes, newStatus)
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = if (a.status == "Akan Datang") "Selesaikan" else "Aktifkan",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Text("Tempat: ${a.location} | Jam: ${a.time}", fontSize = 11.sp, color = Color.LightGray)
                        if (a.description.isNotBlank()) {
                            Text("Pembahasan: ${a.description}", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        } else {
            Text("Belum ada agenda pertemuan terdaftar.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun LainnyaMenuItemCard(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CharcoalBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(description, fontSize = 11.sp, color = Color.LightGray)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

// ==========================================
// SUB SCREEN: SETTINGS & BACKUP/RESTORE
// ==========================================
@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val configState by viewModel.config.collectAsStateWithLifecycle()

    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    
    // Backup JSON state representation
    var backupJsonString by remember { mutableStateOf("") }
    var restoreJsonInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Change PIN
        item {
            Text("Ubah PIN Pengurus", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = oldPin,
                        onValueChange = { oldPin = it },
                        placeholder = { Text("PIN Lama") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it },
                        placeholder = { Text("PIN Baru (6 Digit)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Button(
                        onClick = {
                            viewModel.updatePin(
                                oldPin = oldPin,
                                newPin = newPin,
                                onSuccess = {
                                    Toast.makeText(context, "PIN berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                                    oldPin = ""
                                    newPin = ""
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Simpan PIN Baru", color = Color.White)
                    }
                }
            }
        }

        // Section 2: Backup Local Database (JSON format)
        item {
            Text("Ekspor & Backup Data Lokal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ekspor seluruh data organisasi ke format JSON text yang aman untuk disimpan/pindah perangkat.", fontSize = 11.sp, color = Color.LightGray)

                    Button(
                        onClick = {
                            viewModel.triggerLocalBackup { result ->
                                if (result != null) {
                                    backupJsonString = result
                                    Toast.makeText(context, "Backup JSON berhasil digenerate!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Gagal menggenerate backup", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate Backup Data", color = Color.White)
                    }

                    if (backupJsonString.isNotBlank()) {
                        OutlinedTextField(
                            value = backupJsonString,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Salin teks backup di bawah:") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            textStyle = TextStyle(fontSize = 10.sp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.LightGray, unfocusedTextColor = Color.LightGray)
                        )
                    }
                }
            }
        }

        // Section 3: Restore Local Database
        item {
            Text("Import & Restore Data Lokal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tempel teks JSON hasil backup Anda di sini untuk memulihkan seluruh data database lokal.", fontSize = 11.sp, color = Color.LightGray)

                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        placeholder = { Text("Tempel teks backup JSON di sini...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        textStyle = TextStyle(fontSize = 10.sp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Button(
                        onClick = {
                            if (restoreJsonInput.isNotBlank()) {
                                viewModel.triggerLocalRestore(restoreJsonInput) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Seluruh data berhasil dipulihkan!", Toast.LENGTH_LONG).show()
                                        restoreJsonInput = ""
                                    } else {
                                        Toast.makeText(context, "Format backup tidak sesuai / Gagal memulihkan", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Proses Restore Database", color = Color.White)
                    }
                }
            }
        }

        // Section 4: Version Indicator
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Versi 1.3",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ==========================================
// QUICK SUB SCREENS: FORM INPUT PAGES
// ==========================================

@Composable
fun AddMemberScreen(viewModel: AppViewModel) {
    var name by remember { mutableStateOf("") }
    var whatsApp by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Lengkap Anggota") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("member_name_input"),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = whatsApp,
            onValueChange = { whatsApp = it },
            label = { Text("Nomor WhatsApp (contoh: 08123456789)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Button(
            onClick = {
                if (name.isNotBlank() && whatsApp.isNotBlank()) {
                    viewModel.addNewMember(name, whatsApp)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_member_button")
        ) {
            Text("Simpan Anggota Baru", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun EditMemberScreen(viewModel: AppViewModel) {
    // Standard edit member stub
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Gunakan menu detail profil untuk memperbarui status.", color = Color.Gray)
    }
}

@Composable
fun AddMemberPaymentScreen(viewModel: AppViewModel) {
    val profileData by viewModel.selectedMemberProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (profileData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val member = profileData!!.first

    // Local states
    var paymentDateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    val sdf = remember { java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("id", "ID")) }
    val dateStr = sdf.format(java.util.Date(paymentDateMs))

    // Payment Type selections
    var isMandatoryChecked by remember { mutableStateOf(true) }
    var isVoluntaryChecked by remember { mutableStateOf(false) }

    // Mandatory Dues local states
    var mandatoryAmountText by remember { mutableStateOf("10000") }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var mandatoryNoteText by remember { mutableStateOf("") }

    // Voluntary Dues local states
    var voluntaryAmountText by remember { mutableStateOf("") }
    var voluntaryNoteText by remember { mutableStateOf("") }

    // UI structure
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Nama Anggota (Read-only)
        OutlinedTextField(
            value = member.name,
            onValueChange = {},
            label = { Text("Nama Anggota") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.White,
                disabledBorderColor = CharcoalBorder,
                disabledLabelColor = Color.Gray
            )
        )

        // Tanggal Pembayaran (Editable with DatePicker)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance().apply { timeInMillis = paymentDateMs }
                    android.app.DatePickerDialog(
                        context,
                        { _, year, monthOfYear, dayOfMonth ->
                            val selectedCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, monthOfYear)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }
                            paymentDateMs = selectedCal.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
        ) {
            OutlinedTextField(
                value = dateStr,
                onValueChange = {},
                label = { Text("Tanggal Pembayaran") },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = CharcoalBorder,
                    disabledLabelColor = Color.Gray
                ),
                trailingIcon = {
                    Icon(Icons.Default.Event, contentDescription = "Pilih Tanggal", tint = Color.White)
                }
            )
        }

        // Pilihan Jenis Pembayaran
        Text(
            text = "Jenis Pembayaran",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Checkbox/Card for Iuran Wajib
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isMandatoryChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else CharcoalSurface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isMandatoryChecked) MaterialTheme.colorScheme.primary else CharcoalBorder
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { isMandatoryChecked = !isMandatoryChecked }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isMandatoryChecked,
                        onCheckedChange = { isMandatoryChecked = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Iuran Wajib", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            // Checkbox/Card for Iuran Sukarela
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isVoluntaryChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else CharcoalSurface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isVoluntaryChecked) MaterialTheme.colorScheme.primary else CharcoalBorder
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { isVoluntaryChecked = !isVoluntaryChecked }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isVoluntaryChecked,
                        onCheckedChange = { isVoluntaryChecked = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Iuran Sukarela", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }

        // Detail Iuran Wajib (If Selected)
        if (isMandatoryChecked) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, CharcoalBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DETAIL IURAN WAJIB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Month and Year Pickers Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Month Picker
                        var isMonthExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { isMonthExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, CharcoalBorder),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(viewModel.getIndonesianMonthName(selectedMonth), color = Color.White, fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = isMonthExpanded,
                                onDismissRequest = { isMonthExpanded = false },
                                modifier = Modifier.background(CharcoalSurface)
                            ) {
                                (1..12).forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(viewModel.getIndonesianMonthName(m), color = Color.White) },
                                        onClick = {
                                            selectedMonth = m
                                            isMonthExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Year Picker
                        var isYearExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { isYearExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, CharcoalBorder),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(selectedYear.toString(), color = Color.White, fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = isYearExpanded,
                                onDismissRequest = { isYearExpanded = false },
                                modifier = Modifier.background(CharcoalSurface)
                            ) {
                                listOf(2025, 2026, 2027, 2028).forEach { y ->
                                    DropdownMenuItem(
                                        text = { Text(y.toString(), color = Color.White) },
                                        onClick = {
                                            selectedYear = y
                                            isYearExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Mandatory Amount Input
                    OutlinedTextField(
                        value = mandatoryAmountText,
                        onValueChange = { mandatoryAmountText = it },
                        label = { Text("Nominal Iuran Wajib") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    // Mandatory Note (Optional)
                    OutlinedTextField(
                        value = mandatoryNoteText,
                        onValueChange = { mandatoryNoteText = it },
                        label = { Text("Keterangan Opsional") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            }
        }

        // Detail Iuran Sukarela (If Selected)
        if (isVoluntaryChecked) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, CharcoalBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DETAIL IURAN SUKARELA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Voluntary Amount Input
                    OutlinedTextField(
                        value = voluntaryAmountText,
                        onValueChange = { voluntaryAmountText = it },
                        label = { Text("Nominal Iuran Sukarela") },
                        placeholder = { Text("Isi nominal iuran sukarela...") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    // Voluntary Note (Optional)
                    OutlinedTextField(
                        value = voluntaryNoteText,
                        onValueChange = { voluntaryNoteText = it },
                        label = { Text("Keterangan atau Tujuan Donasi (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save Button Validation
        val isFormValid = remember(isMandatoryChecked, isVoluntaryChecked, mandatoryAmountText, voluntaryAmountText) {
            if (!isMandatoryChecked && !isVoluntaryChecked) {
                false
            } else {
                val mandatoryOk = !isMandatoryChecked || (mandatoryAmountText.toDoubleOrNull() ?: 0.0) > 0.0
                val voluntaryOk = !isVoluntaryChecked || (voluntaryAmountText.toDoubleOrNull() ?: 0.0) > 0.0
                mandatoryOk && voluntaryOk
            }
        }

        Button(
            onClick = {
                val mAmount = if (isMandatoryChecked) (mandatoryAmountText.toDoubleOrNull() ?: 0.0) else 0.0
                val vAmount = if (isVoluntaryChecked) (voluntaryAmountText.toDoubleOrNull() ?: 0.0) else 0.0

                viewModel.addCombinedPayment(
                    memberId = member.id,
                    dateMs = paymentDateMs,
                    isMandatorySelected = isMandatoryChecked,
                    mandatoryMonth = selectedMonth,
                    mandatoryYear = selectedYear,
                    mandatoryAmount = mAmount,
                    mandatoryNote = mandatoryNoteText,
                    isVoluntarySelected = isVoluntaryChecked,
                    voluntaryAmount = vAmount,
                    voluntaryNote = voluntaryNoteText,
                    onSuccess = {
                        Toast.makeText(context, "Pembayaran berhasil disimpan.", Toast.LENGTH_SHORT).show()
                        viewModel.setSubScreen(SubScreen.MEMBER_PROFILE)
                    }
                )
            },
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else Color.Gray,
                disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Simpan Pembayaran",
                fontWeight = FontWeight.Bold,
                color = if (isFormValid) Color.White else Color.LightGray
            )
        }
    }
}

@Composable
fun AddMandatoryScreen(viewModel: AppViewModel) {
    val membersList by viewModel.members.collectAsStateWithLifecycle()
    val activeMembers = membersList.filter { it.status == "Aktif" }

    var selectedMemberId by remember { mutableStateOf(0) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var amountText by remember { mutableStateOf("10000") }
    var noteText by remember { mutableStateOf("") }

    var isMemberExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Dropdown Member Selector
        Box(modifier = Modifier.fillMaxWidth()) {
            val selectedMem = activeMembers.find { it.id == selectedMemberId }
            Button(
                onClick = { isMemberExpanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, CharcoalBorder)
            ) {
                Text(selectedMem?.name ?: "Pilih Anggota Aktif...", color = Color.White)
            }

            DropdownMenu(
                expanded = isMemberExpanded,
                onDismissRequest = { isMemberExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(CharcoalSurface)
            ) {
                activeMembers.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m.name, color = Color.White) },
                        onClick = {
                            selectedMemberId = m.id
                            isMemberExpanded = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Month Picker
            var isMonthExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { isMonthExpanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Text(viewModel.getIndonesianMonthName(selectedMonth), color = Color.White)
                }
                DropdownMenu(
                    expanded = isMonthExpanded,
                    onDismissRequest = { isMonthExpanded = false },
                    modifier = Modifier.background(CharcoalSurface)
                ) {
                    (1..12).forEach { m ->
                        DropdownMenuItem(
                            text = { Text(viewModel.getIndonesianMonthName(m), color = Color.White) },
                            onClick = {
                                selectedMonth = m
                                isMonthExpanded = false
                            }
                        )
                    }
                }
            }

            // Year Picker
            var isYearExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { isYearExpanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Text(selectedYear.toString(), color = Color.White)
                }
                DropdownMenu(
                    expanded = isYearExpanded,
                    onDismissRequest = { isYearExpanded = false },
                    modifier = Modifier.background(CharcoalSurface)
                ) {
                    listOf(2025, 2026, 2027, 2028).forEach { y ->
                        DropdownMenuItem(
                            text = { Text(y.toString(), color = Color.White) },
                            onClick = {
                                selectedYear = y
                                isYearExpanded = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Nominal Pembayaran") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Keterangan Opsional") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Button(
            onClick = {
                val amt = amountText.toDoubleOrNull() ?: 10000.0
                if (selectedMemberId > 0) {
                    viewModel.addMandatoryPayment(
                        memberId = selectedMemberId,
                        month = selectedMonth,
                        year = selectedYear,
                        amount = amt,
                        dateMs = System.currentTimeMillis(),
                        note = noteText
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Simpan Pembayaran Iuran", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun AddVoluntaryScreen(viewModel: AppViewModel) {
    val membersList by viewModel.members.collectAsStateWithLifecycle()
    val activeMembers = remember(membersList) { membersList.filter { it.status == "Aktif" } }

    var selectedMemberId by remember { mutableStateOf(0) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var paymentDateMs by remember { mutableStateOf(System.currentTimeMillis()) }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dateStr = sdf.format(Date(paymentDateMs))
    val context = LocalContext.current

    val selectedMem = activeMembers.find { it.id == selectedMemberId }
    val canSave = selectedMem != null && amountText.toDoubleOrNull() != null && (amountText.toDoubleOrNull() ?: 0.0) > 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (activeMembers.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC62828).copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, Color(0xFFC62828)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Belum ada anggota aktif. Tambahkan atau aktifkan anggota terlebih dahulu.",
                    color = Color(0xFFEF9A9A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            var isDialogShown by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDialogShown = true }
            ) {
                OutlinedTextField(
                    value = selectedMem?.name ?: "Pilih Anggota Aktif...",
                    onValueChange = {},
                    label = { Text("Pilih Anggota Aktif") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.White,
                        disabledBorderColor = CharcoalBorder,
                        disabledLabelColor = Color.Gray
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Pilih Anggota",
                            tint = Color.White
                        )
                    }
                )
            }

            if (isDialogShown) {
                MemberSelectionDialog(
                    members = activeMembers,
                    onDismiss = { isDialogShown = false },
                    onSelect = { m ->
                        selectedMemberId = m.id
                        isDialogShown = false
                    }
                )
            }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Nominal Iuran Sukarela") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance().apply { timeInMillis = paymentDateMs }
                    android.app.DatePickerDialog(
                        context,
                        { _, year, monthOfYear, dayOfMonth ->
                            val selectedCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, monthOfYear)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }
                            paymentDateMs = selectedCal.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
        ) {
            OutlinedTextField(
                value = dateStr,
                onValueChange = {},
                label = { Text("Tanggal Pembayaran") },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = CharcoalBorder,
                    disabledLabelColor = Color.Gray
                ),
                trailingIcon = {
                    Icon(Icons.Default.Event, contentDescription = "Pilih Tanggal", tint = Color.White)
                }
            )
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Keterangan atau Tujuan Donasi") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Button(
            onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (canSave && selectedMem != null) {
                    viewModel.addVoluntaryPayment(
                        memberId = selectedMem.id,
                        donorName = selectedMem.name,
                        amount = amt,
                        dateMs = paymentDateMs,
                        timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(paymentDateMs)),
                        note = noteText
                    )
                }
            },
            enabled = canSave,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canSave) MaterialTheme.colorScheme.primary else Color.Gray,
                disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Simpan Iuran Sukarela", fontWeight = FontWeight.Bold, color = if (canSave) Color.White else Color.LightGray)
        }
    }
}

@Composable
fun EditVoluntaryScreen(viewModel: AppViewModel) {
    val selectedPayment by viewModel.selectedVoluntaryPayment.collectAsStateWithLifecycle()

    if (selectedPayment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada data iuran sukarela yang dipilih", color = Color.Gray)
        }
        return
    }

    val payment = selectedPayment!!
    val membersList by viewModel.members.collectAsStateWithLifecycle()
    val activeMembers = remember(membersList) { membersList.filter { it.status == "Aktif" } }

    var selectedMemberId by remember(payment) {
        val existing = membersList.find { it.id == payment.memberId }
        val fallback = if (existing == null) membersList.find { it.name.trim().lowercase() == payment.donorName.trim().lowercase() } else null
        mutableStateOf(existing?.id ?: fallback?.id ?: 0)
    }

    var amountText by remember(payment) { mutableStateOf(payment.amountPaid.toInt().toString()) }
    var noteText by remember(payment) { mutableStateOf(payment.note) }
    var paymentDateMs by remember(payment) { mutableStateOf(payment.paymentDate) }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dateStr = sdf.format(Date(paymentDateMs))
    val context = LocalContext.current

    val selectedMem = membersList.find { it.id == selectedMemberId }
    val canSave = selectedMem != null && amountText.toDoubleOrNull() != null && (amountText.toDoubleOrNull() ?: 0.0) > 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (activeMembers.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC62828).copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, Color(0xFFC62828)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Belum ada anggota aktif. Tambahkan atau aktifkan anggota terlebih dahulu.",
                    color = Color(0xFFEF9A9A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            var isDialogShown by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDialogShown = true }
            ) {
                OutlinedTextField(
                    value = selectedMem?.name ?: "Pilih Anggota Aktif...",
                    onValueChange = {},
                    label = { Text("Pilih Anggota Aktif") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.White,
                        disabledBorderColor = CharcoalBorder,
                        disabledLabelColor = Color.Gray
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Pilih Anggota",
                            tint = Color.White
                        )
                    }
                )
            }

            if (isDialogShown) {
                MemberSelectionDialog(
                    members = activeMembers,
                    onDismiss = { isDialogShown = false },
                    onSelect = { m ->
                        selectedMemberId = m.id
                        isDialogShown = false
                    }
                )
            }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Nominal Iuran Sukarela") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance().apply { timeInMillis = paymentDateMs }
                    android.app.DatePickerDialog(
                        context,
                        { _, year, monthOfYear, dayOfMonth ->
                            val selectedCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, monthOfYear)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }
                            paymentDateMs = selectedCal.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
        ) {
            OutlinedTextField(
                value = dateStr,
                onValueChange = {},
                label = { Text("Tanggal Pembayaran") },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = CharcoalBorder,
                    disabledLabelColor = Color.Gray
                ),
                trailingIcon = {
                    Icon(Icons.Default.Event, contentDescription = "Pilih Tanggal", tint = Color.White)
                }
            )
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Keterangan atau Tujuan Donasi") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (canSave && selectedMem != null) {
                    viewModel.editVoluntaryPayment(
                        id = payment.id,
                        memberId = selectedMem.id,
                        donorName = selectedMem.name,
                        amount = amt,
                        dateMs = paymentDateMs,
                        timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(paymentDateMs)),
                        note = noteText,
                        isCancelled = payment.isCancelled
                    )
                }
            },
            enabled = canSave,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canSave) MaterialTheme.colorScheme.primary else Color.Gray,
                disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = if (canSave) Color.White else Color.LightGray)
        }

        Button(
            onClick = {
                viewModel.deleteVoluntaryPayment(payment)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Hapus Transaksi", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun AddOtherIncomeScreen(viewModel: AppViewModel) {
    var source by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = source,
            onValueChange = { source = it },
            label = { Text("Sumber Pemasukan Lain") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Nominal Pemasukan") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Keterangan") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Button(
            onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (source.isNotBlank() && amt > 0.0) {
                    viewModel.addOtherIncome(
                        source = source,
                        amount = amt,
                        dateMs = System.currentTimeMillis(),
                        note = noteText
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Simpan Pemasukan Lain", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun AddExpenseScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val membersList by viewModel.members.collectAsStateWithLifecycle()
    val activeMembers = remember(membersList) { membersList.filter { it.status == "Aktif" } }

    var selectedCategory by remember { mutableStateOf("Operasional") } // "Bantuan Sosial", "Kegiatan", "Konsumsi", "Operasional", "Lainnya"
    var amountText by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var expenseDateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var expenseTime by remember { mutableStateOf("") }
    var selectedMemberId by remember { mutableStateOf(0) }

    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isMemberDialogShown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Category Selector
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { isCategoryExpanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, CharcoalBorder)
            ) {
                Text("Kategori: $selectedCategory", color = Color.White)
            }

            DropdownMenu(
                expanded = isCategoryExpanded,
                onDismissRequest = { isCategoryExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(CharcoalSurface)
            ) {
                listOf("Bantuan Sosial", "Kegiatan", "Konsumsi", "Operasional", "Lainnya").forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c, color = Color.White) },
                        onClick = {
                            selectedCategory = c
                            isCategoryExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Nominal Pengeluaran") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        // Tanggal Pengeluaran (Wajib Diisi, Default Hari Ini)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance().apply { timeInMillis = expenseDateMs }
                    android.app.DatePickerDialog(
                        context,
                        { _, year, monthOfYear, dayOfMonth ->
                            val selectedCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, monthOfYear)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }
                            expenseDateMs = selectedCal.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
        ) {
            OutlinedTextField(
                value = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(expenseDateMs)),
                onValueChange = {},
                label = { Text("Tanggal Pengeluaran (Wajib)") },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = CharcoalBorder,
                    disabledLabelColor = Color.Gray
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pilih Tanggal",
                        tint = Color.White
                    )
                }
            )
        }

        // Waktu Pengeluaran (Opsional)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance()
                    android.app.TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            expenseTime = String.format("%02d:%02d", hourOfDay, minute)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                }
        ) {
            OutlinedTextField(
                value = expenseTime,
                onValueChange = { expenseTime = it },
                label = { Text("Waktu Pengeluaran (Opsional)") },
                placeholder = { Text("Klik ikon jam atau isi manual, misal 14:30") },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = CharcoalBorder,
                    disabledLabelColor = Color.Gray
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Pilih Waktu",
                        tint = Color.White
                    )
                }
            )
        }

        // Recipient label and optional member selector
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nama Penerima", fontSize = 12.sp, color = Color.Gray)
                
                if (activeMembers.isNotEmpty()) {
                    TextButton(
                        onClick = { isMemberDialogShown = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Pilih Anggota Opsional", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            OutlinedTextField(
                value = recipient,
                onValueChange = { 
                    recipient = it
                    // Reset member selection if user types something else
                    if (selectedMemberId != 0) {
                        val matchingMember = activeMembers.find { m -> m.name.equals(it, ignoreCase = true) }
                        if (matchingMember == null) {
                            selectedMemberId = 0
                        }
                    }
                },
                placeholder = { Text("Ketik nama bebas atau klik Pilih Anggota...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                trailingIcon = if (selectedMemberId != 0) {
                    {
                        Text(
                            text = "Anggota Terpilih",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                } else null
            )
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Keterangan Pengeluaran") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Button(
            onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (amt > 0.0 && recipient.isNotBlank()) {
                    viewModel.addExpense(
                        category = selectedCategory,
                        amount = amt,
                        dateMs = expenseDateMs,
                        recipient = recipient,
                        note = noteText,
                        time = expenseTime,
                        memberId = selectedMemberId
                    )
                    Toast.makeText(context, "Pengeluaran berhasil disimpan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Mohon lengkapi nominal dan nama penerima", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Simpan Pengeluaran", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }

    if (isMemberDialogShown) {
        MemberSelectionDialog(
            members = activeMembers,
            onDismiss = { isMemberDialogShown = false },
            onSelect = { member ->
                recipient = member.name
                selectedMemberId = member.id
                isMemberDialogShown = false
            }
        )
    }
}

@Composable
fun EditExpenseScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val membersList by viewModel.members.collectAsStateWithLifecycle()
    val activeMembers = remember(membersList) { membersList.filter { it.status == "Aktif" } }
    val expense by viewModel.selectedExpense.collectAsStateWithLifecycle()

    if (expense == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada pengeluaran yang dipilih", color = Color.White)
        }
        return
    }

    val exp = expense!!

    var selectedCategory by remember(exp) { mutableStateOf(exp.category) }
    var amountText by remember(exp) { mutableStateOf(exp.amount.toLong().toString()) }
    var recipient by remember(exp) { mutableStateOf(exp.recipientName.ifEmpty { exp.recipient }) }
    var noteText by remember(exp) { mutableStateOf(exp.notes.ifEmpty { exp.note }) }
    var expenseDateMs by remember(exp) { mutableStateOf(exp.expenseDate) }
    var expenseTime by remember(exp) { mutableStateOf(exp.expenseTime) }
    var selectedMemberId by remember(exp) { mutableStateOf(exp.memberId) }
    var isCancelled by remember(exp) { mutableStateOf(exp.isCancelled) }

    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isMemberDialogShown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Category Selector
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { isCategoryExpanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, CharcoalBorder)
            ) {
                Text("Kategori: $selectedCategory", color = Color.White)
            }

            DropdownMenu(
                expanded = isCategoryExpanded,
                onDismissRequest = { isCategoryExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(CharcoalSurface)
            ) {
                listOf("Bantuan Sosial", "Kegiatan", "Konsumsi", "Operasional", "Lainnya").forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c, color = Color.White) },
                        onClick = {
                            selectedCategory = c
                            isCategoryExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Nominal Pengeluaran") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        // Tanggal Pengeluaran (Wajib Diisi, Editable)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance().apply { timeInMillis = expenseDateMs }
                    android.app.DatePickerDialog(
                        context,
                        { _, year, monthOfYear, dayOfMonth ->
                            val selectedCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, monthOfYear)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }
                            expenseDateMs = selectedCal.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
        ) {
            OutlinedTextField(
                value = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(expenseDateMs)),
                onValueChange = {},
                label = { Text("Tanggal Pengeluaran (Wajib)") },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = CharcoalBorder,
                    disabledLabelColor = Color.Gray
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pilih Tanggal",
                        tint = Color.White
                    )
                }
            )
        }

        // Waktu Pengeluaran (Opsional)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance()
                    android.app.TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            expenseTime = String.format("%02d:%02d", hourOfDay, minute)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                }
        ) {
            OutlinedTextField(
                value = expenseTime,
                onValueChange = { expenseTime = it },
                label = { Text("Waktu Pengeluaran (Opsional)") },
                placeholder = { Text("Klik ikon jam atau isi manual, misal 14:30") },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = CharcoalBorder,
                    disabledLabelColor = Color.Gray
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Pilih Waktu",
                        tint = Color.White
                    )
                }
            )
        }

        // Recipient label and optional member selector
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nama Penerima", fontSize = 12.sp, color = Color.Gray)
                
                if (activeMembers.isNotEmpty()) {
                    TextButton(
                        onClick = { isMemberDialogShown = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Pilih Anggota Opsional", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            OutlinedTextField(
                value = recipient,
                onValueChange = { 
                    recipient = it
                    // Reset member selection if user types something else
                    if (selectedMemberId != 0) {
                        val matchingMember = activeMembers.find { m -> m.name.equals(it, ignoreCase = true) }
                        if (matchingMember == null) {
                            selectedMemberId = 0
                        }
                    }
                },
                placeholder = { Text("Ketik nama bebas atau klik Pilih Anggota...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                trailingIcon = if (selectedMemberId != 0) {
                    {
                        Text(
                            text = "Anggota Terpilih",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                } else null
            )
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Keterangan Pengeluaran") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        // Cancelled Switch (Toggle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Batalkan Transaksi", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Tandai transaksi ini sebagai dibatalkan", fontSize = 11.sp, color = Color.Gray)
            }
            Switch(
                checked = isCancelled,
                onCheckedChange = { isCancelled = it }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    viewModel.setSubScreen(SubScreen.NONE)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, CharcoalBorder),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Batal", color = Color.White)
            }

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0 && recipient.isNotBlank()) {
                        viewModel.editExpense(
                            id = exp.id,
                            category = selectedCategory,
                            amount = amt,
                            dateMs = expenseDateMs,
                            recipient = recipient,
                            note = noteText,
                            isCancelled = isCancelled,
                            time = expenseTime,
                            memberId = selectedMemberId,
                            createdAt = exp.createdAt
                        )
                        Toast.makeText(context, "Pengeluaran berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Mohon lengkapi nominal dan nama penerima", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    if (isMemberDialogShown) {
        MemberSelectionDialog(
            members = activeMembers,
            onDismiss = { isMemberDialogShown = false },
            onSelect = { member ->
                recipient = member.name
                selectedMemberId = member.id
                isMemberDialogShown = false
            }
        )
    }
}

@Composable
fun MemberSelectionDialog(
    members: List<MemberEntity>,
    onDismiss: () -> Unit,
    onSelect: (MemberEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredMembers = remember(searchQuery, members) {
        members.filter { it.name.contains(searchQuery, ignoreCase = true) }
            .sortedBy { it.name.lowercase() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pilih Anggota",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama anggota...", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = CharcoalBorder
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = Color.Gray)
                    }
                )

                if (filteredMembers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada anggota cocok", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMembers) { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CharcoalSurface)
                                    .clickable { onSelect(m) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Avatar with initial
                                val initial = m.name.firstOrNull()?.uppercase() ?: "?"
                                val avatarBgColor = remember(m.id) {
                                    val colors = listOf(
                                        Color(0xFF1565C0), // Blue
                                        Color(0xFF2E7D32), // Green
                                        Color(0xFFC62828), // Red
                                        Color(0xFFE65100), // Orange
                                        Color(0xFF6A1B9A), // Purple
                                        Color(0xFF00838F)  // Teal
                                    )
                                    colors[m.id % colors.size]
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(avatarBgColor)
                                ) {
                                    Text(
                                        text = initial,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }

                                Column {
                                    Text(
                                        text = m.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = m.whatsApp,
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.White)
            }
        },
        containerColor = CharcoalDark,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
fun BackupDataScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val configState by viewModel.config.collectAsStateWithLifecycle()

    val sharedPrefs = remember { context.getSharedPreferences("steker_backup_prefs", android.content.Context.MODE_PRIVATE) }
    var lastBackupInfo by remember {
        mutableStateOf(sharedPrefs.getString("last_backup_info", "Belum ada riwayat backup") ?: "Belum ada riwayat backup")
    }

    // Dialog state variables
    var showExportWarning by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showImportOptionDialog by remember { mutableStateOf(false) }

    // PIN input state
    var pinInput by remember { mutableStateOf("") }
    var pinErrorMsg by remember { mutableStateOf("") }

    // Backup content currently being processed for import
    var pendingImportJson by remember { mutableStateOf("") }

    // File Picker Launcher for JSON import
    val importFilePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonText = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                
                // Preliminary Validation
                if (jsonText.isBlank() || (!jsonText.contains("\"config\"") && !jsonText.contains("\"members\""))) {
                    Toast.makeText(context, "File backup tidak valid atau bukan dari Steker App.", Toast.LENGTH_LONG).show()
                } else {
                    pendingImportJson = jsonText
                    pinInput = ""
                    pinErrorMsg = ""
                    showPinDialog = true
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membaca file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Section / Card Info
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Backup Offline & Pindah Data",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Gunakan fitur ini untuk membuat backup data lokal Anda secara offline atau memindahkannya ke ponsel baru tanpa internet.",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = CharcoalBorder)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Informasi Backup Terakhir:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = lastBackupInfo,
                        fontSize = 11.sp,
                        color = Color.White,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Action Options
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Export Card Button
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CharcoalBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showExportWarning = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1565C0).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Export", tint = Color(0xFF1565C0))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("1. Export Database", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Ekspor seluruh data keuangan dan anggota ke file JSON untuk dipindahkan atau disimpan.", fontSize = 11.sp, color = Color.LightGray)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }

                // Import Card Button
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CharcoalBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { importFilePicker.launch("application/json") }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = "Import", tint = Color(0xFF2E7D32))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("2. Import Database", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Pilih file backup JSON untuk memulihkan seluruh data (ganti semua atau gabungkan data).", fontSize = 11.sp, color = Color.LightGray)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOGS
    // ==========================================

    // 1. Export Warning Dialog
    if (showExportWarning) {
        AlertDialog(
            onDismissRequest = { showExportWarning = false },
            title = { Text("Peringatan Keamanan", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "File backup berisi data keuangan dan data anggota. Jangan dibagikan kepada pihak yang tidak berkepentingan.",
                    color = Color.LightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportWarning = false
                        viewModel.triggerLocalBackup { jsonString ->
                            if (jsonString != null) {
                                try {
                                    val dateStr = SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.getDefault()).format(Date())
                                    val fileName = "Steker-App-Backup-$dateStr.json"
                                    
                                    val cacheFile = java.io.File(context.externalCacheDir ?: context.cacheDir, fileName)
                                    java.io.FileOutputStream(cacheFile).use { fos ->
                                        fos.write(jsonString.toByteArray())
                                    }
                                    
                                    // Update backup info in shared prefs
                                    val backupDateTime = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                                    val updatedInfo = "Berhasil backup pada $backupDateTime\nFile: $fileName"
                                    sharedPrefs.edit().putString("last_backup_info", updatedInfo).apply()
                                    lastBackupInfo = updatedInfo

                                    Toast.makeText(context, "Backup berhasil disimpan di: $fileName", Toast.LENGTH_SHORT).show()

                                    // Trigger Share Sheet
                                    val uri: android.net.Uri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        cacheFile
                                    )
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, fileName)
                                        putExtra(android.content.Intent.EXTRA_TEXT, "Backup Data Steker App v1.5 - Tanggal: $backupDateTime")
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    val chooser = android.content.Intent.createChooser(shareIntent, "Kirim / Simpan Backup JSON")
                                    chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(chooser)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal mengekspor file: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Gagal membuat backup data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Lanjutkan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportWarning = false }) {
                    Text("Batal", color = Color.White)
                }
            },
            containerColor = CharcoalDark,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // 2. Security PIN Prompt Dialog before Import
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Verifikasi Keamanan", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Demi keamanan data keuangan organisasi, silakan masukkan PIN Pengurus aktif Anda saat ini:", color = Color.LightGray)
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        placeholder = { Text("PIN Pengurus (6 digit)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = CharcoalBorder
                        )
                    )
                    if (pinErrorMsg.isNotEmpty()) {
                        Text(pinErrorMsg, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput == configState?.pin) {
                            showPinDialog = false
                            showImportOptionDialog = true
                        } else {
                            pinErrorMsg = "PIN yang Anda masukkan salah. Akses ditolak."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Verifikasi", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Batal", color = Color.White)
                }
            },
            containerColor = CharcoalDark,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // 3. Overwrite vs Merge Option Dialog
    if (showImportOptionDialog) {
        AlertDialog(
            onDismissRequest = { showImportOptionDialog = false },
            title = { Text("Metode Import Data", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Pilih bagaimana data backup ini akan dimasukkan ke dalam aplikasi:\n\n" +
                            "1. Ganti Semua Data (Overwrite / Default untuk HP baru):\n" +
                            "Menghapus seluruh data lama di HP ini dan menggantinya 100% dengan data dari file backup.\n\n" +
                            "2. Gabungkan Data (Merge):\n" +
                            "Menggabungkan data dari file backup dengan data yang sudah ada di HP ini tanpa menghapus data lama (bila ada ID yang sama, diupdate ke versi terbaru).",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showImportOptionDialog = false
                            viewModel.triggerLocalRestore(pendingImportJson, overwrite = true) { success ->
                                if (success) {
                                    val importDateTime = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                                    val updatedInfo = "Berhasil import (Ganti Semua) pada $importDateTime"
                                    sharedPrefs.edit().putString("last_backup_info", updatedInfo).apply()
                                    lastBackupInfo = updatedInfo
                                    Toast.makeText(context, "Seluruh data berhasil dipulihkan (Ganti Semua Data)!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Gagal memproses restore database.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ganti Semua Data (Overwrite)", color = Color.White)
                    }

                    Button(
                        onClick = {
                            showImportOptionDialog = false
                            viewModel.triggerLocalRestore(pendingImportJson, overwrite = false) { success ->
                                if (success) {
                                    val importDateTime = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                                    val updatedInfo = "Berhasil import (Gabungkan) pada $importDateTime"
                                    sharedPrefs.edit().putString("last_backup_info", updatedInfo).apply()
                                    lastBackupInfo = updatedInfo
                                    Toast.makeText(context, "Data berhasil digabungkan dengan database lokal!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Gagal memproses penggabungan database.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Gabungkan Data (Merge)", color = Color.White)
                    }

                    TextButton(
                        onClick = { showImportOptionDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Batal", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            },
            dismissButton = {},
            containerColor = CharcoalDark,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
