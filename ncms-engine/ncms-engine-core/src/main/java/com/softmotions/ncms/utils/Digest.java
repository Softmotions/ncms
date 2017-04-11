package com.softmotions.ncms.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class Digest {

    private Digest() {
    }

    public static String getMD5(String value) {
        return getDigest("MD5", value);
    }

    public static String getSHA(String value) {
        return getDigest("SHA", value);
    }

    public static String getSHA256(String value) {
        return getDigest("SHA-256", value);
    }

    public static String getDigest(String digest, String value) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(digest);
            messageDigest.reset();
            messageDigest.update(value.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return getHEX(messageDigest.digest());
    }

    public static String getHEX(byte[] byteArray) {
        StringBuilder buf = new StringBuilder(32);
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                buf.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                buf.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return buf.toString();
    }

}
