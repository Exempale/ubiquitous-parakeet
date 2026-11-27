package dev.limbus.fpsunlocker

class ShellUserService : IShizukuShellService.Stub() {
    override fun execute(command: String): String {
        return try {
            val process = ProcessBuilder("sh", "-c", command)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            "EXIT:$exitCode\n$output"
        } catch (e: Exception) {
            "EXIT:1\n${e.message ?: e.javaClass.simpleName}"
        }
    }
}
