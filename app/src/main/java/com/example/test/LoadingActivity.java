package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Giả lập thời gian loading (ví dụ: 3 giây)
        new Handler().postDelayed(() -> {
            // Chuyển sang HomeActivity
            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Đóng MainActivity để ngăn người dùng quay lại màn hình loading
        }, 2500); // Thời gian delay 3 giây
    }
}
