package com.example.c2c_online_shop.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.c2c_online_shop.MainActivity;

public class LoginViewModel extends ViewModel {
    private MutableLiveData<String> account = new MutableLiveData<>();
    private MutableLiveData<String> password = new MutableLiveData<>();
    private MutableLiveData<Boolean> keepLogin = new MutableLiveData<>();

    public LoginViewModel() {
        setAccount(MainActivity.keyStoreHelper.decrypt(MainActivity.preferencesHelper.getInput("ACCOUNT")));
        setPassword(MainActivity.keyStoreHelper.decrypt(MainActivity.preferencesHelper.getInput("PASSWORD")));
        setKeepLogin(Boolean.parseBoolean(MainActivity.preferencesHelper.getInput("KEEP_LOGIN")));
    }

    //--------------------儲存帳密--------------------//
    public void storeAccountData(){
        String recoredAccount = keepLogin.getValue() ? account.getValue() : "";
        String recoredPassword = keepLogin.getValue() ? password.getValue() : "";
        MainActivity.preferencesHelper.setInput("ACCOUNT", MainActivity.keyStoreHelper.encrypt(recoredAccount));
        MainActivity.preferencesHelper.setInput("PASSWORD", MainActivity.keyStoreHelper.encrypt(recoredPassword));
        MainActivity.preferencesHelper.setInput("KEEP_LOGIN", String.valueOf(keepLogin.getValue()));
    }

    public void refuseAccountData(){
        account.postValue("");
        password.postValue("");
        keepLogin.postValue(false);
        MainActivity.preferencesHelper.setInput("ACCOUNT", MainActivity.keyStoreHelper.encrypt(""));
        MainActivity.preferencesHelper.setInput("PASSWORD", MainActivity.keyStoreHelper.encrypt(""));
        MainActivity.preferencesHelper.setInput("KEEP_LOGIN", String.valueOf(false));
    }

    //--------------------處理監聽--------------------//
    public LiveData<String> getAccount(){
        return account;
    }
    public void setAccount(String input){
        if(account.getValue() == null || !account.getValue().equals(input))
            account.setValue(input);
    }

    public LiveData<String> getPassword(){
        return password;
    }
    public void setPassword(String input){
        if(password.getValue() == null || !password.getValue().equals(input))
            password.setValue(input);
    }

    public LiveData<Boolean> getKeepLogin(){ return keepLogin; }
    public void setKeepLogin(Boolean input){
        if(keepLogin.getValue() == null || keepLogin.getValue() != input)
            keepLogin.setValue(input);
    }
}