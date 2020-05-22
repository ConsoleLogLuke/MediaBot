package wtf.lpc.mediabot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import kotlin.random.Random

lateinit var bot: JDA

class Bot : ListenerAdapter() {
    fun start() {
        bot = JDABuilder.createDefault(config.botToken)
            .addEventListeners(this)
            .build()
    }

    override fun onReady(event: ReadyEvent) {
        println("Logged in as ${bot.selfUser.asTag}")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.message.contentRaw.equals("+upload", true)) {
            var randomId: Int

            do randomId = Random.nextInt(100000, 1000000)
            while (randomId in uploads.keys)

            val privateChannel = event.author.openPrivateChannel().complete()
            try {
                privateChannel.sendMessage("${config.serverUrl}/new/$randomId").complete()
            } catch (e: ErrorResponseException) {
                event.channel.sendMessage("${event.author.asMention} It appears that your DMs are disabled, so " +
                        "I couldn't send you an upload link. \uD83D\uDE26").queue()
                return
            }

            event.message.addReaction("\uD83D\uDCEB").queue()
            uploads[randomId] = event.message
        }
    }
}
