package com.patrizi.androidthermalprinter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.patrizi.androidthermalprinter.print.RequestPermissionHandler;
import com.patrizi.androidthermalprinter.print.ThreadPoolManager;
import com.patrizi.androidthermalprinter.print.utils.BluetoothUtil;
import com.patrizi.androidthermalprinter.print.utils.ESCUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    private  String TAG = "MainActivity";
    private Context context;
    private  TextView tvTextdata;
    private  Button btnPrint;
    private  String mPrintData = "";

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothPrinterDevice = null;
    private BluetoothSocket socket = null;
    private boolean isBluetoothOpen = false;

    private RequestPermissionHandler mRequestPermissionHandler;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        Log.e(TAG, "init: ----<> BluetoothPrint" );
        mGrantAndroidPermission();
        LoadBluetoothPrinter();

        initComponent();


    }

    private void mGrantAndroidPermission() {
        mRequestPermissionHandler = new RequestPermissionHandler();
        handleButtonClicked();

    }
    private void handleButtonClicked() {
        mRequestPermissionHandler.requestPermission(this, new String[]{
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,

        }, 123, new RequestPermissionHandler.RequestPermissionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "request permission success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, "request permission failed", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void initComponent() {
        tvTextdata = findViewById(R.id.txtTitle);
        btnPrint = findViewById(R.id.btnPrint);

        mPrintData = "";
        mPrintData = tvTextdata.getText().toString();

        //Action to print
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleButtonClicked();
                onPrintCommand();
            }
        });
    }

    private void onPrintCommand() {
        Log.e(TAG, "onPrintCommand: " );
        Log.e(TAG, "print Text -->>  " + mPrintData);
        printerInit(mPrintData);

    }


    private void printerInit(String mTEXTData) {
        Log.e(TAG, "printerInit: " );
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {

                Log.e(TAG, "run: Printing -->  " + mTEXTData );

                try{


                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte)50);

                    byte[] align0 = ESCUtil.alignMode((byte)0);
                    byte[] align1 = ESCUtil.alignMode((byte)1);
                    byte[] align2 = ESCUtil.alignMode((byte)2);
                  /*  byte[] title1 = "蓝牙打印机测试\n".getBytes("GBK");
                    byte[] title2 = "Bluetooth Printer test\n".getBytes("GBK"); //
                    byte[] sign1 = "************************\n".getBytes("GBK");*/
                    byte[] fontTest0 = mTEXTData.getBytes("UTF-8");


                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte)200);

                    byte[][] cmdBytes = {
                            printer_init,
                            lineH1,fontSize0,fontTest0,align0,
                            performPrint};


                    try {
                        if((socket == null) || (!socket.isConnected()))
                        {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data,0,data.length);
                        out.close();
                        socket.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    // Function to check and request permission
    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void LoadBluetoothPrinter() {
        try {


        Log.e(TAG, "LoadBluetoothPrinter: " + "CHECK" );

        // 1: Get BluetoothAdapter
        mBluetoothAdapter = BluetoothUtil.getBluetoothAdapter();
        if(mBluetoothAdapter == null)
        {
            //Toast.makeText(getBaseContext(), R.string.get_BluetoothAdapter_fail, Toast.LENGTH_LONG).show();
            Log.e(TAG, "LoadBluetoothPrinter: "+ "Please Open Bluetooth" );
            isBluetoothOpen = false;
            return;
        } else {
            isBluetoothOpen =true;
        }
        //2: Get bluetoothPrinter Devices
        mBluetoothPrinterDevice = BluetoothUtil.getIposPrinterDevice(mBluetoothAdapter);
        if(mBluetoothPrinterDevice == null)
        {
            //Toast.makeText(getBaseContext(), R.string.get_BluetoothPrinterDevice_fail, Toast.LENGTH_LONG).show();
            Log.e(TAG, "LoadBluetoothPrinter: "+ "Please Make Sure Bluetooth have IposPrinter! " );

            return;
        }
        //3: Get connect Socket
        try {
            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
        // Toast.makeText(getBaseContext(), R.string.get_BluetoothPrinterDevice_success, Toast.LENGTH_LONG).show();
        Log.e(TAG, "LoadBluetoothPrinter: "+ "Bluetooth Printer Driver is loading success! " );


        }catch (Exception e){
            Log.e(TAG, "LoadBluetoothPrinter: Exception " + e.getMessage() );
        }

    }

}