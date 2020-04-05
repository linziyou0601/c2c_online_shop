package com.example.c2c_online_shop.ui.login;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.c2c_online_shop.HttpURLConnectionUtil;
import com.example.c2c_online_shop.MainActivity;
import com.example.c2c_online_shop.R;
import com.example.c2c_online_shop.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, mainLayout;
    EditText account, password;
    Switch switchKeepLogin;
    Button signIn;
    TextView signUp;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "LoginFragment";
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);
        account = root.findViewById(R.id.account);
        password = root.findViewById(R.id.password);
        switchKeepLogin = root.findViewById(R.id.remember_me);
        signIn = root.findViewById(R.id.sign_in_btn);
        signUp = root.findViewById(R.id.sign_up_btn);

        //--------------------Listener Observe--------------------//
        loginViewModel.getAccount().observe(this, s -> {
            if(!account.getEditableText().toString().equals(s))
                account.setText(s);
        });
        account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginViewModel.setAccount(s.toString());
            }
        });

        loginViewModel.getPassword().observe(this, s -> {
            if(!password.getEditableText().toString().equals(s))
                password.setText(s);
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginViewModel.setPassword(s.toString());
            }
        });

        loginViewModel.getKeepLogin().observe(this, s -> {
            if(switchKeepLogin.isChecked() != s) switchKeepLogin.setChecked(s);
        });
        switchKeepLogin.setOnClickListener(view -> {
            loginViewModel.setKeepLogin(((Switch)view).isChecked());
            if(!((Switch)view).isChecked()) loginViewModel.setKeepLogin(false);
        });

        //--------------------Listener Button--------------------//
        signIn.setOnClickListener(arg0 -> login());
        signUp.setOnClickListener(arg0 -> register());

        //--------------------IF HAS LOGIN--------------------//
        if(MainActivity.user != null)
            navController.navigate(R.id.navigation_home);

        //--------------------Main Function--------------------//
        //Hide bottom nav
        outProgress();
        outProgress();

        //When keep login is enable
        if(loginViewModel.getKeepLogin().getValue())
            login();

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
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        navView.setVisibility(View.GONE);
    }

    //--------------------Function--------------------//
    private void login(){
        inProgress();
        new LoginTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/user?account="+loginViewModel.getAccount().getValue());
    }
    private void register(){
        navController.navigate(R.id.navigation_register);
    }

    //--------------------Tasks--------------------//
    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.getDataHttpUriConnection(params[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            processLogin(result);
        }

        private void processLogin(String json){
            Gson gson = new GsonBuilder().setDateFormat("yyyy/MM/dd").create();
            User user = gson.fromJson(json, User.class);

            //attempt to login
            outProgress();
            if(user.getAccount() == null || !user.verifyPassword(loginViewModel.getPassword().getValue())){
                //Prompt login failed
                loginViewModel.setKeepLogin(false);
                new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                        .setTitle("Failed to login.")
                        .setMessage("Incorrect account, password or network is not working.")
                        .setPositiveButton("OK", null)
                        .show();
            }else{
                //Login
                user.login();
                loginViewModel.storeAccountData();
                navController.navigate(R.id.navigation_home);
            }
        }
    }
}
