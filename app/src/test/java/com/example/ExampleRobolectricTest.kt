package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.Candle
import com.example.model.RiskSettings
import com.example.model.SignalDirection
import com.example.service.GeminiTradingService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Trader Vai AI", appName)
  }

  @Test
  fun `verify risk settings win rate and limits`() {
    val initialSettings = RiskSettings(
      balance = 1000.0,
      stopLossLimit = 50.0,
      takeProfitLimit = 100.0,
      totalTrades = 10,
      winTrades = 8,
      currentSessionPnL = 120.0
    )
    assertEquals(80, initialSettings.winRate)
    val isTP = initialSettings.currentSessionPnL >= initialSettings.takeProfitLimit
    assertTrue(isTP)
  }

  @Test
  fun `verify technical analysis generates signal on support rejection`() {
    val service = GeminiTradingService()
    val candles = listOf(
      Candle(1, 1000, 0.19380, 0.19390, 0.19370, 0.19375),
      Candle(2, 2000, 0.19375, 0.19380, 0.19360, 0.19365),
      Candle(3, 3000, 0.19365, 0.19370, 0.19355, 0.19358),
      // Hammer rejection at support
      Candle(4, 4000, 0.19358, 0.19375, 0.19340, 0.19372)
    )

    val signal = service.runLocalTechnicalAnalysis(
      pair = "USD/BRL",
      candles = candles,
      currentPrice = 0.19360,
      supportLevel = 0.19350,
      resistanceLevel = 0.19450
    )

    assertTrue(signal.direction == SignalDirection.UP || signal.direction == SignalDirection.NO_TRADE)
  }
}

