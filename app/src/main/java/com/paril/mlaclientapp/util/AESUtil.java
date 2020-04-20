package com.paril.mlaclientapp.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/*aes enc and dec from https://stackoverflow.com/questions/40123319/easy-way-to-encrypt-decrypt-string-in-android*/
public class AESUtil {

    public static String generateKey(){
        //generate new aes key
        SecretKey secretKey = null;
        try {
            secretKey = KeyGenerator.getInstance("AES").generateKey();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("MLALog:Error generating key");
        }
        String stringKey = Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
        return stringKey;
    }
    public static String encryptMsg(String message, SecretKey secret)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, UnsupportedEncodingException {
        /* Encrypt the message. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        return Base64.encodeToString(cipherText, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }

    public static String decryptMsg(String cipherText, SecretKey secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        /* Decrypt the message, given derived encContentValues and initialization vector. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        String decryptString = new String(cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE)), "UTF-8");
        return decryptString;
    }

    public static String encryptMsg(String message, String secret)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, UnsupportedEncodingException {
        /* Encrypt the message. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        byte[] keyBytes = Base64.decode(secret, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
        SecretKey secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        return Base64.encodeToString(cipherText, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }

    public static String decryptMsg(String cipherText, String secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        /* Decrypt the message, given derived encContentValues and initialization vector. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        byte[] keyBytes = Base64.decode(secret, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
        SecretKey secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");

        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        String decryptString = new String(cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE)), "UTF-8");
        return decryptString;
    }

}
