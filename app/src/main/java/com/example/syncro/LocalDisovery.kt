package com.example.syncro

import android.util.Log
import java.net.*
import kotlin.concurrent.thread

object LocalDiscovery {

    private const val DISCOVERY_PORT = 8987
    private const val MESSAGE = "SYNCRO_AVAILABLE"
    @Volatile private var running = false

    // Receiver: broadcast availability
    fun startBroadcasting() {
        if (running) return
        running = true

        thread {
            try {
                val socket = DatagramSocket().apply {
                    broadcast = true
                }

                val data = MESSAGE.toByteArray()

                while (running) {
                    val interfaces = NetworkInterface.getNetworkInterfaces()
                    for (iface in interfaces) {
                        if (!iface.isUp || iface.isLoopback) continue
                        for (addr in iface.interfaceAddresses) {
                            val broadcast = addr.broadcast ?: continue
                            val packet = DatagramPacket(
                                data,
                                data.size,
                                broadcast,
                                DISCOVERY_PORT
                            )
                            socket.send(packet)
                        }
                    }
                    Thread.sleep(2000)
                }
                socket.close()
            } catch (e: Exception) {
                Log.e("SyncroDiscovery", "Broadcast error", e)
            }
        }
    }

    fun stopBroadcasting() {
        running = false
    }

    // Sender: discover receivers
    fun listen(onFound: (String) -> Unit) {
        thread {
            try {
                val socket = DatagramSocket(null).apply {
                    reuseAddress = true
                    bind(InetSocketAddress(DISCOVERY_PORT))
                }

                val buffer = ByteArray(1024)
                val packet = DatagramPacket(buffer, buffer.size)

                socket.receive(packet)
                val msg = String(packet.data, 0, packet.length)

                if (msg == MESSAGE) {
                    val ip = packet.address.hostAddress
                    Log.d("SyncroDiscovery", "Found receiver: $ip")
                    onFound(ip)
                }
                socket.close()
            } catch (e: Exception) {
                Log.e("SyncroDiscovery", "Listen error", e)
            }
        }
    }
}
