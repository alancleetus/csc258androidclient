package com.paril.mlaclientapp.model;

import java.io.Serializable;

public class MLAJoinRequest implements Serializable {
    public String userId;
    public String groupId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "MLAJoinRequst{" +
                "userId='" + userId + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }

}
