package com.paril.mlaclientapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.MLAGroupMembers;
import com.paril.mlaclientapp.ui.adapter.MLAGroupMembersAdapter;
import com.paril.mlaclientapp.ui.adapter.MLAJoinListAdapter;
import com.paril.mlaclientapp.webservice.Api;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MLAGroupMembersFragment extends Fragment {
    View view;
    public String userId;

    // extract the extras that was sent from the previous intent
    void getExtra() {
        Intent previous = MLAGroupMembersFragment.this.getActivity().getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null) {
            userId = (String) bundle.get("userId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_mla_group_members, container, false);

        getExtra();

        Call<ArrayList<MLAGroupMembers>> getGroupMembers = Api.getClient().getMembersInGroupByUserId(userId);
        getGroupMembers.enqueue(new Callback<ArrayList<MLAGroupMembers>>() {
            @Override
            public void onResponse(Call<ArrayList<MLAGroupMembers>> call, Response<ArrayList<MLAGroupMembers>> response) {

                ArrayList<MLAGroupMembers> memberList = new ArrayList<MLAGroupMembers>();
                for(MLAGroupMembers g:  response.body())
                {
                    if(g.groupName.charAt(0) !='_')
                        memberList.add(g);
                }
                System.out.println("MLALog: memberlist= "+memberList);
                //populate rv
                RecyclerView rv = (RecyclerView) view.findViewById(R.id.groupMemberRV);
                MLAGroupMembersAdapter adapter = new MLAGroupMembersAdapter(getActivity(), memberList);
                rv.setAdapter(adapter);
                rv.setLayoutManager(new LinearLayoutManager(getActivity()));
            }

            @Override
            public void onFailure(Call<ArrayList<MLAGroupMembers>> call, Throwable throwable) {

            }
        });

        return view;
    }
}
