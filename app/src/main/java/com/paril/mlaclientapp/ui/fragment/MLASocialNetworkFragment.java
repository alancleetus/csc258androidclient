package com.paril.mlaclientapp.ui.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;
import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.webservice.Api;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.security.auth.x500.X500Principal;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MLASocialNetworkFragment extends Fragment {

    View view;
    public String userId;
    KeyStore keyStore = null;

    // extract the extras that was sent from the previous intent
    void getExtra() {
        Intent previous = MLASocialNetworkFragment.this.getActivity().getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null) {
            userId = (String) bundle.get("userId");
        }
    }




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mla_social_network, container, false);
        getExtra();


        //check if keystore has public / private key if not generate key pair
        try{
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (!keyStore.containsAlias(userId))
                createKeyPair();

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(userId, null);
            PublicKey publicKey = keyStore.getCertificate(userId).getPublicKey();

            PublicKey unrestrictedPublicKey =
                    KeyFactory.getInstance(publicKey.getAlgorithm()).generatePublic(
                            new X509EncodedKeySpec(publicKey.getEncoded()));


            byte[] publicKeyBytes = Base64.encode(unrestrictedPublicKey.getEncoded(),Base64.NO_WRAP| Base64.URL_SAFE| Base64.NO_PADDING);
            String pubKeyStr = new String(publicKeyBytes);

            String encryptedMessage = encrypt("Test message..123.", stringToPublicKey(pubKeyStr));
            //String encryptedMessage = encrypt("Test message..312.", unrestrictedPublicKey);
            System.out.println("MLALog: enc="+encryptedMessage);
            String decryptedMessage = decrypt(encryptedMessage, privateKey);
            System.out.println("MLALog: dec="+decryptedMessage);


        }catch(Exception e)
        {
            e.printStackTrace();
        }

        //get posts
            //get groups by user id
                //for each group get all posts
        // for each posts user can see:
            // get group key using group id and version
                // decrypt group key using private key
                    //decrypt message using decGroupKey
                        //add to decPosts list
        //show all posts in decPosts list on user device

        return view;
    }

    private final static String CRYPTO_METHOD = "RSA";

    /*references:
        https://gist.github.com/balzss/a287b7ef1e7b6abcf069d522dcc53ffc
        https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec*/
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeySpecException {

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

        byte[] publicKeyBytes = Base64.encode(unrestrictedPublicKey.getEncoded(),Base64.NO_WRAP| Base64.URL_SAFE| Base64.NO_PADDING);
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

        System.out.println("MLALog:new pub key = "+pubKeyStr);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String encrypt(String plain, PublicKey pubk)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException,
            InvalidAlgorithmParameterException {

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec spec = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);

        cipher.init(Cipher.ENCRYPT_MODE, pubk,spec);

        byte[] encryptedBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }

    public static String decrypt(String result, PrivateKey privk)
            throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            BadPaddingException,
            IllegalBlockSizeException,
            InvalidKeyException {

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privk);
        byte[] decryptedBytes = cipher.doFinal(Base64.decode(result, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE));
        return new String(decryptedBytes);
    }

    private static PublicKey stringToPublicKey(String publicKeyString)
            throws InvalidKeySpecException,
            NoSuchAlgorithmException {

        byte[] keyBytes = Base64.decode(publicKeyString, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(CRYPTO_METHOD);
        return keyFactory.generatePublic(spec);
    }
}
