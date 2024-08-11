package com.example.printerlibrary

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.Arrays
import java.util.UUID
import kotlin.math.log

class BltPrinterHelper(private val appActivity: Activity, private val context:Context) {
    private  val bluetoothManager:BluetoothManager =appActivity.getSystemService(BluetoothManager::class.java)
    private  val bluetoothAdapter:BluetoothAdapter? = bluetoothManager.adapter
    private var outputStream:OutputStream?=null
    private val printerName:String="PIX210"
    private val UUID_STRING = "00001101-0000-1000-8000-00805f9b34fb" // Standard SerialPortService ID
    private var bluetoothSocket: BluetoothSocket? = null
    private var isBluetoothPermissionAllow:Boolean = false
    private val imageHelper = ImageHelper(context)
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_BLUETOOTH_PERMISSIONS = 2

    // Commands for printing
    private val cmdPrintStart = byteArrayOf(0x10.toByte(), 0xFF.toByte(), 0xFE.toByte(), 0x01.toByte())
    private val cmdPrintEnd = byteArrayOf(0x1B.toByte(), 0x4A.toByte(), 0x40.toByte(), 0x10.toByte(), 0xFF.toByte(), 0xFE.toByte(), 0x45.toByte())
    private val cmdSetPrintInfo = byteArrayOf(0x1D.toByte(), 0x76.toByte(), 0x30.toByte(), 0x00.toByte(), 0x30.toByte(), 0x00.toByte())

    private  val TAG = "Printer_DEBUG"
    companion object {
        /**
         * A constant used to represent the permission code for requesting Bluetooth connection permission.
         * This code is used when requesting permission to connect to Bluetooth devices.
         */
        private const val BLUETOOTH_CONNECT_PERMISSION_CODE = 1
    }
    init {
        isBluetoothPermissionAllow = checkBlutoothPermission()
    }


