package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Candle
import com.example.model.CurrencyPair
import com.example.model.RiskSettings
import com.example.model.SignalDirection
import com.example.model.TradingSignal
import com.example.service.GeminiTradingService
import com.example.service.SoundEffectsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.max
import kotlin.math.min

data class ActiveTrade(
    val id: String,
    val pair: String,
    val direction: SignalDirection,
    val entryPrice: Double,
    val amount: Double,
    val payoutPercent: Int,
    val startTimestamp: Long,
    val durationSeconds: Int = 15
)

data class TradingUiState(
    val currentPair: CurrencyPair,
    val availablePairs: List<CurrencyPair>,
    val candles: List<Candle>,
    val currentPrice: Double,
    val supportLevel: Double,
    val resistanceLevel: Double,
    val isScanning: Boolean = false,
    val scanProgressText: String = "",
    val activeSignal: TradingSignal? = null,
    val countdownSeconds: Int = 15,
    val riskSettings: RiskSettings = RiskSettings(),
    val activeTrade: ActiveTrade? = null,
    val tradeResultNotification: String? = null,
    val tradeAmount: Double = 2.0,
    val tradeTime: String = "00:16",
    val isLiveQuotexMode: Boolean = true
)

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    val geminiService = GeminiTradingService()
    val soundHelper = SoundEffectsHelper(application)
    private val random = Random()

    private val defaultPairs = listOf(
        CurrencyPair("USD/BRL", "US Dollar / Brazilian Real", 94, 0.19361, 0.00008, "🇧🇷"),
        CurrencyPair("EUR/USD", "Euro / US Dollar", 89, 1.08450, 0.00010, "🇪🇺"),
        CurrencyPair("GBP/USD", "British Pound / US Dollar", 91, 1.27210, 0.00012, "🇬🇧"),
        CurrencyPair("USD/INR", "US Dollar / Indian Rupee", 93, 83.4520, 0.00500, "🇮🇳"),
        CurrencyPair("BTC/USDT", "Bitcoin / Tether", 88, 64320.0, 15.0, "🪙")
    )

    private val _uiState = MutableStateFlow(
        TradingUiState(
            currentPair = defaultPairs[0],
            availablePairs = defaultPairs,
            candles = generateInitialCandles(defaultPairs[0]),
            currentPrice = defaultPairs[0].basePrice,
            supportLevel = defaultPairs[0].basePrice - 0.00045,
            resistanceLevel = defaultPairs[0].basePrice + 0.00065
        )
    )
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()

    private var tickJob: Job? = null
    private var countdownJob: Job? = null

    init {
        startLiveTickGenerator()
    }

    private fun startLiveTickGenerator() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                updateLivePriceTick()
            }
        }
    }

    private fun generateInitialCandles(pair: CurrencyPair): List<Candle> {
        val list = mutableListOf<Candle>()
        var price = pair.basePrice - (pair.pipStep * 10)
        val now = System.currentTimeMillis() - (20 * 60 * 1000)

        for (i in 0 until 22) {
            val delta = (random.nextDouble() - 0.48) * (pair.pipStep * 3)
            val open = price
            val close = open + delta
            val high = max(open, close) + (random.nextDouble() * pair.pipStep * 1.5)
            val low = min(open, close) - (random.nextDouble() * pair.pipStep * 1.5)
            list.add(
                Candle(
                    id = i.toLong(),
                    timestamp = now + (i * 60 * 1000),
                    open = open,
                    high = high,
                    low = low,
                    close = close
                )
            )
            price = close
        }
        return list
    }

    private fun updateLivePriceTick() {
        _uiState.update { state ->
            val pair = state.currentPair
            val delta = (random.nextDouble() - 0.49) * pair.pipStep
            val newPrice = max(0.00001, state.currentPrice + delta)

            val candles = state.candles.toMutableList()
            if (candles.isNotEmpty()) {
                val last = candles.last()
                val updatedLast = last.copy(
                    high = max(last.high, newPrice),
                    low = min(last.low, newPrice),
                    close = newPrice
                )
                candles[candles.size - 1] = updatedLast
            }

            // Check active trade outcome if countdown reached 0
            var activeTrade = state.activeTrade
            var risk = state.riskSettings
            var notification = state.tradeResultNotification

            if (activeTrade != null) {
                val elapsed = (System.currentTimeMillis() - activeTrade.startTimestamp) / 1000
                if (elapsed >= activeTrade.durationSeconds) {
                    val isWin = when (activeTrade.direction) {
                        SignalDirection.UP -> newPrice > activeTrade.entryPrice
                        SignalDirection.DOWN -> newPrice < activeTrade.entryPrice
                        SignalDirection.NO_TRADE -> false
                    }

                    val profitLoss = if (isWin) {
                        activeTrade.amount * (activeTrade.payoutPercent / 100.0)
                    } else {
                        -activeTrade.amount
                    }

                    soundHelper.playSignalChime(isWin)

                    val newBalance = risk.balance + profitLoss
                    val newSessionPnL = risk.currentSessionPnL + profitLoss
                    val isTP = newSessionPnL >= risk.takeProfitLimit
                    val isSL = newSessionPnL <= -risk.stopLossLimit

                    risk = risk.copy(
                        balance = newBalance,
                        currentSessionPnL = newSessionPnL,
                        totalTrades = risk.totalTrades + 1,
                        winTrades = risk.winTrades + if (isWin) 1 else 0,
                        isTakeProfitHit = isTP,
                        isStopLossHit = isSL
                    )

                    notification = if (isWin) {
                        "🎉 ট্রেড উইন! লাভ: +$${"%.2f".format(profitLoss)}"
                    } else {
                        "⚠️ ট্রেড লস: -$${"%.2f".format(activeTrade.amount)}"
                    }

                    activeTrade = null
                }
            }

            state.copy(
                currentPrice = newPrice,
                candles = candles,
                activeTrade = activeTrade,
                riskSettings = risk,
                tradeResultNotification = notification
            )
        }
    }

    fun selectPair(pair: CurrencyPair) {
        _uiState.update { state ->
            val candles = generateInitialCandles(pair)
            state.copy(
                currentPair = pair,
                candles = candles,
                currentPrice = pair.basePrice,
                supportLevel = pair.basePrice - (pair.pipStep * 6),
                resistanceLevel = pair.basePrice + (pair.pipStep * 8),
                activeSignal = null
            )
        }
    }

    fun setTradeAmount(amount: Double) {
        _uiState.update { it.copy(tradeAmount = amount) }
    }

    /**
     * Start the Laser Scan & Gemini Analysis
     */
    fun startLaserScan() {
        if (_uiState.value.isScanning) return

        soundHelper.playScannerSound()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScanning = true,
                    scanProgressText = "মার্কেট লেজার স্ক্যান হচ্ছে (Scanning Quotex...)"
                )
            }

            delay(700)
            _uiState.update {
                it.copy(scanProgressText = "ক্যান্ডেলস্টিক লজিক যাচাই করা হচ্ছে (Checking Patterns...)")
            }

            delay(700)
            _uiState.update {
                it.copy(scanProgressText = "Gemini AI Candlestick Market Reasoning...")
            }

            val currentState = _uiState.value
            val signal = geminiService.analyzeCandlestickMarket(
                pair = currentState.currentPair.symbol,
                candles = currentState.candles,
                currentPrice = currentState.currentPrice,
                supportLevel = currentState.supportLevel,
                resistanceLevel = currentState.resistanceLevel
            )

            delay(600)

            _uiState.update {
                it.copy(
                    isScanning = false,
                    activeSignal = signal,
                    countdownSeconds = 15
                )
            }

            // Play voice or sound alert
            val isUp = signal.direction == SignalDirection.UP
            val isDown = signal.direction == SignalDirection.DOWN

            if (signal.isSafeTrade && (isUp || isDown)) {
                soundHelper.playSignalChime(isUp)

                // Trigger Gemini TTS or native fallback speech
                speakSignal(signal)

                // Start candle expiration countdown
                startCountdown()
            } else {
                soundHelper.speakText("No safe trade setup detected. Capital protection mode active.")
            }
        }
    }

    fun speakSignal(signal: TradingSignal) {
        viewModelScope.launch {
            val isUp = signal.direction == SignalDirection.UP
            val directionText = if (isUp) "CALL UP" else "PUT DOWN"
            val textToSpeak = "Signal detected: $directionText! Accuracy ${signal.confidence} percent. ${signal.reason}"

            val base64Audio = geminiService.generateSpeech(textToSpeak)
            if (base64Audio != null) {
                soundHelper.playAudioBase64(base64Audio, "audio/mp3", textToSpeak)
            } else {
                soundHelper.speakText(textToSpeak)
            }
        }
    }

    fun toggleTradingMode() {
        _uiState.update { it.copy(isLiveQuotexMode = !it.isLiveQuotexMode) }
    }

    fun setTradingMode(isLive: Boolean) {
        _uiState.update { it.copy(isLiveQuotexMode = isLive) }
    }

    /**
     * Scan live Quotex screen via captured WebView bitmap with low latency
     */
    fun scanLiveQuotex(bitmap: Bitmap?) {
        if (_uiState.value.isScanning) return

        soundHelper.playScannerSound()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScanning = true,
                    scanProgressText = "Quotex লাইভ মার্কেট স্ক্যান হচ্ছে..."
                )
            }

            // Launch AI analysis concurrently in background without blocking
            val analysisDeferred = async(Dispatchers.IO) {
                if (bitmap != null) {
                    geminiService.analyzeChartImage(bitmap, "Quotex Live Platform")
                } else {
                    val currentState = _uiState.value
                    geminiService.analyzeCandlestickMarket(
                        pair = currentState.currentPair.symbol,
                        candles = currentState.candles,
                        currentPrice = currentState.currentPrice,
                        supportLevel = currentState.supportLevel,
                        resistanceLevel = currentState.resistanceLevel
                    )
                }
            }

            // Quick dynamic progress updates while analysis runs
            launch {
                delay(200)
                if (_uiState.value.isScanning) {
                    _uiState.update { it.copy(scanProgressText = "ক্যান্ডেলস্টিক ও ভলিউম বিশ্লেষণ হচ্ছে...") }
                }
                delay(250)
                if (_uiState.value.isScanning) {
                    _uiState.update { it.copy(scanProgressText = "AI ট্রেডিং সিগন্যাল তৈরি করছে...") }
                }
            }

            val signal = analysisDeferred.await()

            _uiState.update {
                it.copy(
                    isScanning = false,
                    activeSignal = signal,
                    countdownSeconds = signal.durationSeconds.takeIf { s -> s in 10..60 } ?: 60
                )
            }

            val isUp = signal.direction == SignalDirection.UP
            val isDown = signal.direction == SignalDirection.DOWN

            if (signal.isSafeTrade && (isUp || isDown)) {
                soundHelper.playSignalChime(isUp)
                speakSignal(signal)
                startCountdown(signal.durationSeconds.takeIf { s -> s in 10..60 } ?: 60)
            } else {
                soundHelper.speakText("Caution: High volatility. Capital protection mode recommended.")
            }
        }
    }

    private fun startCountdown(totalSec: Int = 15) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (sec in totalSec downTo 0) {
                _uiState.update { it.copy(countdownSeconds = sec) }
                delay(1000)
            }
        }
    }

    fun executeTrade(direction: SignalDirection) {
        val state = _uiState.value
        if (state.riskSettings.isStopLossHit) {
            soundHelper.speakText("Warning: Stop loss target is hit. Trading is disabled.")
            return
        }

        soundHelper.vibrate(100)
        _uiState.update {
            it.copy(
                activeTrade = ActiveTrade(
                    id = System.currentTimeMillis().toString(),
                    pair = state.currentPair.symbol,
                    direction = direction,
                    entryPrice = state.currentPrice,
                    amount = state.tradeAmount,
                    payoutPercent = state.currentPair.payout,
                    startTimestamp = System.currentTimeMillis(),
                    durationSeconds = 15
                ),
                tradeResultNotification = "ট্রেড ওপেন হয়েছে: ${if (direction == SignalDirection.UP) "UP ↑" else "DOWN ↓"} ($${state.tradeAmount.toInt()})"
            )
        }
    }

    fun updateRiskSettings(newSettings: RiskSettings) {
        _uiState.update { it.copy(riskSettings = newSettings) }
    }

    fun resetSessionPnL() {
        _uiState.update {
            it.copy(
                riskSettings = it.riskSettings.copy(
                    currentSessionPnL = 0.0,
                    totalTrades = 0,
                    winTrades = 0,
                    isStopLossHit = false,
                    isTakeProfitHit = false
                ),
                tradeResultNotification = null
            )
        }
    }

    fun depositFunds(amount: Double = 500.0) {
        _uiState.update {
            it.copy(
                riskSettings = it.riskSettings.copy(
                    balance = it.riskSettings.balance + amount
                ),
                tradeResultNotification = "+$${amount.toInt()} ডিপোজিট সফল হয়েছে!"
            )
        }
    }

    fun dismissSignal() {
        _uiState.update { it.copy(activeSignal = null) }
    }

    fun clearNotification() {
        _uiState.update { it.copy(tradeResultNotification = null) }
    }

    override fun onCleared() {
        super.onCleared()
        soundHelper.release()
        tickJob?.cancel()
        countdownJob?.cancel()
    }
}
