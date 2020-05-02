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
import com.paril.mlaclientapp.model.JoinReqWithInfo;
import com.paril.mlaclientapp.model.MLAJoinRequest;
import com.paril.mlaclientapp.model.MLARegisterUsers;
import com.paril.mlaclientapp.model.MLAUserGroups;
import com.paril.mlaclientapp.ui.adapter.MLAJoinListAdapter;
import com.paril.mlaclientapp.webservice.Api;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MLAGroupRequestsFragment extends Fragment {

    View view;
    public String userId;

    ArrayList<JoinReqWithInfo> joinRequestsList;
    RecyclerView joinListRV;

    void getExtra() {
        Intent previous = MLAGroupRequestsFragment.this.getActivity().getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null) {
            userId = (String) bundle.get("userId");
            //System.out.println("MLALog:userID="+userId);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mla_group_requests, container, false);
        getExtra();

        joinListRV = (RecyclerView) view.findViewById(R.id.groupRequestsRV);
        joinRequestsList = new ArrayList<JoinReqWithInfo>();

        getAllRequestsForUser();

        return view;

    }

    public void getAllRequestsForUser() {

        //get all group requests that user can accept
        Call<ArrayList<JoinReqWithInfo>> getReqs = Api.getClient().getRequestByUserId(userId);
        getReqs.enqueue(new Callback<ArrayList<JoinReqWithInfo>>() {
            @Override
            public void onResponse(Call<ArrayList<JoinReqWithInfo>> call, Response<ArrayList<JoinReqWithInfo>> response) {
                joinRequestsList = response.body();
                System.out.println("MLALOG: join reqs"+joinRequestsList);
                try{
                    populateJoinListRV();
                }catch (Exception e){e.printStackTrace();}
            }

            @Override
            public void onFailure(Call<ArrayList<JoinReqWithInfo>> call, Throwable throwable) {
                System.out.println("MLALOG: unable to get join requests by user id");
            }
        });

    }

    void populateJoinListRV() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        PrivateKey privateKey;
        keyStore.load(null);
        privateKey = (PrivateKey) keyStore.getKey(userId, null);

        /*reference https://www.youtube.com/watch?v=Vyqz_-sJGFk*/
        MLAJoinListAdapter adapter = new MLAJoinListAdapter(this.getActivity(), userId, privateKey, joinRequestsList);
        joinListRV.setAdapter(adapter);
        joinListRV.setLayoutManager(new LinearLayoutManager(this.getActivity()));
    }


}
