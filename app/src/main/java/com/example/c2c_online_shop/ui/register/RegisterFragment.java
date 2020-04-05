package com.example.c2c_online_shop.ui.register;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.c2c_online_shop.StringUtil;
import com.example.c2c_online_shop.model.Profile;
import com.example.c2c_online_shop.model.User;
import com.example.c2c_online_shop.model.Wallet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import at.favre.lib.crypto.bcrypt.BCrypt;

import static com.example.c2c_online_shop.StringUtil.validateEmail;

public class RegisterFragment extends Fragment {

    private RegisterViewModel registerViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, mainLayout, otpLayout;
    EditText account, password, confirm_password, name, email, otp;
    Button signUp, verify, back;
    TextView signIn;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "RegisterFragment";
        registerViewModel = ViewModelProviders.of(this).get(RegisterViewModel.class);
        View root = inflater.inflate(R.layout.fragment_register, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);
        otpLayout = root.findViewById(R.id.otpPage);
        account = root.findViewById(R.id.account);
        password = root.findViewById(R.id.password);
        confirm_password = root.findViewById(R.id.confirm_password);
        name = root.findViewById(R.id.name);
        email = root.findViewById(R.id.email);
        otp = root.findViewById(R.id.otp);
        signIn = root.findViewById(R.id.sign_in_btn);
        signUp = root.findViewById(R.id.sign_up_btn);
        verify = root.findViewById(R.id.verify_btn);
        back = root.findViewById(R.id.back_btn);

        //--------------------Listener Observe--------------------//
        registerViewModel.getAccount().observe(this, s -> {
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
                registerViewModel.setAccount(s.toString());
            }
        });

        registerViewModel.getPassword().observe(this, s -> {
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
                registerViewModel.setPassword(s.toString());
            }
        });

        registerViewModel.getConfirmPassword().observe(this, s -> {
            if(!confirm_password.getEditableText().toString().equals(s))
                confirm_password.setText(s);
        });
        confirm_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                registerViewModel.setConfirmPassword(s.toString());
            }
        });

        registerViewModel.getName().observe(this, s -> {
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
                registerViewModel.setName(s.toString());
            }
        });

        registerViewModel.getEmail().observe(this, s -> {
            if(!email.getEditableText().toString().equals(s))
                email.setText(s);
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                registerViewModel.setEmail(s.toString());
            }
        });

        registerViewModel.getOtp().observe(this, s -> {
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
                registerViewModel.setOtp(s.toString());
            }
        });

        //--------------------Listener Button--------------------//
        signUp.setOnClickListener(arg0 -> register());
        signIn.setOnClickListener(arg0 -> login());
        verify.setOnClickListener(arg0 -> verify());
        back.setOnClickListener(arg0 -> back());

        //--------------------IF HAS LOGIN--------------------//
        if(MainActivity.user != null)
            navController.navigate(R.id.navigation_home);

        //--------------------Main Function--------------------//
        //Hide bottom nav
        outProgress();

        return root;
    }

    //--------------------Process--------------------//
    private void inProgress(){
        progressLayout.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        otpLayout.setVisibility(View.GONE);
    }
    private void outProgress(){
        progressLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        otpLayout.setVisibility(View.GONE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        navView.setVisibility(View.GONE);
    }
    private void showOtpPage(){
        otpLayout.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.GONE);
    }

    //--------------------Function--------------------//
    private void register(){
        inProgress();
        new CheckUserTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/user?account="+registerViewModel.getAccount().getValue()+"&email="+registerViewModel.getEmail().getValue());
    }
    private void verify(){
        inProgress();
        Gson gson = new Gson();
        if(Instant.now().getEpochSecond() > MainActivity.emailOtpExpired) {
            showOtpPage();
            new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                    .setTitle("OTP is expired.")
                    .setMessage("Resent a new one")
                    .setPositiveButton("OK", null)
                    .show();
            Map<String, String> emailVerify = new HashMap<>();
            emailVerify.put("email", MainActivity.temp_user.getProfile().getEmail());
            emailVerify.put("otp", StringUtil.generateOtp());
            new SendOtpTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/operate/sendEmailVerify", gson.toJson(emailVerify));
        }else if(!registerViewModel.getOtp().getValue().equals(MainActivity.emailOtp)){
            showOtpPage();
            new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                    .setTitle("OTP is incorrect.")
                    .setMessage("OTP is incorrect, please check again.")
                    .setPositiveButton("OK", null)
                    .show();
        }else{
            new RegisterTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/create/user", gson.toJson(MainActivity.temp_user));
        }
    }
    private void login(){
        navController.navigate(R.id.navigation_login);
    }
    private void back(){
        outProgress();
    }

    //--------------------Tasks--------------------//
    private class CheckUserTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.getDataHttpUriConnection(params[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            processCheckUser(result);
        }

        private void processCheckUser(String json){
            Gson gson = new GsonBuilder().setDateFormat("yyyy/MM/dd").create();
            User user = gson.fromJson(json, User.class);
            if(user.getAccount() != null){
                outProgress();
                new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                        .setTitle("Account or Email is already used.")
                        .setMessage("Account or Email is already used, please change another one.")
                        .setPositiveButton("OK", null)
                        .show();
            }else{
                if(!registerViewModel.getPassword().getValue().equals(registerViewModel.getConfirmPassword().getValue())) {
                    outProgress();
                    new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                            .setTitle("Password is not equal.")
                            .setMessage("Please check if you enter the same password.")
                            .setPositiveButton("OK", null)
                            .show();
                } else if(registerViewModel.getName().getValue().equals("")){
                    outProgress();
                    new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                            .setTitle("Invalid name.")
                            .setMessage("Please check if you enter the valid name.")
                            .setPositiveButton("OK", null)
                            .show();
                } else if(!validateEmail(registerViewModel.getEmail().getValue())){
                    outProgress();
                    new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                            .setTitle("Invalid email.")
                            .setMessage("Please check if you enter the valid email.")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    Profile profile = new Profile(registerViewModel.getName().getValue(), registerViewModel.getEmail().getValue());
                    Wallet wallet = new Wallet();
                    String password_hash = BCrypt.with(BCrypt.Version.VERSION_2Y).hashToString(10, registerViewModel.getPassword().getValue().toCharArray());
                    user = new User(registerViewModel.getAccount().getValue(), password_hash, profile, wallet);
                    MainActivity.temp_user = user;

                    Map<String, String> emailVerify= new HashMap<>();
                    emailVerify.put("email", profile.getEmail());
                    emailVerify.put("otp", StringUtil.generateOtp());
                    new SendOtpTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/operate/sendEmailVerify", gson.toJson(emailVerify));
                }
            }
        }
    }

    private class SendOtpTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
        }
        @Override
        protected void onPostExecute(String result) {
            processSendOtp(result);
        }

        private void processSendOtp(String response){
            Gson gson = new Gson();
            Map<?, ?> obj = gson.fromJson(response, Map.class);
            MainActivity.emailOtp = (String)obj.get("otp");
            MainActivity.emailOtpExpired = Long.parseLong((String)obj.get("otpExpired"));
            showOtpPage();
        }
    }

    private class RegisterTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.postDataHttpUriConnection(params[0], params[1]);
        }
        @Override
        protected void onPostExecute(String result) {
            processRegister(result);
        }

        private void processRegister(String response){
            Gson gson = new Gson();
            Map<?, ?> obj = gson.fromJson(response, Map.class);
            new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                    .setTitle((String)obj.get("title"))
                    .setMessage((String)obj.get("message"))
                    .setPositiveButton("OK", (arg0, arg1) -> login())
                    .show();
        }
    }
}
