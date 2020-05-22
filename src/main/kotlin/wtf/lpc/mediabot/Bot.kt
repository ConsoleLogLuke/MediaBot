package wtf.lpc.mediabot

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.File
import kotlin.random.Random

class Bot : ListenerAdapter() {
    fun start() {
        JDABuilder.createDefault(config.botToken)
                .addEventListeners(this)
                .build()
    }

    override fun onReady(event: ReadyEvent) {
        println("Logged in as ${event.jda.selfUser.asTag}")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val args = event.message.contentRaw
                .split(" ")
                .map { it.toLowerCase() }
        if (args.isEmpty()) return

        fun sendErrorMessage(message: String) {
            event.channel.sendMessage("${event.author.asMention} $message \uD83D\uDE26").queue()
        }

        if (args[0] == "+help") {
            val commands = listOf(
                    "\uD83D\uDCC4 **+upload**: Uploads a file",
                    "\uD83D\uDDD1 **+delete (message ID)**: (Admin-only) Deletes an uploaded file from the server"
            )

            event.channel.sendMessage(commands.joinToString("\n\n")).queue()
        }

        if (args[0] == "+upload") {
            var randomId: Int

            do randomId = Random.nextInt(100000, 1000000)
            while (randomId in uploads.keys)

            val privateChannel = event.author.openPrivateChannel().complete()
            try {
                privateChannel.sendMessage("${config.serverUrl}/new/$randomId").complete()
            } catch (e: ErrorResponseException) {
                sendErrorMessage("It appears that your DMs are disabled, so I couldn't send you an upload link.")
                return
            }

            event.message.addReaction("\uD83D\uDCEB").queue()
            uploads[randomId] = event.message

            return
        }

        if (args[0] == "+delete") {
            if (event.author.id !in config.adminIds) {
                sendErrorMessage("You must be marked as an admin to run this command.")
                return
            }

            if (args.size < 2) {
                sendErrorMessage("You didn't specify a message ID to delete.")
                return
            }

            val messageId = args[1]
            val messageDir = File(uploadsDir, messageId)

            if (!messageDir.exists()) {
                sendErrorMessage("That message either doesn't exist or isn't an upload command.")
                return
            }

            messageDir.deleteRecursively()
            event.message.addReaction("\uD83D\uDC4D").queue()

            return
        }
    }
}
