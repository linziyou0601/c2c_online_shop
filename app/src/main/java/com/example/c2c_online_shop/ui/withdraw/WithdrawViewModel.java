package com.example.c2c_online_shop.ui.withdraw;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.c2c_online_shop.MainActivity;

public class WithdrawViewModel extends ViewModel {

    private MutableLiveData<String> otp = new MutableLiveData<>();
    private MutableLiveData<String> withdraw_amount = new MutableLiveData<>();

    public WithdrawViewModel() {
        setOtp("");
        setWithdrawAmount("");
    }

    public LiveData<String> getOtp(){
        return otp;
    }
    public void setOtp(String input){
        if(otp.getValue() == null || !otp.getValue().equals(input))
            otp.setValue(input);
    }

    public LiveData<String> getWithdrawAmount(){ return withdraw_amount; }
    public void setWithdrawAmount(String input){
        if(withdraw_amount.getValue() == null || !withdraw_amount.getValue().equals(input))
            withdraw_amount.setValue(input);
        if(!withdraw_amount.getValue().equals("") && Integer.parseInt(withdraw_amount.getValue()) > MainActivity.user.getWallet().checkBalance())
            withdraw_amount.setValue(Integer.toString(MainActivity.user.getWallet().checkBalance()));
    }
}