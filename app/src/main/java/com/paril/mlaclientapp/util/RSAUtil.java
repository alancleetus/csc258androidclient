package com.paril.mlaclientapp.util;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import com.paril.mlaclientapp.webservice.Api;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*references:
    https://gist.github.com/balzss/a287b7ef1e7b6abcf069d522dcc53ffc
    https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec*/
public class RSAUtil {


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void createKeyPair(String userId) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeySpecException {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        kpg.initialize(new KeyGenParameterSpec.Builder(
                userId,
                KeyProperties.PURPOSE_DECRYPT
                        | KeyProperties.PURPOSE_ENCRYPT
                        | KeyProperties.PURPOSE_VERIFY
                        | KeyProperties.PURPOSE_SIGN)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .build());

        KeyPair kp = kpg.genKeyPair();

        PublicKey publicKey = kp.getPublic();

        PublicKey unrestrictedPublicKey =
                KeyFactory.getInstance(publicKey.getAlgorithm()).generatePublic(
                        new X509EncodedKeySpec(publicKey.getEncoded()));

        byte[] publicKeyBytes = Base64.encode(unrestrictedPublicKey.getEncoded(), Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING);
        String pubKeyStr = new String(publicKeyBytes);

        Call<Void> calladdPk = Api.getClient().addPublicKey(userId, pubKeyStr);
        calladdPk.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                System.out.println("MLALog: added pk to db");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                System.out.println("MLALog:error adding  pub key to db");
            }
        });

        System.out.println("MLALog:new pub key = " + pubKeyStr);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String encrypt(String plain, PublicKey unrestrictedPublicKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec spec = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);

        cipher.init(Cipher.ENCRYPT_MODE, unrestrictedPublicKey, spec);

        byte[] encryptedBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String encrypt(String plain, String unrestrictedPublicKeyString)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec spec = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);

        PublicKey unrestrictedPublicKey = stringToPublicKey(unrestrictedPublicKeyString);

        cipher.init(Cipher.ENCRYPT_MODE, unrestrictedPublicKey, spec);

        byte[] encryptedBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }

    public static String decrypt(String result, PrivateKey privateKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.decode(result, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE));
        return new String(decryptedBytes);
    }

    private static PublicKey stringToPublicKey(String unrestrictedPublicKey)
            throws InvalidKeySpecException, NoSuchAlgorithmException {

        byte[] keyBytes = Base64.decode(unrestrictedPublicKey, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
}
