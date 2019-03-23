package com.uestc.app;

import android.support.multidex.MultiDexApplication;

/**
 * @author Linsong Huang
 * @describe 全局初始化操作
 * @date 2017/11/6
 * @email 1044782171@qq.com
 * @org UESTC
 */

public class MyApplication extends MultiDexApplication {

    private static MyApplication app;

    /**
     * 获取单例
     */
    public static MyApplication getInstance() {
        synchronized (MyApplication.class) {
            if (app == null) {
                app = new MyApplication();
            }
        }
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    //    /**
//     * 程序启动时的一些全局初始化操作
//     */
//    @Override
//    public void onCreate() {
//        super.onCreate();
////        app = this;
////        // Glide图片库相关，防止其他控件的setTag和Glide冲突
////        ViewTarget.setTagId(R.id.glide_tag);
////        //加载OpenCV库
////        System.loadLibrary("opencv_java3");
////        //初始化全局Log记录类
////        if (ToolUtil.isApkInDebug(this)) {
////            Log.d("Application", "DEBUG模式");
////            LogUtil.getInstance().initialize(this, false, LogUtil.LogLevel.DEBUG);        //崩溃日志捕获
////        } else {
////            Log.d("Application", "Release模式");
////            LogUtil.getInstance().initialize(this, true, LogUtil.LogLevel.ERROR);        //崩溃日志捕获
////        }
////        CrashCatchHandler.getInstance().init(this);
////        //根据配置文件获取本地服务器的URL
////        String defaultCopyRightURL = "http://120.79.168.215/CopyrightManagement";
////        String CopyRightURL = (String) SharePreferenceUtil.getParam("UserAccount", "CopyRightURL", "");
////        if (CopyRightURL == null || "".equals(CopyRightURL)) {
////            SharePreferenceUtil.setParam("UserAccount", "CopyRightURL", defaultCopyRightURL);
////        }
////        //删除temp文件夹的临时视频文件
////        ToolUtil.delFolder(Global.shareTempFolderPath);
////
////        //检查全局文件，看是否有用户的信息上传本地服务器失败，失败则重传
////
////
////        // 配置文件实例化
////        Config config = Config.getInstance();
////        // 设置核心数
////        config.setCoreNumber(getNumberOfCPUCores());
////        // 获取屏幕宽高,并保存在配置实例中
////        DisplayMetrics dm = getResources().getDisplayMetrics();
////        int screenWidth = dm.widthPixels;
////        int screenHeight = dm.heightPixels;
////        config.setScreenWidth(screenWidth);
////        config.setScreenHeight(screenHeight);
////        // 读取厂商
////        config.setTableBrand(Build.BRAND);
////        // 读取机型
////        String tableModel = Build.MODEL;
////        config.setTabletModel(tableModel);
////        // 读取该机型的配置文件(properties)
////        InputStream in = null;
////        Properties properties = new Properties();
////        try {
////            in = getAssets().open("config/" + tableModel + ".properties");
////            properties.load(in);
////            config.setProperties(properties);
////        } catch (IOException e) {
////            ToastUtil.showLong(getString(R.string.unsupported_model));
////            try {
////                LogUtil.getInstance().e(this, "未配置的机型，加载默认参数", e);
////                in = getAssets().open("config/default.properties");
////                properties.load(in);
////                config.setProperties(properties);
////            } catch (IOException e1) {
////                LogUtil.getInstance().e(this, "默认参数加载失败", e1);
////            }
////        } finally {
////            if (in != null) {
////                try {
////                    in.close();
////                } catch (IOException e) {
////                    LogUtil.getInstance().e(this, "配置流关闭错误", e);
////                }
////            }
////        }
//
//    }


//    /**
//     * @return CPU 核心数
//     */
//    public static int getNumberOfCPUCores() {
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
//            return 1;
//        }
//        int cores;
//        try {
//            cores = new File("/sys/devices/system/cpu/").listFiles(new FileFilter() {
//                @Override
//                public boolean accept(File pathname) {
//                    String path = pathname.getName();
//                    if (path.startsWith("cpu")) {
//                        for (int i = 3; i < path.length(); i++) {
//                            if (path.charAt(i) < '0' || path.charAt(i) > '9') {
//                                return false;
//                            }
//                        }
//                        return true;
//                    }
//                    return false;
//                }
//            }).length;
//        } catch (SecurityException | NullPointerException e) {
//            cores = 1;
//        }
//        return cores;
//    }

}
