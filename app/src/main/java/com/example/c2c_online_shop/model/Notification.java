package com.example.c2c_online_shop.model;

import android.os.AsyncTask;

import com.example.c2c_online_shop.HttpURLConnectionUtil;
import com.google.gson.Gson;

public class Notification {
    private int id;
    private int userId;
    private int orderId;
    private String type;
    private String title;
    private String description;
    private boolean operated;
    private boolean readed;
    public int getId(){ return id; }
    public int getUserId(){ return userId; }
    public int getOrderId(){ return orderId; }
    public String getType(){ return type; }
    public String getTitle(){ return title; }
    public String getDescription(){ return description; }
    public boolean getOperated(){ return operated; }
    public boolean getReaded(){ return readed; }
    public void setOperated(boolean operated){
        this.operated = operated;
        Gson gson = new Gson();
        new UpdateNotificationTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/update/notification", gson.toJson(this));
    }
    public void setReaded(boolean readed){
        this.readed = readed;
        Gson gson = new Gson();
        new UpdateNotificationTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/update/notification", gson.toJson(this));
    }
    private static class UpdateNotificationTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
            return null;
        }
    }
}
