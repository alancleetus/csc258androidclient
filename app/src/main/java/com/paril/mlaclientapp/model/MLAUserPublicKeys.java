package com.paril.mlaclientapp.model;

import java.io.Serializable;

public class MLAUserPublicKeys implements Serializable {
    public int userId;
    public String publicKey;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
