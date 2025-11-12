package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

/**
 * Handles Google Sing In activity
 * Abondoned this feature, but might come back to it
 */
public class GoogleGateActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private GoogleSignInClient googleClient;

    private final ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null) { finish(); return; }
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account == null) { finish(); return; }
                    String idToken = account.getIdToken();
                    if (idToken == null) {
                        Toast.makeText(this, "Missing ID token. Check default_web_client_id.", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    AuthCredential cred = GoogleAuthProvider.getCredential(idToken, null);
                    auth.signInWithCredential(cred).addOnCompleteListener(t -> {
                        if (t.isSuccessful()) {
                            startActivity(new Intent(this, AccountActivity.class));
                            finishAffinity();
                        } else {
                            Toast.makeText(this, "Google login failed", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } catch (Exception e) {
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);
        launcher.launch(googleClient.getSignInIntent());
    }
}