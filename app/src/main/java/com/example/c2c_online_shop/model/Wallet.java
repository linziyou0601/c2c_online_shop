package com.example.c2c_online_shop.model;

import android.os.AsyncTask;

import com.example.c2c_online_shop.Blockchain.Block;
import com.example.c2c_online_shop.Blockchain.Blockchain;
import com.example.c2c_online_shop.Blockchain.Deposit;
import com.example.c2c_online_shop.Blockchain.Payment;
import com.example.c2c_online_shop.Blockchain.Transaction;
import com.example.c2c_online_shop.Blockchain.TransactionInput;
import com.example.c2c_online_shop.Blockchain.TransactionOutput;
import com.example.c2c_online_shop.Blockchain.Withdraw;
import com.example.c2c_online_shop.HttpURLConnectionUtil;
import com.example.c2c_online_shop.MainActivity;
import com.google.gson.Gson;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.example.c2c_online_shop.KeyPairUtil.generateKeyPair;
import static com.example.c2c_online_shop.KeyPairUtil.keyToString;
import static com.example.c2c_online_shop.KeyPairUtil.stringToPrivateKey;
import static com.example.c2c_online_shop.KeyPairUtil.stringToPublicKey;
import static com.example.c2c_online_shop.StringUtil.generateOtp;

public class Wallet {
    private String publicKey;
    private String privateKey;
    private String otp;
    private long otpExpired;
    private int otpRetry;
    public Wallet(){
        Map<String, Key> result = generateKeyPair();
        this.privateKey = keyToString(result.get("privateKey"));
        this.publicKey = keyToString(result.get("publicKey"));
    }
    // getter
    public PublicKey getPublicKey(){ return stringToPublicKey(publicKey); }
    public PrivateKey getPrivateKey(){ return stringToPrivateKey(privateKey); }

    // operator
    public int checkBalance(){
        int total = 0;
        for(TransactionOutput UTXO: Blockchain.getUTXOs().values())
            if(UTXO.verifyOwner(MainActivity.user.getId()))
                total += UTXO.getAmount();
        return total;
    }
    public LinkedList<Transaction> getDetail(){
        LinkedList<Transaction> txs = new LinkedList<>();
        for(Block block: Blockchain.getBlockchain())
            for(Transaction tx: block.getTransactions()){
                if((tx.getClassType().equals("Payment") && ((Payment)tx).getPayerId()==MainActivity.user.getId()) ||
                   (tx.getClassType().equals("Payment") && ((Payment)tx).getReceiverId()==MainActivity.user.getId()) ||
                   (tx.getClassType().equals("Withdraw") && ((Withdraw)tx).getPayerId()==MainActivity.user.getId()) ||
                   (tx.getClassType().equals("Deposit") && ((Deposit)tx).getReceiverId()==MainActivity.user.getId()))
                    txs.add(tx);
            }
        return txs;
    }
    public void deposit(int amount){
        Transaction transaction = new Deposit(publicKey, MainActivity.user.getId(), amount);
        transaction.generateSignature(privateKey);
        Blockchain.addUnveriedTransaction(transaction);
    }
    public void withdraw(int amount){
        boolean txCheck = true;
        LinkedList<TransactionInput> inputs = new LinkedList<>();
        int ownUTXOs = 0;

        //驗證餘額是否足夠本次交易，並更新UTXO
        //取得部分UTXO直到足夠支付本次交易的輸出
        for(TransactionOutput UTXO: Blockchain.getUTXOs().values()){
            if(UTXO.verifyOwner(MainActivity.user.getId())){
                inputs.add(new TransactionInput(UTXO.getHash()));
                ownUTXOs += UTXO.getAmount();
            }
            if(ownUTXOs > amount) break;
        }
        //驗證餘額是否足夠本次交易
        if(ownUTXOs < amount) {
            System.out.println("[x] 餘額不足，本次交易取消。"); //prompt
            txCheck = false;
        }

        //初步驗證無誤
        if(txCheck){
            Transaction transaction = new Withdraw(publicKey, MainActivity.user.getId(), amount, inputs);
            transaction.generateSignature(privateKey);
            MainActivity.temp_tx = transaction;
        }
    }
    public void makePayment(Order order){
        boolean txCheck = true;
        LinkedList<TransactionInput> inputs = new LinkedList<>();
        int ownUTXOs = 0;
        int payerId = (order.getStatus()==0? MainActivity.user.getId(): Blockchain.getThirdParty().getId());
        int receiverId = (order.getStatus()==0? Blockchain.getThirdParty().getId(): order.getProduct().getSeller().getId());
        int amount = order.getAmount();

        //驗證餘額是否足夠本次交易，並更新UTXO
        //取得部分UTXO直到足夠支付本次交易的輸出
        for(TransactionOutput UTXO: Blockchain.getUTXOs().values()){
            if(UTXO.verifyOwner(payerId)){
                inputs.add(new TransactionInput(UTXO.getHash()));
                ownUTXOs += UTXO.getAmount();
            }
            if(ownUTXOs > amount) break;
        }
        //驗證餘額是否足夠本次交易
        if(ownUTXOs < amount) {
            System.out.println("[x] 餘額不足，本次交易取消。"); //prompt
            txCheck = false;
        }

        //初步驗證無誤
        if(txCheck){
            String detail = order.getProduct().getTitle() + " " + order.getProduct().getPrice() + " × " + order.getQuantity() + " = " + order.getAmount();
            Transaction transaction = new Payment(publicKey, payerId, receiverId, order.getId(), detail, amount, inputs);
            transaction.generateSignature(privateKey);
            MainActivity.temp_tx = transaction;
        }
    }

    public void sendOTP(String operation){
        Gson gson = new Gson();
        Map<String, String> otpData = new HashMap<>();
        if(operation.equals("incorrectOTP")){
            otpData.put("id", Integer.toString(MainActivity.user.getId()));
        }else{
            otpData.put("id", Integer.toString(MainActivity.user.getId()));
            otpData.put("otp", generateOtp());
            otpData.put("content", MainActivity.temp_tx.getDetail());
        }
        new SendOtpTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/operate/"+operation, gson.toJson(otpData));
    }
    public int verifyOTP(String otp){
        if(!otp.equals(this.otp)){
            if(this.otpRetry>=4)
                return 4;
            return 2;
        }else if(Instant.now().getEpochSecond() > this.otpExpired) {
            if(this.otpRetry>=4)
                return 4;
            return 3;
        }
        return 1;
    }
    private class SendOtpTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
        }
        @Override
        protected void onPostExecute(String result) {
            processSendOtp(result);
        }
    }
    private void processSendOtp(String json){
        Gson gson = new Gson();
        Wallet wallet = gson.fromJson(json, Wallet.class);
        MainActivity.user.setWallet(wallet);
    }


    public void notifyNewPayment(int orderId){
        new UpdateOrderTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/order?id="+orderId);
    }
    private class UpdateOrderTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.getDataHttpUriConnection(params[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            processUpdateOrder(result);
        }

        public void processUpdateOrder(String json){
            Gson gson = new Gson();
            Order order = gson.fromJson(json, Order.class);
            if(order.getStatus()<3) order.updateStatus();
        }
    }
}
