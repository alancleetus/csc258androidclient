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

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.DecryptedPost;
import com.paril.mlaclientapp.model.MLAGroupKeys;
import com.paril.mlaclientapp.model.MLAPosts;
import com.paril.mlaclientapp.model.MLARegisterUsers;
import com.paril.mlaclientapp.model.MLAUserGroups;
import com.paril.mlaclientapp.ui.adapter.MLAJoinListAdapter;
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

    ArrayList<DecryptedPost> decPostsList = new ArrayList<>();
    int ctr, totalPostCount;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void showPosts()
    {

        //get groups by user id
        //for each group get all posts
        // for each posts user can see:
        // get group key using group id and version
        // decrypt group key using private key
        //decrypt message using decGroupKey
        //add to decPosts list
        //show all posts in decPosts list on user device


        Call<ArrayList<MLAUserGroups>> callUserGroup = Api.getClient().getGroupsByUserId(userId);
        callUserGroup.enqueue(new Callback<ArrayList<MLAUserGroups>>() {
            @Override
            public void onResponse(Call<ArrayList<MLAUserGroups>> call, Response<ArrayList<MLAUserGroups>> response) {
                //ctr = 0;
                totalPostCount = 0;
                decPostsList.clear();
                for(MLAUserGroups g: response.body()){
                    Call<ArrayList<MLAPosts>> callPosts = Api.getClient().getPostsByGroup(""+g.getGroupId());
                    callPosts.enqueue(new Callback<ArrayList<MLAPosts>>() {
                        @Override
                        public void onResponse(Call<ArrayList<MLAPosts>> call, Response<ArrayList<MLAPosts>> response) {

                            totalPostCount+=response.body().size();
                            for(final MLAPosts p: response.body()){
                                System.out.println("MLALog: Posts = "+p);
                                //dec posts
                                //step 1: get group key
                                Call<ArrayList<MLAGroupKeys>> callGetKey = Api.getClient().getGroupKey(userId, ""+p.getGroupId(), ""+p.getKeyVersion());
                                callGetKey.enqueue(new Callback<ArrayList<MLAGroupKeys>>() {
                                    @Override
                                    public void onResponse(Call<ArrayList<MLAGroupKeys>> call, Response<ArrayList<MLAGroupKeys>> response) {

                                        try {
                                            //step 2: dec group key using private key
                                            String decGroupKey = RSAUtil.decrypt(response.body().get(0).getEncryptedGroupKey(), privateKey);
                                            //step 3: dec session key using decGroupkey
                                            String decSessionKey = AESUtil.decryptMsg(p.getMessagekey(), decGroupKey);
                                            //step 4: dec message using session key
                                            //System.out.println("MLALog: getMessage="+p.getMessage());
                                            //System.out.println("MLALog: decSessionKey="+decSessionKey);
                                            final String decMessage = AESUtil.decryptMsg(p.getMessage(), decSessionKey);

                                            //System.out.println("MLALog: decMessage="+decMessage);
                                            //step 5: add decMessage to list after getting user name and group name
                                            Call<ArrayList<MLAUserGroups>> callGetGroupName = Api.getClient().getGroupsByGroupId(""+p.getGroupId());
                                            callGetGroupName.enqueue(new Callback<ArrayList<MLAUserGroups>>() {
                                                @Override
                                                public void onResponse(Call<ArrayList<MLAUserGroups>> call, Response<ArrayList<MLAUserGroups>> response) {
                                                    try{
                                                        final String  groupName = response.body().get(0).getGroupName();

                                                        Call<List<MLARegisterUsers>> callgetRegister = Api.getClient().GetRegisterByUserId(userId);
                                                        callgetRegister.enqueue(new Callback<List<MLARegisterUsers>>() {
                                                            @Override
                                                            public void onResponse(Call<List<MLARegisterUsers>> call, Response<List<MLARegisterUsers>> response) {
                                                                try{
                                                                    String  userName = response.body().get(0).getUserName();

                                                                    DecryptedPost d = new DecryptedPost(""+p.getPostId(), decMessage, userId, ""+p.getGroupId(), userName, groupName);
                                                                    decPostsList.add(d);

                                                                    System.out.println("MLALog: dec posts = "+d);

                                                                    //todo: check if list worked and personal post can be decrypted properly

                                                                    System.out.println("MLALog: all dec posts = "+decPostsList);
                                                                    System.out.println("MLALog: totalPostCount = "+totalPostCount);
                                                                    RecyclerView postsRV = (RecyclerView) view.findViewById(R.id.postsRecyclerView);
                                                                    MLAPostsAdapter adapter = new MLAPostsAdapter(getActivity(), decPostsList);
                                                                    postsRV.setAdapter(adapter);
                                                                    postsRV.setLayoutManager(new LinearLayoutManager(getActivity()));



                                                                }catch (Exception e){
                                                                    e.printStackTrace();
                                                                    System.out.println("MLALog: unable to get userName from id");
                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(Call<List<MLARegisterUsers>> call, Throwable throwable) {

                                                                System.out.println("MLALog: unable to get user creds");
                                                            }
                                                        });

                                                    }catch (Exception e){
                                                        e.printStackTrace();
                                                        System.out.println("MLALog: unable to get group from id");
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<ArrayList<MLAUserGroups>> call, Throwable throwable) {
                                                    System.out.println("MLALog: unable to get group name from id");
                                                }
                                            });




                                        }catch (Exception e){e.printStackTrace();}
                                    }

                                    @Override
                                    public void onFailure(Call<ArrayList<MLAGroupKeys>> call, Throwable throwable) {
                                        System.out.println("MLALog: error getting group key for dec");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<ArrayList<MLAPosts>> call, Throwable throwable) {
                            System.out.println("MLALog: error getting posts");
                        }
                    });


                }
            }

            @Override
            public void onFailure(Call<ArrayList<MLAUserGroups>> call, Throwable throwable) {

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
