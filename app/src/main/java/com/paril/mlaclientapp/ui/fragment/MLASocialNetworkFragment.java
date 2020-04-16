package com.paril.mlaclientapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.paril.mlaclientapp.R;

public class MLASocialNetworkFragment extends android.app.Fragment {

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


        return view;
    }
}
