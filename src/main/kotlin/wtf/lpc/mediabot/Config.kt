package wtf.lpc.mediabot

import org.json.JSONObject
import java.io.File
import kotlin.system.exitProcess

data class Config(
    val botToken: String,
    val fileSizeLimit: Long,
    val limitlessUsers: List<String>,
    val port: Int,
    val serverUrl: String
)

val configFile = File("config.json")
lateinit var config: Config

fun loadConfig() {
    if (!configFile.exists()) {
        val defaultConfig = mapOf(
            "botToken" to "bot-token-here",
            "fileSizeLimit" to 5000000000,
            "limitlessUsers" to listOf("279622919242514432", "162648671005966337"),
            "port" to 7000,
            "serverUrl" to "http://localhost:7000"
        )

        val json = JSONObject(defaultConfig)
        configFile.writeText(json.toString(4))

        println("Config created! Please configure the bot in config.json then run it again.")
        exitProcess(0)
    }

    val json = JSONObject(configFile.readText())
    val botToken = json.getString("botToken")
    val fileSizeLimit = json.getLong("fileSizeLimit")
    val limitlessUsers = json.getJSONArray("limitlessUsers").map { it.toString() }
    val port = json.getInt("port")
    val serverUrl = json.getString("serverUrl")

    config = Config(botToken, fileSizeLimit, limitlessUsers, port, serverUrl)
}
