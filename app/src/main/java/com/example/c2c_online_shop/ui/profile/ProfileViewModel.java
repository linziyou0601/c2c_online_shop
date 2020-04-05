package com.example.c2c_online_shop.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;

public class ProfileViewModel extends ViewModel {

    private MutableLiveData<String> name = new MutableLiveData<>();
    private MutableLiveData<String> phone = new MutableLiveData<>();
    private MutableLiveData<Date> birth = new MutableLiveData<>();
    private MutableLiveData<String> address = new MutableLiveData<>();
    public ProfileViewModel() {
        setName("");
        setPhone("");
        setBirth(null);
        setAddress("");
    }

    public LiveData<String> getName(){ return name; }
    public void setName(String input){
        if(name.getValue() == null || !name.getValue().equals(input))
            name.setValue(input);
    }
    public LiveData<String> getPhone(){ return phone; }
    public void setPhone(String input){
        if(phone.getValue() == null || !phone.getValue().equals(input))
            phone.setValue(input);
    }
    public LiveData<Date> getBirth(){ return birth; }
    public void setBirth(Date input){
        if(birth.getValue() == null || birth.getValue() != input)
            birth.setValue(input);
    }
    public LiveData<String> getAddress(){ return address; }
    public void setAddress(String input){
        if(address.getValue() == null || !address.getValue().equals(input))
            address.setValue(input);
    }
}