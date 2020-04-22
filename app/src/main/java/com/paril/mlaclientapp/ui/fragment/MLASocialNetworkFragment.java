package com.paril.mlaclientapp.ui.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.MLAUserGroups;
import com.paril.mlaclientapp.util.AESUtil;
import com.paril.mlaclientapp.util.RSAUtil;
import com.paril.mlaclientapp.webservice.Api;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MLASocialNetworkFragment extends Fragment {

    View view;
    public String userId;
    KeyStore keyStore = null;
    PrivateKey privateKey;
    PublicKey publicKey;
    PublicKey unrestrictedPublicKey;

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

            privateKey = (PrivateKey) keyStore.getKey(userId, null);
            publicKey = keyStore.getCertificate(userId).getPublicKey();
            unrestrictedPublicKey =
                    KeyFactory.getInstance(publicKey.getAlgorithm()).generatePublic(
                            new X509EncodedKeySpec(publicKey.getEncoded()));

            byte[] publicKeyBytes = Base64.encode(unrestrictedPublicKey.getEncoded(), Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING);



         }catch(Exception e)
        {
            e.printStackTrace();
        }


        Call<List<MLAUserGroups>> callPersonalGroup = Api.getClient().getGroupsByGroupName("_"+userId);
        callPersonalGroup.enqueue(new Callback<List<MLAUserGroups>>() {
            @Override
            public void onResponse(Call<List<MLAUserGroups>> call, Response<List<MLAUserGroups>> response) {
                if(response.body().size()<=0){
                    makePersonalGroup();
                }else
                {
                    System.out.println("MLALog: Personal Group Already exists");
                }
            }

            @Override
            public void onFailure(Call<List<MLAUserGroups>> call, Throwable throwable) {
                System.out.println("MLALog: error getting personal group");
            }
        });

        FloatingActionButton makePostButton = (FloatingActionButton) view.findViewById(R.id.addPostButton);
        makePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postMessageDialog();
            }
        });

        showPosts();

        return view;
    }

    void postMessageDialog(){
        MLANewPostFragment mlaNewPostFragment = new MLANewPostFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(((ViewGroup)getView().getParent()).getId(), mlaNewPostFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

    void makePersonalGroup()
    {
        final String groupName = "_"+userId;

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
                        final String newGroupId = "" + response.body().get(0).getGroupId();

                        /***************Create status of group**************/
                        Call<Void> callNewGroup = Api.getClient().addGroupStatus(newGroupId, "true");
                        callNewGroup.enqueue(new Callback<Void>() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {

                                /****************add new group key***************/
                                String stringKey = AESUtil.generateKey();

                                //encrypt aes key with public key
                                String encSecKey = null;
                                try {
                                    encSecKey = RSAUtil.encrypt(stringKey, unrestrictedPublicKey);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Call<Void> callNewKey = Api.getClient().addGroupKey(userId, newGroupId, encSecKey, 1);
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

}
