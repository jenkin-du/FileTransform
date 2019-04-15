package com.uestc.net.util;

import com.uestc.net.protocol.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/03/15
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MD5Util {

    public static String getFileMD5(File file) {

        int size = 1024;
        FileInputStream fis = null;
        DigestInputStream dis = null;

        try {
            // 创建MD5转换器和文件流
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            dis = new DigestInputStream(fis, messageDigest);

            byte[] buffer = new byte[size];
            // DigestInputStream实际上在流处理文件时就在内部就进行了一定的处理
            while (dis.read(buffer) > 0)
                ;

            // 通过DigestInputStream对象得到一个最终的MessageDigest对象。
            messageDigest = dis.getMessageDigest();

            // 通过messageDigest拿到结果，也是字节数组，包含16个元素
            byte[] array = messageDigest.digest();
            // 同样，把字节数组转换成字符串
            StringBuilder hex = new StringBuilder(array.length * 2);
            for (byte b : array) {
                if ((b & 0xFF) < 0x10) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 0xFF));
            }

            dis.close();
            fis.close();
            return hex.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            try {
                if (dis != null) {
                    dis.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        return null;
    }


    /**
     * 获取文件上传下载时临时保存文件的key
     *
     * @param msg 消息
     * @return key
     */
    public static String getTempFileKey(Message msg) {

        HashMap<String, String> obj = new HashMap<>();
        obj.put("fileName", msg.getFile().getFileName());
        String objString = obj.toString();

        return getMD5(objString);
    }

    private static String getMD5(String str) {

        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String getKey(HashMap<String, String> map) {
        return getMD5(map.toString());
    }

}
