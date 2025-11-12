package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
/**
 * Handles new user/account creation
 */
public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirm;
    private Button btnSignUp;
    private TextView tvGoToLogin;

    private final AuthRepository repo = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnSignUp.setOnClickListener(v -> doSignUp());
        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        addBackButtonToConstraintBelowCard(
                "← Back to Login",
                () -> { startActivity(new Intent(this, LoginActivity.class)); finish(); }
        );
    }

    // Handles a sign up request (email verification/validation)
    private void doSignUp() {
        String name = textOf(etName);
        String email = textOf(etEmail);
        String pw = textOf(etPassword);
        String confirm = textOf(etConfirm);

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(pw) || TextUtils.isEmpty(confirm)) {
            toast("Please fill in all fields");
            return;
        }
        if (!pw.equals(confirm)) {
            toast("Passwords do not match");
            return;
        }

        btnSignUp.setEnabled(false);

        repo.userExists(email).addOnCompleteListener(existsTask -> {
            boolean exists = existsTask.isSuccessful() && Boolean.TRUE.equals(existsTask.getResult());
            if (exists) {
                btnSignUp.setEnabled(true);
                toast("An account already exists for this email.");
                return;
            }

            repo.signUpEmail(email, pw).addOnCompleteListener(t -> {
                btnSignUp.setEnabled(true);
                if (t.isSuccessful()) {
                    repo.updateDisplayName(name);
                    toast("Account created.");
                    goAccount();
                } else {
                    String msg = (t.getException() != null && !TextUtils.isEmpty(t.getException().getMessage()))
                            ? t.getException().getMessage() : "Sign up failed";
                    toast(msg);
                }
            });
        });
    }

    // Directs user to Account activity page
    private void goAccount() {
        startActivity(new Intent(this, AccountActivity.class));
        finishAffinity();
    }

    private String textOf(EditText e) { return e.getText() == null ? "" : e.getText().toString().trim(); }
    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }

    // Back button functionality
    private void addBackButtonToConstraintBelowCard(String label, Runnable onClick) {
        TextView back = new TextView(this);
        back.setId(View.generateViewId());
        back.setText(label);
        back.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        back.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        back.setTypeface(back.getTypeface(), android.graphics.Typeface.BOLD);
        back.setClickable(true);

        TypedValue out = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, out, true);
        back.setBackgroundResource(out.resourceId);

        int pad = dp(12);
        back.setPadding(pad, dp(32), pad, dp(32));

        ConstraintLayout parent = (ConstraintLayout) findViewById(R.id.tvTitle).getParent();
        parent.addView(back);

        ConstraintSet set = new ConstraintSet();
        set.clone(parent);
        set.connect(back.getId(), ConstraintSet.TOP, R.id.card, ConstraintSet.BOTTOM, dp(32));
        set.connect(back.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        set.connect(back.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        set.applyTo(parent);

        back.setOnClickListener(v -> onClick.run());
    }

    private int dp(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * dps);
    }
}
