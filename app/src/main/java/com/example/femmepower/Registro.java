package com.example.femmepower;

import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Patterns;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registro extends AppCompatActivity {

    private TextInputEditText etPassword, etValidarPassword;
    private EditText etNombre, etEmail;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        etNombre = findViewById(R.id.Usuario);
        etEmail = findViewById(R.id.Email);
        etPassword = findViewById(R.id.Password);
        etValidarPassword = findViewById(R.id.confirmPassword);
        btnRegistrar = findViewById(R.id.btn_registrar);

        btnRegistrar.setOnClickListener(v -> {

            String nombre = etNombre.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String validarPassword = etValidarPassword.getText().toString().trim();

            // Campos vacíos
            if (nombre.isEmpty() || email.isEmpty()
                    || password.isEmpty() || validarPassword.isEmpty()) {
                Toast.makeText(Registro.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            }
            // Usuario corto
            else if (nombre.length() < 4) {
                etNombre.setError("El usuario debe tener mínimo 4 caracteres");
            }
            // Correo inválido
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Correo inválido");
            }
            // Contraseña segura
            else if (!password.matches(
                    "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
                etPassword.setError("La contraseña debe tener mínimo 8 caracteres,\n" + "una mayúscula, un número y un símbolo");
            }
            // Contraseñas diferentes
            else if (!password.equals(validarPassword)) {
                Toast.makeText(Registro.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            }
            // Registrar en Firebase
            else {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                //Guaardar el usuario en Firestore
                                FirebaseFirestore us= FirebaseFirestore.getInstance();
                                String uid = mAuth.getCurrentUser().getUid();

                                Map<String, Object> Usuario = new HashMap<>();

                                Usuario.put("nombre", nombre);
                                Usuario.put("email", email);

                                us.collection("usuarios").document(uid).set(Usuario)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, Inicio_sesion.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });

                            } else {
                                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}