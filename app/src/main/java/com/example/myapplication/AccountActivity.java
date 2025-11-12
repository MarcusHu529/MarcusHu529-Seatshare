package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Account settings page
 */
public class AccountActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SpartySpreadsPrefs";
    private static final String KEY_USER_LOGGED_IN = "user_logged_in";
    private static final String KEY_USER_NAME = "user_name";

    private EditText etName;
    private EditText etEmail;
    private EditText etReauthEmailPass;
    private EditText etNewPass;
    private EditText etReauthPass;
    private Button btnUpdateName;
    private Button btnUpdateEmail;
    private Button btnUpdatePassword;
    private Button btnLogout;
    private Button btnSave;

    private final AuthRepository authRepo = new AuthRepository();
    private FirebaseUser current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etReauthEmailPass = findViewById(R.id.etReauthEmailPass);
        etNewPass = findViewById(R.id.etNewPass);
        etReauthPass = findViewById(R.id.etReauthPass);
        btnUpdateName = findViewById(R.id.btnUpdateName);
        btnUpdateEmail = findViewById(R.id.btnUpdateEmail);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        btnLogout = findViewById(R.id.btnLogout);
        btnSave = findViewById(R.id.btnSave);

        current = authRepo.currentUser();
        if (current == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        if (!TextUtils.isEmpty(current.getDisplayName())) etName.setText(current.getDisplayName());
        if (!TextUtils.isEmpty(current.getEmail())) etEmail.setText(current.getEmail());

        btnUpdateName.setVisibility(View.GONE);
        btnUpdateEmail.setVisibility(View.GONE);
        btnUpdatePassword.setVisibility(View.GONE);

        btnSave.setOnClickListener(v -> performCombinedSave());

        btnLogout.setOnClickListener(v -> {
            authRepo.signOut();
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                    .putBoolean(KEY_USER_LOGGED_IN, false)
                    .remove(KEY_USER_NAME)
                    .apply();

            toast("Signed out");
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        TextView back = findViewById(R.id.tvBackToHome);
        if (back != null) {
            back.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        } else {
            addBackButtonBelowCard("← Back to Home");
        }
    }

    // Change account info functionality
    private void performCombinedSave() {
        String newName = safeText(etName);
        String newEmail = safeText(etEmail);
        String newPw = safeText(etNewPass);
        String curPw = safeText(etReauthPass);

        String curDisplayName = current.getDisplayName() == null ? "" : current.getDisplayName();
        String curEmail = current.getEmail() == null ? "" : current.getEmail();

        List<Task<?>> ops = new ArrayList<>();

        // Name
        if (!TextUtils.isEmpty(newName) && !newName.equals(curDisplayName)) {
            Task<?> t = authRepo.updateDisplayName(newName).addOnCompleteListener(done -> {
                if (done.isSuccessful()) {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    prefs.edit().putString(KEY_USER_NAME, newName).apply();
                }
            });
            ops.add(t);
        }

        if (!TextUtils.isEmpty(newEmail) && !newEmail.equals(curEmail)) {
            if (TextUtils.isEmpty(curPw)) {
                toast("Enter current password to confirm email change");
                return;
            }
            ops.add(authRepo.updateEmail(newEmail, curPw));
        }

        if (!TextUtils.isEmpty(newPw)) {
            if (TextUtils.isEmpty(curPw)) {
                toast("Enter current password to confirm password change");
                return;
            }
            if (TextUtils.isEmpty(curEmail)) {
                toast("No email on file for reauthentication");
                return;
            }
            ops.add(authRepo.updatePassword(curEmail, curPw, newPw));
        }

        if (ops.isEmpty()) {
            toast("Nothing to save");
            return;
        }

        Tasks.whenAllComplete(ops).addOnCompleteListener(all -> {
            boolean anyError = false;
            Exception firstErr = null;
            for (Task<?> t : all.getResult()) {
                if (!t.isSuccessful()) {
                    anyError = true;
                    if (firstErr == null) firstErr = t.getException();
                }
            }
            if (anyError) {
                toast(errorText(firstErr, "Some updates failed"));
            } else {
                toast("Account updated");
            }
        });
    }

    // Back button functionality
    private void addBackButtonBelowCard(String label) {
        TextView back = new TextView(this);
        back.setId(View.generateViewId());
        back.setText(label);
        back.setTextColor(getResources().getColor(android.R.color.white));
        back.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        back.setTypeface(back.getTypeface(), Typeface.BOLD);

        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tv, true);
        back.setBackgroundResource(tv.resourceId);

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

        back.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private String safeText(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private String errorText(Throwable ex, String fallback) {
        return (ex != null && !TextUtils.isEmpty(ex.getMessage())) ? ex.getMessage() : fallback;
    }

    private int dp(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * dps);
    }
}
