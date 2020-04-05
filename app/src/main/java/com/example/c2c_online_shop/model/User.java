package com.example.c2c_online_shop.model;

import android.os.AsyncTask;

import com.example.c2c_online_shop.HttpURLConnectionUtil;
import com.example.c2c_online_shop.MainActivity;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

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
    public void createProduct(String title, String description, int price, int stockQty){
        Product product = new Product(this, title, description, price, stockQty);
        Gson gson = new Gson();
        new CreateProductTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/create/product", gson.toJson(product));
    }
    public void notifyUnpaidOrder(Order order){
        String detail = order.getProduct().getTitle() + " " + order.getProduct().getPrice() + " × " + order.getQuantity() + " = " + order.getAmount();

        Gson gson = new Gson();
        Map<String, String> data = new HashMap<>();
        data.put("userId", Integer.toString(id));
        data.put("type","Notify Unpaid Order");
        data.put("title", "Unpaid Order " + order.getProduct().getTitle());
        data.put("description", detail);
        new SendNotifyTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/create/notification", gson.toJson(data));
    }
    public void notifyUnshipOrder(Order order){
        String detail = order.getProduct().getTitle() + " " + order.getProduct().getPrice() + " × " + order.getQuantity() + " = " + order.getAmount();

        Gson gson = new Gson();
        Map<String, String> data = new HashMap<>();
        data.put("userId", Integer.toString(id));
        data.put("type","Notify Unship Order");
        data.put("title", "Unship Order " + order.getProduct().getTitle());
        data.put("description", detail);
        new SendNotifyTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/create/notification", gson.toJson(data));
    }

    private static class CreateProductTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
            return null;
        }
    }

    private static class SendNotifyTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
            return null;
        }
    }
}
