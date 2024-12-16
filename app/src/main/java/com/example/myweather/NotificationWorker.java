package com.example.myweather;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.List;

public class NotificationWorker extends Worker {

    private final String TAG = "NotificationWorker_TAG";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        Context context = getApplicationContext();

        List<WeatherPreferences.WeatherData> weatherDataList = new ArrayList<>();
        weatherDataList = WeatherPreferences.loadWeatherData(context); // 传递 contextweatherDataList = WeatherPreferences.loadWeatherData(this);
        // 打印所有的天气数据 OK
        Log.d(TAG,"======= 从SQLite中加载天气列表 ========");
        String notic = "ERROR";
        for (WeatherPreferences.WeatherData data : weatherDataList) {
            Log.d(TAG, "Date: " + data.date + ", Weather: " + data.weather +
                    ", TempMax: " + data.highTemperature + "°C, TempMin: " + data.lowTemperature + "°C");
            notic = "今天天气: " + data.weather +
                    ", 最高气温: " + data.highTemperature + "°C, 最低气温: " + data.lowTemperature + "°C";
            break ; // 只看第一个，就是当天的
        }

        // 通知渠道 ID
        String channelId = "MyWeather_channel_id";

        // 创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.ic_cloud) // 设置小图标
                .setContentTitle("我的天气定期提醒") // 设置标题
                .setContentText(notic) // 设置内容
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // 获取 NotificationManager
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // 处理点击事件
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        builder.setContentIntent(pendingIntent);

        // 确保创建了通知渠道（仅限 API 26+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "定期通知",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // 显示通知
        notificationManager.notify(1, builder.build());

        return Result.success();
    }
}