    private fun checkBlutoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bluetoothConnectPermission = ActivityCompat.checkSelfPermission(appActivity, Manifest.permission.BLUETOOTH_CONNECT)
            val bluetoothScanPermission = ActivityCompat.checkSelfPermission(appActivity, Manifest.permission.BLUETOOTH_SCAN)
            bluetoothConnectPermission == PackageManager.PERMISSION_GRANTED &&
                    bluetoothScanPermission == PackageManager.PERMISSION_GRANTED
        } else {
            val bluetoothPermission = ActivityCompat.checkSelfPermission(appActivity, Manifest.permission.BLUETOOTH)
            val bluetoothAdminPermission = ActivityCompat.checkSelfPermission(appActivity, Manifest.permission.BLUETOOTH_ADMIN)
            bluetoothPermission == PackageManager.PERMISSION_GRANTED &&
                    bluetoothAdminPermission == PackageManager.PERMISSION_GRANTED
        }
    }

     fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                appActivity,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_BLUETOOTH_PERMISSIONS
            )
        } else {
            ActivityCompat.requestPermissions(
                appActivity,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_BLUETOOTH_PERMISSIONS
            )
        }
    }

    fun isBluetoothOn(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    fun switchOnBluetooth(){
        try {
            Log.d(TAG,"BluetoothSwitchOn section")
            if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
                Log.d(TAG,"BluetoothSwitchOn section enter")
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if(!isBluetoothPermissionAllow){
                    requestBluetoothPermission()
                    // checkAndRequestBluetoothPermission()
                    Log.d(TAG,"No permission for bluetooth")

                }else if(!isBluetoothOn()){


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(appActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                            context.startActivity(enableBluetoothIntent)
                            Log.d("TAG", "Turning on bluetooth")
                        } else {
                            Log.d("TAG", "requestBluetoothPermission")
                            requestBluetoothPermission()
                        }
                    } else {
                        context.startActivity(enableBluetoothIntent)
                        Log.d("TAG", "Turning on bluetooth")
                    }
                }
                else{
                    Log.d(TAG,"Bluetooth already on")
                }
            }else{
                Log.d(TAG,"Bluetooth already on else",)
            }
        }catch (e:Exception){
            Log.e("error",e.message.toString())
            Log.e("error",e.stackTraceToString())
        }

    }


    fun findBluetoothDevice(): BluetoothDevice? {
        if (!isBluetoothPermissionAllow) {
            requestBluetoothPermission()
            return null
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                    appActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermission()
                return null
            }

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            pairedDevices?.forEach { device ->
                Log.d(TAG,device.name)
                if (device.name == printerName) {
                    return device
                }
            }
            return null
        }
    }





     fun printData(data: String) {
        try {

            val device = findBluetoothDevice()
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                        appActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED) {
                    requestBluetoothPermission()
                    return
                }

                val printerUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID

                if (device != null) {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(printerUUID)
                }

                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream

              //  printData("Hello, this is a test print.")
            } catch (e: IOException) {
                e.printStackTrace()

            }


       // outputStream?.write(data.toByteArray(Charsets.UTF_8))

            val bitMapText = messageToBitmap(data)
            //printImage(bitMapText)
            CoroutineScope(Dispatchers.IO).launch {
                val isPrinted = printImage(bitMapText)
                if (isPrinted) {
                    Log.d(TAG, "Bitmap  sent to printer:")
                    // Message printed successfully.
                } else {
                    Log.d(TAG, "Bitmap mot sent to printer:")
                    // Handle the case where printing failed.
                }
              //  outputStream?.flush()
            }


        } catch (e: IOException) {
            e.printStackTrace()

        } finally {
            try {
              //  outputStream?.close()
             //   bluetoothSocket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun printMessage(message: String) {
        val device = findBluetoothDevice()
        if (device != null) {
            try {

                val uuid = UUID.fromString(UUID_STRING)
                // Establish a Bluetooth connection
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                        appActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED) {
                    requestBluetoothPermission()
                    return
                }
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()

                // Send the message to the printer
                outputStream?.write(message.toByteArray())
                val bitMapText = messageToBitmap(message)
                //printImage(bitMapText)
                CoroutineScope(Dispatchers.IO).launch {
                    val isPrinted = printImage(bitMapText)
                    if (isPrinted) {
                        Log.d(TAG, "Bitmap  sent to printer:")
                        // Message printed successfully.
                    } else {
                        Log.d(TAG, "Bitmap mot sent to printer:")
                        // Handle the case where printing failed.
                    }
                }
                // Close the streams and socket
                outputStream?.close()
                bluetoothSocket?.close()

                Log.d(TAG, "Message sent to printer: $message")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message to printer: ${e.message}")
            }
        } else {
            Log.d(TAG, "No paired device found with the name: $printerName")
        }
    }

    fun isPrinterConnected(): Boolean {
       // return bluetoothSocket != null && bluetoothSocket!!.isConnected

        return true
    }

    fun messageToBitmap(message: String,
                        font: Int = R.font.courier_new,
                        fontSize: Float = 20F, ): Bitmap {
        return imageHelper.textToMultilineBitmap(message, font, fontSize, 384)
    }
    suspend fun printImage(bitmap: Bitmap): Boolean {
        if (!isPrinterConnected()) {
            Log.d(TAG,"No Printer is connected")
            return false
        }

        try {
            val width = 384 // PeriPage  image width
            val scale = width.toFloat() / bitmap.width
            val height = (bitmap.height * scale).toInt()

            if (height > 65535) {
                return false
            }

            val grayBitmap = imageHelper.convertToGrayscale(bitmap)
            val resizedBitmap = Bitmap.createScaledBitmap(grayBitmap, width, height, true)
            val imageBytes = imageHelper.bitmapToByteArray(resizedBitmap)

            val heightBytes = ByteBuffer.allocate(2).putShort(height.toShort()).array()

            // A chunk is one line, 48 bytes * 8 = 384 bits
            val chunkSize = 48

            withContext(Dispatchers.IO) {
                outputStream?.write(cmdPrintStart)
                outputStream?.write(cmdSetPrintInfo + heightBytes)

                for (i in imageBytes.indices step chunkSize) {
                    val max = if ((i + chunkSize) >= imageBytes.size) {
                        imageBytes.size
                    } else {
                        i + chunkSize
                    }
                    val chunk = imageBytes.copyOfRange(i, max)
                    outputStream?.write(chunk)
                    Thread.sleep(20)
                }

                val lines = 30
                val lineHeightBytes = ByteBuffer.allocate(2).putShort(lines.toShort()).array()
                outputStream?.write(cmdSetPrintInfo + lineHeightBytes)
                val line = ByteArray(48)

                Arrays.fill(line, 0.toByte())
                for (i in 0 until lines) {
                    outputStream?.write(line)
                    Thread.sleep(20)
                }

                outputStream?.write(cmdPrintEnd)
                outputStream?.flush()
            }

            return true
        } catch (e: IOException) {
            Log.d(TAG, "Bitmap erroe"+e.message)
            e.printStackTrace()
            return false
        }
    }
}