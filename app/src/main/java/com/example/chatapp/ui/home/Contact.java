package com.example.chatapp.ui.home;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class Contact {
    private final int id;
    private final String email;
    private String name;
    private String pfpUrl;

    public Contact(final int id, final String email, String name, String pfpUrl){
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
    public int getId(){
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
        return String.format("%d %s %s", this.id, this.name, this.email);
    }
}
