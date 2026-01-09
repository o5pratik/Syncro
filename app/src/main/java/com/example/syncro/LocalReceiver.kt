package com.example.syncro

import android.util.Log
import java.io.DataInputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

object LocalReceiver {

    private const val PORT = 8988
    @Volatile private var running = false
    private var serverSocket: ServerSocket? = null

    fun startReceiving(onFileReceived: (String, ByteArray) -> Unit) {
        if (running) {
            Log.d("SyncroReceiver", "Receiver already running")
            return
        }
        running = true

        thread {
            try {
                serverSocket = ServerSocket(PORT).apply {
                    reuseAddress = true
                }

                Log.d("SyncroReceiver", "Listening on port $PORT")

                // 🔁 Keep receiving multiple files
                while (running) {
                    val socket = serverSocket!!.accept()
                    handleClient(socket, onFileReceived)
                }
            } catch (e: Exception) {
                Log.e("SyncroReceiver", "Receiver error", e)
            } finally {
                stop()
            }
        }
    }

    private fun handleClient(
        socket: Socket,
        onFileReceived: (String, ByteArray) -> Unit
    ) {
        thread {
            try {
                val input = DataInputStream(socket.getInputStream())

                val fileName = input.readUTF()
                val fileSize = input.readLong()   // 🔑 use Long

                val buffer = ByteArray(fileSize.toInt())
                var bytesRead = 0

                while (bytesRead < buffer.size) {
                    val read = input.read(
                        buffer,
                        bytesRead,
                        buffer.size - bytesRead
                    )
                    if (read == -1) break
                    bytesRead += read
                }

                Log.d("SyncroReceiver", "Received $fileName ($bytesRead bytes)")
                onFileReceived(fileName, buffer)

                socket.close()
            } catch (e: Exception) {
                Log.e("SyncroReceiver", "Client receive failed", e)
            }
        }
    }

    fun stop() {
        running = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
    }
}
