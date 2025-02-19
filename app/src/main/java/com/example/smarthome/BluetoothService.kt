package com.example.smarthome

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.InputStream
import java.util.UUID

class BluetoothService {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null

    fun connect(deviceName: String): Boolean {
        bluetoothAdapter?.bondedDevices?.forEach { device ->
            if (device.name == deviceName) {
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                    )
                    bluetoothSocket?.connect()
                    inputStream = bluetoothSocket?.inputStream
                    return true
                } catch (e: IOException) {
                    e.printStackTrace()
                    return false
                }
            }
        }
        return false
    }

    fun readData(): String? {
        return try {
            val buffer = ByteArray(1024)
            val bytes = inputStream?.read(buffer) ?: -1
            if (bytes != -1) String(buffer, 0, bytes) else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
