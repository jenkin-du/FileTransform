package com.uestc.net.util;

import android.widget.Toast;

import com.uestc.app.App;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/04/15
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ToastUtil {

    public static void showShort(String str) {
        Toast.makeText(App.getInstance(), str, Toast.LENGTH_SHORT).show();
    }

    public static void showLong(String str) {
        Toast.makeText(App.getInstance(), str, Toast.LENGTH_LONG).show();
    }


}
