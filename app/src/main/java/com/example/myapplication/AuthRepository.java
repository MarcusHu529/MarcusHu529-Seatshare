package com.example.myapplication;

import android.text.TextUtils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

import java.util.Locale;

/**
 * AuthRepository
 *
 * Helper class for
 * - Email Verification
 * - Password Verification
 * - Provides Sign In and Sign Up
 * - Updates Users Info
 */
public class AuthRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private static final boolean REQUIRE_EMAIL_VERIFICATION_ON_SIGNIN = false;

    // Email always lower case (not case sensitive)
    private static String canonicalEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.US);
    }

    // Checks if it is an MSU email
    public static boolean isMSU(String email) {
        return canonicalEmail(email).endsWith("@msu.edu");
    }

    // Check if the user exists in the Firebase
    public Task<Boolean> userExists(String rawEmail) {
        String email = canonicalEmail(rawEmail);
        if (TextUtils.isEmpty(email)) {
            return com.google.android.gms.tasks.Tasks.forResult(false);
        }
        return auth.fetchSignInMethodsForEmail(email)
                .continueWith(t -> t.isSuccessful()
                        && t.getResult() != null
                        && t.getResult().getSignInMethods() != null
                        && !t.getResult().getSignInMethods().isEmpty());
    }

    // Account Setup on firebase
    public Task<FirebaseUser> signUpEmail(String rawEmail, String password) {
        final String email = canonicalEmail(rawEmail);
        return auth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(createTask -> {
                    if (!createTask.isSuccessful()) {
                        Exception e = createTask.getException();
                        return com.google.android.gms.tasks.Tasks.forException(
                                e != null ? e : new IllegalStateException("CREATE_FAILED"));
                    }
                    AuthResult res = createTask.getResult();
                    FirebaseUser u = res != null ? res.getUser() : null;
                    if (u == null) {
                        return com.google.android.gms.tasks.Tasks.forException(
                                new IllegalStateException("NO_USER_AFTER_CREATE"));
                    }
                    return com.google.android.gms.tasks.Tasks.forResult(u);
                });
    }

    // Sign Into email that exists on firebase
    public Task<FirebaseUser> signInEmail(String rawEmail, String password) {
        final String email = canonicalEmail(rawEmail);
        return auth.signInWithEmailAndPassword(email, password)
                .onSuccessTask(r -> {
                    FirebaseUser u = r.getUser();
                    if (u == null) {
                        return com.google.android.gms.tasks.Tasks.forException(
                                new IllegalStateException("NO_USER"));
                    }
                    if (!REQUIRE_EMAIL_VERIFICATION_ON_SIGNIN) {
                        return com.google.android.gms.tasks.Tasks.forResult(u);
                    }
                    if (u.isEmailVerified()) {
                        return com.google.android.gms.tasks.Tasks.forResult(u);
                    }
                    return com.google.android.gms.tasks.Tasks.forException(
                            new IllegalStateException("EMAIL_NOT_VERIFIED"));
                });
    }

    // Sign into MSU email
    public Task<FirebaseUser> signInEmailMSU(String email, String password) {
        if (!isMSU(email)) {
            return com.google.android.gms.tasks.Tasks.forException(
                    new IllegalArgumentException("MSU_ONLY"));
        }
        return signInEmail(email, password);
    }

    // Updates display name
    public Task<Void> updateDisplayName(String newName) {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) return com.google.android.gms.tasks.Tasks.forException(new IllegalStateException("NO_USER"));
        UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName).build();
        return u.updateProfile(req);
    }

    // Updates password
    public Task<Void> updatePassword(String email, String currentPassword, String newPassword) {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) return com.google.android.gms.tasks.Tasks.forException(new IllegalStateException("NO_USER"));
        AuthCredential cred = EmailAuthProvider.getCredential(canonicalEmail(email), currentPassword);
        return u.reauthenticate(cred).onSuccessTask(v -> u.updatePassword(newPassword));
    }

    // Updates email
    public Task<Void> updateEmail(String newEmail, String currentPassword) {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) return com.google.android.gms.tasks.Tasks.forException(new IllegalStateException("NO_USER"));
        String curEmail = u.getEmail() == null ? "" : u.getEmail();
        AuthCredential cred = EmailAuthProvider.getCredential(curEmail, currentPassword);
        return u.reauthenticate(cred).onSuccessTask(v -> u.updateEmail(canonicalEmail(newEmail)));
    }

    // Gets rid of build warning, reloads user
    public Task<Void> reloadUser() {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) return com.google.android.gms.tasks.Tasks.forException(new IllegalStateException("NO_USER"));
        return u.reload();
    }


    // Returns current firebase state
    public FirebaseUser currentUser() { return auth.getCurrentUser(); }
    // Sign out
    public void signOut() { auth.signOut(); }
}
