package com.uestc.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * @author ShenHongBin
 * @desribe 本地日志类
 * @date 2018/6/25
 * @email 511460468@qq.com
 * @org UESTC
 */
public class LogUtil {
    /**
     * 上下文环境
     */
    private Context context;
    /**
     * 日志文件地址
     */
    private String logFilePath;
    /**
     * 输出流
     */
    private FileOutputStream fos;
    /**
     * 输出流
     */
    private OutputStreamWriter osWriter;
    /**
     * 输出流
     */
    private BufferedWriter writer;
    /**
     * 是否写到本地
     */
    private boolean isWriter;
    /**
     * 当前日志级别
     */
    private LogLevel currentLevel;

    /**
     * 包名
     */
    private String pkgName;

    /**
     * 时间戳格式
     */
    private final SimpleDateFormat LOG_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    /**
     * 文件名格式
     */
    private final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 日志格式
     */
    private final static String LOG_FORMAT = "%s %d-%d/%s %s/TAG:%S--";

    /**
     * 单例实例,懒汉式
     */
    private static LogUtil instance = new LogUtil();

    /**
     * 日志级别
     */
    public enum LogLevel {
        VERBOSE(Log.VERBOSE),
        DEBUG(Log.DEBUG),
        INFO(Log.INFO),
        WARN(Log.WARN),
        ERROR(Log.ERROR),
        ASSERT(Log.ASSERT),
        CLOSE(Log.ASSERT + 1);
        int value;

        LogLevel(int value) {
            this.value = value;
        }
    }


    /**
     * 私有构造
     */
    private LogUtil() {
    }

    /**
     * 获取单例
     *
     * @return 单例对象
     */
    public static LogUtil getInstance() {
        return instance;
    }

    /**
     * 日志组件初始化
     *
     * @param appCtx   application 上下文,获取包名等信息
     * @param isWriter 是否保存文件
     * @param level    日志级别
     */
    public void initialize(Context appCtx, boolean isWriter, LogLevel level) {
        context = appCtx;
        currentLevel = level;
        if (level == LogLevel.CLOSE) {
            isWriter = false;
            return;
        }
        instance.isWriter = isWriter;
        if (!instance.isWriter) {//不保存日志到文件
            return;
        }
        String logFoldPath = appCtx.getExternalCacheDir().getAbsolutePath() + "/../log/";
        pkgName = appCtx.getPackageName();
        File logFold = new File(logFoldPath);
        boolean flag = false;
        //若不存在文件夹,则创建
        if (!(flag = logFold.exists()))
            flag = logFold.mkdirs();
        //新建文件夹失败,退出
        if (!flag) {
            instance.isWriter = false;
            return;
        }
        logFilePath = logFoldPath + FILE_NAME_FORMAT.format(Calendar.getInstance().getTime()) + ".log";
        try {
            File logFile = new File(logFilePath);
            if (!(flag = logFile.exists()))
                flag = logFile.createNewFile();
            instance.isWriter = isWriter & flag;
            if (instance.isWriter) {
                fos = new FileOutputStream(logFile, true);
                osWriter = new OutputStreamWriter(fos);
                writer = new BufferedWriter(osWriter);
            }
        } catch (IOException e) {
            e.printStackTrace();
            instance.isWriter = false;
        }
    }

    /**
     * 写文件操作
     *
     * @param tag       日志标签
     * @param msg       日志内容
     * @param level     日志级别
     * @param throwable 异常捕获
     */
    private void write(String tag, String msg, String level, Throwable throwable) {
        String timeStamp = LOG_TIME_FORMAT.format(Calendar.getInstance().getTime());

        try {
            writer.write(String.format(LOG_FORMAT, timeStamp, android.os.Process.myPid(), android.os.Process.myTid(), pkgName, level, tag));
            writer.write(msg);
            writer.newLine();
            writer.flush();
            osWriter.flush();
            fos.flush();
            if (throwable != null)
                saveCrash(throwable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存异常
     *
     * @param throwable
     * @throws IOException
     */
    private void saveCrash(Throwable throwable) throws IOException {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(sWriter);
        throwable.printStackTrace(pWriter);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            cause.printStackTrace(pWriter);
            cause = cause.getCause();
        }
        pWriter.flush();
        pWriter.close();
        sWriter.flush();
        String crashInfo = sWriter.toString();
        sWriter.close();
        writer.write(crashInfo);
        writer.newLine();
        writer.flush();
        osWriter.flush();
        fos.flush();
    }

    public final void i(String tag, String msg) {
        if (currentLevel.value > LogLevel.INFO.value)
            return;
        if (isWriter) {
            write(tag, msg, "I", null);
        }
        Log.i(tag, msg);
    }

    public final void i(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > LogLevel.INFO.value)
            return;
        if (isWriter) {
            write(tag, msg, "I", throwable);
        }
        Log.i(tag, msg, throwable);
    }

    public final void v(String tag, String msg) {
        if (currentLevel.value > LogLevel.VERBOSE.value)
            return;
        if (isWriter) {
            write(tag, msg, "V", null);
        }
        Log.v(tag, msg);
    }

    public final void v(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > LogLevel.VERBOSE.value)
            return;
        if (isWriter) {
            write(tag, msg, "V", throwable);
        }
        Log.v(tag, msg, throwable);
    }

    public final void d(String tag, String msg) {
        if (currentLevel.value > LogLevel.DEBUG.value)
            return;
        if (isWriter) {
            write(tag, msg, "D", null);
        }
        Log.d(tag, msg);
    }

    public final void d(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > LogLevel.DEBUG.value)
            return;
        if (isWriter) {
            write(tag, msg, "D", throwable);
        }
        Log.d(tag, msg, throwable);
    }

    public final void e(String tag, String msg) {
        if (currentLevel.value > LogLevel.ERROR.value)
            return;
        if (isWriter) {
            write(tag, msg, "E", null);
        }
        Log.e(tag, msg);
    }

    public final void e(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > LogLevel.ERROR.value)
            return;
        if (isWriter) {
            write(tag, msg, "E", throwable);
        }
        Log.e(tag, msg, throwable);
    }

    public final void w(String tag, String msg) {
        if (currentLevel.value > LogLevel.WARN.value)
            return;
        if (isWriter) {
            write(tag, msg, "W", null);
        }
        Log.w(tag, msg);
    }

    public final void w(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > LogLevel.WARN.value)
            return;
        if (isWriter) {
            write(tag, msg, "W", throwable);
        }
        Log.w(tag, msg, throwable);
    }

    public final void i(Object target, String msg) {
        i(target.getClass().getSimpleName(), msg);
    }

    public final void i(Object target, String msg, Throwable throwable) {
        i(target.getClass().getSimpleName(), msg, throwable);
    }

    public final void v(Object target, String msg) {
        v(target.getClass().getSimpleName(), msg);
    }

    public final void v(Object target, String msg, Throwable throwable) {
        v(target.getClass().getSimpleName(), msg, throwable);
    }

    public final void d(Object target, String msg) {
        d(target.getClass().getSimpleName(), msg);
    }

    public final void d(Object target, String msg, Throwable throwable) {
        d(target.getClass().getSimpleName(), msg, throwable);
    }

    public final void e(Object target, String msg) {
        e(target.getClass().getSimpleName(), msg);
    }

    public final void e(Object target, String msg, Throwable throwable) {
        e(target.getClass().getSimpleName(), msg, throwable);
    }

    public final void w(Object target, String msg) {
        w(target.getClass().getSimpleName(), msg);
    }

    public final void w(Object target, String msg, Throwable throwable) {

        w(target.getClass().getSimpleName(), msg, throwable);
    }
}
