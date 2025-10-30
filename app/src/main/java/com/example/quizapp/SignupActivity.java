package com.example.quizapp;
import android.content.Intent;
import com.example.quizapp.UserManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quizapp.databinding.ActivitySignupBinding;


public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding b;
    private UserManager um;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        um = new UserManager(this);
        b.createAccountButton.setOnClickListener(v -> {
            String username = b.usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                b.usernameInput.setError("Username required");
                return;
            }
            String user_email = b.emailInput.getText().toString().trim();
            if (user_email.isEmpty()) {
                b.emailInput.setError("Email required");
                return;
            }

            um.addUserAndSetCurrent(username);
            um.setEmail(username, user_email);
            um.setPlan(username, "Free");

            startActivity(new Intent(this, InterestsActivity.class));
            finish();
        });
    }
}