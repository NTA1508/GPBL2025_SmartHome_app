package com.example.test;

import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IPLocationHelper {
    public interface IPAddressCallback {
        void onLocationReceived(String city, String country);
        void onError(String errorMessage);
    }

    public static void getLocationFromIP(IPAddressCallback callback) {
        String url = "http://ip-api.com/json/";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String city = jsonObject.optString("city", "Không xác định");
                        String country = jsonObject.optString("country", "Không xác định");
                        callback.onLocationReceived(city, country);
                    } catch (Exception e) {
                        callback.onError("Lỗi xử lý dữ liệu: " + e.getMessage());
                    }
                } else {
                    callback.onError("Lỗi kết nối API.");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Lỗi mạng: " + e.getMessage());
            }
        });
    }
}
