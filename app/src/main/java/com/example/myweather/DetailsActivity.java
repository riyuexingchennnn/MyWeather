package com.example.myweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    private final String TAG = "DetailsActivity_TAG";

    private static Drawable rain_drawable;
    private static Drawable sun_drawable;
    private static Drawable clouds_drawable;
    private static Drawable snow_drawable;
    private static Drawable overcast_drawable;

    // 声明视图组件
    private Toolbar toolbar;
    private TextView weekday;
    private TextView date;
    private TextView highTemperature;
    private TextView lowTemperature;
    private ImageView weatherIcon;
    private TextView weather;
    private TextView details;

    private String share_text = "ERROR";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // 使用 findViewById 绑定视图组件
        toolbar = findViewById(R.id.toolbar);

        // 设置 Toolbar 为 ActionBar
        setSupportActionBar(toolbar);

        // 显示返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 设置返回按钮的点击事件
        toolbar.setNavigationOnClickListener(v -> {
            // 点击返回按钮时，执行返回操作
            onBackPressed(); // 或者使用 finish() 来关闭当前 Activity
        });

        weekday = findViewById(R.id.weekday);
        date = findViewById(R.id.date);
        highTemperature = findViewById(R.id.high_temperature);
        lowTemperature = findViewById(R.id.low_temperature);
        weatherIcon = findViewById(R.id.weather_icon);
        weather = findViewById(R.id.weather);
        details = findViewById(R.id.details);

        // 从资源文件中加载图片
        rain_drawable = getResources().getDrawable(R.drawable.rain);
        snow_drawable = getResources().getDrawable(R.drawable.snow);
        sun_drawable = getResources().getDrawable(R.drawable.daysunny);
        clouds_drawable = getResources().getDrawable(R.drawable.daycloudy);
        overcast_drawable = getResources().getDrawable(R.drawable.overcast);

        // 获取传递过来的日期
        String details_date = getIntent().getStringExtra("DATE");

        List<WeatherPreferences.WeatherData> weatherDataList = new ArrayList<>();
        weatherDataList = WeatherPreferences.loadWeatherData(this); // 传递 contextweatherDataList = WeatherPreferences.loadWeatherData(this);



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

            if(i == 2){
                weekday.setText("Tomorrow");
            }else{
                weekday.setText(MainActivity.getDayOfWeek(data.date)); // 写在这里面了
            }
            date.setText(data.date);
            if(SettingActivity.temperatureUnitSetting.equals("Metric")){
                // 摄氏度
                highTemperature.setText(String.format("%d°C", data.highTemperature));
                lowTemperature.setText(String.format("%d°C", data.lowTemperature));
            }else{
                // 华氏度
                highTemperature.setText(String.format("%d°F", celsiusToFahrenheit(data.highTemperature)));
                lowTemperature.setText(String.format("%d°F", celsiusToFahrenheit(data.lowTemperature)));
            }

            weather.setText(data_weather);
            weatherIcon.setImageDrawable(imageDrawable);

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
            details.setText("Humidity: " + data.humidity + " %\nPressure: " + data.pressure + " hPa\nWind: " + data.windSpeed + " km/h " + windDirection);
            share_text = data.date + "\n天气：" + data.weather + "\n最高温度：" + data.highTemperature + "°C\n最低温度：" + data.lowTemperature +"°C\n湿度：" + data.humidity + " %\n风速：" + data.windSpeed + "km/h\n风向：" + data.windDirection + "\n气压：" + data.pressure + " hPa";
        }


        // 获取分享按钮
        ImageButton btnShare = findViewById(R.id.btn_share);

        // 设置点击事件监听器
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 调用分享方法
                shareWeatherDetails();
            }
        });

    }

    private int celsiusToFahrenheit(int celsius) {
        return celsius * 9 / 5 + 32;
    }

    /**
     * 分享天气信息的方法
     */
    private void shareWeatherDetails() {

        // 创建分享意图
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain"); // 分享纯文本
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "天气详情"); // 分享主题
        shareIntent.putExtra(Intent.EXTRA_TEXT, share_text); // 分享内容

        // 启动分享意图
        startActivity(Intent.createChooser(shareIntent, "分享天气详情"));
    }

    // 这个菜单项目前是假的，没什么用
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 加载菜单资源文件到 Toolbar
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

}
