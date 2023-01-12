package com.example.chatapp;

import android.annotation.SuppressLint;

import java.io.Serializable;

public class Contact implements Serializable {
    private final String id;
    private final String email;
    private String name;
    private String pfpUrl;

    public Contact(final String id, final String email, String name, String pfpUrl){
        this.id = id;
        this.email = email;
        this.name = name;
        this.pfpUrl = pfpUrl;
    }

    public String getName(){
        return this.name;
    }
    public String getPfpUrl(){
        return this.pfpUrl;
    }
    public String getEmail() {
        return email;
    }
    public String getId(){
        return this.id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPfpUrl(String pfpUrl) {
        this.pfpUrl = pfpUrl;
    }

    @SuppressLint("DefaultLocale")
    public String toString(){
        return String.format("%s %s %s", this.id, this.name, this.email);
    }
}
