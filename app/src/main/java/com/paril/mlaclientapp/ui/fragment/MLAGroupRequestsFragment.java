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

    ArrayList<MLAJoinRequest> joinRequestsList;
    RecyclerView joinListRV;

    HashMap<String, String>groupIdNameMap;
    HashMap<String, String>userNameMap;

    int counter,counter2;

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
        joinRequestsList = new ArrayList<MLAJoinRequest>();
        groupIdNameMap = new HashMap<String, String>();
        userNameMap = new HashMap<String, String>();

        getAllRequestsForUser();

        return view;

    }

    public void getAllRequestsForUser() {
         //get groups for userId
        Call<ArrayList<MLAUserGroups>> callAllGroupByID = Api.getClient().getGroupsByUserId(userId);
        callAllGroupByID.enqueue(new Callback<ArrayList<MLAUserGroups>>() {
            @Override
            public void onResponse(Call<ArrayList<MLAUserGroups>> call, Response<ArrayList<MLAUserGroups>> response) {

                //System.out.println("MLALog: group list size= "+response.body().size());
                counter = 0;
                counter2 =0;

                for(MLAUserGroups g : response.body()) {

                    String groupId = ""+g.getGroupId();
                    groupIdNameMap.put(groupId, g.getGroupName());

                    //get requests for each group
                    Call <ArrayList<MLAJoinRequest>> callAllReq = Api.getClient().getRequestByGroupId(groupId);
                    callAllReq.enqueue(new Callback<ArrayList<MLAJoinRequest>>() {

                        @Override
                        public void onResponse(Call<ArrayList<MLAJoinRequest>> call, Response<ArrayList<MLAJoinRequest>> response) {

                            // append request to joinlistRV
                            for (MLAJoinRequest req : response.body()){
                                joinRequestsList.add(req);
                                //System.out.println("MLALog: req= "+req.toString()+" list state="+joinRequestsList.size()+" ctr="+counter);
                            }

                            counter++;

                            Call<List<MLARegisterUsers>> callgetRegister = Api.getClient().GetRegisterByUserId(userId);
                            callgetRegister.enqueue(new Callback<List<MLARegisterUsers>>() {
                                @Override
                                public void onResponse(Call<List<MLARegisterUsers>> call, Response<List<MLARegisterUsers>> response) {
                                    String username =  response.body().get(0).getUserName();

                                    userNameMap.put(response.body().get(0).getUserId(), response.body().get(0).getUserName());
                                    counter2++;
                                    if(counter  >= groupIdNameMap.size() && counter2>=userNameMap.size()) {
                                        //System.out.println("MLALog: group list size2= " + joinRequestsList.size());
                                        try{
                                            populateJoinListRV();
                                        }catch (Exception e){e.printStackTrace();}
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<MLARegisterUsers>> call, Throwable throwable) {

                                }
                            });


                        }

                        @Override
                        public void onFailure(Call<ArrayList<MLAJoinRequest>> call, Throwable throwable) {

                            counter++;
                            System.out.println("MLALog: failed to get join requests");
                        }
                    });
                }


            }

            @Override
            public void onFailure(Call<ArrayList<MLAUserGroups>> call, Throwable throwable) {

                System.out.println("MLALog: failed to get user groups");
            }
        });


    }

    void populateJoinListRV() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        PrivateKey privateKey;
        keyStore.load(null);
        privateKey = (PrivateKey) keyStore.getKey(userId, null);

        System.out.println("MLALog: populate list="+joinRequestsList);
        /*reference https://www.youtube.com/watch?v=Vyqz_-sJGFk*/
        MLAJoinListAdapter adapter = new MLAJoinListAdapter(this.getActivity(), userId, privateKey, joinRequestsList, groupIdNameMap, userNameMap);
        joinListRV.setAdapter(adapter);
        joinListRV.setLayoutManager(new LinearLayoutManager(this.getActivity()));
    }


}
