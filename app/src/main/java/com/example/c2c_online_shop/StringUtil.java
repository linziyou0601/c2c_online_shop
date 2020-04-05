package com.example.c2c_online_shop;

import com.example.c2c_online_shop.Blockchain.Transaction;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    //-------------------- MessageDigest加密 --------------------
    //加密方法
    private static String encode(String input, String hashType){
        try {
            MessageDigest digest = MessageDigest.getInstance(hashType);
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();
            for(int i=0;i<hash.length;i++)
                hexString.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
            return hexString.toString();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    //SHA256、MD5加密方法
    public static String SHA256(String input){ return encode(input, "SHA-256"); }
    public static String MD5(String input){  return encode(input, "MD5"); }

    //BASE64加解密方法
    public static String base64Encode(String input){ return bytesToString(input.getBytes()); }
    public static String base64Decode(String input){ return new String(stringToBytes(input)); }

    //String、Byte互轉
    public static byte[] stringToBytes(String encoded){ return Base64.getDecoder().decode(encoded); }
    public static String bytesToString(byte[] decoded){ return Base64.getEncoder().encodeToString(decoded); }

    //EmailValidate
    public static boolean validateEmail(String emailStr) {
        Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.matches();
    }

    //GenerateOTP
    public static String generateOtp(){
        Random ran = new Random();
        return String.format("%06d", ran.nextInt(999999)+1);
    }

    //MerkleRoot
    public static String getMerkleRoot(LinkedList<Transaction> transactions) {
        LinkedList<String> tempTreeLayer = new LinkedList<>();
        for(Transaction transaction: transactions)
            tempTreeLayer.add(transaction.getHash());

        String left, right;
        LinkedList<String> treeLayer = tempTreeLayer;
        while(treeLayer.size() > 1) {
            treeLayer = new LinkedList<>();
            for(int i=0; i < tempTreeLayer.size(); i+=2){
                left = tempTreeLayer.get(i);
                right = (i+1 != tempTreeLayer.size())? tempTreeLayer.get(i+1): "";
                treeLayer.add(SHA256(left + right));
            }
            tempTreeLayer = treeLayer;
        }
        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }
}
