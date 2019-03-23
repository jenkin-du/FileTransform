package com.uestc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;

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


    public static String getFileMd5(File file) {
        String value = null;
        FileInputStream fis = null;
        byte[] buffer = new byte[2048];
        int numRead;
        MessageDigest md5;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        try {
            if (fis != null) {
                md5 = MessageDigest.getInstance("MD5");
                while ((numRead = fis.read(buffer)) > 0) {
                    md5.update(buffer, 0, numRead);
                }
                BigInteger bi = new BigInteger(1, md5.digest());
                value = bi.toString(16);
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return value;
    }
}
