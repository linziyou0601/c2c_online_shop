package com.example.c2c_online_shop.ui.products;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProductsViewModel extends ViewModel {

    private MutableLiveData<String> deposit_amount = new MutableLiveData<>();

    public ProductsViewModel() {
        setDepositAmount("");
    }

    public LiveData<String> getDepositAmount(){ return deposit_amount; }
    public void setDepositAmount(String input){
        if(deposit_amount.getValue() == null || !deposit_amount.getValue().equals(input))
            deposit_amount.setValue(input);
    }
}