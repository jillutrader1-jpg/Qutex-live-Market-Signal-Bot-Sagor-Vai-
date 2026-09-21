package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrencyPair

@Composable
fun QuotexHeader(
    onDepositClick: () -> Unit,
    onRiskToolClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPromoBanner by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxWidth().background(Color(0xFF0F1420))) {
        // Top App Bar: Account ID, Notification Bell (with 65 badge), Deposit Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Masked Account ID
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1B2335))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "******* **********",
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color(0xFF90A4AE),
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Risk Manager Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E283E))
                        .clickable { onRiskToolClick() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("top_risk_tool_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Risk Tool",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Risk SL/TP", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Notification Bell with 65 badge
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .offset(x = (-2).dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF3D00))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("65", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Green Deposit Button matching screenshot
                Button(
                    onClick = onDepositClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00C853),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp).testTag("deposit_button")
                ) {
                    Text(
                        text = "Deposit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Promo Banner (Rocket: Get a 50% bonus on your deposit! 50% [X])
        if (showPromoBanner) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF00796B))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚀", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Get a 50% bonus on your deposit!",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF004D40))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("50%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Banner",
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { showPromoBanner = false }
                        )
                    }
                }
            }
        }

        // Sub Toolbar: [...] [00:14:46 UT (i)] [1 ▶] [chart indicator icon]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // More button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1A2338)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MoreHoriz, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Time info badge: 00:14:46 UT
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF131A29))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00E676)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("00:14:46 UT", color = Color(0xFF90A4AE), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Info, null, tint = Color(0xFF00B0FF), modifier = Modifier.size(12.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Candle timeframe selector: 1 ▶
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1A2338))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text("1", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("▶", color = Color(0xFF00B0FF), fontSize = 9.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Tune, null, tint = Color(0xFF90A4AE), modifier = Modifier.size(16.dp))
            }

            // End of trade label
            Column(horizontalAlignment = Alignment.End) {
                Text("End of trade", color = Color(0xFF78909C), fontSize = 9.sp)
                Text("01:14", color = Color(0xFFCFD8DC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuotexTradingControls(
    currentPair: CurrencyPair,
    availablePairs: List<CurrencyPair>,
    onSelectPair: (CurrencyPair) -> Unit,
    tradeAmount: Double,
    onAmountChange: (Double) -> Unit,
    tradeTime: String,
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    onOpenPhotoAnalysis: () -> Unit,
    onOpenRiskTool: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPairMenuOpen by remember { mutableStateOf(false) }
    var isPendingTrade by remember { mutableStateOf(false) }

    val payoutAmount = tradeAmount * (1 + (currentPair.payout / 100.0))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F1420))
            .border(width = 1.dp, color = Color(0xFF1B2338))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Currency Pair selector and PENDING TRADE Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B2438))
                        .clickable { isPairMenuOpen = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("currency_pair_selector")
                ) {
                    Text(currentPair.flagEmoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentPair.symbol,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${currentPair.payout}%",
                        color = Color(0xFFFFB300),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = isPairMenuOpen,
                    onDismissRequest = { isPairMenuOpen = false },
                    modifier = Modifier.background(Color(0xFF1A2338))
                ) {
                    availablePairs.forEach { pair ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(pair.flagEmoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(pair.symbol, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${pair.payout}%", color = Color(0xFFFFB300), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                onSelectPair(pair)
                                isPairMenuOpen = false
                            }
                        )
                    }
                }
            }

            // PENDING TRADE Switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PENDING TRADE",
                    color = Color(0xFF00B0FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = isPendingTrade,
                    onCheckedChange = { isPendingTrade = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF00B0FF),
                        uncheckedTrackColor = Color(0xFF263238)
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time and Investment row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Time box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF141C2C))
                    .border(1.dp, Color(0xFF24324D), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column {
                    Text("Time", color = Color(0xFF78909C), fontSize = 9.sp)
                    Text(tradeTime, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Investment box with - and +
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF141C2C))
                    .border(1.dp, Color(0xFF24324D), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (tradeAmount > 1.0) onAmountChange(tradeAmount - 1.0) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Remove, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Investment", color = Color(0xFF78909C), fontSize = 9.sp)
                        Text("${tradeAmount.toInt()} $", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = { onAmountChange(tradeAmount + 1.0) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Payout text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Payout", color = Color(0xFF78909C), fontSize = 10.sp)
            Text("${"%.2f".format(payoutAmount)} $", color = Color(0xFFECEFF1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // UP (Call) and DOWN (Put) big buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // UP Button
            Button(
                onClick = onUpClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("quotex_up_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Up", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↑", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // DOWN Button
            Button(
                onClick = onDownClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF3D00),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("quotex_down_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Down", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom icons bar: [Gallery/Screenshot], [? Help], [Profile], [Cup 4], [Chat 4]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenPhotoAnalysis, modifier = Modifier.size(36.dp).testTag("bottom_gallery_scanner")) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Scan Screenshot", tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help", tint = Color(0xFF78909C), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onOpenRiskTool, modifier = Modifier.size(36.dp).testTag("bottom_risk_tool")) {
                Icon(Icons.Default.Security, contentDescription = "Risk Management", tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
            }
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard", tint = Color(0xFF78909C), modifier = Modifier.size(20.dp))
                }
                Box(modifier = Modifier.clip(CircleShape).background(Color(0xFF00B0FF)).padding(3.dp)) {
                    Text("4", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = Color(0xFF78909C), modifier = Modifier.size(20.dp))
                }
                Box(modifier = Modifier.clip(CircleShape).background(Color(0xFF00B0FF)).padding(3.dp)) {
                    Text("4", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
