package com.example.myweather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherDbHelper extends SQLiteOpenHelper {

    // 数据库名称和版本
    private static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 3;  // 更新数据库版本

    // 表名
    public static final String TABLE_NAME = "weather_data";

    // 表字段
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_WEATHER = "weather";
    private static final String COLUMN_HIGH_TEMP = "high_temperature";
    private static final String COLUMN_LOW_TEMP = "low_temperature";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_HUMIDITY = "humidity";  // 新字段
    private static final String COLUMN_PRESSURE = "pressure";  // 新字段
    private static final String COLUMN_WIND_SPEED = "wind_speed"; // 新字段: 风速
    private static final String COLUMN_WIND_DIR = "wind_direction"; // 新字段: 风向

    // 创建表的 SQL 语句
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_WEATHER + " TEXT, " +
            COLUMN_HIGH_TEMP + " TEXT, " +
            COLUMN_LOW_TEMP + " TEXT, " +
            COLUMN_DATE + " TEXT, " +
            COLUMN_HUMIDITY + " TEXT, " +   // 新字段
            COLUMN_PRESSURE + " TEXT, " +   // 新字段
            COLUMN_WIND_SPEED + " TEXT, " + // 新字段
            COLUMN_WIND_DIR + " TEXT" +     // 新字段
            ")";

    // 升级数据库的 SQL 语句
    private static final String ALTER_TABLE_ADD_HUMIDITY = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_HUMIDITY + " TEXT";
    private static final String ALTER_TABLE_ADD_PRESSURE = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_PRESSURE + " TEXT";
    private static final String ALTER_TABLE_ADD_WIND_SPEED = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_WIND_SPEED + " TEXT"; // 添加风速字段
    private static final String ALTER_TABLE_ADD_WIND_DIR = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_WIND_DIR + " TEXT";     // 添加风向字段

    // 构造函数
    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 创建数据库表
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    // 升级数据库
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果是从版本2升到版本3，添加新的字段
        if (oldVersion < 3) {
            db.execSQL(ALTER_TABLE_ADD_WIND_SPEED);
            db.execSQL(ALTER_TABLE_ADD_WIND_DIR);
        }
    }
}


