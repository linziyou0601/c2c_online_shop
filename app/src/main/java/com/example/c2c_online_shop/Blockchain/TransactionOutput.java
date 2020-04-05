package com.example.c2c_online_shop.Blockchain;

import com.example.c2c_online_shop.StringUtil;

public class TransactionOutput {
    private String hash;                  //TXO id
    private String parentTxHash;          //This time Tx parent id
    private int receiverId;
    private int amount;
    public TransactionOutput(int receiverId, int amount, String parentTxHash) {
        this.receiverId = receiverId;
        this.amount = amount;
        this.parentTxHash = parentTxHash;
        this.hash = StringUtil.SHA256(receiverId + Integer.toString(amount) + parentTxHash);
    }
    // getter
    public String getHash(){ return hash; }
    public String getParentTxId(){ return parentTxHash; }
    public int getReceiverId(){ return receiverId; }
    public int getAmount(){ return amount; }

    // operator
    public boolean verifyOwner(int receiverId) { return this.receiverId == receiverId; }
}