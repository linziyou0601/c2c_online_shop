package com.example.c2c_online_shop.ui.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
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

import com.example.c2c_online_shop.MainActivity;
import com.example.c2c_online_shop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, mainLayout;

    EditText name, phone, birth, address;
    TextView accountText, emailText;
    Button save_btn, date_btn;

    Calendar calendar = Calendar.getInstance();
    int yearA = calendar.get(Calendar.YEAR);
    int monthA = calendar.get(Calendar.MONTH);
    int dayA = calendar.get(Calendar.DAY_OF_MONTH);

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "ProfileFragment";
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);

        name = root.findViewById(R.id.name);
        phone = root.findViewById(R.id.phone);
        birth = root.findViewById(R.id.birth);
        address = root.findViewById(R.id.address);
        accountText = root.findViewById(R.id.accountText);
        emailText = root.findViewById(R.id.emailText);
        save_btn = root.findViewById(R.id.save_btn);
        date_btn = root.findViewById(R.id.date_btn);

        //--------------------Listener Button--------------------//
        save_btn.setOnClickListener(arg0 -> save());
        date_btn.setOnClickListener(arg0 -> datePicker());

        //--------------------Listener Observe--------------------//
        profileViewModel.getName().observe(this, s -> {
            if(!name.getEditableText().toString().equals(s))
                name.setText(s);
        });
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                profileViewModel.setName(s.toString());
            }
        });

        profileViewModel.getPhone().observe(this, s -> {
            if(!phone.getEditableText().toString().equals(s))
                phone.setText(s);
        });
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                profileViewModel.setPhone(s.toString());
            }
        });

        profileViewModel.getBirth().observe(this, s -> {
            if(s!=null) {
                DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                String formattedDate = formatter.format(s);
                if (!birth.getEditableText().toString().equals(formattedDate))
                    birth.setText(formattedDate);
            }
        });
        birth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                String dateInString = s.toString();
                Date date = null;
                try {
                    date = formatter.parse(dateInString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                profileViewModel.setBirth(date);
            }
        });

        profileViewModel.getAddress().observe(this, s -> {
            if(!address.getEditableText().toString().equals(s))
                address.setText(s);
        });
        address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                profileViewModel.setAddress(s.toString());
            }
        });

        //--------------------IF HASN'T LOGIN--------------------//
        if(MainActivity.user == null)
            navController.navigate(R.id.navigation_login);

        //--------------------Main Function--------------------//
        outProgress();
        accountText.setText(MainActivity.user.getAccount());
        emailText.setText(MainActivity.user.getProfile().getEmail());
        name.setText(MainActivity.user.getProfile().getName());
        phone.setText(MainActivity.user.getProfile().getPhone());
        address.setText(MainActivity.user.getProfile().getAddress());

        if(MainActivity.user.getProfile().getBirth()!=null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(MainActivity.user.getProfile().getBirth());
            yearA = cal.get(Calendar.YEAR);
            monthA = cal.get(Calendar.MONTH);
            dayA = cal.get(Calendar.DAY_OF_MONTH);
            birth.setText(yearA+"/"+(monthA+1)+"/"+dayA);
        }

        return root;
    }

    public void datePicker(){
        new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String dateTime = year+"/"+(month+1)+"/"+day;
                yearA = year;
                monthA = month;
                dayA = day;
                birth.setText(dateTime);
            }
        }, yearA, monthA, dayA).show();
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
    private void save(){
        MainActivity.user.getProfile().edit(
                profileViewModel.getName().getValue(),
                profileViewModel.getPhone().getValue(),
                MainActivity.user.getProfile().getEmail(),
                profileViewModel.getAddress().getValue(),
                profileViewModel.getBirth().getValue()
        );
        Toast.makeText(getContext(), "Editing profile is processing...", Toast.LENGTH_SHORT).show();
        getActivity().onBackPressed();
    }
}
