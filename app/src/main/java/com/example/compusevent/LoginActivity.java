package com.example.compusevent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private EditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoToRegister;
    private TextView tvContinueGuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            goToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);
        NavigationHelper.setup(this, "Connexion", "Espace étudiant");

        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoToRegister = findViewById(R.id.btn_go_to_register);
        tvContinueGuest = findViewById(R.id.tv_continue_guest);

        btnLogin.setOnClickListener(v -> performLogin());

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvContinueGuest.setOnClickListener(v -> goToMainActivity());
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Veuillez saisir votre adresse email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Veuillez saisir votre mot de passe");
            etPassword.requestFocus();
            return;
        }

        User user = dbHelper.loginUser(email, password);
        if (user != null) {
            sessionManager.createSession(user);
            String roleText = user.isAdmin() ? "Administrateur" : "Étudiant";
            Toast.makeText(this, "Bienvenue " + user.getName() + " (" + roleText + ") !", Toast.LENGTH_SHORT).show();
            goToMainActivity();
        } else {
            Toast.makeText(this, "Email ou mot de passe incorrect.", Toast.LENGTH_LONG).show();
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
