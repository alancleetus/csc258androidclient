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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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

public class MLAGroupsFragment extends Fragment {

    View view;
    public String userId;

    KeyStore keyStore = null;
    PrivateKey privateKey;
    PublicKey publicKey;
    PublicKey unrestrictedPublicKey;

    // extract the extras that was sent from the previous intent
    void getExtra() {
        Intent previous = MLAGroupsFragment.this.getActivity().getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null) {
            userId = (String) bundle.get("userId");
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mla_groups, container, false);
        getExtra();


        //check if keystore has public / private key if not generate key pair
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (!keyStore.containsAlias(userId))
                RSAUtil.createKeyPair(userId);

            privateKey = (PrivateKey) keyStore.getKey(userId, null);
            publicKey = keyStore.getCertificate(userId).getPublicKey();
            unrestrictedPublicKey =
                    KeyFactory.getInstance(publicKey.getAlgorithm()).generatePublic(
                            new X509EncodedKeySpec(publicKey.getEncoded()));
    /*        //generate new aes key
            String stringKey = AESUtil.generateKey();
            System.out.println("MLALog: strSecretKey=" + stringKey);
            //enc aes key using public key
            String encSecKey = RSAUtil.encrypt(stringKey, unrestrictedPublicKey);
            System.out.println("MLALog: encSecretKey=" + encSecKey);

            //decrypt aes key recieved from database and convert it back to aes key
            String decSecKey = RSAUtil.decrypt(encSecKey, privateKey);
            System.out.println("MLALog: decSecretKey=" + decSecKey);

            //aes encrypt and aes decrypt testing
            String encryptedMessage = AESUtil.encryptMsg("Test message..11234523.", stringKey);
            System.out.println("MLALog: encMsg=" + encryptedMessage);
            String decryptedMessage = AESUtil.decryptMsg(encryptedMessage, decSecKey);
            System.out.println("MLALog: decMsg=" + decryptedMessage);
*/

        } catch (Exception e) {
            e.printStackTrace();
        }

        Button createNewGroupButton = (Button) view.findViewById(R.id.createGroupButton);
        Button showGroupRequestsButton = (Button) view.findViewById(R.id.showGroupRequestsButton);
        Button searchGroupsButton = (Button) view.findViewById(R.id.searchGroupsButton);


        createNewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroupDialog();
            }
        });

        searchGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchGroupsFragment();
            }
        });

        showGroupRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupRequestsFragment();
            }
        });

        return view;
    }


    /**********************************************************************************************/
    /*********************************Create New Group*********************************************/
    /**********************************************************************************************/
    public void createGroupDialog() {
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
                        System.out.println("MLALog: new group name = " + groupName);

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
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }



    /**********************************************************************************************/
    /*********************************Search for Group*********************************************/
    /**********************************************************************************************/
    public void searchGroupsFragment()
    {
        MLASearchGroupsFragment mlaSearchGroupsFragment = new MLASearchGroupsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(((ViewGroup)getView().getParent()).getId(), mlaSearchGroupsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    /**********************************************************************************************/
    /*********************************Accept Group requests****************************************/
    /**********************************************************************************************/
    public void groupRequestsFragment()
    {
        MLAGroupRequestsFragment mlaGroupRequestsFragment = new MLAGroupRequestsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(((ViewGroup)getView().getParent()).getId(), mlaGroupRequestsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

}
