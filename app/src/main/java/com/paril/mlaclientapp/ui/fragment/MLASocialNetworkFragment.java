package com.paril.mlaclientapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;
import com.paril.mlaclientapp.R;

public class MLASocialNetworkFragment extends Fragment {

    View view;
    public int userId;

    // extract the extras that was sent from the previous intent
    void getExtra() {
        Intent previous = MLASocialNetworkFragment.this.getActivity().getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null) {
            userId = (int) bundle.get("userId");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mla_social_network, container, false);
        getExtra();

        //todo: check if keystore has public / private key if not generate key pair

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
}
