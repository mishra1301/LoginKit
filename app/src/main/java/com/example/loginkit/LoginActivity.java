package com.example.loginkit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.loginkit.model.Login;
import com.example.loginkit.model.RegisterResponse;
import com.example.loginkit.rest.ApiClient;
import com.example.loginkit.rest.ApiInterface;
import com.example.loginkit.sessionManager.SessionManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.txt_email)
    EditText inputEmail;

    @BindView(R.id.txt_password)
    EditText inputPassword;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }*/

        // set the view now
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sessionManager = new SessionManager(this);

    }

    @OnClick(R.id.btn_link_to_reset_password)
    protected void onResetPassword() {
        startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
    }

    @OnClick(R.id.btn_login)
    protected void onSignIn() {
        String email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        //authenticate user
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<RegisterResponse> call = apiService.signIn(new Login(email, password));
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                int statusCode = response.code();
                if (statusCode == 200) {
                    boolean succeeded = response.body().isSuccess();
                    if (succeeded) {
                        sessionManager.createUserLoginSession(response.body().getData().getFullname(),
                                response.body().getData().getEmail(),
                                response.body().getData().getToken());
                        sessionManager.checkLogin();
                        Toast.makeText(LoginActivity.this, "Logged in!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, response.body().getError().getDescription(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                // Log error here since request failed
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.btn_link_to_register)
    protected void onSignUp() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

}

