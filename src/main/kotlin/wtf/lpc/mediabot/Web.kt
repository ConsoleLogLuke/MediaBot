package wtf.lpc.mediabot

import io.javalin.Javalin
import io.javalin.core.util.FileUtil
import io.javalin.http.BadRequestResponse
import io.javalin.http.staticfiles.Location
import net.dv8tion.jda.api.EmbedBuilder
import org.apache.commons.io.FileUtils
import java.awt.Color
import java.io.File

lateinit var app: Javalin

fun startWebApp() {
    app = Javalin.create { config ->
        config.addStaticFiles("uploads", Location.EXTERNAL)
    }.start(config.port)

    app.get("/new/:randomId") { ctx ->
        val randomId = ctx.pathParam("randomId")
            .toIntOrNull() ?: throw BadRequestResponse()
        if (randomId !in uploads) throw BadRequestResponse()

        ctx.html("""
            <form method="post" action="/new/$randomId" enctype="multipart/form-data">
                <input type="file" name="upload">
                <button>Upload</button>
            </form>
        """)
    }

    app.post("/new/:randomId") { ctx ->
        val randomId = ctx.pathParam("randomId")
            .toIntOrNull() ?: throw BadRequestResponse()
        val message = uploads[randomId] ?: throw BadRequestResponse()

        val uploaded = ctx.uploadedFile("upload") ?: throw BadRequestResponse()
        if (message.author.id !in config.limitlessUsers && uploaded.size > config.fileSizeLimit) {
            val limit = FileUtils.byteCountToDisplaySize(config.fileSizeLimit)
            ctx.result("Your file is too large! The file size limit is $limit.")
            return@post
        }

        val fileDir = File(uploadsDir, message.id)
        if (!fileDir.exists()) fileDir.mkdirs()

        FileUtil.streamToFile(uploaded.content, "$fileDir${File.separator}${uploaded.filename}")

        message.channel.sendMessage("${config.serverUrl}/${message.id}/${uploaded.filename}").queue {
            val size = FileUtils.byteCountToDisplaySize(uploaded.size)

            val embed = EmbedBuilder()
            embed.setTitle("\uD83D\uDCBE ${uploaded.filename}")
            embed.setColor(Color(0x7289DA))
            embed.addField("Uploaded By", message.author.asMention, true)
            embed.addField("Size", size, true)

            message.channel.sendMessage(embed.build()).queue()
        }

        uploads.remove(randomId)
        ctx.result("Done! Your file has been uploaded and sent.")
    }
}
