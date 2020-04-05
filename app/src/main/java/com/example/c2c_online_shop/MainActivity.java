package com.example.c2c_online_shop;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.c2c_online_shop.Blockchain.Blockchain;
import com.example.c2c_online_shop.Blockchain.Transaction;
import com.example.c2c_online_shop.model.Notification;
import com.example.c2c_online_shop.model.Order;
import com.example.c2c_online_shop.model.Product;
import com.example.c2c_online_shop.model.User;
import com.example.c2c_online_shop.ui.login.LoginViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    public static KeyStoreHelper keyStoreHelper;
    public static SharedPreferencesHelper preferencesHelper;
    private static LoginViewModel loginViewModel;
    public static NavController navController;
    public static User user = null;             //Session User
    public static LinkedList<Notification> notifications = new LinkedList<>();

    public static User temp_user = null;        //User is going to be register that wait for Email OTP Authentication
    public static Transaction temp_tx = null;   //Transaction is going to be add that wait for Email OTP Authentication
    public static Product temp_pd = null;       //Product is going to be edit
    public static Order temp_od = null;       //Order is going to be edit
    public static Context temp_context = null;

    public static String emailOtp;              //Registration OTP
    public static long emailOtpExpired;         //Registration OTPExpired

    public static String fragmentName = new String();   //Current Fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //--------------------設定儲存器、加密器、登入資料庫--------------------//
        preferencesHelper = new SharedPreferencesHelper(getApplicationContext());
        keyStoreHelper = new KeyStoreHelper(getApplicationContext(), preferencesHelper);
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_login,
                R.id.navigation_register,
                R.id.navigation_home,
                R.id.navigation_search,
                R.id.navigation_notifications,
                R.id.navigation_wallet,
                R.id.navigation_orders,
                R.id.navigation_order,
                R.id.navigation_products,
                R.id.navigation_edit_product,
                R.id.navigation_create_product,
                R.id.navigation_profile,
                R.id.navigation_deposit,
                R.id.navigation_withdraw,
                R.id.navigation_detail)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        //--------------------更新區塊鏈--------------------//
        Blockchain.setThirdParty();
        Blockchain.updateBlockchain();
    }

    public static void unsetUser(){
        user = null;
        loginViewModel.refuseAccountData();
        navController.navigate(R.id.navigation_login);
    }

    public static void updateNotifications(){
        if(user != null)
            new GetNotificationListTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/notifications?id="+user.getId());
    }

    private static class GetNotificationListTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.getDataHttpUriConnection(params[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            processGetProductList(result);
        }

        public void processGetProductList(String json){
            Gson gson = new Gson();
            LinkedList<Notification> notifies = gson.fromJson(json, new TypeToken<LinkedList<Notification>>(){}.getType());
            notifications = new LinkedList<>();
            //notifications
            for(Notification notify: notifies){
                if(notify.getOperated()==false){
                    if((notify.getType().equals("Payment Received") && (notify.getUserId()==Blockchain.getThirdParty().getId() || notify.getUserId()==user.getId())) ||
                       (notify.getType().equals("Payment Paid") && notify.getUserId()==Blockchain.getThirdParty().getId())){
                        new NotifyNewPaymentTask(notify.getOrderId()).execute(
                                "https://linziyou.nctu.me:7777/api/c2c_shop/select/user?id="+notify.getUserId()
                        );
                    }
                    notify.setOperated(true);
                }
                notifications.add(notify);
            }
        }
    }

    private static class NotifyNewPaymentTask extends AsyncTask<String, Void, String> {
        public int orderId;
        NotifyNewPaymentTask(int orderId) {
            this.orderId = orderId;
        }
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.getDataHttpUriConnection(params[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            processNotifyNewPayment(result);
        }

        public void processNotifyNewPayment(String json){
            Gson gson = new Gson();
            User user = gson.fromJson(json, User.class);
            user.getWallet().notifyNewPayment(this.orderId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if(fragmentName.equals("HomeFragment")) {
            if (doubleBackToExitPressedOnce) {
                finish();
                System.exit(0);
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Back again to close app.", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
        } else {
            super.onBackPressed();
        }

    }
}
