package com.example.androidnotifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;
import android.util.Log;  // 追加
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ActivityCompat;

import android.media.RingtoneManager;
import android.net.Uri;

public class MainActivity extends AppCompatActivity {

    private static final String NEW_CHANNEL_ID = "new_notification_channel";
    private static final String CHANNEL_NAME = "Simplified_Coding";
    private static final String CHANNEL_DESC = "Simplified_Coding Notifications";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //画面表示
        setContentView(R.layout.activity_main);

        // 通知チャンネルの作成
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NEW_CHANNEL_ID,
                    "New_Channel",
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setShowBadge(true); // ← 追加：通知のバッジを表示する
            //通知音の設定
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            channel.setSound(soundUri, null);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            Log.d("NotificationDebug", "通知チャンネル作成完了");
        }


        //画面上のボタンbuttonNotifyがクリックされたときに、displayNotification()メソッドを呼び出すのでここをセンサーで取得したデータをandroidに送信したときにメソッドを呼び出すようにすればいいかも
        findViewById(R.id.buttonNotify).setOnClickListener(new View.OnClickListener() {
            @Override
            //buttonNotifyが押されたらdisplayNotification()を実行する、このボタンが通知を表示する役割を持つ
            public void onClick(View v) {
                displayNotification();

            }
        });
    }

    private void displayNotification() {
        try {
            Log.d("NotificationDebug", "通知の作成を開始");

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NEW_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("緊急通知！")
                    .setContentText("これは重要な通知です")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setVibrate(new long[]{1000, 1000, 1000})
                    .setTimeoutAfter(10000)
                    .setAutoCancel(true);

            Log.d("NotificationDebug", "Builder作成完了");

            NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d("NotificationDebug", "権限あり - 通知を送信試行");

                int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
                mNotificationMgr.notify(notificationId, mBuilder.build());

                Log.d("NotificationDebug", "通知を送信完了: ID = " + notificationId);
                Toast.makeText(this, "緊急事態です！！！", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("NotificationDebug", "権限なし");
                Toast.makeText(this, "通知の権限がありません", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("NotificationDebug", "エラー発生: " + e.getMessage());
            Toast.makeText(this, "エラー: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}