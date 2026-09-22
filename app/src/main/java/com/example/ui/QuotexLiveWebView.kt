package com.example.ui

import android.annotation.SuppressLint
import java.io.File
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Controller for Quotex live WebView to allow external capture, refresh and navigation.
 */
class QuotexWebViewController {
    private var webView: WebView? = null
    var lastDetectedTrend: String = "UP"

    fun attach(view: WebView) {
        webView = view
    }

    fun detach() {
        webView = null
    }

    fun onResume() {
        webView?.onResume()
    }

    fun onPause() {
        webView?.onPause()
    }

    fun reload() {
        webView?.reload()
    }

    fun goBack(): Boolean {
        return if (webView?.canGoBack() == true) {
            webView?.goBack()
            true
        } else {
            false
        }
    }

    fun goForward(): Boolean {
        return if (webView?.canGoForward() == true) {
            webView?.goForward()
            true
        } else {
            false
        }
    }

    fun loadUrl(url: String) {
        webView?.loadUrl(url)
    }

    fun evaluateTrendFromDom(onResult: (String) -> Unit) {
        val view = webView ?: run {
            onResult(lastDetectedTrend)
            return
        }
        try {
            view.evaluateJavascript("(function() { return (window.__quotex_state && window.__quotex_state.lastTrend) ? window.__quotex_state.lastTrend : 'AUTO'; })();") { res ->
                val clean = res?.replace("\"", "")?.trim() ?: "AUTO"
                if (clean == "UP" || clean == "DOWN") {
                    lastDetectedTrend = clean
                }
                onResult(lastDetectedTrend)
            }
        } catch (_: Exception) {
            onResult(lastDetectedTrend)
        }
    }

    /**
     * Capture the current rendered frame of the Quotex chart with optimized scaling to prevent lag
     */
    fun captureCurrentScreen(): Bitmap? {
        val view = webView ?: return null
        val width = view.width
        val height = view.height
        if (width <= 0 || height <= 0) return null

        // Try PixelCopy from Window first (captures hardware-accelerated WebGL / Canvas / Chromium layers)
        try {
            val activity = view.context as? Activity
            val window = activity?.window
            if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val location = IntArray(2)
                view.getLocationInWindow(location)
                val srcRect = android.graphics.Rect(
                    location[0],
                    location[1],
                    location[0] + width,
                    location[1] + height
                )
                val pixelBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val latch = java.util.concurrent.CountDownLatch(1)
                var success = false

                PixelCopy.request(
                    window,
                    srcRect,
                    pixelBitmap,
                    { copyResult ->
                        success = (copyResult == PixelCopy.SUCCESS)
                        latch.countDown()
                    },
                    Handler(Looper.getMainLooper())
                )

                // Wait up to 350ms for the frame to be copied from the surface
                latch.await(350, java.util.concurrent.TimeUnit.MILLISECONDS)
                if (success) {
                    val scaled = Bitmap.createScaledBitmap(pixelBitmap, maxOf(1, width / 2), maxOf(1, height / 2), true)
                    if (scaled != pixelBitmap) {
                        pixelBitmap.recycle()
                    }
                    return scaled
                }
            }
        } catch (_: Exception) {}

