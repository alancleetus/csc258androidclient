package com.paril.mlaclientapp.model;

import java.io.Serializable;

public class MLAGroupStatus implements Serializable {
    public int groupId;
    public String status;

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
}
