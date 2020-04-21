package com.paril.mlaclientapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.MLAUserGroups;
import com.paril.mlaclientapp.util.AESUtil;
import com.paril.mlaclientapp.util.RSAUtil;
import com.paril.mlaclientapp.webservice.Api;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MLANewPostFragment extends Fragment {
    View view;
    public String userId;
    KeyStore keyStore = null;
    PrivateKey privateKey;
    PublicKey publicKey;
    PublicKey unrestrictedPublicKey;

    HashMap<String, String> groupMap = new HashMap<String, String>();
    // extract the extras that was sent from the previous intent
    void getExtra() {
        Intent previous = MLANewPostFragment.this.getActivity().getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null) {
            userId = (String) bundle.get("userId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_mla_new_post, container, false);
        getExtra();
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            privateKey = (PrivateKey) keyStore.getKey(userId, null);
            publicKey = keyStore.getCertificate(userId).getPublicKey();
            unrestrictedPublicKey =
                    KeyFactory.getInstance(publicKey.getAlgorithm()).generatePublic(
                            new X509EncodedKeySpec(publicKey.getEncoded()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        final EditText messageEditText = (EditText) view.findViewById(R.id.newPostMessageET);
        final Spinner groupsSpinner = (Spinner) view.findViewById(R.id.groupListSpinner);
        final Spinner postTypeSpinner = (Spinner) view.findViewById(R.id.postTypeSpinner);

        String[] postTypesArray = {"personal","group"};

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_spinner_dropdown_item, postTypesArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        postTypeSpinner.setAdapter(spinnerArrayAdapter);

        Button postButton = (Button) view.findViewById(R.id.addNewPostButton);
        Button cancelButton = (Button) view.findViewById(R.id.cancelNewPostButton);


        Call<ArrayList<MLAUserGroups>> callGetGroups = Api.getClient().getGroupsByUserId(userId);
        callGetGroups.enqueue(new Callback<ArrayList<MLAUserGroups>>() {
            @Override
            public void onResponse(Call<ArrayList<MLAUserGroups>> call, Response<ArrayList<MLAUserGroups>> response) {

                if(response.body().size()<=0)
                {
                    System.out.println("MLALog: response:"+response);
                    return;
                }

                ArrayList<String> groupNameArray = new ArrayList<String>();

                for(MLAUserGroups g : response.body()) {
                    groupMap.put(g.getGroupName(), "" + g.getGroupId());
                    groupNameArray.add(g.getGroupName());
                }

                ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>
                        (getActivity(), android.R.layout.simple_spinner_dropdown_item, groupNameArray);
                spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                groupsSpinner.setAdapter(spinnerArrayAdapter2);

            }

            @Override
            public void onFailure(Call<ArrayList<MLAUserGroups>> call, Throwable throwable) {
                System.out.println("MLALog: Unable to create group name spinner");
            }
        });


        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEditText.getText().toString();
                String groupId = groupMap.get(groupsSpinner.getSelectedItem().toString());
                String postType = postTypeSpinner.getSelectedItem().toString();
                makePost(message, groupId, postType );
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStackImmediate();
            }
        });

        return view;
    }


    public void makePost(String message, String groupId, String postType){
        /*
            Steps for making a post:

            Assume Api = makePostApi(encryptedMessage, encryptedSessionKey, digitalSignature,
                                    signer, postType, groupId, keyVersion)

            Step 1: Generate a symmetric session key
            Step 2: Encrypt message using session key     ------------------=> encryptedMessage
            Step 3: Get latest group key from db          ------------------=> keyVersion
            Step 4: Decrypt latest group key using private key in keystore
            Step 5: Encrypt session Key using decrypted group Key  -------=> encryptedSessionKey
            Step 6: Create Digital Signature                       -------=> digitalSignature

            signer = currentUserId

            Step 7: makeApiRequest(...);
         */

        if(postType.equalsIgnoreCase("personal"))
            groupId="_"+userId;

        if(groupId.equalsIgnoreCase("_"+userId))
            postType="personal";

        try {
            String sessionKey = AESUtil.generateKey();
            String encryptedMessage = AESUtil.encryptMsg(message, sessionKey);
            
        }catch (Exception e){e.printStackTrace();}

        getFragmentManager().popBackStackImmediate();
    }

}
