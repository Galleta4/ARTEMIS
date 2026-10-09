package com.example.femmepower;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Se usara para el patrón de gmail
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Inicio_sesion extends AppCompatActivity {

    private Button btnLogin;
    private EditText Usuario, Contra;
    private TextView Registro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_sesion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        btnLogin = findViewById(R.id.btnLogin);
        Usuario = findViewById(R.id.Usuario);
        Contra = findViewById(R.id.Contra);
        Registro = findViewById(R.id.Registro);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Inicio_sesion.this, Principal.class);
            startActivity(intent);
            finish();
        }); // btnLogin


        Registro.setOnClickListener(v -> {
            Intent intent = new Intent(Inicio_sesion.this, Registro.class);
            startActivity(intent);
        }); // Debe de llevar a Registro

        btnLogin.setOnClickListener(v -> {
            if (valUsuario(Usuario) && valContra(Contra)) {

                String usuario = Usuario.getText().toString().trim();
                String password = Contra.getText().toString().trim();

                // Buscar correo en Firestore a partir del usuario
                db.collection("usuarios")
                        .whereEqualTo("nombre", usuario)
                        .get()
                        .addOnSuccessListener(query -> {
                            if (!query.isEmpty()) {
                                String email = query.getDocuments().get(0).getString("email");

                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(this,
                                                        "¡Bienvenida, " + usuario + "! 💜",
                                                        Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(this, Principal.class));
                                                finish();
                                            } else {
                                                Toast.makeText(this,
                                                        "Contraseña incorrecta",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Usuario.setError("Usuario no encontrado");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

    }

    //Validacion de estructura del usuario y si esta vacio
    public boolean valUsuario(EditText Usuario) {
        String usuario = Usuario.getText().toString().trim();

        if (usuario.isEmpty()) {
            Usuario.setError("Ingrese su usuario");
            return false;
        } else if (usuario.length() < 4) {
            Usuario.setError("El usuario debe tener mínimo 4 caracteres");
            return false;
        } else {
            Usuario.setError(null);
            return true;
        }
    }

    //Validacion de contraseña vacía --- Falta poner que sean 8 caract
    public boolean valContra(EditText Contra) {
        String contra = Contra.getText().toString().trim();

        if (contra.isEmpty()) {
            Contra.setError("Ingrese su contraseña");
            return false;
        }
        else {
            Contra.setError(null);
            return true;
        }
    }
}

