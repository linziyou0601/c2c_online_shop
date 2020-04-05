package com.example.c2c_online_shop.ui.wallet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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


public class WalletFragment extends Fragment {

    private WalletViewModel walletViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, mainLayout;
    Button deposit, withdraw, update, detail, orders, products, profiles, logout;
    TextView balance;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "WalletFragment";
        MainActivity.updateNotifications();
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        View root = inflater.inflate(R.layout.fragment_wallet, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);
        deposit = root.findViewById(R.id.Deposit);
        withdraw = root.findViewById(R.id.Withdraw);
        update = root.findViewById(R.id.Update);
        detail = root.findViewById(R.id.Detail);
        orders = root.findViewById(R.id.Orders);
        products = root.findViewById(R.id.Products);
        profiles = root.findViewById(R.id.Profiles);
        logout = root.findViewById(R.id.Logout);
        balance = root.findViewById(R.id.balance);

        //--------------------Listener Button--------------------//
        deposit.setOnClickListener(arg0 -> deposit());
        withdraw.setOnClickListener(arg0 -> withdraw());
        update.setOnClickListener(arg0 -> update());
        detail.setOnClickListener(arg0 -> detail());
        orders.setOnClickListener(arg0 -> orders());
        products.setOnClickListener(arg0 -> products());
        profiles.setOnClickListener(arg0 -> profiles());
        logout.setOnClickListener(arg0 -> logout());

        //--------------------IF HASN'T LOGIN--------------------//
        if(MainActivity.user == null)
            navController.navigate(R.id.navigation_login);

        //--------------------Main Function--------------------//
        MainActivity.temp_tx = null;
        outProgress();
        balance.setText(Integer.toString(MainActivity.user.getWallet().checkBalance()));

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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        navView.setVisibility(View.VISIBLE);
    }

    //--------------------Function--------------------//
    //deposit
    private void deposit(){
        navController.navigate(R.id.navigation_deposit);
    }

    //withdraw
    private void withdraw(){
        update();
        navController.navigate(R.id.navigation_withdraw);
    }

    //update
    private void update(){
        inProgress();
        Blockchain.updateBlockchain();
        balance.setText(Integer.toString(MainActivity.user.getWallet().checkBalance()));
        outProgress();
    }

    //detail
    private void detail(){
        navController.navigate(R.id.navigation_detail);
    }

    //orders
    private void orders(){ navController.navigate(R.id.navigation_orders); }

    //products
    private void products(){ navController.navigate(R.id.navigation_products); }

    //profile
    private void profiles(){ navController.navigate(R.id.navigation_profile); }

    //logout
    private void logout(){
        new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                .setTitle("Logout")
                .setMessage("Do you want to logout?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Logout", (arg0, arg1) -> MainActivity.user.logout())
                .show();
    }
}
