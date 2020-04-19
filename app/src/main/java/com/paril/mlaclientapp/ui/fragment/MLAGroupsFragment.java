package com.paril.mlaclientapp.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.MLAUserGroups;
import com.paril.mlaclientapp.webservice.Api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MLAGroupsFragment extends Fragment {

    View view;
    public String userId;

    // extract the extras that was sent from the previous intent
    void getExtra() {
        Intent previous = MLAGroupsFragment.this.getActivity().getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null) {
            userId = (String) bundle.get("userId");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mla_groups, container, false);
        getExtra();

        Button createNewGroupButton = (Button) view.findViewById(R.id.createGroupButton);
        Button addMemberToGroup = (Button) view.findViewById(R.id.addMemberToGroupButton);
        Button searchGroupsButton = (Button) view.findViewById(R.id.searchGroupsButton);


        createNewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroupDialog();
            }
        });


        return view;
    }

    public  void createGroupDialog(){
        /*
        * creates new group, new group status, and adds new key
        * */
        final EditText nameEditText = new EditText(getActivity());
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Create New Group")
                .setMessage("Enter a group name:")
                .setView(nameEditText)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String groupName = String.valueOf(nameEditText.getText());
                        System.out.println("MLALog: new group name = "+groupName);

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
                                        final String newGroupId = ""+response.body().get(0).getGroupId();

                                        /***************Create status of group**************/
                                        Call<Void> callNewGroup = Api.getClient().addGroupStatus(newGroupId, "true");
                                        callNewGroup.enqueue(new Callback<Void>() {
                                            @Override
                                            public void onResponse(Call<Void> call, Response<Void> response) {

                                                /****************add new group key***************/
                                                //generate new aes key
                                                SecretKey secretKey = null;
                                                String stringKey = "";

                                                try {
                                                    secretKey = KeyGenerator.getInstance("AES").generateKey();
                                                } catch (Exception e) {
                                                    e.printStackTrace();

                                                    System.out.println("MLALog:Error generating key");
                                                }

                                                if (secretKey != null) {
                                                    stringKey = Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
                                                }

                                                //todo:encrypt group key using public key of user

                                                //add key to db
                                                Call<Void> callNewKey = Api.getClient().addGroupKey(Integer.parseInt(userId), Integer.parseInt(newGroupId), stringKey, 1);
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


                        //create key for group

                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

}
