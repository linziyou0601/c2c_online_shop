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


public class EditProductFragment extends Fragment {

    private CreateProductViewModel editProductViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, mainLayout;

    EditText title, description, price, stockQty;
    Button edit_btn;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "EditProductFragment";
        editProductViewModel = ViewModelProviders.of(this).get(CreateProductViewModel.class);
        View root = inflater.inflate(R.layout.fragment_edit_product, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);
        edit_btn = root.findViewById(R.id.edit_btn);
        title = root.findViewById(R.id.title);
        description = root.findViewById(R.id.description);
        price = root.findViewById(R.id.price);
        stockQty = root.findViewById(R.id.stockQty);

        //--------------------Listener Button--------------------//
        edit_btn.setOnClickListener(arg0 -> editProduct());

        //--------------------Listener Observe--------------------//
        editProductViewModel.getTitle().observe(this, s -> {
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
                editProductViewModel.setTitle(s.toString());
            }
        });

        editProductViewModel.getDescription().observe(this, s -> {
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
                editProductViewModel.setDescription(s.toString());
            }
        });

        editProductViewModel.getPrice().observe(this, s -> {
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
                editProductViewModel.setPrice(s.toString());
            }
        });

        editProductViewModel.getStockQty().observe(this, s -> {
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
                editProductViewModel.setStockQty(s.toString());
            }
        });

        //--------------------IF HASN'T LOGIN--------------------//
        if(MainActivity.user == null)
            navController.navigate(R.id.navigation_login);

        //--------------------Main Function--------------------//
        outProgress();
        title.setText(MainActivity.temp_pd.getTitle());
        description.setText(MainActivity.temp_pd.getDescription());
        price.setText(Integer.toString(MainActivity.temp_pd.getPrice()));
        stockQty.setText(Integer.toString(MainActivity.temp_pd.getStockQty()));

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
    private void editProduct(){
        inProgress();
        MainActivity.temp_pd.editProduct(
                editProductViewModel.getTitle().getValue(),
                editProductViewModel.getDescription().getValue(),
                Integer.parseInt(editProductViewModel.getPrice().getValue()),
                Integer.parseInt(editProductViewModel.getStockQty().getValue())
        );
        Toast.makeText(getContext(), "Editing product is processing...", Toast.LENGTH_SHORT).show();
        getActivity().onBackPressed();
    }
}
