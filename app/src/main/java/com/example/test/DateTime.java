package com.example.test;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTime {
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, yyyy/MM/dd", new Locale("en", "EN"));
        return sdf.format(new Date());
    }

    @SuppressLint("DefaultLocale")
    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY); // Giờ 24h
        int minute = calendar.get(Calendar.MINUTE);    // Phút
        int second = calendar.get(Calendar.SECOND);    // Giây

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }
}
