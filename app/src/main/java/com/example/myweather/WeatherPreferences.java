package com.example.myweather;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class WeatherPreferences {

    // 获取7天的天气数据
    public static List<WeatherData> loadWeatherData(Context context) {
        WeatherDbHelper dbHelper = new WeatherDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 查询所有天气数据，按日期排序
        Cursor cursor = db.query("weather_data", null, null, null, null, null, "date ASC");
        List<WeatherData> weatherDataList = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 检查每一列的索引值
                int weatherColumnIndex = cursor.getColumnIndex("weather");
                int highTempColumnIndex = cursor.getColumnIndex("high_temperature");
                int lowTempColumnIndex = cursor.getColumnIndex("low_temperature");
                int dateColumnIndex = cursor.getColumnIndex("date");
                int humidityColumnIndex = cursor.getColumnIndex("humidity");   // 新增字段索引
                int pressureColumnIndex = cursor.getColumnIndex("pressure");   // 新增字段索引
                int windSpeedColumnIndex = cursor.getColumnIndex("wind_speed"); // 风速字段索引
                int windDirColumnIndex = cursor.getColumnIndex("wind_direction"); // 风向字段索引

                // 如果索引有效，则读取数据
                if (weatherColumnIndex != -1 && highTempColumnIndex != -1 && lowTempColumnIndex != -1 &&
                        dateColumnIndex != -1 && humidityColumnIndex != -1 && pressureColumnIndex != -1 &&
                        windSpeedColumnIndex != -1 && windDirColumnIndex != -1) {

                    String weather = cursor.getString(weatherColumnIndex);
                    int highTemperature = cursor.getInt(highTempColumnIndex);
                    int lowTemperature = cursor.getInt(lowTempColumnIndex);
                    String date = cursor.getString(dateColumnIndex);
                    String humidity = cursor.getString(humidityColumnIndex);  // 读取湿度
                    String pressure = cursor.getString(pressureColumnIndex);  // 读取气压
                    String windSpeed = cursor.getString(windSpeedColumnIndex);  // 读取风速
                    String windDirection = cursor.getString(windDirColumnIndex); // 读取风向

                    // 将数据添加到列表中
                    weatherDataList.add(new WeatherData(weather, highTemperature, lowTemperature, date, humidity, pressure, windSpeed, windDirection));
                } else {
                    // 处理列索引无效的情况
                    System.out.println("One of the column indices is invalid.");
                }
            }
            cursor.close();
        }

        db.close();
        return weatherDataList;
    }

    // 用来表示天气数据的模型，包括日期
    public static class WeatherData {
        public String weather;
        public int highTemperature;
        public int lowTemperature;
        public String date;
        public String humidity;  // 新增字段
        public String pressure;  // 新增字段
        public String windSpeed; // 新增字段: 风速
        public String windDirection; // 新增字段: 风向

        // 更新构造函数，包含风速和风向
        public WeatherData(String weather, int highTemperature, int lowTemperature, String date, String humidity, String pressure, String windSpeed, String windDirection) {
            this.weather = weather;
            this.highTemperature = highTemperature;
            this.lowTemperature = lowTemperature;
            this.date = date;
            this.humidity = humidity;  // 新增字段赋值
            this.pressure = pressure;  // 新增字段赋值
            this.windSpeed = windSpeed;  // 新增字段赋值
            this.windDirection = windDirection;  // 新增字段赋值
        }
    }
}
