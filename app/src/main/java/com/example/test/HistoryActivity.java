package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView notificationListView;
    private ArrayAdapter<String> adapter;
    private List<String> notificationList;

    private DatabaseReference envRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Nút Back
        Button btnHistory = findViewById(R.id.btnBack);
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Khởi tạo ListView
        notificationListView = findViewById(R.id.notification_list);
        notificationList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notificationList);
        notificationListView.setAdapter(adapter);

        // Firebase - Lấy dữ liệu từ "notifications"
        envRef = FirebaseDatabase.getInstance().getReference("notifications");

        envRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String message = childSnapshot.child("message").getValue(String.class);
                    if (message != null) {
                        notificationList.add(message);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistoryActivity.this, "Lỗi lấy dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Lỗi: " + error.getMessage());
            }
        });
    }
}
