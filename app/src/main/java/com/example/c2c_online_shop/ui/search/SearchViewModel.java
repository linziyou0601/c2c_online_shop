package com.example.c2c_online_shop.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.c2c_online_shop.MainActivity;

public class SearchViewModel extends ViewModel {

    private MutableLiveData<String> query = new MutableLiveData<>();
    private MutableLiveData<String> order_qty = new MutableLiveData<>();

    public SearchViewModel() {
        setQuery("");
        setOrderQty("");
    }

    public LiveData<String> getQuery(){ return query; }
    public void setQuery(String input){
        if(query.getValue() == null || !query.getValue().equals(input))
            query.setValue(input);
    }

    public LiveData<String> getOrderQty(){ return order_qty; }
    public void setOrderQty(String input){
        if(order_qty.getValue() == null || !order_qty.getValue().equals(input))
            order_qty.setValue(input);
        if(!order_qty.getValue().equals("") && Integer.parseInt(order_qty.getValue()) > MainActivity.temp_pd.getStockQty())
            order_qty.setValue(Integer.toString(MainActivity.temp_pd.getStockQty()));
    }
}