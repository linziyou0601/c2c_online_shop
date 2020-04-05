package com.example.c2c_online_shop.ui.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RegisterViewModel extends ViewModel {
    private MutableLiveData<String> account = new MutableLiveData<>();
    private MutableLiveData<String> password = new MutableLiveData<>();
    private MutableLiveData<String> confirm_password = new MutableLiveData<>();
    private MutableLiveData<String> name = new MutableLiveData<>();
    private MutableLiveData<String> email = new MutableLiveData<>();
    private MutableLiveData<String> otp = new MutableLiveData<>();

    public RegisterViewModel(){
        setAccount("");
        setPassword("");
        setConfirmPassword("");
        setName("");
        setEmail("");
        setOtp("");
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

    public LiveData<String> getConfirmPassword(){
        return confirm_password;
    }
    public void setConfirmPassword(String input){
        if(confirm_password.getValue() == null || !confirm_password.getValue().equals(input))
            confirm_password.setValue(input);
    }

    public LiveData<String> getName(){
        return name;
    }
    public void setName(String input){
        if(name.getValue() == null || !name.getValue().equals(input))
            name.setValue(input);
    }

    public LiveData<String> getEmail(){
        return email;
    }
    public void setEmail(String input){
        if(email.getValue() == null || !email.getValue().equals(input))
            email.setValue(input);
    }

    public LiveData<String> getOtp(){
        return otp;
    }
    public void setOtp(String input){
        if(otp.getValue() == null || !otp.getValue().equals(input))
            otp.setValue(input);
    }
}