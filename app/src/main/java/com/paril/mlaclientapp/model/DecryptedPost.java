package com.paril.mlaclientapp.model;

import android.support.annotation.NonNull;

public class DecryptedPost implements Comparable<DecryptedPost>{
    String userId;
    String userName;
    String decMessage;
    String groupId;
    String groupName;
    String postid;

    //todo:maybe add timestamp

    public DecryptedPost(String postId, String decMessage, String userId, String groupId, String userName, String groupName) {
        this.userId = userId;
        this.postid = postId;
        this.decMessage = decMessage;
        this.groupId = groupId;
        this.groupName = groupName;
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    @Override
    public String toString() {
        return "DecryptedPost{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", decMessage='" + decMessage + '\'' +
                ", groupId='" + groupId + '\'' +
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