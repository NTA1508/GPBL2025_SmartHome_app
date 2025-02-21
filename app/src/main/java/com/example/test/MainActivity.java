package com.example.test;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Bluetooth";
    private BluetoothAdapter bluetoothAdapter;

    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket btSocket = null;

    private final ActivityResultLauncher<String> requestBluetoothPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getBondedDevices();
                } else {
                    Log.e(TAG, "Quyền BLUETOOTH_CONNECT bị từ chối!");
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Thiết bị không hỗ trợ Bluetooth");
            return;
        }

        //light
        checkAndRequestBluetoothPermission();

        //switch light
        Switch switchLight = findViewById(R.id.switchLight);

        switchLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                turnOnLight();  // Bật đèn khi switch được bật
            } else {
                turnOffLight(); // Tắt đèn khi switch bị tắt
            }
        });

        BluetoothDevice hc05 = bluetoothAdapter.getRemoteDevice("58:56:00:00:7B:ED");
        Log.d(TAG, "name: " + hc05.getName());

        //BluetoothSocket btSocket = null;
        int counter = 0;
        do {
            try {
                btSocket = hc05.createRfcommSocketToServiceRecord(myUUID);
                Log.d(TAG, "btSocket: " + btSocket);
                btSocket.connect();
                Log.d(TAG, "on connect: " + btSocket.isConnected());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            counter++;
        }while (!btSocket.isConnected() && counter < 3);

//        try {
//            OutputStream outputStream = btSocket.getOutputStream();
//            outputStream.write(48);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        InputStream inputStream = null;
//        try {
//            inputStream = btSocket.getInputStream();
//            inputStream.skip(inputStream.available());
//
//            if (inputStream.available() > 0) {
//                byte b = (byte) inputStream.read();
//                Log.d(TAG, "CHAR: " + (char) b);
//            } else {
//                Log.w(TAG, "Không có dữ liệu trong InputStream");
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

//        try {
//            btSocket.close();
//            Log.d(TAG, "Bluetooth đã ngắt kết nối");
//        } catch (IOException e) {
//            Log.e(TAG, "Lỗi khi đóng Bluetooth: " + e.getMessage());
//        }
    }

    private void checkAndRequestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED) {
                getBondedDevices();
            } else {
                requestBluetoothPermission.launch(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            getBondedDevices();
        }
    }

    private void getBondedDevices() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "BluetoothAdapter = null, có thể thiết bị không hỗ trợ Bluetooth!");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Quyền BLUETOOTH_CONNECT chưa được cấp!");
                return;
            }
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.isEmpty()) {
            Log.d(TAG, "Không có thiết bị Bluetooth nào đã ghép đôi.");
        } else {
            for (BluetoothDevice device : pairedDevices) {
                Log.d(TAG, "Thiết bị: " + device.getName() + ", Địa chỉ MAC: " + device.getAddress());
            }
        }
    }

    //Test turn light 1
    private void turnOnLight() {
        new Thread(() -> {
            if (btSocket != null && btSocket.isConnected()) {
                try {
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write(1);
                    outputStream.flush();
                    Log.d(TAG, "✅ Đã gửi lệnh BẬT đèn");
                } catch (IOException e) {
                    Log.e(TAG, "❌ Lỗi khi gửi lệnh BẬT đèn: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "❌ btSocket chưa được kết nối!");
            }
        }).start();
    }

    private void turnOffLight() {
        new Thread(() -> {
            if (btSocket != null && btSocket.isConnected()) {
                try {
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write(0); // Lệnh tắt đèn
                    outputStream.flush();
                    Log.d(TAG, "✅ Đã gửi lệnh TẮT đèn");
                } catch (IOException e) {
                    Log.e(TAG, "❌ Lỗi khi gửi lệnh TẮT đèn: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "❌ btSocket chưa được kết nối!");
            }
        }).start();
    }


}
