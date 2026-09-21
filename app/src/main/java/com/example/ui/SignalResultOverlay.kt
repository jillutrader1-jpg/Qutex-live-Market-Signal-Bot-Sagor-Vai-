package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun SignalResultOverlay(
    signal: TradingSignal?,
    countdownSeconds: Int,
    onDismiss: () -> Unit,
    onSpeakClick: () -> Unit,
    onExecuteTrade: (SignalDirection) -> Unit,
    modifier: Modifier = Modifier
) {
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
                Color(0xF0151C2C),
                Color(0xF00D1322)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(cardBgGradient)
                .border(2.dp, mainColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                .padding(14.dp)
                .testTag("signal_result_card")
        ) {
            Column {
                // Header row: Status badge, countdown timer, audio button, close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(mainColor.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when {
                                        isUp -> Icons.AutoMirrored.Filled.TrendingUp
                                        isDown -> Icons.AutoMirrored.Filled.TrendingDown
                                        else -> Icons.Default.Security
                                    },
                                    contentDescription = "Signal Icon",
                                    tint = mainColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        isUp -> "SIGNAL: CALL (UP)"
                                        isDown -> "SIGNAL: PUT (DOWN)"
                                        else -> "CAPITAL PROTECTION (NO TRADE)"
                                    },
                                    color = mainColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Expiration Countdown Box
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⏱ 00:${countdownSeconds.toString().padStart(2, '0')}",
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = onSpeakClick,
                            modifier = Modifier.size(32.dp).testTag("tts_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen to Signal Voice",
                                tint = Color(0xFF80D8FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Confidence Level Meter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Accuracy / Confidence:",
                        color = Color(0xFFB0BEC5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${signal.confidence}%",
                        color = mainColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { signal.confidence / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = mainColor,
                    trackColor = Color(0xFF242E44)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Reason and Logic Explanation
                Text(
                    text = signal.reason,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = signal.logicExplanation,
                    color = Color(0xFFCFD8DC),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Quick Execution / Action row
                if (!isNoTrade) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onExecuteTrade(signal.direction) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = mainColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("execute_signal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUp) "ENTER UP (CALL) TRADE NOW" else "ENTER DOWN (PUT) TRADE NOW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
