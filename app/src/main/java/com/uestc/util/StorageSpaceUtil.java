package com.uestc.util;

import android.os.Environment;

/**
 * @author Linsong Huang
 * @describe 检测安卓sd卡剩余空间
 * @date 2018/5/16
 * @email 1044782171@qq.com
 * @org UESTC
 */


public class StorageSpaceUtil {

    /**
     * 存储空间是否充足
     *
     * @param fileSize 要存储的文件的大小，以字节为单位
     */
    public static boolean storageSpaceEnough(long fileSize) {

        long SDFreeSpace = Environment.getExternalStorageDirectory().getFreeSpace();
        return SDFreeSpace > fileSize;
    }
}
