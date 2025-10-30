package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quizapp.databinding.ActivityLoginBinding;
import com.example.quizapp.UserManager;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding b;
    private UserManager um;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // ← initialize UserManager once
        um = new UserManager(this);

        b.loginButton.setOnClickListener(v -> {
            String username = b.usernameInput.getText().toString().trim();
            String password = b.passwordInput.getText().toString();

            // TODO: replace with real authentication
            if (username.isEmpty()) {
                b.usernameInput.setError("Username required");
                return;
            }

            // ← NEW: only allow known users
            if (!um.exists(username)) {
                b.usernameInput.setError("Unknown user");
                return;
            }

            um.setCurrent(username);
            String currentEmail = um.getCurrentEmail();
            String currentPlan  = um.getCurrentPlan();

            // Navigate to HomeActivity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        b.needAccount.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class))
        );
    }
}
