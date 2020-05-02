package com.paril.mlaclientapp.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.DecryptedPost;
import com.paril.mlaclientapp.model.EncryptedPost;
import com.paril.mlaclientapp.model.MLAUserGroups;
import com.paril.mlaclientapp.ui.adapter.MLAPostsAdapter;
import com.paril.mlaclientapp.util.AESUtil;
import com.paril.mlaclientapp.util.RSAUtil;
import com.paril.mlaclientapp.webservice.Api;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MLASocialNetworkFragment extends Fragment{

    View view;
    public String userId;
    KeyStore keyStore = null;
    PrivateKey privateKey;
    PublicKey publicKey;
    PublicKey unrestrictedPublicKey;
    Bundle bundle;
    boolean personalGroupCreated = true;

    // extract the extras that was sent from the previous intent
    void getExtra() {
        Intent previous = MLASocialNetworkFragment.this.getActivity().getIntent();
        bundle = previous.getExtras();
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

        //check if personal group exists and if not create it
        if(!personalGroupCreated) {
            Call<List<MLAUserGroups>> callPersonalGroup = Api.getClient().getGroupsByGroupName("_" + userId);
            callPersonalGroup.enqueue(new Callback<List<MLAUserGroups>>() {
                @Override
                public void onResponse(Call<List<MLAUserGroups>> call, Response<List<MLAUserGroups>> response) {
                    if (response.body().size() <= 0) {
                        makePersonalGroup();
                    } else {
                        personalGroupCreated = true;
                        System.out.println("MLALog: Personal Group Already exists");
                    }
                }

                @Override
                public void onFailure(Call<List<MLAUserGroups>> call, Throwable throwable) {
                    System.out.println("MLALog: error getting personal group");
                }
            });
        }

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

    public void sharePost(DecryptedPost d) {
        System.out.println("MLALog: sharePost() d= "+d);
        MLASharePostFragment mlaSharePostFragment = new MLASharePostFragment(d.getDecMessage(), d.getPostid());
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(((ViewGroup)getView().getParent()).getId(), mlaSharePostFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    ArrayList<DecryptedPost> decPostsList = new ArrayList<>();
    public void showPosts()
    {
        //Step 1: get all encrypted posts along with necessary keys for this user
        Call<ArrayList<EncryptedPost>> getEncPosts = Api.getClient().getPostsByUserId(userId);
        getEncPosts.enqueue(new Callback<ArrayList<EncryptedPost>>() {
            @Override
            public void onResponse(Call<ArrayList<EncryptedPost>> call, Response<ArrayList<EncryptedPost>> response) {
                decPostsList.clear();
                for(EncryptedPost p: response.body()) {
                    try {
                        System.out.println("MLALog: decrypting post: "+p);
                        //step 2: dec group key using private key
                        String decGroupKey = RSAUtil.decrypt(p.getEncryptedGroupKey(), privateKey);
                        System.out.println("MLALog: decGroupKey: "+decGroupKey);
                        //step 3: dec session key using decGroupkey
                        String decSessionKey = AESUtil.decryptMsg(p.getMessagekey(), decGroupKey);
                        System.out.println("MLALog: decSessionKey: "+decSessionKey);
                        //step 4: dec message using session key
                        final String decMessage = AESUtil.decryptMsg(p.getMessage(), decSessionKey);
                        final String groupName = (p.getGroupName().charAt(0)=='_')?"personal":p.getGroupName();
                        final String timestamp = p.getTimestamp().replace('T', ' ');
                        DecryptedPost d = new DecryptedPost(p.getUserName(), decMessage, groupName, p.getPostId(), timestamp);
                        decPostsList.add(d);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("MLALog: error decrypting encryptedPosts");
                    }
                }
                System.out.println("MLALog: all dec posts = " + decPostsList);
                //step 5: show posts on screen
                RecyclerView postsRV = (RecyclerView) view.findViewById(R.id.postsRecyclerView);
                MLAPostsAdapter adapter = new MLAPostsAdapter(getActivity(), decPostsList, MLASocialNetworkFragment.this);
                postsRV.setAdapter(adapter);
                postsRV.setLayoutManager(new LinearLayoutManager(getActivity()));
            }

            @Override
            public void onFailure(Call<ArrayList<EncryptedPost>> call, Throwable throwable) {
                System.out.println("MLALog: error getting encPost list");
            }
        });

    }

    void makePersonalGroup()
    {
        final String groupName = "_"+userId;

        /****************create new group************/
        Call<Void> callNewGroup = Api.getClient().createNewGroup(userId, groupName);
        callNewGroup.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                personalGroupCreated = true;
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
