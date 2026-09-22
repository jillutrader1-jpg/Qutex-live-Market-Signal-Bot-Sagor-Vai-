package com.example

import android.os.Bundle
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.SignalDirection
import com.example.ui.FloatingScanButton
import com.example.ui.QuotexLiveWebView
import com.example.ui.QuotexWebViewController
import com.example.ui.RiskManagementDialog
import com.example.ui.SignalResultOverlay
import com.example.ui.TradingViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure WebView HTTP and Code Cache directories exist to prevent Chromium simple_file_enumerator error
        try {
            val codeCacheDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
            File(codeCacheDir, "js").mkdirs()
            File(codeCacheDir, "wasm").mkdirs()
        } catch (_: Exception) {}

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TradingBotApp()
            }
        }
    }
}

@Composable
fun TradingBotApp(viewModel: TradingViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val webViewController = remember { QuotexWebViewController() }

    var showRiskDialog by remember { mutableStateOf(false) }
    var showMirrorDialog by remember { mutableStateOf(false) }
    var currentQuotexUrl by remember { mutableStateOf("https://market-qx.info/en/sign-in/") }
    var customUrlInput by remember { mutableStateOf("") }

    // Intercept back button to navigate inside the Quotex live browser
    BackHandler(enabled = true) {
        if (!webViewController.goBack()) {
            // Reached start page
        }
    }

    LaunchedEffect(uiState.tradeResultNotification) {
        val notif = uiState.tradeResultNotification
        if (notif != null) {
            snackbarHostState.showSnackbar(notif)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0C101A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Bar: Sg Trader Vai AI Bot Header with Quick Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D47A1))
                    .padding(vertical = 4.dp, horizontal = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF80D8FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Md Sagor Trader Vai AI Bot",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Quick Tools & Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { webViewController.goBack() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { webViewController.reload() },
                            modifier = Modifier.size(32.dp).testTag("quotex_reload_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload Quotex",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { showMirrorDialog = true },
                            modifier = Modifier.size(32.dp).testTag("quotex_mirror_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Quotex Mirrors & Login Help",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { showRiskDialog = true },
                            modifier = Modifier.size(32.dp).testTag("top_risk_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Risk Management",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Notice strip for US Region Block explanation & quick mirror bypass
            if (uiState.isLiveQuotexMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF132238))
                        .clickable { showMirrorDialog = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "US Region Block দেখতে পাচ্ছেন? সমাধান ও বিকল্প লিংক দেখতে ট্যাপ করুন",
                            color = Color(0xFFFFE082),
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "সহায়তা",
                            color = Color(0xFF00E5FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Main Market Body: Dedicated to official live Quotex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Live Quotex Official Platform WebView
                QuotexLiveWebView(
                    controller = webViewController,
                    isScanning = uiState.isScanning,
                    scanProgressText = uiState.scanProgressText,
                    modifier = Modifier.fillMaxSize(),
                    initialUrl = currentQuotexUrl
                )

                // Floating "Sg Vai Scan" AI Bot circular button
                FloatingScanButton(
                    isScanning = uiState.isScanning,
                    onScanClick = {
                        val bitmap = webViewController.captureCurrentScreen()
                        webViewController.evaluateTrendFromDom { trend ->
                            viewModel.scanLiveQuotex(bitmap, trend)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 20.dp, top = 40.dp)
                )

                // Floating Signal Result Overlay once scan finishes
                SignalResultOverlay(
                    signal = uiState.activeSignal,
                    countdownSeconds = uiState.countdownSeconds,
                    onDismiss = { viewModel.dismissSignal() },
                    onSpeakClick = {
                        uiState.activeSignal?.let { viewModel.speakSignal(it) }
                    },
                    onExecuteTrade = { dir ->
                        viewModel.executeTrade(dir)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                )
            }
        }

        // Risk Management Dialog (Stop Loss & Take Profit Tool)
        if (showRiskDialog) {
            RiskManagementDialog(
                currentSettings = uiState.riskSettings,
                onSaveSettings = { viewModel.updateRiskSettings(it) },
                onResetSession = { viewModel.resetSessionPnL() },
                onDismiss = { showRiskDialog = false }
            )
        }

        // Quotex Mirror Selection & Region Bypass Help Dialog
        if (showMirrorDialog) {
            val mirrors = listOf(
                "market-qx.info" to "https://market-qx.info/en/sign-in/",
                "qxbroker.com" to "https://qxbroker.com/en/sign-in/",
                "quotex.com" to "https://quotex.com/en/sign-in/",
                "qx-market.com" to "https://qx-market.com/en/sign-in/"
            )

            AlertDialog(
                onDismissRequest = { showMirrorDialog = false },
                containerColor = Color(0xFF101726),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quotex লগইন ও মিরর সহায়তা",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Region Block Explanation Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "⚠️ কেন 'Not available in (United States)' দেখাচ্ছে?",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "গুগল এআই স্টুডিও (AI Studio)-এর ব্রাউজার এমুলেটরটি ইউএসএ (USA) ক্লাউড সার্ভারে চলে। আমেরিকায় বাইনারি ব্রোকার নিষিদ্ধ থাকায় কোটেক্স সার্ভার ক্লাউড এমুলেটরের ইউএস আইপি (US IP) ব্লক করে দেয়।",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "💡 সমাধান ১: আপনার নিজের ফোনে APK চালান",
                            color = Color(0xFF00E5FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "অ্যাপটি ডাউনলোড/এক্সপোর্ট করে সরাসরি আপনার আসল অ্যান্ড্রয়েড ফোনে ইনস্টল করলে আপনার বাংলাদেশি নেটওয়ার্ক (বা ফোনের VPN) ব্যবহার হবে—তখন কোনো রিজিয়ন ব্লক থাকবে না, সরাসরি লগইন করতে পারবেন।",
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "💡 সমাধান ২: কার্যকর মিরর লিংক নির্বাচন করুন",
                            color = Color(0xFF00E5FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        mirrors.forEach { (name, url) ->
                            Button(
                                onClick = {
                                    currentQuotexUrl = url
                                    webViewController.loadUrl(url)
                                    showMirrorDialog = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentQuotexUrl == url) Color(0xFF0288D1) else Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (currentQuotexUrl == url) "সক্রিয় (Active)" else "লোড করুন",
                                        color = if (currentQuotexUrl == url) Color(0xFF80D8FF) else Color(0xFF90A4AE),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Custom URL Field
                        Text(
                            text = "কাস্টম লিংক (Custom Quotex URL):",
                            color = Color(0xFF90A4AE),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = customUrlInput,
                                onValueChange = { customUrlInput = it },
                                placeholder = {
                                    Text("https://...", fontSize = 11.sp, color = Color(0xFF64748B))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (customUrlInput.isNotBlank()) {
                                        val target = if (!customUrlInput.startsWith("http")) {
                                            "https://$customUrlInput"
                                        } else {
                                            customUrlInput
                                        }
                                        currentQuotexUrl = target
                                        webViewController.loadUrl(target)
                                        showMirrorDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("যান", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showMirrorDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                    ) {
                        Text("বন্ধ করুন", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

