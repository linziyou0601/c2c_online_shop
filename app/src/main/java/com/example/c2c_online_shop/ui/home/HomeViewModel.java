package com.example.c2c_online_shop.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.c2c_online_shop.MainActivity;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> order_qty = new MutableLiveData<>();

    public HomeViewModel() {
        setOrderQty("");
    }

    public LiveData<String> getOrderQty(){ return order_qty; }
    public void setOrderQty(String input){
        if(order_qty.getValue() == null || !order_qty.getValue().equals(input))
            order_qty.setValue(input);
        if(!order_qty.getValue().equals("") && Integer.parseInt(order_qty.getValue()) > MainActivity.temp_pd.getStockQty())
            order_qty.setValue(Integer.toString(MainActivity.temp_pd.getStockQty()));
    }
}