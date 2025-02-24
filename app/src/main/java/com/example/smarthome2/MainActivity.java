package com.example.smarthome2;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.smarthome2.R;

public class MainActivity extends AppCompatActivity {

    private TextView tempTextView, humidityTextView;
    private DatabaseReference envRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempTextView = findViewById(R.id.tempTextView);
        humidityTextView = findViewById(R.id.humidityTextView);

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tempTextView.setText("温度: エラー");
                humidityTextView.setText("湿度: エラー");
            }
        });
    }
}