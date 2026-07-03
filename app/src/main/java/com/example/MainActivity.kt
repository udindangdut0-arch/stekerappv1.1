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
                text = "Steker App v1.1",
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
                text = "Organisasi Steker Hitam",
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
        viewModel.setSubScreen(SubScreen.NONE)
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
                    SubScreen.ADD_MANDATORY -> AddMandatoryScreen(viewModel)
                    SubScreen.ADD_VOLUNTARY -> AddVoluntaryScreen(viewModel)
                    SubScreen.ADD_EXPENSE -> AddExpenseScreen(viewModel)
                    SubScreen.ADD_OTHER_INCOME -> AddOtherIncomeScreen(viewModel)
                    SubScreen.SETTINGS -> SettingsScreen(viewModel)
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
                    text = "STEKER APP V1.1",
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
        SubScreen.ADD_MANDATORY -> "Tambah Iuran Wajib"
        SubScreen.ADD_VOLUNTARY -> "Tambah Iuran Sukarela"
        SubScreen.ADD_EXPENSE -> "Tambah Pengeluaran"
        SubScreen.ADD_OTHER_INCOME -> "Tambah Pemasukan Lain"
        SubScreen.SETTINGS -> "Pengaturan Aplikasi"
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
                onClick = { viewModel.setSubScreen(SubScreen.NONE) },
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
                        text = "Halo, Pengurus!",
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
                        onClick = { viewModel.deactivateMember(member) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Nonaktifkan", color = Color.White)
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

        // Payments History Section
        item {
            Text(
                text = "Riwayat Pembayaran Anggota",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Mandatory List items
        item {
            Text("Iuran Wajib (Bulanan)", fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.SemiBold)
        }

        if (mandatoryHistory.isNotEmpty()) {
            items(mandatoryHistory) { p ->
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
        } else {
            item {
                Text(
                    text = "Belum ada riwayat pembayaran iuran wajib",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // Voluntary List items
        item {
            Text("Iuran Sukarela (Donasi)", fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.SemiBold)
        }

        if (voluntaryHistory.isNotEmpty()) {
            items(voluntaryHistory) { p ->
                TransactionListItem(
                    title = "Iuran Sukarela ${if (p.paymentTime.isNotBlank()) "- " + p.paymentTime else ""}",
                    amount = p.amountPaid,
                    dateMs = p.paymentDate,
                    isCancelled = p.isCancelled,
                    note = p.note,
                    onToggleCancel = {
                        viewModel.editVoluntaryPayment(
                            id = p.id,
                            donorName = p.donorName,
                            amount = p.amountPaid,
                            dateMs = p.paymentDate,
                            timeStr = p.paymentTime,
                            note = p.note,
                            isCancelled = !p.isCancelled
                        )
                    }
                )
            }
        } else {
            item {
                Text(
                    text = "Belum ada riwayat iuran sukarela",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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
    onToggleCancel: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CharcoalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCancelled) Color.Gray else Color.White,
                        style = if (isCancelled) LocalTextStyle.current.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else LocalTextStyle.current
                    )
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

            // Cancel action button (to toggle cancel state)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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
                            donorName = p.donorName,
                            amount = p.amountPaid,
                            dateMs = p.paymentDate,
                            timeStr = p.paymentTime,
                            note = p.note,
                            isCancelled = !p.isCancelled
                        )
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
                                    viewModel.editVoluntaryPayment(original.id, original.donorName, original.amountPaid, original.paymentDate, original.paymentTime, original.note, !original.isCancelled)
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
                                    viewModel.editExpense(original.id, original.category, original.amount, original.expenseDate, original.recipient, original.note, !original.isCancelled)
                                }
                            }
                        }
                    }
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
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("REKAP PEMBAYAR PERIODE INI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Anggota Bayar Wajib: ${summary.numMandatoryPayers}", fontSize = 11.sp, color = Color.White)
                    Text("Wajib: ${formatRupiah(summary.totalMandatoryCollected)}", fontSize = 11.sp, color = Color.LightGray)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Anggota Bayar Sukarela: ${summary.numVoluntaryPayers}", fontSize = 11.sp, color = Color.White)
                    Text("Sukarela: ${formatRupiah(summary.totalVoluntaryCollected)}", fontSize = 11.sp, color = Color.LightGray)
                }
                Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL DANA TERKUMPUL:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(formatRupiah(summary.totalFundsReceived), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
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
                    Text("Steker App v1.1", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Satu Aplikasi untuk Semua Kebutuhan", fontSize = 11.sp, color = Color.LightGray)
                    Text("Organisasi Steker Hitam", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
                title = "Pengaturan PIN & Backup Data",
                description = "Ubah PIN, backup, restore database, dan ekspor.",
                icon = Icons.Default.Settings,
                onClick = { viewModel.setSubScreen(SubScreen.SETTINGS) }
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
    var donorName by remember { mutableStateOf("") }
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
            value = donorName,
            onValueChange = { donorName = it },
            label = { Text("Nama Anggota / Donatur") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Nominal Iuran Sukarela") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

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
                if (donorName.isNotBlank() && amt > 0.0) {
                    viewModel.addVoluntaryPayment(
                        donorName = donorName,
                        amount = amt,
                        dateMs = System.currentTimeMillis(),
                        timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
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
            Text("Simpan Iuran Sukarela", fontWeight = FontWeight.Bold, color = Color.White)
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
    var selectedCategory by remember { mutableStateOf("Operasional") } // "Bantuan Sosial", "Kegiatan", "Konsumsi", "Operasional", "Lainnya"
    var amountText by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    var isCategoryExpanded by remember { mutableStateOf(false) }

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

        OutlinedTextField(
            value = recipient,
            onValueChange = { recipient = it },
            label = { Text("Nama Penerima") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

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
                        dateMs = System.currentTimeMillis(),
                        recipient = recipient,
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
            Text("Simpan Pengeluaran", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
