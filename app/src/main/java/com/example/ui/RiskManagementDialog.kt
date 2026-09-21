package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.RiskSettings

@Composable
fun RiskManagementDialog(
    currentSettings: RiskSettings,
    onSaveSettings: (RiskSettings) -> Unit,
    onResetSession: () -> Unit,
    onDismiss: () -> Unit
) {
    var stopLossText by remember { mutableStateOf(currentSettings.stopLossLimit.toString()) }
    var takeProfitText by remember { mutableStateOf(currentSettings.takeProfitLimit.toString()) }
    var tradeAmountText by remember { mutableStateOf(currentSettings.tradeAmount.toString()) }
    var martingaleText by remember { mutableStateOf(currentSettings.martingaleMultiplier.toString()) }

    val baseTrade = tradeAmountText.toDoubleOrNull() ?: 2.0
    val multi = martingaleText.toDoubleOrNull() ?: 2.2

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141B2B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .testTag("risk_management_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ঝুঁকি ব্যবস্থাপনা (Risk Control)",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Session PnL Banner
                val pnlColor = when {
                    currentSettings.currentSessionPnL > 0 -> Color(0xFF00E676)
                    currentSettings.currentSessionPnL < 0 -> Color(0xFFFF3D00)
                    else -> Color(0xFFB0BEC5)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E283E), Color(0xFF182236))
                            )
                        )
                        .border(1.dp, pnlColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Session Profit / Loss:", color = Color(0xFF90A4AE), fontSize = 11.sp)
                            Text(
                                text = "${if (currentSettings.currentSessionPnL >= 0) "+$" else "-$"}${"%.2f".format(kotlin.math.abs(currentSettings.currentSessionPnL))}",
                                color = pnlColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Win Rate:", color = Color(0xFF90A4AE), fontSize = 11.sp)
                            Text(
                                text = "${currentSettings.winRate}% (${currentSettings.winTrades}/${currentSettings.totalTrades})",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Alert status if Stop Loss or Take Profit hit
                if (currentSettings.isTakeProfitHit) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x2200E676))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🎯 অভিনন্দন! আপনার টেক প্রফিট টার্গেট অর্জিত হয়েছে। আজকের মত লাভ তুলে নিয়ে ট্রেডিং বন্ধ করুন।",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (currentSettings.isStopLossHit) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22FF3D00))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFFF3D00), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🛑 সাবধান! স্টপ লস লিমিট স্পর্শ করেছে। মূলধন রক্ষার্থে আজকের সেশনের ট্রেড বন্ধ রাখা আবশ্যক।",
                                color = Color(0xFFFF3D00),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Inputs for Stop Loss and Take Profit
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = stopLossText,
                        onValueChange = { stopLossText = it },
                        label = { Text("Stop Loss ($)", color = Color(0xFFFF8A80), fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.TrendingDown, null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFF5252),
                            unfocusedBorderColor = Color(0xFF455A64)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("stop_loss_input")
                    )

                    OutlinedTextField(
                        value = takeProfitText,
                        onValueChange = { takeProfitText = it },
                        label = { Text("Take Profit ($)", color = Color(0xFFB9F6CA), fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF455A64)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("take_profit_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Base Trade & Martingale
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tradeAmountText,
                        onValueChange = { tradeAmountText = it },
                        label = { Text("Base Trade ($)", color = Color(0xFF80D8FF), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF455A64)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = martingaleText,
                        onValueChange = { martingaleText = it },
                        label = { Text("Martingale (X)", color = Color(0xFFFFD180), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFB300),
                            unfocusedBorderColor = Color(0xFF455A64)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Safe Money Management Table (Bengali & English)
                Text(
                    text = "📊 মানি ম্যানেজমেন্ট সেফটি লেভেল (Martingale Plan):",
                    color = Color(0xFFECEFF1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                val step1 = baseTrade
                val step2 = baseTrade * multi
                val step3 = step2 * multi

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0E1422))
                        .border(1.dp, Color(0xFF263238), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("1st Entry (Standard):", color = Color(0xFF90A4AE), fontSize = 11.sp)
                            Text("$${"%.2f".format(step1)}", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("2nd Entry (Recovery):", color = Color(0xFF90A4AE), fontSize = 11.sp)
                            Text("$${"%.2f".format(step2)}", color = Color(0xFFFFB300), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("3rd Entry (Max Safe Cap):", color = Color(0xFF90A4AE), fontSize = 11.sp)
                            Text("$${"%.2f".format(step3)}", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save & Reset Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onResetSession,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset PnL", fontSize = 12.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            val newSL = stopLossText.toDoubleOrNull() ?: currentSettings.stopLossLimit
                            val newTP = takeProfitText.toDoubleOrNull() ?: currentSettings.takeProfitLimit
                            val newAmt = tradeAmountText.toDoubleOrNull() ?: currentSettings.tradeAmount
                            val newMulti = martingaleText.toDoubleOrNull() ?: currentSettings.martingaleMultiplier

                            onSaveSettings(
                                currentSettings.copy(
                                    stopLossLimit = newSL,
                                    takeProfitLimit = newTP,
                                    tradeAmount = newAmt,
                                    martingaleMultiplier = newMulti
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f).testTag("save_risk_settings_button")
                    ) {
                        Text("Save Settings", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
