package com.zkxl.locklibrary.bluetoothlib.utils

import android.content.Context

/**
 * @author Anubis
 * Date: 2021/03/08
 * Time: 09:45
 * Description: 存储用户设备的数据
 */
object SharedPrefUtils {
    private const val FILE_NAME = "config"
    private const val STATION_NAME = "STATION_NAME"
    private const val STATION_NUMBER = "STATION_NUMBER"
    private const val WORK_MODE = "WORK_MODE"
    private const val WORK_SPEED = "WORK_SPEED"
    private const val HEART_INTERVAL = "HEART_INTERVAL"
    private const val OPEN_LOCK_TIME = "OPEN_LOCK_TIME"
    private const val LOCK_ADRESS = "LOCK_ADRESS"
    private const val LOCK_FREQUENCY_POINT = "LOCK_FREQUENCY_POINT"

    /**
     * 存储基站名称
     */
    fun saveUserStationName(context: Context, stationName: String) {
        val pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(STATION_NAME, stationName).apply()
    }

    /**
     * 获得基站名称
     */
    fun getUserStationName(context: Context): String {
      return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getString(STATION_NAME, "").toString()
    }


    /**
     * 存储基站编号
     */
    fun saveUserStationNumber(context: Context, stationNumber: String) {
        val pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(STATION_NUMBER, stationNumber).apply()
    }


    /**
     * 获得基站编号
     */
    fun getUserStationNumber(context: Context): String {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getString(STATION_NUMBER, "").toString()
    }



    /**
     * 存储工作模式
     */
    fun saveUserWorkMode(context: Context, workMode: String) {
        val pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(WORK_MODE, workMode).apply()
    }



    /**
     * 获得工作模式
     */
    fun getUserWorkMode(context: Context): String {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getString(WORK_MODE, "").toString()
    }


    /**
     * 存储工作速率
     */
    fun saveUserWorkSpeed(context: Context, speed: String) {
        val pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(WORK_SPEED, speed).apply()
    }



    /**
     * 获得工作速率
     */
    fun getUserWorkSpeed(context: Context): String {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getString(WORK_SPEED, "").toString()
    }


    /**
     * 存储心跳间隔
     */
    fun saveUserHeart(context: Context, heart: String) {
        val pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(HEART_INTERVAL, heart).apply()
    }



    /**
     * 获得心跳间隔
     */
    fun getUserWorkHeart(context: Context): String {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getString(HEART_INTERVAL, "").toString()
    }

    /**
     * 存储开锁时间
     */
    fun saveUserOpenLockTime(context: Context, lockTime: String) {
        val pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(OPEN_LOCK_TIME, lockTime).apply()
    }



    /**
     * 获得开锁时间
     */
    fun getUserWorkOpenLockTime(context: Context): String {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getString(OPEN_LOCK_TIME, "").toString()
    }


    /**
     * 存储空口地址
     */
    fun saveUserLockAdress(context: Context, lockAdress: String) {
        val pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(LOCK_ADRESS, lockAdress).apply()
    }



    /**
     * 获得空口地址
     */
    fun getUserLockAdress(context: Context): String {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getString(LOCK_ADRESS, "").toString()
    }


/**
     * 存储频点
     */
    fun saveUserFrequencyPoint(context: Context, frequencyPoint: String) {
        val pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(LOCK_FREQUENCY_POINT, frequencyPoint).apply()
    }



    /**
     * 获得频点
     */
    fun getUserFrequencyPoint(context: Context): String {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getString(LOCK_FREQUENCY_POINT, "").toString()
    }


}