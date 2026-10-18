package com.example.femmepower;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Principal extends AppCompatActivity {

    private Button btninicio, btncontactos, btnmensaje, btnperfil;
    private TextView txtNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_principal);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // BOTONES
        btninicio = findViewById(R.id.btninicio);
        btncontactos = findViewById(R.id.btncontactos);
        btnmensaje = findViewById(R.id.btnmensaje);
        btnperfil = findViewById(R.id.btnperfil);

        txtNombre = findViewById(R.id.txtNombre);

        // FIREBASE
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {

            String uid = mAuth.getCurrentUser().getUid();

            db.collection("usuarios")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(document -> {

                        if (document.exists()) {

                            String nombre = document.getString("nombre");
                            txtNombre.setText(nombre + " <3");
                        }
                    });
        }

        // 🔥 PEDIR PERMISOS
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.SEND_SMS
                },
                1
        );

        // 🔥 INICIAR SERVICIO
        if (!BluetoothService.isRunning) {

            Intent serviceIntent = new Intent(this, BluetoothService.class);
            startService(serviceIntent);
        }

        // BOTÓN ACTIVO
        seleccionar(btninicio);

        // NAVEGACIÓN
        btncontactos.setOnClickListener(v -> {
            seleccionar(btncontactos);
            startActivity(new Intent(Principal.this, Contacto.class));
        });

        btnmensaje.setOnClickListener(v -> {
            seleccionar(btnmensaje);
            startActivity(new Intent(Principal.this, Mensajes.class));
        });

        btnperfil.setOnClickListener(v -> {
            seleccionar(btnperfil);
            startActivity(new Intent(Principal.this, Inicio.class));
        });

    } // 👈 ESTE CIERRE FALTABA

    // 🔥 MÉTODO SELECCIONAR
    private void seleccionar(Button activo) {

        btninicio.setBackground(getDrawable(R.drawable.inactivo));
        btncontactos.setBackground(getDrawable(R.drawable.inactivo));
        btnmensaje.setBackground(getDrawable(R.drawable.inactivo));
        btnperfil.setBackground(getDrawable(R.drawable.inactivo));

        btninicio.setTextColor(getColor(R.color.azul_fuerte1));
        btncontactos.setTextColor(getColor(R.color.azul_fuerte1));
        btnmensaje.setTextColor(getColor(R.color.azul_fuerte1));
        btnperfil.setTextColor(getColor(R.color.azul_fuerte1));

        activo.setBackground(getDrawable(R.drawable.activo));
        activo.setTextColor(getColor(R.color.morado2));
    }
}