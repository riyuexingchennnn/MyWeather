package com.example.myweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int SETTINGS_REQUEST_CODE = 1;

    private static LinearLayout weatherList_linearLayout;
    private static final String TAG = "MainActivity_TAG";

    private static TextView today_high_temperature_textView;
    private static TextView today_low_temperature_textView;
    private static TextView today_weather_textView;
    private static TextView today_date_textView;
    private static ImageView today_weather_icon_imageView;

    private static Drawable rain_drawable;
    private static Drawable sun_drawable;
    private static Drawable clouds_drawable;
    private static Drawable snow_drawable;
    private static Drawable overcast_drawable;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static boolean isLandScape = false;
    private String temperatureUnitSetting ;

    private static TextView high_temperature_textView;
    private static TextView low_temperature_textView;
    private static TextView weather_textView;
    private static TextView date_textView;
    private static TextView weekday_textView;
    private static ImageView weather_icon_imageView;
    private static TextView details_textView;

    private static List<WeatherPreferences.WeatherData> weatherDataList;

    public static boolean isTablet(Context context) {
        Configuration config = context.getResources().getConfiguration();
        // 检查设备屏幕布局类别
        return (config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private String getPreference(String key, String defaultValue) {
        return getSharedPreferences("Settings", MODE_PRIVATE)
                .getString(key, defaultValue);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isTablet(this)){
            // 允许竖屏上下旋转
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        // 检查屏幕方向
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏时加载横屏布局
            setContentView(R.layout.activity_main_land);
            isLandScape = true;

            high_temperature_textView = findViewById(R.id.high_temperature);
            low_temperature_textView = findViewById(R.id.low_temperature);
            date_textView = findViewById(R.id.date);
            weekday_textView = findViewById(R.id.weekday);
            weather_textView = findViewById(R.id.weather);
            details_textView = findViewById(R.id.details);
            weather_icon_imageView = findViewById(R.id.weather_icon);

        } else {

            isLandScape = false;
            // 竖屏时加载竖屏布局
            setContentView(R.layout.activity_main);

            today_high_temperature_textView = findViewById(R.id.today_high_temperature);
            today_low_temperature_textView = findViewById(R.id.today_low_temperature);
            today_date_textView = findViewById(R.id.today_data);
            today_weather_textView = findViewById(R.id.today_weather_text);
            today_weather_icon_imageView = findViewById(R.id.today_weather_icon);
        }
        // 找到自定义的 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // 设置为 ActionBar

        // 找到linearLayout
        weatherList_linearLayout = findViewById(R.id.weather_list);

        // 从资源文件中加载图片
        rain_drawable = getResources().getDrawable(R.drawable.rain);
        snow_drawable = getResources().getDrawable(R.drawable.snow);
        sun_drawable = getResources().getDrawable(R.drawable.daysunny);
        clouds_drawable = getResources().getDrawable(R.drawable.daycloudy);
        overcast_drawable = getResources().getDrawable(R.drawable.overcast);

        // 不用静态变量了，shit
        String locationSetting = getPreference("Location", "Changsha");
        HttpUtils.fetchWeatherData(this,locationSetting);

        temperatureUnitSetting = getPreference("TemperatureUnit", "Metric");

    }

    private static int celsiusToFahrenheit(int celsius) {
        return celsius * 9 / 5 + 32;
    }

    public static String getDayOfWeek(String date) {
        try {
            // 定义日期格式
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parsedDate = dateFormat.parse(date); // 将字符串转换为 Date 对象

            // 创建新的 SimpleDateFormat 来获取星期几
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
            //SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault()); // 这个是跟随系统语言
            return dayFormat.format(parsedDate); // 返回星期几的名称，如 "Monday"
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    // 这是给平板用的
    private static void updateDetails(String details_date){

        // 打印所有的天气数据 OK
        Log.d(TAG,"======= 从SQLite中加载天气列表 ========");
        int i = 0;
        for (WeatherPreferences.WeatherData data : weatherDataList) {
            i++;
            if(!data.date.equals(details_date)){
                continue;
            }
            Log.d(TAG, "Date: " + data.date + ", Weather: " + data.weather +
                    ", TempMax: " + data.highTemperature + "°C, TempMin: " + data.lowTemperature + "°C");
            String data_weather;
            Drawable imageDrawable = sun_drawable;
            // 切换成英文，以及切换天气图标
            switch(data.weather){
                case "晴":
                    imageDrawable = sun_drawable;
                    data_weather = "Sun";
                    break;
                case "雨":
                    imageDrawable = rain_drawable;
                    data_weather = "Rain";
                    break;
                case "阴":
                    imageDrawable = overcast_drawable;
                    data_weather = "Overcast";
                    break;
                case "雪":
                    imageDrawable = snow_drawable;
                    data_weather = "snow";
                    break;
                case "多风":

                    data_weather = "Winds";
                    break;
                case "多云":
                    imageDrawable = clouds_drawable;
                    data_weather = "Clouds";
                    break;
                default:
                    data_weather = "ERROR";
            }
            if(i == 1){
                weekday_textView.setText("Today");
            }else if(i == 2){
                weekday_textView.setText("Tomorrow");
            }else{
                weekday_textView.setText(MainActivity.getDayOfWeek(data.date)); // 写在这里面了
            }
            date_textView.setText(data.date);
            if(SettingActivity.temperatureUnitSetting.equals("Metric")){
                // 摄氏度
                high_temperature_textView.setText(String.format("%d°C", data.highTemperature));
                low_temperature_textView.setText(String.format("%d°C", data.lowTemperature));
            }else{
                // 华氏度
                high_temperature_textView.setText(String.format("%d°F", celsiusToFahrenheit(data.highTemperature)));
                low_temperature_textView.setText(String.format("%d°F", celsiusToFahrenheit(data.lowTemperature)));
            }

            weather_textView.setText(data_weather);
            weather_icon_imageView.setImageDrawable(imageDrawable);

            String windDirection = data.windDirection;
            switch(windDirection){
                case "西风":
                    windDirection = "W";  // West
                    break;
                case "西北风":
                    windDirection = "WN";  // West-Northwest
                    break;
                case "北风":
                    windDirection = "N";  // North
                    break;
                case "东北风":
                    windDirection = "NE";  // North-East
                    break;
                case "东风":
                    windDirection = "E";  // East
                    break;
                case "东南风":
                    windDirection = "SE";  // South-East
                    break;
                case "南风":
                    windDirection = "S";  // South
                    break;
                case "西南风":
                    windDirection = "SW";  // South-West
                    break;
                case "风速过大":
                    windDirection = "ERROR";  // If wind speed is too high
                    break;
                case "无风":
                    windDirection = "CALM";  // Calm or no wind
                    break;
                default:
                    windDirection = "ERROR";  // Unknown or invalid wind direction
                    break;
            }
            details_textView.setText("Humidity: " + data.humidity + " %\nPressure: " + data.pressure + " hPa\nWind: " + data.windSpeed + " km/h " + windDirection);

            // 平板下没用share
            String share_text = data.date + "\n天气：" + data.weather + "\n最高温度：" + data.highTemperature + "°C\n最低温度：" + data.lowTemperature +"°C\n湿度：" + data.humidity + " %\n风速：" + data.windSpeed + "km/h\n风向：" + data.windDirection + "\n气压：" + data.pressure + " hPa";
        }
    }

    // 定义一个变量保存当前选中的 weatherItemView
    private static View selectedWeatherItemView;

    // 给HttpUtils调用的， 自己也调用了一次
    public static void updateWeatherList(Context context){

        Log.d(TAG,"有人叫我更新");

        // 清空 LinearLayout 中的所有视图
        weatherList_linearLayout.removeAllViews();
        weatherDataList = new ArrayList<>(); // 改成全局变量
        weatherDataList = WeatherPreferences.loadWeatherData(context); // 传递 contextweatherDataList = WeatherPreferences.loadWeatherData(this);

        if (isLandScape) {
            // 如果是平板横屏

            // 打印所有的天气数据 OK
            Log.d(TAG,"======= 从SQLite中加载天气列表 ========");
            int i = 0;
            for (WeatherPreferences.WeatherData data : weatherDataList) {
                Log.d(TAG, "Date: " + data.date + ", Weather: " + data.weather +
                        ", TempMax: " + data.highTemperature + "°C, TempMin: " + data.lowTemperature + "°C");
                String data_weather;
                Drawable imageDrawable = sun_drawable;
                // 切换成英文，以及切换天气图标
                switch(data.weather){
                    case "晴":
                        imageDrawable = sun_drawable;
                        data_weather = "Sun";
                        break;
                    case "雨":
                        imageDrawable = rain_drawable;
                        data_weather = "Rain";
                        break;
                    case "阴":
                        imageDrawable = overcast_drawable;
                        data_weather = "Overcast";
                        break;
                    case "雪":
                        imageDrawable = snow_drawable;
                        data_weather = "snow";
                        break;
                    case "多风":

                        data_weather = "Winds";
                        break;
                    case "多云":
                        imageDrawable = clouds_drawable;
                        data_weather = "Clouds";
                        break;
                    default:
                        data_weather = "ERROR";
                }
                i++;

                String weekday = getDayOfWeek(data.date);

                // 获取自定义天气条目布局
                LayoutInflater inflater = LayoutInflater.from(context);
                View weatherItemView = inflater.inflate(R.layout.weather_item, weatherList_linearLayout, false);

                // 获取子视图
                TextView weatherTextView = weatherItemView.findViewById(R.id.weather);
                TextView highTempTextView = weatherItemView.findViewById(R.id.high_temperature);
                TextView lowTempTextView = weatherItemView.findViewById(R.id.low_temperature);
                TextView weekdayTextView = weatherItemView.findViewById(R.id.weekday);

                ImageView weatherImageView = weatherItemView.findViewById(R.id.weather_icon);

                // 设置数据
                weatherImageView.setImageDrawable(imageDrawable);
                weatherTextView.setText(data_weather); // 换成英文字

                if(SettingActivity.temperatureUnitSetting.equals("Metric")){
                    // 摄氏度
                    highTempTextView.setText(String.format("%d°C", data.highTemperature));
                    lowTempTextView.setText(String.format("%d°C", data.lowTemperature));
                }else{
                    // 华氏度
                    highTempTextView.setText(String.format("%d°F", celsiusToFahrenheit(data.highTemperature)));
                    lowTempTextView.setText(String.format("%d°F", celsiusToFahrenheit(data.lowTemperature)));
                }

                if(i == 1){
                    weekdayTextView.setText("Today");
                    updateDetails(data.date);
                    // 设置当前点击的 view 背景为蓝色
                    weatherItemView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
                    // 更新选中项
                    selectedWeatherItemView = weatherItemView;
                }else if(i == 2){
                    weekdayTextView.setText("Tomorrow");
                }else{
                    weekdayTextView.setText(weekday);
                }

                // 设置点击事件监听器
                weatherItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 显示详细视图
                        updateDetails(data.date);
                        // 如果有之前选中的 view，将其背景恢复为白色
                        if (selectedWeatherItemView != null) {
                            selectedWeatherItemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        }

                        // 设置当前点击的 view 背景为蓝色
                        weatherItemView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));

                        // 更新选中项
                        selectedWeatherItemView = weatherItemView;
                    }
                });

                // 将自定义布局添加到 weather_list
                weatherList_linearLayout.addView(weatherItemView);
            }

        }else{
            // ---------------------- 如果是竖屏 ------------------------------------------

            // 打印所有的天气数据 OK
            Log.d(TAG,"======= 从SQLite中加载天气列表 ========");
            int i = 0;
            for (WeatherPreferences.WeatherData data : weatherDataList) {
                Log.d(TAG, "Date: " + data.date + ", Weather: " + data.weather +
                        ", TempMax: " + data.highTemperature + "°C, TempMin: " + data.lowTemperature + "°C");
                String data_weather;
                Drawable imageDrawable = sun_drawable;
                // 切换成英文，以及切换天气图标
                switch(data.weather){
                    case "晴":
                        imageDrawable = sun_drawable;
                        data_weather = "Sun";
                        break;
                    case "雨":
                        imageDrawable = rain_drawable;
                        data_weather = "Rain";
                        break;
                    case "阴":
                        imageDrawable = overcast_drawable;
                        data_weather = "Overcast";
                        break;
                    case "雪":
                        imageDrawable = snow_drawable;
                        data_weather = "snow";
                        break;
                    case "多风":

                        data_weather = "Winds";
                        break;
                    case "多云":
                        imageDrawable = clouds_drawable;
                        data_weather = "Clouds";
                        break;
                    default:
                        data_weather = "ERROR";
                }
                i++;
                if(i == 1){

                    if(SettingActivity.temperatureUnitSetting.equals("Metric")){
                        // 摄氏度
                        today_high_temperature_textView.setText(String.format("%d°C", data.highTemperature));
                        today_low_temperature_textView.setText(String.format("%d°C", data.lowTemperature));
                    }else{
                        // 华氏度
                        today_high_temperature_textView.setText(String.format("%d°F", celsiusToFahrenheit(data.highTemperature)));
                        today_low_temperature_textView.setText(String.format("%d°F", celsiusToFahrenheit(data.lowTemperature)));
                    }

                    today_date_textView.setText("Today, " + data.date);
                    today_weather_textView.setText(data_weather);
                    today_weather_icon_imageView.setImageDrawable(imageDrawable);
                    continue;
                }
                String weekday = getDayOfWeek(data.date);

                // 获取自定义天气条目布局
                LayoutInflater inflater = LayoutInflater.from(context);
                View weatherItemView = inflater.inflate(R.layout.weather_item, weatherList_linearLayout, false);

                // 获取子视图
                TextView weatherTextView = weatherItemView.findViewById(R.id.weather);
                TextView highTempTextView = weatherItemView.findViewById(R.id.high_temperature);
                TextView lowTempTextView = weatherItemView.findViewById(R.id.low_temperature);
                TextView weekdayTextView = weatherItemView.findViewById(R.id.weekday);

                ImageView weatherImageView = weatherItemView.findViewById(R.id.weather_icon);

                // 设置数据
                weatherImageView.setImageDrawable(imageDrawable);
                weatherTextView.setText(data_weather); // 换成英文字
                if(SettingActivity.temperatureUnitSetting.equals("Metric")){
                    // 摄氏度
                    highTempTextView.setText(String.format("%d°C", data.highTemperature));
                    lowTempTextView.setText(String.format("%d°C", data.lowTemperature));
                }else{
                    // 华氏度
                    highTempTextView.setText(String.format("%d°F", celsiusToFahrenheit(data.highTemperature)));
                    lowTempTextView.setText(String.format("%d°F", celsiusToFahrenheit(data.lowTemperature)));
                }

                if(i == 2){
                    weekdayTextView.setText("Tomorrow");
                }else{
                    weekdayTextView.setText(weekday);
                }

                // 设置点击事件监听器
                weatherItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 创建Intent，跳转到DetailsActivity
                        Intent intent = new Intent(context, DetailsActivity.class);
                        // 将日期传递到DetailsActivity
                        intent.putExtra("DATE", data.date);
                        // 启动DetailsActivity
                        context.startActivity(intent);

                    }
                });

                // 将自定义布局添加到 weather_list
                weatherList_linearLayout.addView(weatherItemView);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 加载菜单资源文件到 Toolbar
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理菜单项点击事件
        switch (item.getItemId()) {
            case R.id.map_location:
                // 检查权限并获取位置
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // 如果没有权限，则请求权限
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    // 权限已授予，获取位置
                    getLocation();
                }
                //Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.setting:

                Intent intent = new Intent(this, SettingActivity.class);
                //startActivity(intent);
                startActivityForResult(intent, SETTINGS_REQUEST_CODE);

                //Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 检查权限请求的结果
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，获取位置
                getLocation();
            } else {
                // 权限被拒绝，提示用户
                Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 获取位置
    private void getLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // 获取最后已知的位置
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                showLocationOnMap(latitude, longitude);
            } else {
                // 如果无法获取最后已知的位置，可以考虑请求更新位置
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        // 获取到新的位置，更新UI
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        showLocationOnMap(latitude, longitude);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}

                    @Override
                    public void onProviderEnabled(String provider) {}

                    @Override
                    public void onProviderDisabled(String provider) {}
                });
            }
        } catch (SecurityException e) {
            // 如果没有权限，处理 SecurityException
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
        }
    }

    // 显示位置的地图
    private void showLocationOnMap(double latitude, double longitude) {
        Uri locationUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, locationUri);
        startActivity(mapIntent);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {

            Log.d(TAG,"Setting后的地点：" + SettingActivity.locationSetting);
            HttpUtils.fetchWeatherData(this, SettingActivity.locationSetting);
            // 从设置页面返回，更新设置
            //updateWeatherList(this);
        }
    }

}