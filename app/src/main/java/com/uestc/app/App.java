package com.uestc.app;

import android.app.Application;


public class App extends Application {

    private static App app;

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
