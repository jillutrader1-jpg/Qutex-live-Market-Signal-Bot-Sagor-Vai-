package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SignalDirection
import com.example.model.TradingSignal

/**
 * Compact, minimizable and closable Signal Result Card.
 * Designed to not obstruct the Quotex candlestick chart while providing full signal details.
 */
@Composable
fun SignalResultOverlay(
    signal: TradingSignal?,
    countdownSeconds: Int,
    onDismiss: () -> Unit,
    onSpeakClick: () -> Unit,
    onExecuteTrade: (SignalDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = signal != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        if (signal == null) return@AnimatedVisibility

        val isUp = signal.direction == SignalDirection.UP
        val isDown = signal.direction == SignalDirection.DOWN
        val isNoTrade = signal.direction == SignalDirection.NO_TRADE

        val mainColor = when {
            isUp -> Color(0xFF00E676)
            isDown -> Color(0xFFFF3D00)
            else -> Color(0xFFFFB300)
        }

        val cardBgGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xF5131A29),
                Color(0xF50B0F1A)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .shadow(12.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(cardBgGradient)
                .border(1.5.dp, mainColor.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .animateContentSize()
                .testTag("signal_result_card")
        ) {
            Column {
                // Header row: Compact Signal Badge, Timer, TTS, Minimize/Expand toggle, and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Signal Badge + Direction text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(mainColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isUp -> Icons.AutoMirrored.Filled.TrendingUp
                                isDown -> Icons.AutoMirrored.Filled.TrendingDown
                                else -> Icons.Default.Security
                            },
                            contentDescription = "Signal Icon",
                            tint = mainColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                isUp -> "CALL (UP) ↑"
                                isDown -> "PUT (DOWN) ↓"
                                else -> "PROTECT (HOLD)"
                            },
                            color = mainColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${signal.confidence}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Right: Expiration Countdown + Quick Controls (Speak, Minimize, Close)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Expiration Timer Box
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "⏱ 00:${countdownSeconds.toString().padStart(2, '0')}",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Voice Button
                        IconButton(
                            onClick = onSpeakClick,
                            modifier = Modifier.size(28.dp).testTag("tts_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen to Signal Voice",
                                tint = Color(0xFF80D8FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Minimize / Expand Toggle Button
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Minimize Dashboard" else "Expand Dashboard",
                                tint = Color(0xFF80D8FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Close Button (cuts/dismisses the dashboard)
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp).testTag("close_signal_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Dashboard",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Quick One-liner reason always visible in compact view
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = signal.reason,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isNoTrade && !isExpanded) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(mainColor)
                                .clickable { onExecuteTrade(signal.direction) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isUp) "TRADE UP ↑" else "TRADE DOWN ↓",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Expanded Section: Detailed logic, confidence meter, full trade action button
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Confidence Level Meter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI কনফিডেন্স / সিগন্যাল অ্যাকুরেসি:",
                            color = Color(0xFFB0BEC5),
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${signal.confidence}%",
                            color = mainColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { signal.confidence / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = mainColor,
                        trackColor = Color(0xFF242E44)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Full logic explanation
                    Text(
                        text = signal.logicExplanation,
                        color = Color(0xFFCFD8DC),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )

                    if (!isNoTrade) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onExecuteTrade(signal.direction) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = mainColor,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                                .testTag("execute_signal_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isUp) "ENTER UP (CALL) TRADE NOW" else "ENTER DOWN (PUT) TRADE NOW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
