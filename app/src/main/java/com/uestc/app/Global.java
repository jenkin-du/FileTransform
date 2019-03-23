package com.uestc.app;

import android.os.Environment;

/**
 * @author Linsong Huang, Siyuan Yi
 * @describe 一些公共常量和变量
 * @date 2017/11/7
 * @email 1044782171@qq.com
 * @org UESTC
 */


public class Global {

    /**
     * 是否要美颜
     */
    public static final boolean needBeauty = true;
    /**
     * 美颜等级
     */
    public static int beautyLevel = -1;

    /**
     * 是否显示广告
     */
    public static final boolean showAd = true;
    /**
     * 是否显示静态效果动态效果广告
     */
    public static final boolean showEffectAd = false;

    /***/
    public static final String softwareID = "0420171016001";


    /**
     * 口腔标识框 X
     */
    public static float mouthAreaLeftRatio = 0.175f;
    /**
     * 口腔标识框 Width
     */
    public static float mouthAreaWidthRatio = 0.65f;


    public static float mouthAreaTopRatio = 0.7f;
    public static float mouthAreaHeightRatio = 0.15f;


    public static float high_scale = 1.0f;
    public static boolean isHighScale = true;
//
//    /**
//     * 版权服务器url
//     */
//    public static String copyrightURL = "http://192.168.0.110:8080/CopyrightManagement/";
    /**
     * 是否将debug级别的log打印
     */
    public static final boolean DEBUG = true;

    /**
     * 项目名称
     */
    public static final String APP_NAME = "SmileTeeth";

    /**
     * 项目文件路径
     */
    public static String filePath = Environment.getExternalStorageDirectory().getPath() + "/" + APP_NAME;

    /**
     * 分享视频时导出的temp视频文件夹
     */
    public static final String shareTempFolderPath = filePath + "/" + "temp";
    /**
     * 本地服务器的IP (启动软件时读取配置文件获取，也可通过用户设置)
     */
    public static String IP = "";
    /**
     * 服务器端口号
     */
    public static int port = 20001;
    /**
     * 客户端设备名称
     */
    public static String deviceName;

    /**
     * @edit jenkin
     * 用户名
     */
    public static String username;

    /**
     * 全局文件名，存放所有患者的信息
     */
    public static String globalFileName = "patientList.st";
    /**
     * 登录文件名，存储上一次登录成功的用户信息
     **/
    public static String loginFileName = "login.st";

    /**
     * 角度信息
     */
    public static String angleInfoName = "angleInfo.st";

    /**
     * 视频角度
     */
    public static String videoAngleInfoName = "videoAngle.st";
    /**
     * 原始照片的命名
     **/
    public static String originalLeftImg = "original_left.stpng";
    public static String originalFrontImg = "original_front.stpng";
    public static String originalRightImg = "original_right.stpng";


    /**
     * 口腔原始照片的命名
     */
    public static String originalFrontCropImg = "original_crop_front.stpng";
    public static String originalLeftCropImg = "original_crop_left.stpng";
    public static String originalRightCropImg = "original_crop_right.stpng";


    /**
     * 效果照片的名称
     **/
    public static String effectsLeftImg = "effects_left.stpng";
    public static String effectsFrontImg = "effects_front.stpng";
    public static String effectsRightImg = "effects_right.stpng";
    /**
     * 效果照片美白后
     **/
    public static String effectsBeautyLeftImg = "effects_left_beauty.stpng";
    public static String effectsBeautyFrontImg = "effects_front_beauty.stpng";
    public static String effectsBeautyRightImg = "effects_right_beauty.stpng";


    public static String originalVideoYUV420p = "originalYUV420p.yuv";
    public static String originalVideoCropYUV420p = "originalCropYUV420p.yuv";
    public static String originalVideoH264 = "originalH264.h264";
    public static String originalVideoMp4 = "originalMp4.stmp4";
    public static String originalVideoCropH264 = "originalCropH264.h264";

    /**
     * 判断完整视频的h264是否编码完成；
     */

    public static String h264EndFile = "originalH264.end";

    /**
     * 文件状态；
     */
    public static String fileState = "fileState.st";


    /**
     * 第二步的完整视频贴上牙齿后的效果视频
     **/
    public static String effectsVideoYUV420p = "effectsYUV420p.yuv";
    /**
     * 文件，若存在则表示上次分享视频后未修改效果视频
     */
    public static String effectsVideoSharedNoChanged = "effectsVideoShared.nochange";

    //测试；
    public static String test = "test.h264";


    /**
     * 视频和图片文件夹名称
     **/
    public static String mediaFolder = "media";

    /**
     * 根据对称信息产生的对称牙齿图片的文件夹名称(还包含pb文件)
     */
    public static String toothFolder = "tooth";

    /**
     * 静态3张图片的pb文件所在文件夹
     */
    public static String imagePbFolder = "imagePb";

    /**
     * 口腔视频的pb文件所在文件夹
     */
    public static String videoPbFolder = "videoPb";

    /**
     * 贴牙齿信息文件
     */
    public static String toothInfoName = "tooth.st";

    /**
     * 贴牙齿线程结束标志文件名称
     */
    public static String addTeethFinishedFileName = "AddTeethFinished.st";
    /**
     * 贴视频牙齿线程结束标志文件名称
     */
    public static String addVideoTeethFinishedFileName = "AddVideoTeethFinished.st";
    /**
     * 原始视频最短拍摄时长设置，单位s
     */
    public static long minRecordDuration = 3000;

    /**
     * 原始视频最大拍摄时长设置，单位s
     */

    public static long maxRecordDuration = 10000;

    /**
     * 口腔区域亮度信息文件
     */
    public static String mouthLightInfoFileName = "mouthLightInfo.st";

    /**
     * end文件(判断文件是否已写到本地)
     */
    public static String trackInfoEnd = "trackInfoFromServer.end";
    public static String imagePbEnd = "imagePb.end";
    public static String videoPbEnd = "videoPb.end";

    /**
     * 选择牙齿的框的位置比正常位置大，因为包括了框的宽度
     */
    public static int pixelShift = 1;

    /**
     * 判断完整视频编码等几分钟后是否结束（不同设备时间不一样，release版本要7分半钟）
     */
    public static long encodeThreadWaitTime = 7 * 60 * 1000 + 40 * 1000;

    /**
     * 由服务器传过来的文件列表
     */
    public static String fileListName = "fileList.txt";

    /**
     * 临时的文件列表
     */
    public static String tempFileListName = "tempFileList.txt";

    /**
     * 当前步骤枚举
     * LinsongHuang：增加贴牙齿线程和贴视频牙齿线程状态
     */
    public enum Step {
        INFO, RECORD_VIDEO, CHOOSE_TEETH, IMG_TEETH_THREAD, EDIT_TEETH, VIDEO_TEETH_THREAD, EFFECTS
    }


    public enum ImageDirection {
        FRONT, LEFT, RIGHT
    }


    public enum RotationAxis {
        X, Y, Z
    }

    /**
     * 性别枚举
     */
    public enum Sex {
        MALE, FEMALE
    }


}
