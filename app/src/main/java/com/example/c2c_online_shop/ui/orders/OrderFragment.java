package com.example.c2c_online_shop.ui.orders;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.c2c_online_shop.Blockchain.Blockchain;
import com.example.c2c_online_shop.MainActivity;
import com.example.c2c_online_shop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;


public class OrderFragment extends Fragment {

    private OrderViewModel orderViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, otpPage;
    ScrollView mainLayout;

    EditText otp;
    Button otp_back_btn, otp_btn;

    TextView title, description, seller, buyer, quantity, price, amount, status;
    Button cancel_btn, payment_btn, shipped_btn, completed_btn;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "OrderFragment";
        orderViewModel = ViewModelProviders.of(this).get(OrderViewModel.class);
        View root = inflater.inflate(R.layout.fragment_order, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);
        title = root.findViewById(R.id.title);
        description = root.findViewById(R.id.description);
        seller = root.findViewById(R.id.seller);
        buyer = root.findViewById(R.id.buyer);
        quantity = root.findViewById(R.id.quantity);
        price = root.findViewById(R.id.price);
        amount = root.findViewById(R.id.amount);
        status = root.findViewById(R.id.status);

        cancel_btn = root.findViewById(R.id.cancel_btn);
        payment_btn = root.findViewById(R.id.payment_btn);
        shipped_btn = root.findViewById(R.id.shipped_btn);
        completed_btn = root.findViewById(R.id.completed_btn);

        otpPage = root.findViewById(R.id.otpPage);
        otp = root.findViewById(R.id.otp);
        otp_back_btn = root.findViewById(R.id.otp_back_btn);
        otp_btn = root.findViewById(R.id.otp_btn);

        //--------------------Listener Button--------------------//
        cancel_btn.setOnClickListener(arg0 -> cancelOrder());
        payment_btn.setOnClickListener(arg0 -> paymentOrder());
        shipped_btn.setOnClickListener(arg0 -> shippedOrder());
        completed_btn.setOnClickListener(arg0 -> completedOrder());

        otp_btn.setOnClickListener(arg0 -> otp_submit());
        otp_back_btn.setOnClickListener(arg0 -> otp_back());

        //--------------------Listener Observe--------------------//
        orderViewModel.getOtp().observe(this, s -> {
            if(!otp.getEditableText().toString().equals(s))
                otp.setText(s);
        });
        otp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                orderViewModel.setOtp(s.toString());
            }
        });

        //--------------------IF HASN'T LOGIN--------------------//
        if(MainActivity.user == null)
            navController.navigate(R.id.navigation_login);

        //--------------------Main Function--------------------//
        outProgress();
        title.setText(MainActivity.temp_od.getProduct().getTitle());
        description.setText(MainActivity.temp_od.getProduct().getDescription());
        seller.setText(MainActivity.temp_od.getProduct().getSeller().getProfile().getName());
        buyer.setText(MainActivity.temp_od.getBuyer().getProfile().getName());
        quantity.setText(Integer.toString(MainActivity.temp_od.getQuantity()));
        price.setText(Integer.toString(MainActivity.temp_od.getProduct().getPrice()));
        amount.setText(Integer.toString(MainActivity.temp_od.getAmount()));
        cancel_btn.setVisibility(View.GONE);
        payment_btn.setVisibility(View.GONE);
        shipped_btn.setVisibility(View.GONE);
        completed_btn.setVisibility(View.GONE);

        switch(MainActivity.temp_od.getStatus()){
            case 0:
                cancel_btn.setVisibility(View.VISIBLE);
                if(MainActivity.user.getId()==MainActivity.temp_od.getBuyer().getId()) payment_btn.setVisibility(View.VISIBLE);
                status.setText("Ordered");
                break;
            case 1:
                if(MainActivity.user.getId()==MainActivity.temp_od.getProduct().getSeller().getId()) shipped_btn.setVisibility(View.VISIBLE);
                status.setText("Paid");
                break;
            case 2:
                if(MainActivity.user.getId()==MainActivity.temp_od.getBuyer().getId()) completed_btn.setVisibility(View.VISIBLE);
                status.setText("Shipping");
                break;
            case 3:
                status.setText("Completed");
                break;
        }

        return root;
    }

    //--------------------Process--------------------//
    private void inProgress(){
        otpPage.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
    }
    private void outProgress(){
        otpPage.setVisibility(View.GONE);
        progressLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navView.setVisibility(View.GONE);
    }
    private void showOtpPage(){
        otpPage.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.GONE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    //--------------------Function--------------------//
    private void cancelOrder(){
        new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                .setTitle("Order Cancellation")
                .setMessage("Do you want to cancel this order?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes", (arg0, arg1) -> {
                    MainActivity.temp_od.cancelOrder();
                    Toast.makeText(getContext(), "Canceling order is processing...", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                })
                .show();
    }
    private void paymentOrder(){
        inProgress();
        MainActivity.user.getWallet().makePayment(MainActivity.temp_od);
        if(MainActivity.temp_tx!=null) {
            MainActivity.user.getWallet().sendOTP("sendOTP");
            showOtpPage();
        }else{
            Toast.makeText(getContext(), "Insufficient Balance", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        }

    }
    private void shippedOrder(){
        new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                .setTitle("Order Shipped")
                .setMessage("Have you already shipped the order?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes", (arg0, arg1) -> {
                    MainActivity.temp_od.shippedOrder();
                    Toast.makeText(getContext(), "Shipping order is processing...", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                })
                .show();
    }
    private void completedOrder(){
        new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                .setTitle("Order Received")
                .setMessage("Have you already received the order?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes", (arg0, arg1) -> {
                    MainActivity.temp_od.completedOrder();
                    Toast.makeText(getContext(), "Completing order is processing...", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                })
                .show();
    }

    //otp
    private void otp_submit(){
        inProgress();
        Gson gson = new Gson();
        int verify = MainActivity.user.getWallet().verifyOTP(orderViewModel.getOtp().getValue());
        if(verify == 2){
            showOtpPage();
            new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                    .setTitle("OTP is incorrect.")
                    .setMessage("OTP is incorrect, please check again.")
                    .setPositiveButton("OK", null)
                    .show();
            MainActivity.user.getWallet().sendOTP("incorrectOTP");
            showOtpPage();
        }else if(verify == 3){
            showOtpPage();
            new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                    .setTitle("OTP is expired.")
                    .setMessage("Resent a new one")
                    .setPositiveButton("OK", null)
                    .show();
            MainActivity.user.getWallet().sendOTP("resendOTP");
            showOtpPage();
        }else if(verify == 4){
            new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                    .setTitle("OTP incorrect times limited.")
                    .setMessage("Cancel this transaction automatically.")
                    .setPositiveButton("OK", null)
                    .show();
            otp_back();
        }else{
            Blockchain.addUnveriedTransaction(MainActivity.temp_tx);
            outProgress();
            Toast.makeText(getContext(), "Payment transaction is processing...", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        }
    }

    //--------------------Tasks--------------------//
    private void otp_back(){
        MainActivity.temp_tx = null;
        outProgress();
    }
}
