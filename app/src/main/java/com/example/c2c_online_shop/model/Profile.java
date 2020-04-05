package com.example.c2c_online_shop.model;

import android.os.AsyncTask;

import com.example.c2c_online_shop.HttpURLConnectionUtil;
import com.example.c2c_online_shop.MainActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Calendar;
import java.util.Date;

public class Profile {
    private String name;
    private String phone;
    private String email;
    private String address;
    private Date birth;
    public Profile(String name, String email){
        this.name = name;
        this.email = email;
    }
    public void edit(String name, String phone, String email, String address, Date birth){
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.birth = birth;
        //call to mysql
        Gson gson = new GsonBuilder().setDateFormat("yyyy/MM/dd").create();
        System.out.println(gson.toJson(MainActivity.user));
        new EditProfileTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/update/user", gson.toJson(MainActivity.user));
    }
    public String getName(){ return name; }
    public String getPhone(){ return phone; }
    public String getEmail(){ return email; }
    public String getAddress(){ return address; }
    public Date getBirth(){ return birth; }
    public int getAge(){
        Calendar currentCal = Calendar.getInstance();
        int currentYear = currentCal.get(Calendar.YEAR);
        int currentMonth = currentCal.get(Calendar.MONTH);
        int currentDay = currentCal.get(Calendar.DAY_OF_MONTH);
        Calendar birthCal = Calendar.getInstance();
        birthCal.setTime(birth);
        int birthYear = birthCal.get(Calendar.YEAR);
        int birthMonth = birthCal.get(Calendar.MONTH);
        int birthDay = birthCal.get(Calendar.DAY_OF_MONTH);
        int age = currentYear - birthYear;
        if(currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)){
            age--;
        }
        return age;
    }

    private static class EditProfileTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
            return null;
        }
    }
}
