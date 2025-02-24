package com.example.smarthome2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView tempTextView, humidityTextView;
    private DatabaseReference envRef;
    private static final String CHANNEL_ID = "sensor_notifications";

    private Double lastTemp = null;
    private Double lastHumidity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempTextView = findViewById(R.id.tempTextView);
        humidityTextView = findViewById(R.id.humidityTextView);

        createNotificationChannel();

        envRef = FirebaseDatabase.getInstance().getReference("sensors/environment");

        envRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double temp = snapshot.child("temperature").getValue(Double.class);
                Double humidity = snapshot.child("humidity").getValue(Double.class);

                if (temp != null) {
                    tempTextView.setText("温度: " + temp + "°C");
                } else {
                    tempTextView.setText("温度: データなし");
                }

                if (humidity != null) {
                    humidityTextView.setText("湿度: " + humidity + "%");
                } else {
                    humidityTextView.setText("湿度: データなし");
                }

                // 温度または湿度が変化した場合のみ通知
                if ((temp != null && !temp.equals(lastTemp)) || (humidity != null && !humidity.equals(lastHumidity))) {
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
}
