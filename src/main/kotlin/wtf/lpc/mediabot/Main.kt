package wtf.lpc.mediabot

import net.dv8tion.jda.api.entities.Message
import java.io.File

val uploadsDir = File("uploads")
val uploads = mutableMapOf<Int, Message>()

fun main() {
    loadConfig()

    if (!uploadsDir.exists()) uploadsDir.mkdirs()

    Bot().start()
    startWebApp()
}
