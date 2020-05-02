package com.paril.mlaclientapp.model;

import android.support.annotation.NonNull;

public class DecryptedPost implements Comparable<DecryptedPost>{
    String userName;
    String decMessage;
    String groupName;
    String postid;
    String timestamp;

    public DecryptedPost(String userName, String decMessage, String groupName, String postid, String timestamp) {
        this.userName = userName;
        this.decMessage = decMessage;
        this.groupName = groupName;
        this.postid = postid;
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDecMessage() {
        return decMessage;
    }

    public void setDecMessage(String decMessage) {
        this.decMessage = decMessage;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DecryptedPost{" +
                ", userName='" + userName + '\'' +
                ", decMessage='" + decMessage + '\'' +
                ", groupName='" + groupName + '\'' +
                ", postid='" + postid + '\'' +
                '}';
    }


    @Override
    public int compareTo(@NonNull DecryptedPost decryptedPost) {
        if (Integer.getInteger(getPostid()) == null || Integer.getInteger(decryptedPost.getPostid()) == null) {
            return 0;
        }
        return Integer.getInteger(getPostid()).compareTo(Integer.getInteger(decryptedPost.getPostid()));
    }
}