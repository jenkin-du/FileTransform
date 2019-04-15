package com.uestc.app;

import android.app.Application;
import android.os.Environment;


public class App extends Application {

    public static App app;
    public static String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FileTransform";
    public static String ip = "192.168.1.117";
    public static int port = 20002;

    /**
     * 获取单例
     */
    public static App getInstance() {
        synchronized (App.class) {
            if (app == null) {
                app = new App();
            }
        }
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

}
