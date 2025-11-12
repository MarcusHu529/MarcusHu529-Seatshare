package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Entry Point for User Authentication
 */
public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_SIGNIN_MODE = "signin_mode";
    public static final String MODE_MSU = "MSU";
    public static final String MODE_NON_MSU = "NON_MSU";

    private Button btnCreateAccount;
    private Button btnLoginMSU;
    private Button btnLoginGoogle;
    private TextView tvBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnLoginMSU = findViewById(R.id.btnLoginMSU);
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        tvBackToHome = findViewById(R.id.tvBackToHome);

        // Create Account Page
        btnCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        // MSU Sign In
        btnLoginMSU.setOnClickListener(v -> {
            Intent i = new Intent(this, SignInActivity.class);
            i.putExtra(EXTRA_SIGNIN_MODE, MODE_MSU);
            startActivity(i);
        });

        // Non-MSU Sing In
        btnLoginGoogle.setOnClickListener(v -> {
            Intent i = new Intent(this, SignInActivity.class);
            i.putExtra(EXTRA_SIGNIN_MODE, MODE_NON_MSU);
            startActivity(i);
        });

        tvBackToHome.setOnClickListener(v -> finish());
    }
}
