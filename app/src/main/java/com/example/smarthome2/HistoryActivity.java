package com.example.smarthome2;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> notificationList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listView = findViewById(R.id.notification_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notificationList);
        listView.setAdapter(adapter);

        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("sensors/gas_leak/history");

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String log = data.getValue(String.class);
                    notificationList.add(log);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HistoryActivity.this, "履歴の取得に失敗しました: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
