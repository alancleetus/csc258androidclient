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
import com.paril.mlaclientapp.model.MLAUserGroups;
import com.paril.mlaclientapp.webservice.Api;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MLAGroupsListAdapter extends RecyclerView.Adapter<MLAGroupsListAdapter.ViewHolder> {

    ArrayList<MLAUserGroups> groupsList;
    String userId;
    private Context mContext;

    public MLAGroupsListAdapter(Context mContext, String userId, ArrayList<MLAUserGroups> groupsList) {
        this.groupsList = groupsList;
        this.userId = userId;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_grouplist_rv, parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.groupNameTV.setText(groupsList.get(position).getGroupName());
        holder.joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String groupId = ""+ groupsList.get(position).getGroupId();
                Call<Void> callAllGroup = Api.getClient().addGroupJoinRequest(userId, groupId);
                callAllGroup.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Toast.makeText(mContext, "Request gid="+groupId+", uid="+userId, Toast.LENGTH_SHORT).show();
                        System.out.println("MLALog: Request = "+response);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable throwable) {
                        Toast.makeText(mContext, "Request failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView groupNameTV;
        Button joinButton;
        LinearLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            groupNameTV = (TextView)itemView.findViewById(R.id.groupNameTv);
            joinButton = (Button) itemView.findViewById(R.id.joinRequestButton);
            parentLayout = (LinearLayout) itemView.findViewById(R.id.parent_layout_grouplistRv);
        }
    }
}
