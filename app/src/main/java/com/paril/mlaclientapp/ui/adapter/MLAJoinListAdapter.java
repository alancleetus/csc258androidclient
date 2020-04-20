package com.paril.mlaclientapp.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.MLAJoinRequest;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;

public class MLAJoinListAdapter extends RecyclerView.Adapter<MLAJoinListAdapter.ViewHolder> {

    ArrayList<MLAJoinRequest> joinList;
    private Context mContext;
    HashMap<String, String> groupIdNameMap;
    PrivateKey privateKey;

    public MLAJoinListAdapter(Context mContext, PrivateKey privateKey, ArrayList<MLAJoinRequest> joinList, HashMap<String, String> groupMap) {
        this.joinList = joinList;
        this.mContext = mContext;
        this.groupIdNameMap = groupMap;
        this.privateKey = privateKey;

        System.out.println("MLALog: list= "+joinList+" map="+groupIdNameMap+" prk="+privateKey);
    }

    @Override
    public MLAJoinListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_group_request_rv, parent,false);
        MLAJoinListAdapter.ViewHolder holder = new MLAJoinListAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MLAJoinListAdapter.ViewHolder holder, final int position) {
        String userName= getUserName(joinList.get(position).getUserId());
        String groupName = groupIdNameMap.get(joinList.get(position).getGroupId());

        holder.groupNameTV.setText(userName);
        holder.userNameTV.setText(groupName);

        System.out.println("MLALog: username="+userName+", groupname="+groupName);
        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String groupId = ""+ joinList.get(position).getGroupId();
                final String userId = ""+ joinList.get(position).getUserId();
                Toast.makeText(mContext, "gid="+groupId+", uid="+userId, Toast.LENGTH_SHORT).show();

                //todo: add user to group
                //get enc aes key of group from db
                //dec aes key of user using private key
                //get public key of user from db
                //enc aes key using new users public key
                //add user to usergroups table with userid, groupid, groupname
                //add user to groupkeys table with userid, groupid, enc aes key
            }
        });
    }

    public String getUserName(String userId)
    {
        return "";
    }

    @Override
    public int getItemCount() {
        return joinList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView groupNameTV;
        TextView userNameTV;
        Button acceptButton;
        LinearLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            groupNameTV = (TextView)itemView.findViewById(R.id.groupNameTv_group_request_rv);
            userNameTV = (TextView) itemView.findViewById(R.id.userNameTv_group_request_rv);
            acceptButton = (Button) itemView.findViewById(R.id.joinRequestButton);
            parentLayout = (LinearLayout) itemView.findViewById(R.id.parent_layout_group_request_rv);
        }
    }
}
