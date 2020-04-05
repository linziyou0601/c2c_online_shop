package com.example.c2c_online_shop;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyStoreHelper {

    private static final String TAG = "KEYSTORE";

    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";

    private static final String KEYSTORE_ALIAS = "KEYSTORE_DEMO";


    private KeyStore keyStore;
    private SharedPreferencesHelper prefsHelper;

    public KeyStoreHelper(Context context, SharedPreferencesHelper sharedPreferencesHelper) {
        try {
            prefsHelper = sharedPreferencesHelper;
            keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);

            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                prefsHelper.setIV("");
                genRSAKey();
                genAESKey();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //產生隨機的RSA Key
    private void genRSAKey() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER);

        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec
                .Builder(KEYSTORE_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build();

        keyPairGenerator.initialize(keyGenParameterSpec);
        keyPairGenerator.generateKeyPair();

    }

    //產生AES Key
    private void genAESKey() throws Exception {
        // Generate AES-Key
        byte[] aesKey = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(aesKey);


        // Generate 12 bytes iv then save to SharedPrefs
        byte[] generated = secureRandom.generateSeed(12);
        String iv = Base64.encodeToString(generated, Base64.DEFAULT);
        prefsHelper.setIV(iv);


        // Encrypt AES-Key with RSA Public Key then save to SharedPrefs
        String encryptAESKey = encryptRSA(aesKey);
        prefsHelper.setAESKey(encryptAESKey);
    }

    //RSA加密
    private String encryptRSA(byte[] plainText) throws Exception {
        PublicKey publicKey = keyStore.getCertificate(KEYSTORE_ALIAS).getPublicKey();

        Cipher cipher = Cipher.getInstance(RSA_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encryptedByte = cipher.doFinal(plainText);
        return Base64.encodeToString(encryptedByte, Base64.DEFAULT);
    }

    //RSA解密
    private byte[] decryptRSA(String encryptedText) throws Exception {
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(KEYSTORE_ALIAS, null);

        Cipher cipher = Cipher.getInstance(RSA_MODE);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return decryptedBytes;
    }


    /**
     * AES Encryption
     * @param plainText: A string which needs to be encrypted.
     * @return A base64's string after encrypting.
     */
    //AES加密
    private String encryptAES(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, getAESKey(), new IvParameterSpec(getIV()));

        // 加密過後的byte
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());

        // 將byte轉為base64的string編碼
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    //AES解密
    private String decryptAES(String encryptedText) throws Exception {
        // 將加密過後的Base64編碼格式 解碼成 byte
        byte[] decodedBytes = Base64.decode(encryptedText.getBytes(), Base64.DEFAULT);

        // 將解碼過後的byte 使用AES解密
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.DECRYPT_MODE, getAESKey(), new IvParameterSpec(getIV()));

        return new String(cipher.doFinal(decodedBytes));
    }


    private byte[] getIV() {
        String prefIV = prefsHelper.getIV();
        return Base64.decode(prefIV, Base64.DEFAULT);
    }


    private SecretKeySpec getAESKey() throws Exception {
        String encryptedKey = prefsHelper.getAESKey();
        byte[] aesKey = decryptRSA(encryptedKey);

        return new SecretKeySpec(aesKey, AES_MODE);
    }


    /**
     * 明文加解密
     */
    //加密
    public String encrypt(String plainText) {
        try {
            return encryptAES(plainText);

        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
            return "";
        }
    }

    //解密
    public String decrypt(String encryptedText) {
        try {
            return decryptAES(encryptedText);

        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
            return "";
        }

    }

}
