package com.example.test;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.os.Handler;

import java.io.IOException;
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

    private final Handler handler = new Handler();

    private TextView textViewTime;

    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            textViewTime.setText(DateTime.getCurrentTime()); // Cập nhật thời gian
            handler.postDelayed(this, 1000); // Lặp lại sau 1 giây
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Thiết bị không hỗ trợ Bluetooth");
            return;
        }

        //Time
        textViewTime = findViewById(R.id.textViewTime);

        handler.post(updateTimeRunnable);
        //Date
        String currentDate = DateTime.getCurrentDate();
        TextView textViewInfo = findViewById(R.id.textViewInfo);

        if (textViewInfo != null) {
            textViewInfo.setText(currentDate);
        } else {
            Log.e(TAG, "textViewInfo is null!");
        }

        //Location
        TextView textViewIPLocation = findViewById(R.id.textViewLocation); // Kết nối TextView từ XML

        IPLocationHelper.getLocationFromIP(new IPLocationHelper.IPAddressCallback() {
            @Override
            public void onLocationReceived(String city, String country) {
                runOnUiThread(() -> textViewIPLocation.setText(city + ", " + country));
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> textViewIPLocation.setText("Lỗi: " + errorMessage));
            }
        });

        //Bluetooth Permission
        checkAndRequestBluetoothPermission();

        //Light
        Switch switchLight = findViewById(R.id.switchLight);
        Switch switchLight2 = findViewById(R.id.switchLight2);

        switchLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                turnOnLight();
            } else {
                turnOffLight();
            }
        });
        switchLight2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                turnOnLight2();
            } else {
                turnOffLight2();
            }
        });

        //switch fan
        Switch switchFan = findViewById(R.id.switchFan);
        Switch switchFan2 = findViewById(R.id.switchFan2);
        switchFan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                turnOnFan();
            } else {
                turnOffFan();
            }
        });
        switchFan2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                turnOnFan2();
            } else {
                turnOffFan2();
            }
        });

        Switch switchDoor = findViewById(R.id.switchDoor);
        switchDoor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                turnOnDoor();
            } else {
                turnOffDoor();
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
                Log.e(TAG, "❌ Lỗi kết nối Bluetooth: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Không thể kết nối với thiết bị Bluetooth", Toast.LENGTH_SHORT).show());
                break;
            }
            counter++;
        } while (!btSocket.isConnected() && counter < 3);
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

    //Turn light 1
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

    //Turn light 2
    private void turnOnLight2() {
        new Thread(() -> {
            if (btSocket != null && btSocket.isConnected()) {
                try {
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write(3);
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

    private void turnOffLight2() {
        new Thread(() -> {
            if (btSocket != null && btSocket.isConnected()) {
                try {
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write(2); // Lệnh tắt đèn
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

    //Turn on fan 1
    private void turnOnFan() {
        new Thread(() -> {
            if (btSocket != null && btSocket.isConnected()) {
                try {
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write(5);
                    outputStream.flush();
                    Log.d(TAG, "✅ Đã gửi lệnh BẬT quạt");
                } catch (IOException e) {
                    Log.e(TAG, "❌ Lỗi khi gửi lệnh BẬT quạt: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "❌ btSocket chưa được kết nối!");
            }
        }).start();
    }

    private void turnOffFan() {
        new Thread(() -> {
            if (btSocket != null && btSocket.isConnected()) {
                try {
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write(4); // Lệnh tắt đèn
                    outputStream.flush();
                    Log.d(TAG, "✅ Đã gửi lệnh TẮT quạt");
                } catch (IOException e) {
                    Log.e(TAG, "❌ Lỗi khi gửi lệnh TẮT quạt: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "❌ btSocket chưa được kết nối!");
            }
        }).start();
    }
    //Turn on fan 2
    private void turnOnFan2() {
        new Thread(() -> {
            if (btSocket != null && btSocket.isConnected()) {
                try {
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write(7);
                    outputStream.flush();
                    Log.d(TAG, "✅ Đã gửi lệnh BẬT quạt");
                } catch (IOException e) {
                    Log.e(TAG, "❌ Lỗi khi gửi lệnh BẬT quạt: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "❌ btSocket chưa được kết nối!");
            }
        }).start();
    }

    private void turnOffFan2() {
        new Thread(() -> {
            if (btSocket != null && btSocket.isConnected()) {
                try {
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write(6); // Lệnh tắt đèn
                    outputStream.flush();
                    Log.d(TAG, "✅ Đã gửi lệnh TẮT quạt");
                } catch (IOException e) {
                    Log.e(TAG, "❌ Lỗi khi gửi lệnh TẮT quạt: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "❌ btSocket chưa được kết nối!");
            }
        }).start();
    }

    //Turn on door
    private void turnOnDoor() {
        new Thread(() -> {
            if (btSocket != null && btSocket.isConnected()) {
                try {
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write(9);
                    outputStream.flush();
                    Log.d(TAG, "✅ Đã gửi lệnh OPEN door");
                } catch (IOException e) {
                    Log.e(TAG, "❌ Lỗi khi gửi lệnh OPEN door: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "❌ btSocket chưa được kết nối!");
            }
        }).start();
    }

    private void turnOffDoor() {
        new Thread(() -> {
            if (btSocket != null && btSocket.isConnected()) {
                try {
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write(8); // Lệnh tắt đèn
                    outputStream.flush();
                    Log.d(TAG, "✅ Đã gửi lệnh CLOSE door");
                } catch (IOException e) {
                    Log.e(TAG, "❌ Lỗi khi gửi lệnh CLOSE quạt: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "❌ btSocket chưa được kết nối!");
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimeRunnable); // Ngừng cập nhật khi thoát Activity
    }
}
