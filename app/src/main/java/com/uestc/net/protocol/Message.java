package com.uestc.net.protocol;

import java.util.HashMap;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/03/08
 *     desc   : 传送的消息
 *     version: 1.0
 * </pre>
 */
public class Message {


    //消息的动作
    private String action;


    //是否附带文件数据
    private boolean hasFileData = false;

    //文件
    private File file;


    //传递其他与业务相关的的参数
    private HashMap<String, String> params = new HashMap<>();


    /**
     * 结果类型
     */
    public static class Result {

        public static final String SUCCESS = "success";

        public static final String FILE_MD5_WRONG = "file md5 is wrong";
    }

    /**
     * 应答类型
     */
    public static class Ack {
        //文件准备就绪
        public static final String FILE_READY = "file is ready";
        //文件被加锁，不可写
        public static final String FILE_LOCKED = "file is locked";
        // 文件不存在
        public static final String FILE_NOT_EXIST = "file is not exist";
        //文件加密错误，重传
        public static final String FILE_ENCODE_WRONG = "file encode wrong";
    }

    /**
     * 添加参数
     */
    public void addParam(String key, String value) {
        params.put(key, value);
    }

    /**
     * 获取参数
     *
     * @return actionParam
     */
    public String getParam(String key) {
        return params.get(key);
    }


    public HashMap<String, String> getParams() {
        return params;
    }


    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isHasFileData() {
        return hasFileData;
    }

    public void setHasFileData(boolean hasFileData) {
        this.hasFileData = hasFileData;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }


    @Override
    public String toString() {
        return "Message{" +
                "action='" + action + '\'' +
                ", hasFileData=" + hasFileData +
                ", file=" + file +
                ", params=" + params +
                '}';
    }

    /**
     * 消息携带的文件
     */
    public static class File {

        public File() {

        }

        //文件名
        private String fileName;
        //文件路径
        private String filePath;
        //MD5
        private String md5;
        //文件长度
        private long fileLength;
        //已传输的文件偏移量
        private long fileOffset;
        //本次传输的文件大小（段长度）
        private long segmentLength;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        public long getFileLength() {
            return fileLength;
        }

        public void setFileLength(long fileLength) {
            this.fileLength = fileLength;
        }

        public long getFileOffset() {
            return fileOffset;
        }

        public void setFileOffset(long fileOffset) {
            this.fileOffset = fileOffset;
        }

        public long getSegmentLength() {
            return segmentLength;
        }

        public void setSegmentLength(long segmentLength) {
            this.segmentLength = segmentLength;
        }

        @Override
        public String toString() {
            return "File{" +
                    "fileName='" + fileName + '\'' +
                    ", filePath='" + filePath + '\'' +
                    ", md5='" + md5 + '\'' +
                    ", fileLength=" + fileLength +
                    ", fileOffset=" + fileOffset +
                    ", segmentLength=" + segmentLength +
                    '}';
        }
    }
}
