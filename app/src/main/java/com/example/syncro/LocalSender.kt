package com.example.syncro

import android.util.Log
import java.io.DataOutputStream
import java.net.Socket
import kotlin.concurrent.thread

object LocalSender {

    private const val PORT = 8988

    fun sendFile(
        receiverIp: String,
        fileName: String,
        fileBytes: ByteArray
    ) {
        thread {
            try {
                Log.d("SyncroSender", "Connecting to $receiverIp:$PORT")

                val socket = Socket(receiverIp, PORT)
                val output = DataOutputStream(socket.getOutputStream())

                // 🔑 MUST MATCH RECEIVER
                output.writeUTF(fileName)
                output.writeLong(fileBytes.size.toLong()) // ✅ FIX
                output.write(fileBytes)
                output.flush()

                socket.close()
                Log.d("SyncroSender", "File sent successfully")

            } catch (e: Exception) {
                Log.e("SyncroSender", "Send failed", e)
            }
        }
    }
}
