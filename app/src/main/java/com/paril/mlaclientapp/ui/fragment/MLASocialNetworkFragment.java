package com.paril.mlaclientapp.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.util.RSAUtil;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

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
                RSAUtil.createKeyPair(userId);

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(userId, null);
            PublicKey publicKey = keyStore.getCertificate(userId).getPublicKey();
            PublicKey unrestrictedPublicKey =
                    KeyFactory.getInstance(publicKey.getAlgorithm()).generatePublic(
                            new X509EncodedKeySpec(publicKey.getEncoded()));

            byte[] publicKeyBytes = Base64.encode(unrestrictedPublicKey.getEncoded(), Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING);
            String pubKeyStr = new String(publicKeyBytes);

            /*
            String encryptedMessage = RSAUtil.encrypt("Test message..123.", unrestrictedPublicKey);
            String encryptedMessage2 = RSAUtil.encrypt("Test message..123.", pubKeyStr);
            System.out.println("MLALog: enc="+encryptedMessage);
            System.out.println("MLALog: enc2="+encryptedMessage2);
            String decryptedMessage = RSAUtil.decrypt(encryptedMessage, privateKey);
            System.out.println("MLALog: dec="+decryptedMessage);
            String decryptedMessage2 = RSAUtil.decrypt(encryptedMessage2, privateKey);
            System.out.println("MLALog: dec2="+decryptedMessage2);
            */
        }catch(Exception e)
        {
            e.printStackTrace();
        }


        showPosts();
        return view;
    }

    public void showPosts()
    {
        RecyclerView postsRV = (RecyclerView) view.findViewById(R.id.postsRecyclerView);

        //get groups by user id
        //for each group get all posts
        // for each posts user can see:
        // get group key using group id and version
        // decrypt group key using private key
        //decrypt message using decGroupKey
        //add to decPosts list
        //show all posts in decPosts list on user device

    }

    

}
