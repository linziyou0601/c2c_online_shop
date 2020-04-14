package com.example.c2c_online_shop.model;

import android.os.AsyncTask;
import android.widget.Toast;

import com.example.c2c_online_shop.Blockchain.Blockchain;
import com.example.c2c_online_shop.HttpURLConnectionUtil;
import com.example.c2c_online_shop.MainActivity;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Order {
    private int id;
    private Product product;
    private User buyer;
    private int quantity;
    private int status;

    Order(Product product, User buyer, int quantity){
        this.product = product;
        this.buyer = buyer;
        this.quantity = quantity;
        this.status = 0;
    }
    // getter
    public int getId(){ return id; }
    public Product getProduct(){ return product; }
    public User getBuyer(){ return buyer; }
    public int getQuantity(){ return quantity; }
    public int getAmount(){ return product.getPrice() * quantity; }
    public int getStatus(){ return status; }

    // operator
    public void updateStatus(){
        this.status++;
        switch(this.status) {
            case 0:
                break;
            case 1:
                this.notifyUnshipOrder();
                break;
            case 2:
                break;
            case 3:
                Blockchain.getThirdParty().getWallet().makePayment(this);
                Blockchain.addUnveriedTransaction(MainActivity.temp_tx);
                break;
        }
        Gson gson = new Gson();
        new UpdateOrderTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/update/order",gson.toJson(this));
    }
    public void confirmOrder(){
        new ConfirmOrderTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/product?id="+this.product.getId());
    }
    public void cancelOrder(){
        Gson gson = new Gson();
        new CancelOrderTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/product?id="+this.product.getId());
    }
    public void shippedOrder(){
        updateStatus();
    }
    public void completedOrder(){
        updateStatus();
    }
    public void sendOrderDetail(){}
    public void notifyUnpaidOrder(){
        String detail = this.getProduct().getTitle() + " " + this.getProduct().getPrice() + " × " + this.getQuantity() + " = " + this.getAmount();

        Gson gson = new Gson();
        Map<String, String> data = new HashMap<>();
        data.put("userId", Integer.toString(buyer.getId()));
        data.put("type","Notify Unpaid Order");
        data.put("title", "Unpaid Order " + this.getProduct().getTitle());
        data.put("description", detail);
        new SendNotifyTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/create/notification", gson.toJson(data));
    }
    public void notifyUnshipOrder(){
        String detail = this.getProduct().getTitle() + " " + this.getProduct().getPrice() + " × " + this.getQuantity() + " = " + this.getAmount();

        Gson gson = new Gson();
        Map<String, String> data = new HashMap<>();
        data.put("userId", Integer.toString(this.getProduct().getSeller().getId()));
        data.put("type","Notify Unship Order");
        data.put("title", "Unship Order " + this.getProduct().getTitle());
        data.put("description", detail);
        new SendNotifyTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/create/notification", gson.toJson(data));
    }

    // AsyncTack
    private static class UpdateOrderTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
            return null;
        }
    }

    private class ConfirmOrderTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.getDataHttpUriConnection(params[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            processConfirmOrder(result);
        }
    }
    public void processConfirmOrder(String json){
        Gson gson = new Gson();
        MainActivity.temp_pd = gson.fromJson(json, Product.class);

        if(MainActivity.temp_pd.getStockQty() >= quantity){
            Toast.makeText(MainActivity.temp_context, "Order is processing...", Toast.LENGTH_SHORT).show();
            MainActivity.temp_pd.minusStockQty(quantity);
            new CreateOrderTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/create/order", gson.toJson(this));
            this.notifyUnpaidOrder();
        }
        else{
            Toast.makeText(MainActivity.temp_context, "Failed! Product stock quantity insufficient...", Toast.LENGTH_SHORT).show();
        }
    }
    private class CreateOrderTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
            return null;
        }
    }


    private class CancelOrderTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.getDataHttpUriConnection(params[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            processCancelOrder(result);
        }
    }
    public void processCancelOrder(String json){
        Gson gson = new Gson();
        MainActivity.temp_pd = gson.fromJson(json, Product.class);
        MainActivity.temp_pd.addStockQty(quantity);
        System.out.println(gson.toJson(this));
        new DeleteOrderTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/delete/order", gson.toJson(this));
    }
    private class DeleteOrderTask extends AsyncTask<String, Void, Void> {
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
