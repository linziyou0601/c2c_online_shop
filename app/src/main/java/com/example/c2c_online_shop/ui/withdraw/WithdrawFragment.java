package com.example.c2c_online_shop.ui.withdraw;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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


public class WithdrawFragment extends Fragment {

    private WithdrawViewModel withdrawViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, mainLayout, otpPage;

    EditText otp;
    Button otp_back_btn, otp_btn;

    EditText withdraw_amount;
    Button withdraw_back_btn, withdraw_btn;
    TextView withdraw_currentCoin;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "WithdrawFragment";
        withdrawViewModel = ViewModelProviders.of(this).get(WithdrawViewModel.class);
        View root = inflater.inflate(R.layout.fragment_withdraw, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);

        otpPage = root.findViewById(R.id.otpPage);
        otp = root.findViewById(R.id.otp);
        otp_back_btn = root.findViewById(R.id.otp_back_btn);
        otp_btn = root.findViewById(R.id.otp_btn);

        withdraw_amount = root.findViewById(R.id.withdraw_amount);
        withdraw_btn = root.findViewById(R.id.withdraw_btn);
        withdraw_currentCoin = root.findViewById(R.id.withdraw_currentCoin);

        //--------------------Listener Button--------------------//
        otp_btn.setOnClickListener(arg0 -> otp_submit());
        otp_back_btn.setOnClickListener(arg0 -> otp_back());

        withdraw_btn.setOnClickListener(arg0 -> withdraw_submit());

        //--------------------Listener Observe--------------------//
        withdrawViewModel.getOtp().observe(this, s -> {
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
                withdrawViewModel.setOtp(s.toString());
            }
        });

        withdrawViewModel.getWithdrawAmount().observe(this, s -> {
            if(!withdraw_amount.getEditableText().toString().equals(s))
                withdraw_amount.setText(s);
        });
        withdraw_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                withdrawViewModel.setWithdrawAmount(s.toString());
            }
        });

        //--------------------IF HASN'T LOGIN--------------------//
        if(MainActivity.user == null)
            navController.navigate(R.id.navigation_login);

        //--------------------Main Function--------------------//
        update();
        MainActivity.temp_tx = null;
        outProgress();

        return root;
    }

    //--------------------Process--------------------//
    private void inProgress(){
        progressLayout.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        otpPage.setVisibility(View.GONE);
    }
    private void outProgress(){
        progressLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        otpPage.setVisibility(View.GONE);
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
    //withdraw
    private void withdraw_submit(){
        inProgress();
        MainActivity.user.getWallet().withdraw(Integer.parseInt(withdrawViewModel.getWithdrawAmount().getValue()));
        if(MainActivity.temp_tx!=null) {
            MainActivity.user.getWallet().sendOTP("sendOTP");
            showOtpPage();
        }else{
            Toast.makeText(getContext(), "Unknown Error Occurred", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        }
    }

    //update
    private void update(){
        inProgress();
        Blockchain.updateBlockchain();
        withdraw_currentCoin.setText(Integer.toString(MainActivity.user.getWallet().checkBalance()));
        outProgress();
    }

    //otp
    private void otp_submit(){
        inProgress();
        Gson gson = new Gson();
        int verify = MainActivity.user.getWallet().verifyOTP(withdrawViewModel.getOtp().getValue());
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
            Toast.makeText(getContext(), "Withdraw transaction is processing...", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        }
    }

    //--------------------Tasks--------------------//
    private void otp_back(){
        MainActivity.temp_tx = null;
        outProgress();
    }
}
