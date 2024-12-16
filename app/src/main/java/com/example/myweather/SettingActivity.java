package com.example.myweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SettingActivity extends AppCompatActivity {

    private final String TAG = "SettingActivity_TAG";
    private TextView locationText;
    private TextView temperatureUnitText;
    private TextView weatherNotificationsText;

    private LinearLayout location;
    private LinearLayout temperatureUnit;
    private CheckBox weatherNotificationCheckbox;

    public static String temperatureUnitSetting = "Metric";
    public static String locationSetting = "changsha";
//    public static String temperatureUnitSetting;
//    public static String locationSetting;

    public static void loadSetting(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 初始化 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed()); // 下面有重写所以不用担心

        // 初始化控件
        location = findViewById(R.id.settings_list).findViewById(R.id.Location);
        temperatureUnit = findViewById(R.id.settings_list).findViewById(R.id.TemperatureUnits);

        locationText = location.findViewById(R.id.LocationText);
        temperatureUnitText = temperatureUnit.findViewById(R.id.TemperatureUnitsText);
        weatherNotificationsText = findViewById(R.id.settings_list).findViewById(R.id.WeatherNotifications).findViewById(R.id.WeatherNotificationsText);
        weatherNotificationCheckbox = findViewById(R.id.settings_list).findViewById(R.id.WeatherNotifications).findViewById(R.id.weather_notification_checkbox);

        // 恢复设置
        locationText.setText(getPreference("Location", "Changsha")); // 默认长沙
        locationSetting = locationText.getText().toString();

//        temperatureUnitText.setText(getPreference("TemperatureUnit", "Metric")); // 默认摄氏度
//        temperatureUnitSetting = temperatureUnitText.getText().toString();
        temperatureUnitText.setText("Metric"); // 强制初始是摄氏度
        savePreference("TemperatureUnit", "Metric"); // 保存为摄氏度

        String weatherNotification = getPreference("WeatherNotification", "Disabled"); // 默认关闭
        weatherNotificationCheckbox.setChecked("Enabled".equals(weatherNotification));
        if("Enabled".equals(weatherNotification)){
            weatherNotificationsText.setText("Enabled");
        }else{
            weatherNotificationsText.setText("Disabled");
        }


        // 设置点击监听
        location.setOnClickListener(v -> toggleLocation());
        temperatureUnit.setOnClickListener(v -> toggleTemperatureUnit());
        weatherNotificationCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleWeatherNotification(isChecked));
    }

    /**
     * 切换地点
     */
    private void toggleLocation() {
        String currentLocation = locationText.getText().toString();
        switch (currentLocation) {
            case "Changsha":
                locationText.setText("Shanghai");
                savePreference("Location", "Shanghai"); // 保存为上海
                break;
            case "Shanghai":
                locationText.setText("Guangzhou");
                savePreference("Location", "Guangzhou"); // 保存为广州
                break;
            case "Guangzhou":
                locationText.setText("Harbin");
                savePreference("Location", "Harbin"); // 保存为哈尔滨
                break;
            case "Harbin":
            default:
                locationText.setText("Changsha");
                savePreference("Location", "Changsha"); // 保存为长沙
                break;
        }
        locationSetting = locationText.getText().toString();
    }


    /**
     * 切换温度单位
     */
    private void toggleTemperatureUnit() {
        String currentUnit = temperatureUnitText.getText().toString();
        if ("Metric".equals(currentUnit)) {
            temperatureUnitText.setText("Imperial");
            savePreference("TemperatureUnit", "Imperial"); // 保存为华氏度
        } else {
            temperatureUnitText.setText("Metric");
            savePreference("TemperatureUnit", "Metric"); // 保存为摄氏度
        }
        temperatureUnitSetting = temperatureUnitText.getText().toString();
    }

    /**
     * 切换天气通知状态
     */
    private void toggleWeatherNotification(boolean isChecked) {
        String status = isChecked ? "Enabled" : "Disabled";
        weatherNotificationsText.setText(status);
        savePreference("WeatherNotification", status); // 保存通知状态

        List<WeatherPreferences.WeatherData> weatherDataList = new ArrayList<>();
        weatherDataList = WeatherPreferences.loadWeatherData(this); // 传递 contextweatherDataList = WeatherPreferences.loadWeatherData(this);
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
        if(isChecked){

            // 开启定期提示
            scheduleNotificationWithWorkManager();

//            // -------------------------------------------- 开启通知 ---------------------------------------------
//            // 通知渠道 ID
//            String channelId = "MyWeather_channel_id";
//
//            // 创建通知渠道（仅限 Android 8.0+）
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                CharSequence name = "MyWeather"; // 渠道名称
//                String description = "Channel for default notifications"; // 渠道描述
//                int importance = NotificationManager.IMPORTANCE_DEFAULT; // 渠道重要性
//                NotificationChannel channel = new NotificationChannel(channelId, name, importance);
//                channel.setDescription(description);
//
//                // 注册通知渠道
//                NotificationManager notificationManager = getSystemService(NotificationManager.class);
//                if (notificationManager != null) {
//                    notificationManager.createNotificationChannel(channel);
//                }
//            }
//
//            // 检查权限（仅限 Android 13+）
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
//                    return; // 如果没有权限，停止后续逻辑
//                }
//            }
//
//            // 创建通知
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
//                    .setSmallIcon(R.drawable.ic_cloud) // 设置小图标
//                    .setContentTitle("我的天气") // 设置标题
//                    .setContentText(notic) // 设置内容
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT); // 设置优先级（适用于低于 API 26 的版本）
//
//            // 处理点击事件
//            Intent intent = new Intent(this, MainActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(
//                    this,
//                    0,
//                    intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//            );
//            builder.setContentIntent(pendingIntent);
//
//            // 显示通知
//            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            if (notificationManager != null) {
//                notificationManager.notify(1, builder.build());
//            }
        }else{
            // 不然就关闭定期提示
            WorkManager.getInstance(this).cancelAllWork();
        }
    }

    /**
     * 保存设置到 SharedPreferences
     */
    private void savePreference(String key, String value) {
        getSharedPreferences("Settings", MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply();
    }

    /**
     * 从 SharedPreferences 中读取设置
     */
    private String getPreference(String key, String defaultValue) {
        return getSharedPreferences("Settings", MODE_PRIVATE)
                .getString(key, defaultValue);
    }

    @Override
    public void onBackPressed() {
        // 修改设置后，返回给调用的 Activity
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed(); // 或 finish();
    }


    private void scheduleNotificationWithWorkManager() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                NotificationWorker.class,
                24, // 每 24 小时
                TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(this).enqueue(workRequest);
    }


}