        // Fallback: draw view hierarchy directly
        return try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            val scaled = Bitmap.createScaledBitmap(bitmap, maxOf(1, width / 2), maxOf(1, height / 2), true)
            if (scaled != bitmap) {
                bitmap.recycle()
            }
            scaled
        } catch (_: Exception) {
            null
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun QuotexLiveWebView(
    controller: QuotexWebViewController,
    isScanning: Boolean,
    scanProgressText: String,
    modifier: Modifier = Modifier,
    initialUrl: String = "https://market-qx.info/en/sign-in/"
) {
    LocalContext.current
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var currentUrl by remember { mutableStateOf(initialUrl) }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0A0E17))) {
        // Embedded Android WebView
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("quotex_live_webview"),
            factory = { ctx ->
                // Ensure HTTP Cache Code Cache directories exist to suppress Chromium simple_file_enumerator error
                try {
                    val codeCacheDir = File(ctx.cacheDir, "WebView/Default/HTTP Cache/Code Cache")
                    File(codeCacheDir, "js").mkdirs()
                    File(codeCacheDir, "wasm").mkdirs()
                } catch (_: Exception) {}

                try {
                    WebView.enableSlowWholeDocumentDraw()
                } catch (_: Exception) {}

                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Set explicit background color to match dark trading theme
                    // This ensures root layer is painted immediately during view attachment
                    setBackgroundColor(android.graphics.Color.parseColor("#0A0E17"))

                    // Do NOT force LAYER_TYPE_HARDWARE or SOFTWARE on WebView.
                    // Chromium handles internal GPU rasterization cleanly with LAYER_TYPE_NONE.
                    setLayerType(View.LAYER_TYPE_NONE, null)

                    // Enable Cookies for persistent login session
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    // WebSettings
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        setSupportZoom(true)
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            // Stay inside the WebView
                            return false
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            url?.let { currentUrl = it }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            url?.let { currentUrl = it }

                            // Continuously observe Quotex DOM to detect price action and candle colors
                            val script = """
                                (function() {
                                    try {
                                        window.__quotex_state = window.__quotex_state || { lastTrend: 'UP' };
                                        setInterval(function() {
                                            // Check price or payout or candle indicators
                                            var greenCandles = document.querySelectorAll('.chart-candle--up, [class*="green"], [class*="bullish"]').length;
                                            var redCandles = document.querySelectorAll('.chart-candle--down, [class*="red"], [class*="bearish"]').length;
                                            if (greenCandles > redCandles) {
                                                window.__quotex_state.lastTrend = 'UP';
                                            } else if (redCandles > greenCandles) {
                                                window.__quotex_state.lastTrend = 'DOWN';
                                            }
                                        }, 1000);
                                    } catch(e) {}
                                })();
                            """.trimIndent()
                            view?.evaluateJavascript(script, null)
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                isLoading = false
                            }
                        }

                        @SuppressLint("WebViewClientOnReceivedSslError")
                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            handler?.proceed()
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            loadProgress = newProgress / 100f
                            if (newProgress >= 100) {
                                isLoading = false
                            }
                        }
                    }

                    controller.attach(this)
                    onResume()
                    loadUrl(initialUrl)
                }
            },
            update = {
                // Ensure attached
                controller.attach(it)
            }
        )

        DisposableEffect(controller) {
            controller.onResume()
            onDispose {
                controller.onPause()
                controller.detach()
            }
        }

        // Top thin web loading progress bar
        if (isLoading && loadProgress < 1f) {
            LinearProgressIndicator(
                progress = { loadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter),
                color = Color(0xFF00E5FF),
                trackColor = Color(0xFF1E293B)
            )
        }

        // Live Scanning HUD & Laser Line Overlay (Sci-Fi Cyber AI Scan effect)
        AnimatedVisibility(
            visible = isScanning,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            LiveLaserScanOverlay(scanProgressText = scanProgressText)
        }
    }
}

/**
 * Animated Laser Scan Overlay displayed directly over Quotex live candles
 */
@Composable
private fun LiveLaserScanOverlay(scanProgressText: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00E5FF).copy(alpha = 0.08f))
    ) {
        val density = LocalDensity.current
        val totalHeightPx = with(density) { maxHeight.toPx() }

        // Scanning HUD Banner at Top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xEE09101D))
                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color(0xFF00E5FF),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = scanProgressText.ifEmpty { "AI লেজার স্ক্যান হচ্ছে (Scanning Quotex Market...)" },
                    color = Color(0xFF80D8FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        // Moving Laser Beam Line across entire market from top to bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .offset {
                    IntOffset(0, (laserProgress * totalHeightPx).toInt())
                }
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF00E5FF).copy(alpha = 0.6f),
                            Color(0xFF00E5FF),
                            Color.White,
                            Color(0xFF00E5FF),
                            Color(0xFF00E5FF).copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Laser glow effect trail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .offset {
                    IntOffset(0, ((laserProgress * totalHeightPx) - with(density) { 10.dp.toPx() }).toInt())
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF00E5FF).copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Corner Targeting Reticles
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(24.dp)
                .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.6f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(24.dp)
                .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.6f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .size(24.dp)
                .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.6f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(24.dp)
                .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.6f))
        )
    }
}
