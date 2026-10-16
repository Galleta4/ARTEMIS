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

// Importaciones para la autenticación y base de datos de Firebase
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainInicioSesion), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance(); //controla el inicio de sesión y autenticación
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // instancia de la base de datos Firestore

        //Vinculación de las variables locales con los elementos del XML mediante sus ID

        btnLogin = findViewById(R.id.btnLogin);
        Usuario = findViewById(R.id.etUsuario);
        Contra = findViewById(R.id.etContra);
        Registro = findViewById(R.id.tvRegistro);


        Registro.setOnClickListener(v -> {
            Intent intent = new Intent(Inicio_sesion.this, Registro.class);
            startActivity(intent);
        }); // LLeva a registro si no tiene una cuenta para iniciar sesión

        // Boton que inicia sesion

        btnLogin.setOnClickListener(v -> {

            //se valida que las entradas de usuario y contraseña sean correctas
            if (valUsuario(Usuario) && valContra(Contra)) {
                //Obtención del texto ingresado sin espacios
                String usuario = Usuario.getText().toString().trim();
                String password = Contra.getText().toString().trim();

                // Buscar correo en Firestore a partir del usuario
                db.collection("usuarios")
                        .whereEqualTo("nombre", usuario)
                        .get()
                        .addOnSuccessListener(query -> {
                            if (!query.isEmpty()) {
                                String email = query.getDocuments().get(0).getString("email");

                                if (email != null && !email.isEmpty()) {
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
                                    Toast.makeText(this, "El usuario no tiene un correo registrado", Toast.LENGTH_SHORT).show();
                                }
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

    /**
     * Métodos de Validación
     */


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

    //Validacion del campo de contraseña
    public boolean valContra(EditText Contra) {
        String contra = Contra.getText().toString().trim();

        if (contra.isEmpty()) {
            Contra.setError("Ingrese su contraseña");
            return false;
        } else if (contra.length() < 6) { // Requisito mínimo de longitud exigido por Firebase Authentication
            Contra.setError("La contraseña debe tener al menos 6 caracteres");
            return false;
        } else {
            Contra.setError(null); // Quita el mensaje de error si es válido
            return true;
        }
    }
}

