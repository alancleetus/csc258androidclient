package com.paril.mlaclientapp.model;

import java.io.Serializable;

public class MLAPosts implements Serializable {
    public int postId;
    public String message;
    public String messageKey;
    public String digitalSignature;
    public int signer;
    public String timestamp;
    public int keyVersion;
    public int groupId;
    public int originalPostId;
    public String postType;

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getDigitalSignature() {
        return digitalSignature;
    }

    public void setDigitalSignature(String digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    public int getSigner() {
        return signer;
    }

    public void setSigner(int signer) {
        this.signer = signer;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getKeyVersion() {
        return keyVersion;
    }

    public void setKeyVersion(int keyVersion) {
        this.keyVersion = keyVersion;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getOriginalPostId() {
        return originalPostId;
    }

    public void setOriginalPostId(int originalPostId) {
        this.originalPostId = originalPostId;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }
}
