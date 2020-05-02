package com.paril.mlaclientapp.ui.adapter;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.MLAGroupMembers;
import com.paril.mlaclientapp.webservice.Api;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MLAGroupMembersAdapter extends RecyclerView.Adapter<MLAGroupMembersAdapter.ViewHolder> {

    ArrayList<MLAGroupMembers> membersList;
    Context mContext;

    public MLAGroupMembersAdapter(Context mContext, ArrayList<MLAGroupMembers> membersList) {
        this.membersList = membersList;
        this.mContext = mContext;
    }

    @Override
    public MLAGroupMembersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_group_member_info, parent,false);
        MLAGroupMembersAdapter.ViewHolder holder = new MLAGroupMembersAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MLAGroupMembersAdapter.ViewHolder holder, final int position) {
        holder.userNameTV.setText(membersList.get(position).getUserName());
        holder.groupNameTV.setText(membersList.get(position).getGroupName());
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<Void> deleteMember = Api.getClient().removeUserFromGroup(membersList.get(position).userId, membersList.get(position).groupId);
                deleteMember.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        membersList.remove(position);
                        notifyDataSetChanged();
                        notifyItemRemoved(position);
                        System.out.println("MLALog: Deleted member = "+membersList.get(position));

                        Call<Void> updateStatus = Api.getClient().updateGroupStatus(membersList.get(position).groupId, "false");
                        updateStatus.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                System.out.println("MLALog: update group status to false for group id= "+membersList.get(position).getGroupId());
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                System.out.println("MLALog: error update group status");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        System.out.println("MLALog: Delete member error");
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView groupNameTV;
        TextView userNameTV;
        Button deleteButton;
        ConstraintLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            groupNameTV = (TextView)itemView.findViewById(R.id.groupMember_GroupNameTV);
            userNameTV = (TextView)itemView.findViewById(R.id.groupMember_UserNameTV);
            deleteButton = (Button) itemView.findViewById(R.id.groupMemberDeleteButton);
            parentLayout = (ConstraintLayout) itemView.findViewById(R.id.parentLayoutForGroupMembers);
        }
    }
}
