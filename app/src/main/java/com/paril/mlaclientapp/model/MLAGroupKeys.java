package com.paril.mlaclientapp.model;

import java.io.Serializable;

public class MLAGroupKeys implements Serializable {
    public int userId;
    public String encryptedGroupKey;
    public int groupKeyVersion;
    public int groupId;
    public String status;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEncryptedGroupKey() {
        return encryptedGroupKey;
    }

    public void setEncryptedGroupKey(String encryptedGroupKey) {
        this.encryptedGroupKey = encryptedGroupKey;
    }

    public int getGroupKeyVersion() {
        return groupKeyVersion;
    }

    public void setGroupKeyVersion(int groupKeyVersion) {
        this.groupKeyVersion = groupKeyVersion;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MLAGroupKeys{" +
                "userId=" + userId +
                ", encryptedGroupKey='" + encryptedGroupKey + '\'' +
                ", groupKeyVersion=" + groupKeyVersion +
                ", groupId=" + groupId +
                ", status='" + status + '\'' +
                '}';
    }
}
