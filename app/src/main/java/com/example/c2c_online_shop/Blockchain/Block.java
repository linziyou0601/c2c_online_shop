package com.example.c2c_online_shop.Blockchain;

import android.os.AsyncTask;

import com.example.c2c_online_shop.HttpURLConnectionUtil;
import com.example.c2c_online_shop.StringUtil;
import com.google.gson.Gson;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Block {
    private int nonce;              //不重複自增數（挖礦用）
    private String hash;            //區塊Hash值 -> SHA256(前一區塊Hash值 + 時間戳 + 自增值 + merkleRoot)
    private String previousHash;    //前一區塊Hash值
    private String merkleRoot;      //merkleRoot
    private long timestamp;         //區塊建立Unix時間戳
    private LinkedList<Transaction> transactions = new LinkedList<>(); //交易資料
    Block(String hash, String previousHash, String merkleRoot, int nonce, long timestamp, LinkedList<Transaction> transactions){
        this.hash = hash;
        this.previousHash = previousHash;
        this.merkleRoot = merkleRoot;
        this.nonce = nonce;
        this.timestamp = timestamp;
        this.transactions = transactions;
    }
    Block(String previousHash) {
        this.timestamp = Instant.now().getEpochSecond();
        this.previousHash = previousHash;
        this.hash = calculateHash();
    }
    // getter
    public String getHash(){ return hash; }
    public String getPreviousHash(){ return previousHash; }
    public String getMerkleRoot(){ return merkleRoot; }
    public long getTimestamp(){ return timestamp; }
    public LinkedList<Transaction> getTransactions(){ return transactions; }

    // operator
    //加入交易至該區塊
    public void addTransaction(Transaction transaction) {
        //加入前先驗證交易資料 處理交易金流至Global UTXO
        if(transaction != null) {
            if(transaction.processTransaction()) {
                System.out.println("[v] 交易成功放入區塊"); //prompt
                transactions.add(transaction);      //放入區塊
            } else {
                System.out.println("[v] 交易不合理，移除"); //prompt
                Gson gson = new Gson();
                Map<String, String> data = new HashMap<>();
                data.put("hash", transaction.getHash());
                new RemoveUnveriedTransactionTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/blockchain/removeTransaction", gson.toJson(data));
            }
        }
    }
    //算力證明挖礦
    public void mineBlock(int DIFFICULTY) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = String.join("", Collections.nCopies(DIFFICULTY, "0"));
        while(!hash.substring(0, DIFFICULTY).equals(target)) {
            this.nonce ++;
            this.hash = calculateHash();
        }
    }
    //計算該區塊Hash值
    public String calculateHash() {
        return StringUtil.SHA256(previousHash +  Long.toString(timestamp) +  Integer.toString(nonce) + merkleRoot);
    }

    private class RemoveUnveriedTransactionTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
            return null;
        }
    }
}
