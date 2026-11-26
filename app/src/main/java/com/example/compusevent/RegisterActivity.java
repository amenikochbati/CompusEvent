package com.example.compusevent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private EditText etName, etEmail, etPassword;
    private RadioGroup rgRole;
    private RadioButton rbRoleUser, rbRoleAdmin;
    private MaterialButton btnRegister;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        NavigationHelper.setup(this, "Inscription", "Rejoins la communauté");

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        etName = findViewById(R.id.et_reg_name);
        etEmail = findViewById(R.id.et_reg_email);
        etPassword = findViewById(R.id.et_reg_password);
        rgRole = findViewById(R.id.rg_reg_role);
        rbRoleUser = findViewById(R.id.rb_role_user);
        rbRoleAdmin = findViewById(R.id.rb_role_admin);
        btnRegister = findViewById(R.id.btn_register);
        tvGoToLogin = findViewById(R.id.tv_go_to_login);

        btnRegister.setOnClickListener(v -> performRegister());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void performRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Veuillez saisir votre nom");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Veuillez saisir votre adresse email");
            etEmail.requestFocus();
            return;
        }

        if (password.length() < 4) {
            etPassword.setError("Le mot de passe doit contenir au moins 4 caractères");
            etPassword.requestFocus();
            return;
        }

        String selectedRole = rbRoleAdmin.isChecked() ? User.ROLE_ADMIN : User.ROLE_USER;

        boolean success = dbHelper.registerUser(name, email, password, selectedRole);
        if (success) {
            User newUser = dbHelper.loginUser(email, password);
            if (newUser != null) {
                sessionManager.createSession(newUser);
            }
            Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Cette adresse email est déjà utilisée.", Toast.LENGTH_LONG).show();
        }
    }
}
