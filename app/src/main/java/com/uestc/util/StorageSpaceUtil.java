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
     * 得到SD卡剩余空间,单位GB
     *
     * @return 字符串形式，大于1GB时以GB结尾，否则以MB或其他形式结尾
     */
    public double getSDCardSpace() {
        //获取SD卡可用剩余空间
        long SDFreeSpace = Environment.getExternalStorageDirectory().getFreeSpace();
        //格式化 byte
        return SDFreeSpace / 1024d / 1024d / 1024d; //9.52GB
    }

//    /**
//     * 判断剩余空间是否充足，若小于2GB则不让他使用
//     * (在新建档案、下载档案时判断)
//     *
//     * @param needAlert 是否需要弹出框提醒
//     */
//    public boolean isSpaceEnough(Context context, boolean needAlert) {
//        // 获取SD卡可用剩余空间
//        double spaceStr = getSDCardSpace();
//        //test
////        spaceStr = "1.45GB";
//        boolean isEnough;
//        //空间不足2GB
//        if (Double.compare(spaceStr, 2) < 0) {
//            isEnough = false;
//        } else {
//            isEnough = true;
//        }
//        //若空间不足，则弹出提示框提醒
//        if (!isEnough && needAlert) {
//            spaceNotEnoughAlert(String.valueOf(spaceStr).substring(0, 5), context);
//        }
//        return isEnough;
//    }

//    /**
//     * 弹框提醒空间不足
//     *
//     * @param spaceStr
//     * @param context
//     */
//    public void spaceNotEnoughAlert(String spaceStr, Context context) {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
//        dialog.setTitle(R.string.storage_space_alert_title);
//        dialog.setMessage(context.getString(R.string.storage_space_alert_message,spaceStr));
//        dialog.setCancelable(false);
//        dialog.setPositiveButton(context.getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//            }
//        });
//        dialog.show();
//    }

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
