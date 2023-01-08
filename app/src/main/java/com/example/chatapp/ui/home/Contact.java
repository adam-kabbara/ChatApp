package com.example.chatapp.ui.home;

public class Contact {
    private final int id;
    private final String email;
    private String name;
    private String pfpDir;

    public Contact(final int id, final String email, String name, String pfpDir){
        this.id = id;
        this.email = email;
        this.name = name;
        this.pfpDir = pfpDir;
    }
    public Contact(final int id, final String email, String name){
        this(id, email, name, "");
    }

    public String getName(){
        return this.name;
    }
    public String getPfpDir(){
        return this.pfpDir;
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
    public void setPfpDir(String pfpDir) {
        this.pfpDir = pfpDir;
    }
}
