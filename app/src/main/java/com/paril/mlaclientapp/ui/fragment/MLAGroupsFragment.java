package com.paril.mlaclientapp.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.MLAUserGroups;
import com.paril.mlaclientapp.webservice.Api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MLAGroupsFragment extends Fragment {

    View view;
    public String userId;

    KeyStore keyStore = null;
    PrivateKey privateKey;
    PublicKey publicKey;
    PublicKey unrestrictedPublicKey;
    // extract the extras that was sent from the previous intent
    void getExtra() {
        Intent previous = MLAGroupsFragment.this.getActivity().getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null) {
            userId = (String) bundle.get("userId");
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mla_groups, container, false);
        getExtra();


        //check if keystore has public / private key if not generate key pair
        try{
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (!keyStore.containsAlias(userId))
                createKeyPair();

            privateKey = (PrivateKey) keyStore.getKey(userId, null);
            publicKey = keyStore.getCertificate(userId).getPublicKey();
            unrestrictedPublicKey =
                    KeyFactory.getInstance(publicKey.getAlgorithm()).generatePublic(
                            new X509EncodedKeySpec(publicKey.getEncoded()));

        }catch(Exception e)
        {
            e.printStackTrace();
        }

        Button createNewGroupButton = (Button) view.findViewById(R.id.createGroupButton);
        Button addMemberToGroup = (Button) view.findViewById(R.id.addMemberToGroupButton);
        Button searchGroupsButton = (Button) view.findViewById(R.id.searchGroupsButton);


        createNewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroupDialog();
            }
        });


        return view;
    }

    public  void createGroupDialog(){
        /*
        * creates new group, new group status, and adds new key
        * */
        final EditText nameEditText = new EditText(getActivity());
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Create New Group")
                .setMessage("Enter a group name:")
                .setView(nameEditText)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String groupName = String.valueOf(nameEditText.getText());
                        System.out.println("MLALog: new group name = "+groupName);

                        /****************create new group************/
                        Call<Void> callNewGroup = Api.getClient().createNewGroup(userId, groupName);
                        callNewGroup.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {

                                System.out.println("MLALog: New Group Created");

                                /***********get new group's id by name***********/
                                Call<List<MLAUserGroups>> callGroup = Api.getClient().getGroupsByGroupName(groupName);
                                callGroup.enqueue(new Callback<List<MLAUserGroups>>() {
                                    @Override
                                    public void onResponse(Call<List<MLAUserGroups>> call, Response<List<MLAUserGroups>> response) {
                                        final String newGroupId = ""+response.body().get(0).getGroupId();

                                        /***************Create status of group**************/
                                        Call<Void> callNewGroup = Api.getClient().addGroupStatus(newGroupId, "true");
                                        callNewGroup.enqueue(new Callback<Void>() {
                                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                            @Override
                                            public void onResponse(Call<Void> call, Response<Void> response) {

                                                /****************add new group key***************/
                                                //generate new aes key
                                                SecretKey secretKey = null;

                                                try {
                                                    secretKey = KeyGenerator.getInstance("AES").generateKey();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    System.out.println("MLALog:Error generating key");
                                                }

                                                String stringKey = "";
                                                if (secretKey != null) {
                                                    stringKey = Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
                                                }
                                                //todo:test encryption of aes key and dec of aes key
                                                String encKey = null;
                                                try {
                                                    encKey = encrypt(stringKey, unrestrictedPublicKey);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                //add key to db
                                                Call<Void> callNewKey = Api.getClient().addGroupKey(Integer.parseInt(userId), Integer.parseInt(newGroupId), encKey, 1);
                                                callNewKey.enqueue(new Callback<Void>() {

                                                    @Override
                                                    public void onResponse(Call<Void> call, Response<Void> response) {

                                                        System.out.println("MLALog:DONE CREATING NEW GROUP W/ STATUS & KEY");
                                                        //System.out.println("MLALog: "+response.toString());
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Void> call, Throwable throwable) {
                                                        System.out.println("MLALog:Error unable to add add key");

                                                    }
                                                });
                                            }


                                            @Override
                                            public void onFailure(Call<Void> call, Throwable throwable) {
                                                System.out.println("MLALog:Error unable to add new status");
                                            }
                                        });

                                    }

                                    @Override
                                    public void onFailure(Call<List<MLAUserGroups>> call, Throwable throwable) {
                                        System.out.println("MLALog:Error unable to get new group id");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable throwable) {

                                System.out.println("MLALog:Error unable to create new group");
                            }
                        });

                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
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
