package com.example.c2c_online_shop.ui.deposit;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.c2c_online_shop.MainActivity;
import com.example.c2c_online_shop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class DepositFragment extends Fragment {

    private DepositViewModel depositViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, mainLayout;

    EditText deposit_amount;
    Button deposit_back_btn, deposit_btn;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "DepositFragment";
        depositViewModel = ViewModelProviders.of(this).get(DepositViewModel.class);
        View root = inflater.inflate(R.layout.fragment_deposit, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);

        deposit_amount = root.findViewById(R.id.deposit_amount);
        deposit_btn = root.findViewById(R.id.deposit_btn);

        //--------------------Listener Button--------------------//
        deposit_btn.setOnClickListener(arg0 -> deposit_submit());

        //--------------------Listener Observe--------------------//
        depositViewModel.getDepositAmount().observe(this, s -> {
            if(!deposit_amount.getEditableText().toString().equals(s))
                deposit_amount.setText(s);
        });
        deposit_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                depositViewModel.setDepositAmount(s.toString());
            }
        });

        //--------------------IF HASN'T LOGIN--------------------//
        if(MainActivity.user == null)
            navController.navigate(R.id.navigation_login);

        //--------------------Main Function--------------------//
        MainActivity.temp_tx = null;
        outProgress();

        return root;
    }

    //--------------------Process--------------------//
    private void inProgress(){
        progressLayout.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
    }
    private void outProgress(){
        progressLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navView.setVisibility(View.GONE);
    }

    //--------------------Function--------------------//
    //deposit
    private void deposit_submit(){
        inProgress();
        MainActivity.user.getWallet().deposit(Integer.parseInt(depositViewModel.getDepositAmount().getValue()));
        outProgress();
        Toast.makeText(getContext(), "Deposit transaction is processing...", Toast.LENGTH_SHORT).show();
        getActivity().onBackPressed();
    }
}
