package com.example.bltprinter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.bltprinter.ui.theme.BltPrinterTheme
import com.example.printerlibrary.BltPrinterHelper

class MainActivity : AppCompatActivity() {

    private lateinit var bltPrinterHelper: BltPrinterHelper

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bltPrinterHelper = BltPrinterHelper(this, this)
        bltPrinterHelper.requestBluetoothPermission()
        bltPrinterHelper.BluetoothSwitchOn()
    }
}



