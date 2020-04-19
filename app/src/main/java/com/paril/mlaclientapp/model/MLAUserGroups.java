package com.paril.mlaclientapp.model;

import com.sinch.gson.annotations.SerializedName;

import java.io.Serializable;

public class MLAUserGroups implements Serializable {
    @SerializedName("userId")
    public int userId;
    @SerializedName("groupId")
    public int groupId;
    @SerializedName("groupName")
    public String groupName;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
