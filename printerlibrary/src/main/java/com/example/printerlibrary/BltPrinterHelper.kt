package com.example.printerlibrary

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import java.io.OutputStream

class BltPrinterHelper(private val appActivity: Activity, private val context:Context) {
    private  val bluetoothManager:BluetoothManager =appActivity.getSystemService(BluetoothManager::class.java)
    private  val bluetoothAdapter:BluetoothAdapter? = bluetoothManager.adapter
    private val bluetoothSocket:BluetoothSocket?=null
    private  val outputStream:OutputStream?=null
    private val printerName:String="PIX210"
    private var isBluetoothPermissionAllow:Boolean = false

    private  val TAG = "Printer_DEBUG"
    companion object {
        /**
         * A constant used to represent the permission code for requesting Bluetooth connection permission.
         * This code is used when requesting permission to connect to Bluetooth devices.
         */
        private const val BLUETOOTH_CONNECT_PERMISSION_CODE = 1
    }
    init {
        checkBlutoothPermission()
    }

     private  fun checkBlutoothPermission() {
         isBluetoothPermissionAllow = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                appActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        }else{
            // For Android versions lower than 12 (API level 31), you can handle it differently.
            ActivityCompat.checkSelfPermission(
                appActivity,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        appActivity,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ) == PackageManager.PERMISSION_GRANTED

        }


        Log.d(TAG,"isBluetoothPermissionOn:"+isBluetoothPermissionAllow.toString())

    }
    fun requestBluetoothPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Request BLUETOOTH_CONNECT permission.
            if (ActivityCompat.checkSelfPermission(
                    appActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    appActivity, // Make sure your context is an AppCompatActivity. by adding as AppCompatActivity
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    BLUETOOTH_CONNECT_PERMISSION_CODE
                )
            }
        } else {
            // For Android versions lower than 12 (API level 31), you can handle it differently.
            // For example, you can request BLUETOOTH and BLUETOOTH_ADMIN permissions here.
            if (ActivityCompat.checkSelfPermission(
                    appActivity,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    appActivity,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    appActivity, // Make sure your context is an AppCompatActivity. by adding as AppCompatActivity
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ),
                    BLUETOOTH_CONNECT_PERMISSION_CODE
                )
            }
        }
    }

    fun isBluetoothOn(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    fun BluetoothSwitchOn(){
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


                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    context.startActivity(enableBluetoothIntent)
                    Log.d(TAG,"Turning on bluetooth")
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
}