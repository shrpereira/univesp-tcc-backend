package br.com.shrpereira

import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.websocket.webSocket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

        webSocket("/messages/three-seconds") {
            GlobalScope.launch {
                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        send(Frame.Text(createWebSocketResponse(frame)))
                    }
                }
            }

            while (true) {
                delay(2999)
                send(Frame.Text(Gson().toJson(mapOf("message" to "This is a chat message"))))
            }
        }

        webSocket("/messages/fifteen-seconds") {
            GlobalScope.launch {
                while (true) {
                    delay(14999)
                    send(Frame.Text(Gson().toJson(mapOf("message" to "This is a chat message"))))
                }
            }

            GlobalScope.launch {
                while (true) {
                    incoming.receive()
                    send(Frame.Text(Gson().toJson(mapOf("result" to "Message received"))))
                }
            }
        }

        webSocket("/messages/thirty-seconds") {
            GlobalScope.launch {
                while (true) {
                    delay(29999)
                    send(Frame.Text(Gson().toJson(mapOf("message" to "This is a chat message"))))
                }
            }

            GlobalScope.launch {
                while (true) {
                    incoming.receive()
                    send(Frame.Text(Gson().toJson(mapOf("result" to "Message received"))))
                }
            }
        }

        webSocket("/messages/ninety-seconds") {
            GlobalScope.launch {
                while (true) {
                    delay(89999)
                    send(Frame.Text(Gson().toJson(mapOf("message" to "This is a chat message"))))
                }
            }

            GlobalScope.launch {
                while (true) {
                    incoming.receive()
                    send(Frame.Text(Gson().toJson(mapOf("result" to "Message received"))))
                }
            }
        }
    }
}

private fun createWebSocketResponse(frame: Frame) =
    Gson().toJson(mapOf("result" to "Message received", "message" to "${frame.data}"))

