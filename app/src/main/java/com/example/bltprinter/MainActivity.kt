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
        bltPrinterHelper.switchOnBluetooth()


         val message = "****************************************\n" +
                "       Sample Fancy Bill Receipt\n" +
                "****************************************\n" +
                "Date: 2023-10-26 15:30:00\n" +
                "Customer: John Doe\n" +
                "Email: johndoe@example.com\n" +
                "\n" +
                "---------------------------\n" +
                "Item          |  Qty  |  Price  \n" +
                "------------------------------\n" +
                 "হলুদের গুড়া  |   3   | ১০ টাকা \n" +
                "মসলা       |   3   | ১০ টাকা \n" +
                 "মরিচের গুড়া  |  2   | ১০ টাকা \n" +
                "----------------------------------------\n" +
                "Subtotal      |       |  \$30.00 \n" +
                "Tax (7%)      |       |   \$2.10 \n" +
                "Total         |       |  \$32.10 \n" +
                "\n" +
                "----------------------------------------\n" +
                "Payment Method: Credit Card\n" +
                "Transaction ID: 1234567890\n" +
                "\n" +
                "Thank you for your purchase!\n" +
                "Please come again soon!\n" +
                "\n" +
                "****************************************\n"
        bltPrinterHelper.printData(message);
    }
}



