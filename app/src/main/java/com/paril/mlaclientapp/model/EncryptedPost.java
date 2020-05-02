package com.paril.mlaclientapp.model;

import java.io.Serializable;

public class EncryptedPost implements Serializable {
    String postId;
    String message;
    String messagekey;
    String encryptedGroupKey;
    String userName;
    String timestamp;
    String groupName;

    public EncryptedPost(String postId, String message, String messagekey, String encryptedGroupKey, String userName, String timestamp, String groupName) {
        this.postId = postId;
        this.message = message;
        this.messagekey = messagekey;
        this.encryptedGroupKey = encryptedGroupKey;
        this.userName = userName;
        this.timestamp = timestamp;
        this.groupName = groupName;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessagekey() {
        return messagekey;
    }

    public void setMessagekey(String messagekey) {
        this.messagekey = messagekey;
    }

    public String getEncryptedGroupKey() {
        return encryptedGroupKey;
    }

    public void setEncryptedGroupKey(String encryptedGroupKey) {
        this.encryptedGroupKey = encryptedGroupKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "EncryptedPost{" +
                "postId='" + postId + '\'' +
                ", message='" + message + '\'' +
                ", messageKey='" + messagekey + '\'' +
                ", encryptedGroupKey='" + encryptedGroupKey + '\'' +
                ", userName='" + userName + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}


