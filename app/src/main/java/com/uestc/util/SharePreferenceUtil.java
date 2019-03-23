package com.uestc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.SharedPreferencesCompat;

import com.uestc.app.MyApplication;


/**
 * @author xiaanming
 * @describe 网上找的工具类
 * SharedPreferences的一个工具类
 * 可用于保存患者步骤信息
 */

public class SharePreferenceUtil {

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param key
     * @param object
     */
    public static void setParam(String fileName, String key, Object object) {

        String type = object.getClass().getSimpleName();
        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if ("String".equals(type)) {
            editor.putString(key, (String) object);
        } else if ("Integer".equals(type)) {
            editor.putInt(key, (Integer) object);
        } else if ("Boolean".equals(type)) {
            editor.putBoolean(key, (Boolean) object);
        } else if ("Float".equals(type)) {
            editor.putFloat(key, (Float) object);
        } else if ("Long".equals(type)) {
            editor.putLong(key, (Long) object);
        }

        editor.commit();
    }


    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object getParam(String fileName, String key, Object defaultObject) {
        String type = defaultObject.getClass().getSimpleName();
        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(fileName, Context.MODE_PRIVATE);

        if ("String".equals(type)) {
            return sp.getString(key, (String) defaultObject);
        } else if ("Integer".equals(type)) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if ("Boolean".equals(type)) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if ("Float".equals(type)) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if ("Long".equals(type)) {
            return sp.getLong(key, (Long) defaultObject);
        }

        return null;
    }


    /**
     * 删除某个key的数据
     *
     * @param key
     */
    public static void removeParam(String fileName, String key) {

        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.remove(key);
        editor.commit();
    }

    /**
     * 清除所有内容
     */
    public static void clear(String fileName) {
        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.clear();
        SharedPreferencesCompat.EditorCompat.getInstance().apply(edit);
    }

    /**
     * 保存临时存储文件路径
     */
    public static void saveTempPath(String fileName, String tempFilePath) {

        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(fileName, tempFilePath);
        editor.apply();
    }

    /**
     * 获取临时存储文件路径
     */
    public static String getTempPath(String fileName) {

        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sp.getString(fileName, "");
    }

    /**
     * 删除临时存储文件路径
     */
    public static void removeTempPath(String fileName) {

        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(fileName);
        editor.apply();
    }

}
