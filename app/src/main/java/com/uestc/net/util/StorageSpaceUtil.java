package com.uestc.net.util;

import android.os.Environment;


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
