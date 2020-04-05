package com.example.c2c_online_shop.ui.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OrderViewModel extends ViewModel {

    private MutableLiveData<String> otp = new MutableLiveData<>();

    public OrderViewModel() {
        setOtp("");
    }

    public LiveData<String> getOtp(){
        return otp;
    }
    public void setOtp(String input){
        if(otp.getValue() == null || !otp.getValue().equals(input))
            otp.setValue(input);
    }
}