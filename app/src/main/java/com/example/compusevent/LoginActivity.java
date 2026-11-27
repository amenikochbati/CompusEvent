package com.example.compusevent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

/** Connexion séparée : l'espace Étudiant et l'espace Admin ont des accès distincts. */
public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private EditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoToRegister, btnRoleStudent, btnRoleAdmin;
    private TextView tvContinueGuest, tvRoleHint, tvDemoAccounts;
    private boolean adminSpaceSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            goToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);
        NavigationHelper.setup(this, "Connexion", "Espace étudiant ou administrateur");

        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoToRegister = findViewById(R.id.btn_go_to_register);
        btnRoleStudent = findViewById(R.id.btn_role_student);
        btnRoleAdmin = findViewById(R.id.btn_role_admin);
        tvContinueGuest = findViewById(R.id.tv_continue_guest);
        tvRoleHint = findViewById(R.id.tv_role_hint);
        tvDemoAccounts = findViewById(R.id.tv_demo_accounts);

        btnRoleStudent.setOnClickListener(v -> selectSpace(false));
        btnRoleAdmin.setOnClickListener(v -> selectSpace(true));
        btnLogin.setOnClickListener(v -> performLogin());

        btnGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        tvContinueGuest.setOnClickListener(v -> goToMainActivity());
        selectSpace(false);
    }

    /** Change l'espace visé et adapte les informations affichées. */
    private void selectSpace(boolean admin) {
        adminSpaceSelected = admin;

        btnRoleStudent.setBackgroundTintList(ContextCompat.getColorStateList(this,
                admin ? R.color.surface_alt : R.color.action_blue));
        btnRoleStudent.setTextColor(ContextCompat.getColor(this,
                admin ? R.color.text_secondary : R.color.white));
        btnRoleStudent.setStrokeColorResource(admin ? R.color.line_strong : R.color.action_blue);

        btnRoleAdmin.setBackgroundTintList(ContextCompat.getColorStateList(this,
                admin ? R.color.navy : R.color.surface_alt));
        btnRoleAdmin.setTextColor(ContextCompat.getColor(this,
                admin ? R.color.white : R.color.text_secondary));
        btnRoleAdmin.setStrokeColorResource(admin ? R.color.navy : R.color.line_strong);

        // L'inscription publique crée uniquement des comptes étudiants.
        btnGoToRegister.setVisibility(admin ? View.GONE : View.VISIBLE);
        tvRoleHint.setText(admin
                ? "Espace Administrateur : création, gestion et suivi des événements."
                : "Espace Étudiant : favoris, inscriptions et découverte des événements.");
        tvDemoAccounts.setText(admin
                ? "Admin : admin@campus.tn / admin123\n(compte attribué par l'établissement)"
                : "Étudiant : etudiant@campus.tn / student123\nInscription ouverte aux étudiants");
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
        if (user == null) {
            Toast.makeText(this, "Email ou mot de passe incorrect.", Toast.LENGTH_LONG).show();
            return;
        }

        if (user.isAdmin() != adminSpaceSelected) {
            Toast.makeText(this, adminSpaceSelected
                            ? "Accès réservé aux administrateurs. Choisis l'espace Étudiant."
                            : "Accès réservé aux étudiants. Choisis l'espace Admin.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        sessionManager.createSession(user);
        Toast.makeText(this, "Bienvenue " + user.getName() + " !", Toast.LENGTH_SHORT).show();
        goToRoleSpace(user);
    }

    /** Chaque rôle ouvre directement son propre espace. */
    private void goToRoleSpace(User user) {
        Class<?> target = user.isAdmin() ? AdminSpaceActivity.class : UserSpaceActivity.class;
        Intent intent = new Intent(this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
