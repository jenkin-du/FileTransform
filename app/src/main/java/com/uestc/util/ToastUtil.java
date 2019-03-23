package com.uestc.util;

import android.widget.Toast;

import com.uestc.app.Global;
import com.uestc.app.MyApplication;


/**
 * @describe  Toast工具类
 * @author Linsong Huang
 * @date 2017/11/7
 * @email 1044782171@qq.com
 * @org UESTC
 */

public class ToastUtil {

    public static void showShort(String msg) {
        Toast.makeText(MyApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();

    }

    public static void showShort(String msg,int gravity) {
        Toast toast =Toast.makeText(MyApplication.getInstance(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(gravity,0,0);
        toast.show();
    }

    public static void showLong(String msg) {
        Toast.makeText(MyApplication.getInstance(), msg, Toast.LENGTH_LONG).show();
    }

    public static void showDebugInfo(String msg) {
        if (Global.DEBUG) {
            Toast.makeText(MyApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
