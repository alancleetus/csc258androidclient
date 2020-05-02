package com.paril.mlaclientapp.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.DecryptedPost;
import com.paril.mlaclientapp.ui.fragment.MLASocialNetworkFragment;

import java.util.ArrayList;
import java.util.Collections;

public class MLAPostsAdapter extends RecyclerView.Adapter<MLAPostsAdapter.ViewHolder> {

    ArrayList<DecryptedPost> postsList;
    private Context mContext;
    private MLASocialNetworkFragment fragment;


    public MLAPostsAdapter(Context mContext, ArrayList<DecryptedPost> postsList, MLASocialNetworkFragment fragment) {
        Collections.reverse(postsList);
        this.postsList =postsList;
        this.mContext = mContext;
        this.fragment = fragment;
    }

    @Override
    public MLAPostsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mla_posts, parent, false);
        MLAPostsAdapter.ViewHolder holder = new MLAPostsAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MLAPostsAdapter.ViewHolder holder, final int position) {
        //System.out.println("MLALog: dec post = "+postsList.get(position).toString());
        holder.messageTV.setText(""+postsList.get(position).getDecMessage());
        holder.groupNameTV.setText(""+postsList.get(position).getGroupName());
        holder.userNameTV.setText(""+postsList.get(position).getUserName());
        holder.timestampTV.setText(""+postsList.get(position).getTimestamp());
        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DecryptedPost d = postsList.get(position);
                ((MLASocialNetworkFragment)fragment).sharePost(d);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTV;
        TextView groupNameTV;
        TextView userNameTV;
        TextView timestampTV;
        ImageButton shareButton;
        LinearLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            messageTV = (TextView) itemView.findViewById(R.id.messageTV);
            groupNameTV = (TextView) itemView.findViewById(R.id.groupNameTv);
            userNameTV = (TextView) itemView.findViewById(R.id.userNameTv);
            timestampTV = (TextView) itemView.findViewById(R.id.timestampTV);
            shareButton = (ImageButton) itemView.findViewById(R.id.shareButton);
            parentLayout = (LinearLayout) itemView.findViewById(R.id.parent_posts_layout);
        }
    }



}


