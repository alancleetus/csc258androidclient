package com.paril.mlaclientapp.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.DecryptedPost;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;

public class MLAPostsAdapter extends RecyclerView.Adapter<MLAPostsAdapter.ViewHolder> {

    ArrayList<DecryptedPost> postsList;
    private Context mContext;


    public MLAPostsAdapter(Context mContext, ArrayList<DecryptedPost> postsList) {
        Collections.sort(postsList);
        this.postsList =postsList;
        this.mContext = mContext;
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
        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //share post
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
        Button shareButton;
        LinearLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            messageTV = (TextView) itemView.findViewById(R.id.postLayoutMessageTV);
            groupNameTV = (TextView) itemView.findViewById(R.id.postGroupNameTV);
            userNameTV = (TextView) itemView.findViewById(R.id.postUserNameTV);
            shareButton = (Button) itemView.findViewById(R.id.postShareButton);
            parentLayout = (LinearLayout) itemView.findViewById(R.id.parent_posts_layout);
        }
    }
}


