package com.example.test;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.os.Handler;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Bluetooth";
    private BluetoothAdapter bluetoothAdapter;

    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket btSocket = null;

    private ImageView weatherImageView;

    private ImageView imageViewTemp;
    private final ActivityResultLauncher<String> requestBluetoothPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getBondedDevices();
                } else {
                    Log.e(TAG, "Quyền BLUETOOTH_CONNECT bị từ chối!");
                }
            });

    private final Handler handler = new Handler();

    private TextView textViewTime, tempTextView, humidityTextView;

    private DatabaseReference envRef;

    private static final String CHANNEL_ID = "sensor_notifications";

    private Double lastTemp = null;
    private Double lastHumidity = null;

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

        weatherImageView = findViewById(R.id.weatherImageView);

        imageViewTemp = findViewById(R.id.imageViewTemp);

        //Time
        textViewTime = findViewById(R.id.textViewTime);

        createNotificationChannel();

        Button btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
            finish();
        });

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

        //Humidity and temp
        tempTextView = findViewById(R.id.tempTextView);
        humidityTextView = findViewById(R.id.humidityTextView);

        envRef = FirebaseDatabase.getInstance().getReference("sensors/environment");
        envRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double temp = snapshot.child("temperature").getValue(Double.class);
                Double humidity = snapshot.child("humidity").getValue(Double.class);

                updateWeatherStatus(temp, humidity);

                updateTemp(temp);

                if (temp != null) {
                    tempTextView.setText(temp + "°C");
                } else {
                    tempTextView.setText("温度: データなし");
                }

                if (humidity != null) {
                    humidityTextView.setText(humidity + "%");
                } else {
                    humidityTextView.setText("湿度: データなし");
                }

                if ((temp != null && lastTemp != null && Math.abs(temp - lastTemp) >= 3) ||
                        (humidity != null && lastHumidity != null && Math.abs(humidity - lastHumidity) >= 3)) {
                    sendNotification(temp, humidity);
                }


                lastTemp = temp;
                lastHumidity = humidity;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tempTextView.setText("温度: エラー");
                humidityTextView.setText("湿度: エラー");
            }
        });
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


    //notification
    private void sendNotification(Double temp, Double humidity) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String contentText = "現在の温度: " + (temp != null ? temp + "°C" : "データなし") +
                "\n現在の湿度: " + (humidity != null ? humidity + "%" : "データなし");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("環境センサー更新")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(100, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "環境センサー通知",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("温度や湿度の変更を通知します");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //Wheather
    private void updateWeatherStatus(Double temp, Double humidity) {
        if (temp == null || humidity == null) {
            weatherImageView.setImageResource(R.drawable.goodwheather);
            return;
        }

        if (temp > 30 && humidity < 50) {
            //weatherStatusTextView.setText("🌞 Trời nắng");
            weatherImageView.setImageResource(R.drawable.sun);
        } else if (temp < 20 && humidity > 70) {
            //weatherStatusTextView.setText("🌧 Trời mưa");
            weatherImageView.setImageResource(R.drawable.rain);
        } else if (temp < 0) {
            //weatherStatusTextView.setText("❄️ Tuyết rơi");
            weatherImageView.setImageResource(R.drawable.snow);
        } else if (humidity > 80) {
            //weatherStatusTextView.setText("☁️ Nhiều mây");
            weatherImageView.setImageResource(R.drawable.cloud);
        } else {
            //weatherStatusTextView.setText("⛅ Thời tiết ổn định");
            weatherImageView.setImageResource(R.drawable.goodwheather);
        }
    }

    private void updateTemp(Double temp){
        if (temp == null) {
            weatherImageView.setImageResource(R.drawable.goodwheather);
            return;
        }
        if(temp <= 10){
            imageViewTemp.setImageResource(R.drawable.temp0to10);
        }else if(temp > 10 && temp <= 20){
            imageViewTemp.setImageResource(R.drawable.temp10to20);
        }else if(temp > 20 && temp <=30){
            imageViewTemp.setImageResource(R.drawable.temp20to30);
        }else if(temp > 30 && temp <=40 ){
            imageViewTemp.setImageResource(R.drawable.temp30to40);
        }else{
            imageViewTemp.setImageResource(R.drawable.tempup40);
        }
    }
}
