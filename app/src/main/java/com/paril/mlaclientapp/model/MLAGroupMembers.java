package com.paril.mlaclientapp.model;

import java.io.Serializable;

public class MLAGroupMembers  implements Serializable {

    public String userId;
    public String groupId;
    public String userName;
    public String groupName;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "MLAGroupMembers{" +
                "userId='" + userId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", userName='" + userName + '\'' +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
