package com.example.femmepower;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Mensajes extends AppCompatActivity {

    EditText mensajeEdit;
    Switch switchGPS, switchPredeterminado, switchPersonalizado;
    Button btnGuardar;
    private Button btninicio, btncontactos, btnmensaje, btnperfil;

    FirebaseFirestore db;
    FirebaseAuth auth;

    SharedPreferences prefs;

    boolean cargando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mensajes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // SharedPreferences
        prefs = getSharedPreferences("femme_power", MODE_PRIVATE);

        // NAV
        btninicio = findViewById(R.id.btninicio);
        btncontactos = findViewById(R.id.btncontactos);
        btnmensaje = findViewById(R.id.btnmensaje);
        btnperfil = findViewById(R.id.btnperfil);

        seleccionar(btnmensaje);

        btncontactos.setOnClickListener(v -> startActivity(new Intent(this, Contacto.class)));
        btninicio.setOnClickListener(v -> startActivity(new Intent(this, Principal.class)));

        //CAMPOS
        mensajeEdit = findViewById(R.id.sms);
        switchGPS = findViewById(R.id.gps);
        switchPredeterminado = findViewById(R.id.msjP);
        switchPersonalizado = findViewById(R.id.msj);
        btnGuardar = findViewById(R.id.button);

        // VALIDAR USUARIO Y CARGAR
        if (auth.getCurrentUser() != null) {
            cargarDesdeFirebase();
        } else {
            cargarLocal();
        }

        // SWITCHES
        switchPredeterminado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (cargando) return;
            if (isChecked) {
                switchPersonalizado.setChecked(false);
                mensajeEdit.setText("Estoy en peligro, necesito ayuda.");
                mensajeEdit.setEnabled(false);
            }
        });

        switchPersonalizado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (cargando) return;

            if (isChecked) {
                switchPredeterminado.setChecked(false);
                mensajeEdit.setText("");
                mensajeEdit.setEnabled(true);
            }
        });

        // GUARDAR
        btnGuardar.setOnClickListener(view -> {

            String mensaje = mensajeEdit.getText().toString();

            if (mensaje.isEmpty()) {
                Toast.makeText(this, "Escribe un mensaje", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean gps = switchGPS.isChecked();
            boolean personalizado = switchPersonalizado.isChecked();

            // LOCAL
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("mensaje", mensaje);
            editor.putBoolean("gps", gps);
            editor.putBoolean("personalizado", personalizado);
            editor.apply();

            // FIREBASE
            if (auth.getCurrentUser() != null) {

                String uid = auth.getCurrentUser().getUid();

                Map<String, Object> config = new HashMap<>();
                config.put("mensaje", mensaje);
                config.put("gps", gps);
                config.put("personalizado", personalizado);

                db.collection("usuarios")
                        .document(uid)
                        .collection("configuracion")
                        .document("mensajes")
                        .set(config);
            }

            Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarDesdeFirebase() {

        String uid = auth.getCurrentUser().getUid();

        cargando = true;

        db.collection("usuarios")
                .document(uid)
                .collection("configuracion")
                .document("mensajes")
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        String mensaje = doc.getString("mensaje");
                        Boolean gps = doc.getBoolean("gps");
                        Boolean personalizado = doc.getBoolean("personalizado");

                        if (mensaje != null) {
                            mensajeEdit.setText(mensaje);
                        }

                        switchGPS.setChecked(gps != null && gps);

                        if (personalizado != null && personalizado) {
                            switchPersonalizado.setChecked(true);
                            mensajeEdit.setEnabled(true);
                        } else {
                            switchPredeterminado.setChecked(true);
                            mensajeEdit.setEnabled(false);

                            if (mensaje == null || mensaje.isEmpty()) {
                                mensajeEdit.setText("Estoy en peligro, necesito ayuda.");
                            }
                        }
                    } else {
                        cargarLocal();
                    }

                    cargando = false;
                })
                .addOnFailureListener(e -> {
                    cargarLocal();
                    cargando = false;
                });
    }

    // LOCAL
    private void cargarLocal() {

        String mensaje = prefs.getString("mensaje", "");
        boolean gps = prefs.getBoolean("gps", false);
        boolean personalizado = prefs.getBoolean("personalizado", false);

        mensajeEdit.setText(mensaje);
        switchGPS.setChecked(gps);

        if (personalizado) {
            switchPersonalizado.setChecked(true);
            mensajeEdit.setEnabled(true);
        } else {
            switchPredeterminado.setChecked(true);
            mensajeEdit.setEnabled(false);

            if (mensaje.isEmpty()) {
                mensajeEdit.setText("Estoy en peligro, necesito ayuda.");
            }
        }
    }

    private void seleccionar(Button activo) {
        btninicio.setBackground(getDrawable(R.drawable.inactivo));
        btncontactos.setBackground(getDrawable(R.drawable.inactivo));
        btnmensaje.setBackground(getDrawable(R.drawable.inactivo));
        btnperfil.setBackground(getDrawable(R.drawable.inactivo));

        activo.setBackground(getDrawable(R.drawable.activo));
    }
}