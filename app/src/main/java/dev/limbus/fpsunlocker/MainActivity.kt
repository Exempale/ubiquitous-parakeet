package dev.limbus.fpsunlocker

import android.app.Activity
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), DisplayManager.DisplayListener {
    private lateinit var displayManager: DisplayManager
    private lateinit var currentRate: TextView
    private lateinit var modes: TextView
    private lateinit var gameStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager

        val pad = (20 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val title = TextView(this).apply {
            text = "LIMBUS FPS UNLOCKER"
            textSize = 23f
            gravity = Gravity.CENTER
        }
        currentRate = TextView(this).apply {
            textSize = 34f
            gravity = Gravity.CENTER
            setPadding(0, pad, 0, pad / 2)
        }
        modes = TextView(this).apply { textSize = 16f }
        gameStatus = TextView(this).apply {
            textSize = 16f
            setPadding(0, pad, 0, pad)
        }

        root.addView(title, matchWrap())
        root.addView(currentRate, matchWrap())
        root.addView(modes, matchWrap())
        root.addView(gameStatus, matchWrap())
        root.addView(makeButton("Открыть настройки экрана") {
            runCatching { startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)) }
        }, matchWrap())
        root.addView(makeButton("Запустить Limbus Company") {
            val launch = packageManager.getLaunchIntentForPackage(GAME_PACKAGE)
            if (launch != null) startActivity(launch)
            else gameStatus.text = "Игра не найдена. Проверьте установку Google Play."
        }, matchWrap())

        val note = TextView(this).apply {
            text = "\nВажно: приложение показывает частоту обновления дисплея, а не FPS игры. Обычный APK без специальных разрешений не может читать или менять внутренний FPS другого приложения."
            textSize = 13f
        }
        root.addView(note, matchWrap())
        setContentView(root)
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
        val display = windowManager.defaultDisplay
        currentRate.text = String.format(Locale.US, "%.1f Hz", display.refreshRate)
        val supported = display.supportedModes
            .map { it.refreshRate }
            .distinct()
            .sorted()
            .joinToString(", ") { String.format(Locale.US, "%.0f Hz", it) }
        modes.text = "Режимы дисплея: ${supported.ifBlank { "не удалось определить" }}"
        gameStatus.text = if (packageManager.getLaunchIntentForPackage(GAME_PACKAGE) != null) {
            "Limbus Company: обнаружена"
        } else {
            "Limbus Company: не обнаружена (проверьте установку Google Play)"
        }
    }

    private fun makeButton(label: String, action: () -> Unit): Button =
        Button(this).apply { text = label; setOnClickListener { action() } }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = (10 * resources.displayMetrics.density).toInt() }

    override fun onDisplayAdded(displayId: Int) = refreshInfo()
    override fun onDisplayRemoved(displayId: Int) = refreshInfo()
    override fun onDisplayChanged(displayId: Int) = refreshInfo()

    companion object {
        private const val GAME_PACKAGE = "com.ProjectMoon.LimbusCompany"
    }
}
