package com.example.myweather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

    private static final String TAG = "HttpUtils_TAG";

    // 替换 your_token 为实际的令牌
    private static final String AUTHORIZATION = "4137a71604fc4e478af2afffcb695a2f";

    private static final String WEATHER_API_BASE_URL = "https://devapi.qweather.com/v7/weather/7d";
    private static final String LOCATION_API_BASE_URL = "https://geoapi.qweather.com/v2/city/lookup";

    private static String city = "beijing";
    private static String cityCode = "101010100"; // 默认城市编码

    // 获取天气数据
    public static void fetchWeatherData(final Context context, String location) {
        city = location;
        fetchCityCode(context); // 先查询城市编码
        // 后来发现其实没有必要，可以预先手动查好城市编码，然后写在代码里的，就不需要再上网API查找城市编码
    }

    // 查询城市编码
    private static void fetchCityCode(final Context context) {
        String url = LOCATION_API_BASE_URL + "?location=" + city + "&key=" + AUTHORIZATION;
        new FetchCityCodeTask(context).execute(url);
    }

    // 获取城市编码的异步任务
    private static class FetchCityCodeTask extends AsyncTask<String, Void, String> {

        private Context mContext;

        public FetchCityCodeTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                response = stringBuilder.toString();
                reader.close();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching city code", e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    // 解析城市编码
                    JSONObject jsonResponse = new JSONObject(result);
                    if (jsonResponse.getString("code").equals("200")) {
                        JSONArray locationArray = jsonResponse.getJSONArray("location");
                        JSONObject cityInfo = locationArray.getJSONObject(0);
                        cityCode = cityInfo.getString("id"); // 获取城市编码
                        Log.d(TAG, "City Code for " + city + ": " + cityCode);

                        // 成功获取城市编码后，发起天气数据请求
                        String weatherUrl = WEATHER_API_BASE_URL + "?location=" + cityCode + "&key=" + AUTHORIZATION;
                        new FetchWeatherTask(mContext).execute(weatherUrl);
                    } else {
                        Log.e(TAG, "City lookup failed: " + jsonResponse.getString("code"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing city code JSON", e);
                }
            } else {
                Log.e(TAG, "No response from city lookup API");
            }
        }
    }

    // 获取天气数据的异步任务（保留现有逻辑）
    private static class FetchWeatherTask extends AsyncTask<String, Void, String> {

        private Context mContext;

        public FetchWeatherTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                response = stringBuilder.toString();
                reader.close();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather data", e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    // 解析 JSON 数据
                    JSONObject jsonResponse = new JSONObject(result);
                    if (jsonResponse.getString("code").equals("200")) {
                        JSONArray dailyArray = jsonResponse.getJSONArray("daily");

                        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
                        SQLiteDatabase db = dbHelper.getWritableDatabase();

                        if (getWeatherDataCount(db) >= 7) {
                            db.delete("weather_data", null, null);
                        }

                        for (int i = 0; i < dailyArray.length(); i++) {
                            JSONObject dayWeather = dailyArray.getJSONObject(i);
                            String date = dayWeather.getString("fxDate");
                            String tempMax = dayWeather.getString("tempMax");
                            String tempMin = dayWeather.getString("tempMin");
                            String weather = dayWeather.getString("textDay");
                            String humidity = dayWeather.getString("humidity");
                            String pressure = dayWeather.getString("pressure");
                            String windSpeedDay = dayWeather.getString("windSpeedDay");
                            String windDirDay = dayWeather.getString("windDirDay");

                            ContentValues values = new ContentValues();
                            values.put("weather", weather);
                            values.put("high_temperature", tempMax);
                            values.put("low_temperature", tempMin);
                            values.put("date", date);
                            values.put("humidity", humidity);
                            values.put("pressure", pressure);
                            values.put("wind_speed", windSpeedDay);
                            values.put("wind_direction", windDirDay);

                            db.insertWithOnConflict("weather_data", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                        }

                        db.close();
                    } else {
                        Log.e(TAG, "Error fetching weather data: " + jsonResponse.getString("code"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing weather JSON", e);
                }
            } else {
                Log.e(TAG, "No response from weather API");
            }

            MainActivity.updateWeatherList(mContext);
        }

        private int getWeatherDataCount(SQLiteDatabase db) {
            String countQuery = "SELECT COUNT(*) FROM weather_data";
            Cursor cursor = db.rawQuery(countQuery, null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count;
        }
    }
}
