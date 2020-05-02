package com.paril.mlaclientapp.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.JoinReqWithInfo;
import com.paril.mlaclientapp.model.MLAGroupKeys;
import com.paril.mlaclientapp.model.MLAUserPublicKeys;
import com.paril.mlaclientapp.util.RSAUtil;
import com.paril.mlaclientapp.webservice.Api;

import java.security.PrivateKey;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MLAJoinListAdapter extends RecyclerView.Adapter<MLAJoinListAdapter.ViewHolder> {

    ArrayList<JoinReqWithInfo> joinList;
    private Context mContext;

    PrivateKey privateKey;
    String currentUserId;

    public MLAJoinListAdapter(Context mContext, String currentUserId, PrivateKey privateKey, ArrayList<JoinReqWithInfo> joinList) {
        this.joinList = joinList;
        this.mContext = mContext;
        this.privateKey = privateKey;
        this.currentUserId = currentUserId;
    }

    @Override
    public MLAJoinListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_group_request_rv, parent,false);
        MLAJoinListAdapter.ViewHolder holder = new MLAJoinListAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MLAJoinListAdapter.ViewHolder holder, final int position) {
        String userName = joinList.get(position).getUserName();
        final String groupName = joinList.get(position).getGroupName();
        holder.groupNameTV.setText("Group name:"+groupName);
        holder.userNameTV.setText("User name:"+userName);

        System.out.println("MLALog: username="+userName+", groupname="+groupName);

        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String groupId = ""+ joinList.get(position).getGroupId();
                final String reqUserId = ""+ joinList.get(position).getUserId();
                Toast.makeText(mContext, "gid="+groupId+", uid="+reqUserId, Toast.LENGTH_SHORT).show();

                //todo: add user to group
                //get enc aes key of group from db
                Call<ArrayList<MLAGroupKeys>> callGetKey = Api.getClient().getLatestKey(currentUserId, groupId);
                callGetKey.enqueue(new Callback<ArrayList<MLAGroupKeys>>() {
                    @Override
                    public void onResponse(Call<ArrayList<MLAGroupKeys>> call, Response<ArrayList<MLAGroupKeys>> response) {
                        final MLAGroupKeys key = response.body().get(0);

                        //get public key of user from db
                        Call<ArrayList<MLAUserPublicKeys>> callGetPk = Api.getClient().getPublicKeyById(reqUserId);
                        callGetPk.enqueue(new Callback<ArrayList<MLAUserPublicKeys>>() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onResponse(Call<ArrayList<MLAUserPublicKeys>> call, Response<ArrayList<MLAUserPublicKeys>> response) {
                                final String pubKey = response.body().get(0).getPublicKey();

                                //add user to usergroups table with userid, groupid, groupname
                                Call<Void> callAddtoGroup = Api.getClient().addMemberToGroup(reqUserId, groupId, groupName);
                                callAddtoGroup.enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {

                                        String encGroupKey = "";
                                        try {
                                            //dec aes key of user using private key
                                            String decAesGroupKey = RSAUtil.decrypt(key.getEncryptedGroupKey(), privateKey);
                                            //enc aes key using new users public key
                                            encGroupKey = RSAUtil.encrypt(decAesGroupKey,pubKey);
                                        }catch(Exception e) {e.printStackTrace();}

                                        //add user to groupkeys table with userid, groupid, enc aes key
                                        Call<Void> callAddGroupKey = Api.getClient().addGroupKey(reqUserId, groupId, encGroupKey, key.getGroupKeyVersion());
                                        callAddGroupKey.enqueue(new Callback<Void>() {
                                            @Override
                                            public void onResponse(Call<Void> call, Response<Void> response) {
                                                //remove request from db
                                                Call<Void> callremoveReq = Api.getClient().removeGroupJoinRequest(reqUserId, groupId);
                                                callremoveReq.enqueue(new Callback<Void>() {
                                                    @Override
                                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                                        System.out.println("MLALog: Success in adding member");
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Void> call, Throwable throwable) {
                                                        System.out.println("MLALog: Failed in adding member b/c remove req");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(Call<Void> call, Throwable throwable) {

                                                System.out.println("MLALog: Failed in adding group key");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable throwable) {

                                        System.out.println("MLALog: Failed in adding member to usergroups table");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<ArrayList<MLAUserPublicKeys>> call, Throwable throwable) {

                                System.out.println("MLALog: Failed in getting user public key");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ArrayList<MLAGroupKeys>> call, Throwable throwable) {

                        System.out.println("MLALog: Failed in getting group aes key");
                    }
                });

                removeItem(position);

            }
        });

        holder.denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //remove request and remove from list

                removeItem(position);
            }
        });
    }

    void removeItem(int position){
        this.joinList.remove(position);
        notifyDataSetChanged();
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return joinList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView groupNameTV;
        TextView userNameTV;
        Button acceptButton, denyButton;
        LinearLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            groupNameTV = (TextView)itemView.findViewById(R.id.groupNameTv_group_request_rv);
            userNameTV = (TextView) itemView.findViewById(R.id.userNameTv_group_request_rv);
            acceptButton = (Button) itemView.findViewById(R.id.joinRequestButton);
            denyButton = (Button) itemView.findViewById(R.id.denyJoinRequestButton);
            parentLayout = (LinearLayout) itemView.findViewById(R.id.parent_layout_group_request_rv);
        }
    }
}
