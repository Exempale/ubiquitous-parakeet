package dev.limbus.fpsunlocker

import android.app.Activity
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import rikka.shizuku.Shizuku
import java.util.Locale
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

class MainActivity : Activity(), DisplayManager.DisplayListener {
    private lateinit var displayManager: DisplayManager
    private lateinit var currentRate: TextView
    private lateinit var supportedModes: TextView
    private lateinit var gameStatus: TextView
    private lateinit var shizukuStatus: TextView
    private lateinit var resultStatus: TextView
    private val diagnosticLog = StringBuilder()
    @Volatile private var lastCommandSummary: String = "Команды ещё не выполнялись."

    private val backgroundColor = Color.rgb(8, 11, 18)
    private val cardColor = Color.rgb(20, 27, 39)
    private val cyan = Color.rgb(0, 217, 245)
    private val muted = Color.rgb(159, 177, 195)
    private val white = Color.WHITE

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_PERMISSION_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                updateShizukuStatus("Shizuku подключён • разрешение выдано")
            } else {
                updateShizukuStatus("Разрешение Shizuku не выдано")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager
        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor
        Shizuku.addRequestPermissionResultListener(permissionListener)
        logDiagnostic("APP START: sdk=${android.os.Build.VERSION.SDK_INT}, app=${packageName}, shizukuApi=${runCatching { Shizuku.getVersion() }.getOrDefault(-1)}")

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
        currentRate = label("— Hz", 48f, cyan, true)
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
            if (launchIntent != null) startActivity(launchIntent)
            else gameStatus.text = "Игра не найдена. Проверь установку Google Play."
        })
        content.addView(gameCard)

        content.addView(space(14))
        val shizukuCard = card()
        shizukuCard.addView(label("SHIZUKU / ADB ACCESS", 16f, white, true))
        shizukuStatus = label("Проверка Shizuku…", 13f, muted, false)
        shizukuCard.addView(shizukuStatus)
        shizukuCard.addView(space(10))
        shizukuCard.addView(actionButton("ПРОВЕРИТЬ SHIZUKU", Color.rgb(38, 51, 68)) {
            checkShizuku(requestIfNeeded = true)
        })
        shizukuCard.addView(space(8))
        shizukuCard.addView(actionButton("ВКЛЮЧИТЬ PERFORMANCE MODE", cyan) {
            if (ensureShizukuPermission()) applyFpsTarget()
        })
        shizukuCard.addView(space(8))
        shizukuCard.addView(actionButton("ВЕРНУТЬ STANDARD MODE", Color.rgb(76, 45, 57)) {
            if (ensureShizukuPermission()) restoreFpsTarget()
        })
        resultStatus = label("Изменения не применялись.", 12f, muted, false)
        shizukuCard.addView(space(8))
        shizukuCard.addView(resultStatus)
        shizukuCard.addView(space(8))
        shizukuCard.addView(actionButton("КОПИРОВАТЬ ЛОГИ", Color.rgb(38, 51, 68)) { copyLogs() })
        content.addView(shizukuCard)

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
        infoCard.addView(label("ЧТО ДЕЛАЕТ ЭТА НАСТРОЙКА", 14f, white, true))
        infoCard.addView(space(6))
        infoCard.addView(label(
            "Через Shizuku приложение переключает поддерживаемый Android Game Mode для Limbus Company. Android 16 на этом устройстве блокирует запись game_overlay, поэтому приложение не пытается обходить системный allowlist. Performance mode не гарантирует 120 FPS и не снимает внутренний лимит игры.",
            13f, muted, false
        ))
        infoCard.addView(space(8))
        infoCard.addView(label(
            "Перед включением проверяется список режимов, которые Android действительно поддерживает для игры. Кнопка восстановления возвращает Standard mode. FPS игры нужно проверять отдельно.",
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
        checkShizuku(requestIfNeeded = false)
    }

    override fun onResume() {
        super.onResume()
        displayManager.registerDisplayListener(this, null)
        refreshInfo()
        checkShizuku(requestIfNeeded = false)
    }

    override fun onPause() {
        displayManager.unregisterDisplayListener(this)
        super.onPause()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }

    private fun ensureShizukuPermission(): Boolean {
        if (!Shizuku.pingBinder()) {
            updateShizukuStatus("Shizuku не запущен. Открой Shizuku и нажми Start.")
            return false
        }
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) return true
        Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
        updateShizukuStatus("Выдай разрешение этому приложению в окне Shizuku.")
        return false
    }

    private fun checkShizuku(requestIfNeeded: Boolean) {
        logDiagnostic("CHECK Shizuku: ping=${Shizuku.pingBinder()}, permission=${runCatching { Shizuku.checkSelfPermission() }.getOrDefault(-1)}, api=${runCatching { Shizuku.getVersion() }.getOrDefault(-1)}")
        if (!Shizuku.pingBinder()) {
            updateShizukuStatus("Shizuku не запущен")
            return
        }
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            updateShizukuStatus("Shizuku подключён • shell-доступ разрешён")
        } else {
            updateShizukuStatus("Shizuku работает • требуется разрешение приложения")
            if (requestIfNeeded) Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
        }
    }

    private fun applyFpsTarget() {
        resultStatus.text = "Проверяю GameManager и применяю FPS intervention…"
        thread(name = "limbus-game-mode-apply") {
            try {
                val before = runShell("cmd game list-modes $GAME_PACKAGE")
                if (before.exitCode != 0) {
                    throw IllegalStateException("Не удалось прочитать режимы игры: ${before.stderr.ifBlank { before.stdout }}")
                }
                logDiagnostic("GAME MODES BEFORE: ${before.stdout}")

                val supported = before.stdout.substringAfter("available game modes: [", "")
                    .substringBefore("]", "")
                    .split(",")
                    .map { it.trim().lowercase(Locale.US) }
                    .filter { it.isNotBlank() }

                if ("performance" !in supported) {
                    throw IllegalStateException(
                        "Android не сообщает Performance mode как доступный для Limbus Company. " +
                        "Ответ GameManager: ${before.stdout}"
                    )
                }

                // Use GameManagerService's supported shell API instead of writing
                // the protected DeviceConfig game_overlay namespace directly.
                val setConfig = runShell("cmd game set --mode 2 --fps 120 $GAME_PACKAGE")
                if (setConfig.exitCode != 0) {
                    throw IllegalStateException("GameManager не принял FPS intervention: ${setConfig.stderr.ifBlank { setConfig.stdout }}")
                }

                val config = runShell("cmd game list-configs $GAME_PACKAGE")
                if (config.exitCode != 0 || config.stdout.contains("No interventions found")) {
                    throw IllegalStateException("Не удалось подтвердить FPS intervention: ${config.stdout}; ${config.stderr}")
                }
                logDiagnostic("GAME CONFIG AFTER SET: ${config.stdout}")

                val mode = runShell("cmd game mode performance $GAME_PACKAGE")
                if (mode.exitCode != 0) {
                    throw IllegalStateException("Не удалось включить Performance mode: ${mode.stderr.ifBlank { mode.stdout }}")
                }

                val after = runShell("cmd game list-modes $GAME_PACKAGE")
                if (after.exitCode != 0 || !after.stdout.contains("current mode: performance")) {
                    throw IllegalStateException("Проверка Performance mode не прошла: ${after.stdout}; ${after.stderr}")
                }

                val message = "FPS intervention передан GameManager, Performance mode включён. Конфигурация: ${config.stdout.trim()}. Это системная настройка-ограничитель, а не гарантия 120 FPS в игре."
                logDiagnostic("APPLY RESULT: $message")
                runOnUiThread { resultStatus.text = message }
            } catch (e: Exception) {
                val message = "Не удалось применить GameManager intervention: ${e.message ?: e.javaClass.simpleName}"
                logDiagnostic("APPLY ERROR: ${Log.getStackTraceString(e)}")
                runOnUiThread { resultStatus.text = message }
            }
        }
    }

    private fun restoreFpsTarget() {
        resultStatus.text = "Сбрасываю FPS intervention и возвращаю Standard mode…"
        thread(name = "limbus-game-mode-restore") {
            try {
                val reset = runShell("cmd game reset --mode performance $GAME_PACKAGE")
                if (reset.exitCode != 0) {
                    throw IllegalStateException("Не удалось сбросить FPS intervention: ${reset.stderr.ifBlank { reset.stdout }}")
                }
                val restored = runShell("cmd game mode standard $GAME_PACKAGE")
                if (restored.exitCode != 0) {
                    throw IllegalStateException("Не удалось вернуть Standard mode: ${restored.stderr.ifBlank { restored.stdout }}")
                }
                val verify = runShell("cmd game list-modes $GAME_PACKAGE")
                if (verify.exitCode != 0 || !verify.stdout.contains("current mode: standard")) {
                    throw IllegalStateException("Проверка Standard mode не прошла: ${verify.stdout}; ${verify.stderr}")
                }
                val config = runShell("cmd game list-configs $GAME_PACKAGE")
                val message = "Standard mode восстановлен. FPS intervention сброшен. ${verify.stdout.trim()} Конфигурация: ${config.stdout.trim()}"
                logDiagnostic("RESTORE RESULT: $message")
                runOnUiThread { resultStatus.text = message }
            } catch (e: Exception) {
                val message = "Ошибка восстановления: ${e.message ?: e.javaClass.simpleName}"
                logDiagnostic("RESTORE ERROR: ${Log.getStackTraceString(e)}")
                runOnUiThread { resultStatus.text = message }
            }
        }
    }

    private data class ShellResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String
    )

    @Suppress("DiscouragedPrivateApi")
    private fun runShell(command: String): ShellResult {
        if (!Shizuku.pingBinder()) {
            throw IllegalStateException("Shizuku Binder недоступен")
        }
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("Приложению не выдано разрешение Shizuku")
        }

        val method = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        method.isAccessible = true

        val remote = try {
            method.invoke(null, arrayOf("sh", "-c", command), null, null) as? Process
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw (e.targetException ?: e)
        } ?: throw IllegalStateException("Shizuku newProcess вернул null")

        val stdoutBytes = ByteArrayOutputStream()
        val stderrBytes = ByteArrayOutputStream()
        val stdoutThread = thread(name = "limbus-shizuku-stdout") {
            remote.inputStream.use { it.copyTo(stdoutBytes) }
        }
        val stderrThread = thread(name = "limbus-shizuku-stderr") {
            remote.errorStream.use { it.copyTo(stderrBytes) }
        }

        val exitCode = try {
            remote.waitFor()
        } finally {
            stdoutThread.join(5_000)
            stderrThread.join(5_000)
            remote.destroy()
        }

        val stdout = stdoutBytes.toString(Charsets.UTF_8.name()).trim()
        val stderr = stderrBytes.toString(Charsets.UTF_8.name()).trim()
        lastCommandSummary = "exit=$exitCode; stdout=$stdout; stderr=$stderr"
        logDiagnostic("COMMAND [$command] -> $lastCommandSummary")
        return ShellResult(exitCode, stdout, stderr)
    }

    private fun logDiagnostic(message: String) {
        val line = "${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())} | $message"
        synchronized(diagnosticLog) {
            diagnosticLog.append(line).append('\n')
            if (diagnosticLog.length > 24000) diagnosticLog.delete(0, diagnosticLog.length - 24000)
        }
        Log.i("LimbusFPS", message)
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("diagnostics", synchronized(diagnosticLog) { diagnosticLog.toString() }).apply()
    }

    private fun copyLogs() {
        val saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString("diagnostics", "") ?: ""
        val live = synchronized(diagnosticLog) { diagnosticLog.toString() }
        val report = buildString {
            append("Limbus FPS Unlocker diagnostics\n")
            append("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}; Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})\n")
            append("Shizuku ping: ${runCatching { Shizuku.pingBinder() }.getOrDefault(false)}\n")
            append("Shizuku permission: ${runCatching { Shizuku.checkSelfPermission() }.getOrDefault(-1)}\n")
            append("Shizuku API: ${runCatching { Shizuku.getVersion() }.getOrDefault(-1)}; UID: ${runCatching { Shizuku.getUid() }.getOrDefault(-1)}\n")
            append("Execution: Shizuku.newProcess; last command: $lastCommandSummary\n\n")
            append(if (live.isNotBlank()) live else saved)
        }
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Limbus FPS diagnostics", report))
        Toast.makeText(this, "Логи скопированы. Вставь их сюда.", Toast.LENGTH_LONG).show()
        logDiagnostic("USER copied diagnostics")
    }

    private fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

    private fun updateShizukuStatus(message: String) {
        if (::shizukuStatus.isInitialized) shizukuStatus.text = message
    }

    private fun refreshInfo() {
        val display = displayManager.getDisplay(android.view.Display.DEFAULT_DISPLAY)
        if (display == null) {
            currentRate.text = "Недоступно"
            supportedModes.text = "Не удалось прочитать режим дисплея"
        } else {
            currentRate.text = String.format(Locale.US, "%.0f Hz", display.refreshRate)
            val modes = display.supportedModes.map { it.refreshRate }.distinct().sorted()
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
        try { startActivity(Intent(action)) }
        catch (_: Exception) { startActivity(Intent(Settings.ACTION_SETTINGS)) }
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        val p = (16 * resources.displayMetrics.density).toInt()
        setPadding(p, p, p, p)
        background = roundedBackground(cardColor, 18)
    }

    private fun label(textValue: String, size: Float, color: Int, bold: Boolean) =
        TextView(this).apply {
            text = textValue
            textSize = size
            setTextColor(color)
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            includeFontPadding = true
        }

    private fun actionButton(textValue: String, color: Int, action: () -> Unit) =
        Button(this).apply {
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
        private const val SHIZUKU_PERMISSION_CODE = 120
        private const val PREFS = "limbus_fps_unlocker"
        private const val BACKUP_KEY = "previous_game_overlay"
    }
}
