package com.example.model

enum class SignalDirection {
    UP,
    DOWN,
    NO_TRADE
}

data class Candle(
    val id: Long,
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 1.0
) {
    val isBullish: Boolean get() = close >= open
}

data class TradingSignal(
    val id: String,
    val direction: SignalDirection,
    val confidence: Int,
    val pair: String,
    val reason: String,
    val logicExplanation: String,
    val supportLevel: Double,
    val resistanceLevel: Double,
    val rsi: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 60,
    val isSafeTrade: Boolean = true
)

data class CurrencyPair(
    val symbol: String,
    val name: String,
    val payout: Int,
    val basePrice: Double,
    val pipStep: Double,
    val flagEmoji: String
)

data class RiskSettings(
    val balance: Double = 1000.0,
    val stopLossLimit: Double = 50.0,
    val takeProfitLimit: Double = 100.0,
    val tradeAmount: Double = 2.0,
    val martingaleMultiplier: Double = 2.2,
    val currentSessionPnL: Double = 0.0,
    val totalTrades: Int = 0,
    val winTrades: Int = 0,
    val isStopLossHit: Boolean = false,
    val isTakeProfitHit: Boolean = false
) {
    val winRate: Int get() = if (totalTrades > 0) ((winTrades.toDouble() / totalTrades) * 100).toInt() else 0
}
