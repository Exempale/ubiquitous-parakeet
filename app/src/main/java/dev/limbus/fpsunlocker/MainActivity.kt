package dev.limbus.fpsunlocker

import android.app.Activity
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), DisplayManager.DisplayListener {
    private lateinit var displayManager: DisplayManager
    private lateinit var currentRate: TextView
    private lateinit var supportedModes: TextView
    private lateinit var gameStatus: TextView

    private val backgroundColor = Color.rgb(8, 11, 18)
    private val cardColor = Color.rgb(20, 27, 39)
    private val cyan = Color.rgb(0, 217, 245)
    private val muted = Color.rgb(159, 177, 195)
    private val white = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager
        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        val density = resources.displayMetrics.density
        val pagePadding = (22 * density).toInt()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pagePadding, pagePadding, pagePadding, pagePadding)
            setBackgroundColor(backgroundColor)
        }

        content.addView(label("LIMBUS  /  PERFORMANCE TOOL", 12f, cyan, true))
        content.addView(label("FPS UNLOCKER", 27f, white, true).apply {
            setPadding(0, (5 * density).toInt(), 0, (4 * density).toInt())
        })
        content.addView(label("Nothing Phone (2a) Plus • Android", 13f, muted, false))
        content.addView(space(22))

        val rateCard = card()
        rateCard.addView(label("ТЕКУЩАЯ ЧАСТОТА ДИСПЛЕЯ", 11f, muted, true))
        currentRate = label("— Hz", 48f, cyan, true).apply {
            setPadding(0, (4 * density).toInt(), 0, (2 * density).toInt())
        }
        rateCard.addView(currentRate)
        supportedModes = label("Доступные режимы: проверка…", 13f, muted, false)
        rateCard.addView(supportedModes)
        content.addView(rateCard)

        content.addView(space(14))
        val gameCard = card()
        gameCard.addView(label("LIMBUS COMPANY", 16f, white, true))
        gameStatus = label("Проверка установки…", 13f, muted, false)
        gameCard.addView(gameStatus)
        gameCard.addView(space(10))
        gameCard.addView(actionButton("ЗАПУСТИТЬ ИГРУ", cyan) {
            val launchIntent = packageManager.getLaunchIntentForPackage(GAME_PACKAGE)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                gameStatus.text = "Игра не найдена. Проверь установку Google Play."
            }
        })
        content.addView(gameCard)

        content.addView(space(14))
        content.addView(actionButton("НАСТРОЙКИ ЭКРАНА", Color.rgb(38, 51, 68)) {
            openSettings(Settings.ACTION_DISPLAY_SETTINGS)
        })
        content.addView(space(8))
        content.addView(actionButton("НАСТРОЙКИ РАЗРАБОТЧИКА", Color.rgb(38, 51, 68)) {
            openSettings(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        })

        content.addView(space(14))
        val infoCard = card()
        infoCard.addView(label("ВАЖНО О 120 FPS", 14f, white, true))
        infoCard.addView(space(6))
        infoCard.addView(label(
            "Это приложение показывает частоту обновления дисплея (Hz), а не реальный FPS Limbus Company. Обычный APK без специальных системных прав не может изменить лимит кадров внутри другой игры или системное ограничение GameManager.",
            13f, muted, false
        ))
        infoCard.addView(space(8))
        infoCard.addView(label(
            "На Nothing OS 4.0 можно проверить режим высокой частоты экрана и параметры разработчика, но они не гарантируют снятие лимита игры. 120 Hz на экране не означает 120 FPS в игре.",
            13f, muted, false
        ))
        content.addView(infoCard)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
        }
        setContentView(scroll)
        refreshInfo()
    }

    override fun onResume() {
        super.onResume()
        displayManager.registerDisplayListener(this, null)
        refreshInfo()
    }

    override fun onPause() {
        displayManager.unregisterDisplayListener(this)
        super.onPause()
    }

    private fun refreshInfo() {
        val display = displayManager.getDisplay(android.view.Display.DEFAULT_DISPLAY)
        if (display == null) {
            currentRate.text = "Недоступно"
            supportedModes.text = "Не удалось прочитать режим дисплея"
        } else {
            currentRate.text = String.format(Locale.US, "%.0f Hz", display.refreshRate)
            val modes = display.supportedModes
                .map { it.refreshRate }
                .distinct()
                .sorted()
                .joinToString(" • ") { String.format(Locale.US, "%.0f Hz", it) }
            supportedModes.text = "Доступные режимы: ${modes.ifBlank { "нет данных" }}"
        }

        gameStatus.text = if (packageManager.getLaunchIntentForPackage(GAME_PACKAGE) != null) {
            "Игра найдена • Google Play"
        } else {
            "Игра не найдена — проверь установку"
        }
    }

    private fun openSettings(action: String) {
        try {
            startActivity(Intent(action))
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        val p = (16 * resources.displayMetrics.density).toInt()
        setPadding(p, p, p, p)
        background = roundedBackground(cardColor, 18)
    }

    private fun label(
        textValue: String,
        size: Float,
        color: Int,
        bold: Boolean
    ) = TextView(this).apply {
        text = textValue
        textSize = size
        setTextColor(color)
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        includeFontPadding = true
    }

    private fun actionButton(
        textValue: String,
        color: Int,
        action: () -> Unit
    ) = Button(this).apply {
        text = textValue
        textSize = 13f
        isAllCaps = false
        setTextColor(white)
        background = roundedBackground(color, 14)
        setOnClickListener { action() }
        val p = (10 * resources.displayMetrics.density).toInt()
        setPadding(p, p / 2, p, p / 2)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun roundedBackground(color: Int, radiusDp: Int) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radiusDp * resources.displayMetrics.density
        }

    private fun space(dp: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (dp * resources.displayMetrics.density).toInt()
        )
    }

    override fun onDisplayAdded(displayId: Int) = refreshInfo()
    override fun onDisplayRemoved(displayId: Int) = refreshInfo()
    override fun onDisplayChanged(displayId: Int) = refreshInfo()

    companion object {
        private const val GAME_PACKAGE = "com.ProjectMoon.LimbusCompany"
    }
}
