package com.example.c2c_online_shop.ui.products;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CreateProductViewModel extends ViewModel {

    private MutableLiveData<String> title = new MutableLiveData<>();
    private MutableLiveData<String> description = new MutableLiveData<>();
    private MutableLiveData<String> price = new MutableLiveData<>();
    private MutableLiveData<String> stockQty = new MutableLiveData<>();

    public CreateProductViewModel() {
        setTitle("");
        setDescription("");
        setPrice("");
        setStockQty("");
    }

    public LiveData<String> getTitle(){ return title; }
    public void setTitle(String input){
        if(title.getValue() == null || !title.getValue().equals(input))
            title.setValue(input);
    }

    public LiveData<String> getDescription(){ return description; }
    public void setDescription(String input){
        if(description.getValue() == null || !description.getValue().equals(input))
            description.setValue(input);
    }

    public LiveData<String> getPrice(){ return price; }
    public void setPrice(String input){
        if(price.getValue() == null || !price.getValue().equals(input))
            price.setValue(input);
    }

    public LiveData<String> getStockQty(){ return stockQty; }
    public void setStockQty(String input){
        if(stockQty.getValue() == null || !stockQty.getValue().equals(input))
            stockQty.setValue(input);
    }
}