package com.uestc.net.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.SharedPreferencesCompat;

import com.uestc.app.App;


/**
 * @author jenkin
 */

public class SharePreferenceUtil {


    /**
     * 清除所有内容
     */
    public static void clear(String key) {
        SharedPreferences sp = App.getInstance().getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.clear();
        SharedPreferencesCompat.EditorCompat.getInstance().apply(edit);
    }

    /**
     * 保存临时存储文件路径
     */
    public static void save(String key, String value) {

        SharedPreferences sp = App.getInstance().getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 获取临时存储文件路径
     */
    public static String get(String value) {

        SharedPreferences sp = App.getInstance().getSharedPreferences(value, Context.MODE_PRIVATE);
        return sp.getString(value, "");
    }

    /**
     * 删除临时存储文件路径
     */
    public static void remove(String value) {

        SharedPreferences sp = App.getInstance().getSharedPreferences(value, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(value);
        editor.apply();
    }

}
