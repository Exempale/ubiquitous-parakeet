package dev.limbus.fpsunlocker

import android.content.Context
import android.os.Binder
import android.util.Log

class ShellUserService : IShizukuShellService.Stub {
    private val createdWithContext: Boolean

    constructor() : super() {
        createdWithContext = false
        Log.i(TAG, "UserService constructed with default constructor; uid=${Binder.getCallingUid()}")
    }

    constructor(context: Context) : super() {
        createdWithContext = true
        Log.i(TAG, "UserService constructed with Context constructor; package=${context.packageName}")
    }

    override fun getDiagnostics(): String {
        return "UserService alive; uid=${android.os.Process.myUid()}; pid=${android.os.Process.myPid()}; contextConstructor=$createdWithContext"
    }

    override fun execute(command: String): String {
        return try {
            val process = ProcessBuilder("sh", "-c", command)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            "EXIT:$exitCode\n$output"
        } catch (e: Exception) {
            Log.e(TAG, "Shell command failed", e)
            "EXIT:1\n${e.message ?: e.javaClass.simpleName}"
        }
    }

    companion object {
        private const val TAG = "LimbusFpsUserService"
    }
}
