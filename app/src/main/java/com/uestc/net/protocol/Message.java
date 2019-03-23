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

    //消息类型
    private int type;

    //是否附带文件
    private boolean hasFile = false;


    //传递的参数
    private HashMap<String, String> params = new HashMap<>();


    //消息类型
    public static class Type {

        public static final int REQUEST = 1;//请求
        public static final int RESPONSE = 2;//响应
    }

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
     * @return value
     */
    public String getParam(String key) {
        return params.get(key);
    }


    public HashMap<String, String> getParams() {
        return params;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public boolean isHasFile() {
        return hasFile;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }

    @Override
    public String toString() {
        return "Message{" +
                "action='" + action + '\'' +
                ", type=" + type +
                ", hasFile=" + hasFile +
                ", params=" + params +
                '}';
    }
}
