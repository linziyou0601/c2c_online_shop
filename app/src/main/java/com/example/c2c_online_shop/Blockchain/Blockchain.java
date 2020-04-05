package com.example.c2c_online_shop.Blockchain;

import android.os.AsyncTask;

import com.example.c2c_online_shop.HttpURLConnectionUtil;
import com.example.c2c_online_shop.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

public class Blockchain {
    public final static int DIFFICULTY = 3;
    private static LinkedList<Block> blockchain = new LinkedList<>();
    private static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    private static User thirdParty;

    // getter
    public static void setThirdParty(){
        new SetThirdPartyTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/user?account=System");
    }
    public static User getThirdParty(){ return thirdParty; }
    public static HashMap<String, TransactionOutput> getUTXOs(){ return UTXOs; }
    public static void putUTXOs(String key, TransactionOutput transactionOutput){ UTXOs.put(key, transactionOutput); }
    public static void removeUTXOs(String key){ UTXOs.remove(key); }
    public static LinkedList<Block> getBlockchain(){ return blockchain; }
    public static void updateBlockchain(){
        new UpdateBlockchainTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/blockchain/getBlockchain");
        packTransaction();
    }

    // operator
    //-------------------- block build --------------------
    public static String lastBlockHash(){ return blockchain.size()>0? blockchain.getLast().getHash(): "0"; }
    public static void addUnveriedTransaction(Transaction newTransacion) {
        Gson gson = new Gson();
        new AddUnveriedTransactionTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/blockchain/addTransaction", gson.toJson(newTransacion));
        updateBlockchain();
    }
    public static void packTransaction() {
        new PackTransactionTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/blockchain/getTransaction");
    }
    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(DIFFICULTY);
        blockchain.add(newBlock);
        Gson gson = new Gson();
        new AddBlockchainTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/blockchain/addBlockchain", gson.toJson(blockchain));
    }

    //-------------------- Verify --------------------
    public static boolean verifyChain() {
        Block currentBlock, previousBlock;
        String hashTarget = String.join("", Collections.nCopies(DIFFICULTY, "0"));
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        int sequence = 0;

        //驗證所有區塊Hash值
        for(int i=0; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = i>0? blockchain.get(i-1): null;

            //驗證區塊Hash值
            if(!currentBlock.getHash().equals(currentBlock.calculateHash()) ||
               (previousBlock!=null && !previousBlock.getHash().equals(currentBlock.getPreviousHash())) ||
               !currentBlock.getHash().substring(0, DIFFICULTY).equals(hashTarget)) {
                System.out.println("[x] 區塊(" + currentBlock.getHash() + ") 的Hash值有誤！");  //prompt
                return false;
            }

            //驗證區塊中所有交易 （按鏈->區塊->交易 順序驗，跑一次整個鏈的交易流程）
            TransactionOutput tempOutput;
            for(int t=0; t < currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);
                if(currentTransaction.getSequence()>=sequence)
                    sequence = currentTransaction.getSequence()+1;

                if(!currentTransaction.verifiySignature()) {
                    System.out.println("[x] 交易(" + currentTransaction.getHash() + ") 的簽章不合法！");   //prompt
                    return false;
                }

                if(currentTransaction instanceof Payment) {
                    if (currentTransaction.getInputsAmount() != currentTransaction.getOutputsAmount()) {
                        System.out.println("[x] 交易(" + currentTransaction.getHash() + ") 的輸入與輸出金額不相等！"); //prompt
                        return false;
                    }
                }

                if(!(currentTransaction instanceof Deposit)) {
                    for(TransactionInput input : currentTransaction.getInputs()) {
                        tempOutput = tempUTXOs.get(input.getSourceOutputHash());
                        if (tempOutput == null) {
                            System.out.println("[x] 交易(" + currentTransaction.getHash() + ") 的金流來源遺失！"); //prompt
                            return false;
                        }
                        if (input.getAmount() != tempOutput.getAmount()) {
                            System.out.println("[x] 交易(" + currentTransaction.getHash() + ") 的金流有問題！");   //prompt
                            return false;
                        }
                        tempUTXOs.remove(input.getSourceOutputHash());
                    }
                }

                if(currentTransaction.getOutputs()!=null) {
                    //此交易所有輸出的UTXO再放入tempUTXOs
                    for(TransactionOutput output: currentTransaction.getOutputs())
                        tempUTXOs.put(output.getHash(), output);
                }
            }
        }
        System.out.println("[v] 區塊鏈驗證成功！"); //prompt
        UTXOs = tempUTXOs;
        Transaction.nonce = sequence;
        return true;
    }

    //--------------------Tasks--------------------//
    private static class SetThirdPartyTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.getDataHttpUriConnection(params[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            processLogin(result);
        }

        private void processLogin(String json){
            Gson gson = new Gson();
            thirdParty = gson.fromJson(json, User.class);
        }
    }

    private static class AddUnveriedTransactionTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
            return null;
        }
    }

    private static class PackTransactionTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.getDataHttpUriConnection(params[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            processPackTransaction(result);
        }

        public static void processPackTransaction(String json){
            Block newBlock = new Block(lastBlockHash());
            Gson gson = new GsonBuilder().registerTypeAdapter(Transaction.class, new TransactionJsonDeserializer()).create();
            LinkedList<Transaction> unverifiedPool = gson.fromJson(json, new TypeToken<LinkedList<Transaction>>() {}.getType());
            while(unverifiedPool.size() > 0){
                newBlock.addTransaction(unverifiedPool.pop());
                if(newBlock.getTransactions().size()>=3 || unverifiedPool.size()==0)
                    addBlock(newBlock);
            }
        }
    }

    private static class AddBlockchainTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
            return null;
        }
    }

    private static class UpdateBlockchainTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.getDataHttpUriConnection(params[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            processUpdateBlockchain(result);
        }

        public static void processUpdateBlockchain(String json){
            Gson gson = new GsonBuilder().registerTypeAdapter(Block.class, new BlockJsonDeserializer()).create();
            LinkedList<Block> bc = gson.fromJson(json, new TypeToken<LinkedList<Block>>(){}.getType());
            if(bc!=null) bc.sort(Comparator.comparingLong(Block::getTimestamp));
            blockchain = bc;
            verifyChain();
        }
    }
}
