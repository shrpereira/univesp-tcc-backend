package br.com.shrpereira

import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.webSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {

        get("/") {
            call.respondText("Welcome!")
        }

        get("/health") {
            call.respond(mapOf("message" to "Server Online"))
        }

        var requestCount = 0

        get("/message") {
            requestCount++
            if (requestCount == 3) {
                requestCount = 0

                call.respond(mapOf("message" to "This is a chat message"))
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }

        post("/message") {
            call.respond(HttpStatusCode.OK)
        }

        webSocket("/messages") {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    when (frame.readText().replace("\"", "")) {
                        "first" -> createMessageSender(2999)
                        "second" -> createMessageSender(14999)
                        "third" -> createMessageSender(29999)
                        "forth" -> createMessageSender(89999)
                        "bye" -> close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        else -> {
                            if (frame.readText().isNotEmpty()) {
                                outgoing.send(Frame.Text(createWebSocketResponse()))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DefaultWebSocketServerSession.createMessageSender(interval: Long) = launch {
    withContext(Dispatchers.IO) {
        while (true) {
            delay(interval)
            send(Frame.Text(Gson().toJson(mapOf("message" to "This is a chat message"))))
        }
    }
}

private fun createWebSocketResponse() =
    Gson().toJson(mapOf("message" to "Message received"))