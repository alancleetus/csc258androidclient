package com.paril.mlaclientapp.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.MLAUserGroups;
import com.paril.mlaclientapp.ui.adapter.MLAGroupsListAdapter;
import com.paril.mlaclientapp.util.RSAUtil;
import com.paril.mlaclientapp.webservice.Api;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MLASearchGroupsFragment extends Fragment {

    View view;
    public String userId;

    KeyStore keyStore = null;
    PrivateKey privateKey;
    PublicKey publicKey;
    PublicKey unrestrictedPublicKey;

    ArrayList<MLAUserGroups> groupsList = new ArrayList<MLAUserGroups>();
    RecyclerView groupListRV;

    void getExtra() {
        Intent previous = MLASearchGroupsFragment.this.getActivity().getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null) {
            userId = (String) bundle.get("userId");
            //System.out.println("MLALog:userID="+userId);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void getKeyPairs() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (!keyStore.containsAlias(userId))
                RSAUtil.createKeyPair(userId);

            privateKey = (PrivateKey) keyStore.getKey(userId, null);
            publicKey = keyStore.getCertificate(userId).getPublicKey();
            unrestrictedPublicKey =
                    KeyFactory.getInstance(publicKey.getAlgorithm()).generatePublic(
                            new X509EncodedKeySpec(publicKey.getEncoded()));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mla_search_groups, container, false);
        getExtra();

        getKeyPairs();
        groupListRV = (RecyclerView) view.findViewById(R.id.groupsListRV);

        //get all groups
        Call<ArrayList<MLAUserGroups>> callAllGroup = Api.getClient().getAllUserGroups();
        callAllGroup.enqueue(new Callback<ArrayList<MLAUserGroups>>() {
            @Override
            public void onResponse(Call<ArrayList<MLAUserGroups>> call, Response<ArrayList<MLAUserGroups>> response) {
                showList(response.body());
            }

            @Override
            public void onFailure(Call<ArrayList<MLAUserGroups>> call, Throwable throwable) {

                System.out.println("MLALog: failed to get user groups");
            }
        });

        return view;
    }

    public void showList(ArrayList<MLAUserGroups> list){

        for(MLAUserGroups g : list)
        {
            if(((String) g.getGroupName()).length()>0 && !(((String) g.getGroupName()).charAt(0)=='_')){
                groupsList = list;
            }
        }

        /*reference https://www.youtube.com/watch?v=Vyqz_-sJGFk*/
        MLAGroupsListAdapter adapter = new MLAGroupsListAdapter(this.getActivity(), userId, groupsList);
        groupListRV.setAdapter(adapter);
        groupListRV.setLayoutManager(new LinearLayoutManager(this.getActivity()));

    }
}
