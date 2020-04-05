package com.example.c2c_online_shop.ui.products;

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


public class CreateProductFragment extends Fragment {

    private CreateProductViewModel createProductViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, mainLayout;

    EditText title, description, price, stockQty;
    Button create_btn;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "CreateProductFragment";
        createProductViewModel = ViewModelProviders.of(this).get(CreateProductViewModel.class);
        View root = inflater.inflate(R.layout.fragment_create_product, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);
        create_btn = root.findViewById(R.id.create_btn);
        title = root.findViewById(R.id.title);
        description = root.findViewById(R.id.description);
        price = root.findViewById(R.id.price);
        stockQty = root.findViewById(R.id.stockQty);

        //--------------------Listener Button--------------------//
        create_btn.setOnClickListener(arg0 -> createProduct());

        //--------------------Listener Observe--------------------//
        createProductViewModel.getTitle().observe(this, s -> {
            if(!title.getEditableText().toString().equals(s))
                title.setText(s);
        });
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                createProductViewModel.setTitle(s.toString());
            }
        });

        createProductViewModel.getDescription().observe(this, s -> {
            if(!description.getEditableText().toString().equals(s))
                description.setText(s);
        });
        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                createProductViewModel.setDescription(s.toString());
            }
        });

        createProductViewModel.getPrice().observe(this, s -> {
            if(!price.getEditableText().toString().equals(s))
                price.setText(s);
        });
        price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                createProductViewModel.setPrice(s.toString());
            }
        });

        createProductViewModel.getStockQty().observe(this, s -> {
            if(!stockQty.getEditableText().toString().equals(s))
                stockQty.setText(s);
        });
        stockQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                createProductViewModel.setStockQty(s.toString());
            }
        });

        //--------------------IF HASN'T LOGIN--------------------//
        if(MainActivity.user == null)
            navController.navigate(R.id.navigation_login);

        //--------------------Main Function--------------------//
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
    private void createProduct(){
        inProgress();
        MainActivity.user.createProduct(
                createProductViewModel.getTitle().getValue(),
                createProductViewModel.getDescription().getValue(),
                Integer.parseInt(createProductViewModel.getPrice().getValue()),
                Integer.parseInt(createProductViewModel.getStockQty().getValue())
        );
        Toast.makeText(getContext(), "Creating product is processing...", Toast.LENGTH_SHORT).show();
        getActivity().onBackPressed();
    }
}
