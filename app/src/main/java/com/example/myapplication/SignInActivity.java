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
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Handles a Log In activity for both MSU and non-MSU email
 */
public class SignInActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvGoToSignUp;
    private final AuthRepository repo = new AuthRepository();
    private String mode = LoginActivity.MODE_MSU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp);

        String fromIntent = getIntent().getStringExtra(LoginActivity.EXTRA_SIGNIN_MODE);
        if (!TextUtils.isEmpty(fromIntent)) mode = fromIntent;

        btnSignIn.setOnClickListener(v -> doSignIn());
        tvGoToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });

        addBackButtonToConstraintBelowCard(
                "← Back to Login",
                () -> { startActivity(new Intent(this, LoginActivity.class)); finish(); }
        );
    }

    // Executes a Sign In activity/action
    private void doSignIn() {
        final String email = textOf(etEmail);
        final String pw = textOf(etPassword);
        final boolean isMsu = AuthRepository.isMSU(email);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pw)) {
            toast("Enter email & password");
            return;
        }
        if (LoginActivity.MODE_MSU.equals(mode) && !isMsu) {
            toast("Please use an @msu.edu email for this sign in.");
            return;
        }
        if (LoginActivity.MODE_NON_MSU.equals(mode) && isMsu) {
            toast("Please use a non-MSU email for this sign in.");
            return;
        }

        btnSignIn.setEnabled(false);

        (LoginActivity.MODE_MSU.equals(mode)
                ? repo.signInEmailMSU(email, pw)
                : repo.signInEmail(email, pw))
                .addOnCompleteListener(t -> {
                    btnSignIn.setEnabled(true);

                    if (t.isSuccessful()) {
                        FirebaseUser u = repo.currentUser();
                        if (u == null) { toast("Login succeeded but user missing."); return; }
                        goAccount();
                        return;
                    }

                    Throwable ex = t.getException();
                    if (ex instanceof FirebaseAuthInvalidUserException) {
                        toast("No account found for this email.");
                    } else if (ex instanceof FirebaseAuthInvalidCredentialsException) {
                        toast("Incorrect password. Please try again.");
                    } else if (ex instanceof FirebaseAuthException) {
                        String code = ((FirebaseAuthException) ex).getErrorCode();
                        if ("ERROR_USER_NOT_FOUND".equals(code)) {
                            toast("No account found for this email.");
                        } else if ("ERROR_WRONG_PASSWORD".equals(code)) {
                            toast("Incorrect password. Please try again.");
                        } else if ("MSU_ONLY".equals(code)) {
                            toast("Please use an @msu.edu email for this sign in.");
                        } else {
                            toast("Login failed: " + code);
                        }
                    } else {
                        toast(ex != null && !TextUtils.isEmpty(ex.getMessage())
                                ? ex.getMessage()
                                : "Login failed");
                    }
                });
    }

    // Directs user to account page
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
        back.setTextColor(getResources().getColor(android.R.color.white));
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
