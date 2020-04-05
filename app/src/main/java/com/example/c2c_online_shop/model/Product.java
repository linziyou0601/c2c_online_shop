package com.example.c2c_online_shop.model;

import android.os.AsyncTask;

import com.example.c2c_online_shop.HttpURLConnectionUtil;
import com.example.c2c_online_shop.MainActivity;
import com.google.gson.Gson;

import java.util.ArrayList;

public class Product {
    private int id;
    private User seller;
    private String title;
    private String description;
    private int price;
    private ArrayList<String> imgs = new ArrayList<>();
    private int stockQty;
    Product(User seller, String title, String description, int price, int stockQty){
        this.seller = seller;
        this.title = title;
        this.description = description;
        this.price = price;
        this.stockQty = stockQty;
    }
    // getter
    public int getId() { return id; }
    public User getSeller(){ return seller; }
    public String getTitle(){ return title; }
    public String getDescription(){ return description; }
    public int getPrice(){ return price; }
    public ArrayList<String> getImgs(){ return imgs; }
    public int getStockQty(){ return stockQty; }

    // operator
    public void editProduct(String title, String description, int price, int stockQty){
        this.title = title;
        this.description = description;
        this.price = price;
        this.stockQty = stockQty;
        //call to mysql
        Gson gson = new Gson();
        new EditProductTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/update/product", gson.toJson(this));
    }
    public Order createOrder(int qty){
        Order order = new Order(this, MainActivity.user, qty);
        return order;

    }
    public void addStockQty(int qty){
        this.stockQty += qty;
        //call to mysql
        Gson gson = new Gson();
        new EditProductTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/update/product", gson.toJson(this));
    }
    public void minusStockQty(int qty){
        this.stockQty -= qty;
        //call to mysql
        Gson gson = new Gson();
        new EditProductTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/update/product", gson.toJson(this));
    }

    private static class EditProductTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
            return null;
        }
    }
}
