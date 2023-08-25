package com.patrizi.androidthermalprinter.print.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * address = '66:22:F9:3E:F0:B9'
 * uuid = '00001101-0000-1000-8000-00805f9b34fb'
 * name = 'MHT-P58D'
 */

public class BluetoothUtil{
    private static final String TAG = "BluetoothUtil";
   // private static final UUID IPOSPRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  old
    private static final UUID IPOSPRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
  //  private static final String IPosPrinter_Address = "00:AA:11:BB:22:CC";   //old
    private static final String IPosPrinter_Address = "66:22:F9:3E:F0:B9";

    public static BluetoothAdapter getBluetoothAdapter(){
        return BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothDevice getIposPrinterDevice(BluetoothAdapter mBluetoothAdapter){
        BluetoothDevice IPosPrinter_device = null;
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices){
            if(device.getAddress().equals(IPosPrinter_Address))
            {
                IPosPrinter_device =device;
                break;
            }
        }
        return IPosPrinter_device;
    }

    public static BluetoothSocket getSocket(BluetoothDevice mDevice) throws IOException
    {
        if (mDevice != null) {
            BluetoothSocket socket = mDevice.createRfcommSocketToServiceRecord(IPOSPRINTER_UUID);
            socket.connect();
            return socket;
        }else {
            Log.e(TAG, "getSocket: ***> "+ " Device Not Connected" );
            return null;
        }
    }
}