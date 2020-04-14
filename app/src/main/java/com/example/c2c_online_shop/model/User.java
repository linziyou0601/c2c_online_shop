package com.example.c2c_online_shop.model;

import com.example.c2c_online_shop.MainActivity;

import at.favre.lib.crypto.bcrypt.BCrypt;


public class User {
    private int id;
    private String account;
    private String password;
    private Profile profile;
    private Wallet wallet;
    public User(String account, String password, Profile profile, Wallet wallet){
        this.account = account;
        this.password = password;
        this.profile = profile;
        this.wallet = wallet;
    }
    // getter
    public int getId(){ return id; }
    public String getAccount(){ return account; }
    public String getPassword(){ return password; }
    public Profile getProfile(){ return profile; }
    public Wallet getWallet(){ return wallet; }
    public void setWallet(Wallet wallet){ this.wallet = wallet; }

    // operator
    public boolean verifyPassword(String pw){
        BCrypt.Result result = BCrypt.verifyer().verify(pw.toCharArray(), this.password);
        return result.verified;
    }
    public void login(){
        MainActivity.user = this;
    }
    public void logout(){
        MainActivity.unsetUser();
    }
}
