package com.example.femmepower;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log; // 👈 Importación requerida para Log
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Alarma extends AppCompatActivity {

    private Button btnCancelar;
    private static final String TAG = "alarma_activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alerta);

        btnCancelar = findViewById(R.id.btnCancelar);

        // Permiso SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, 1);
        }

        // BOTÓN CANCELAR
        btnCancelar.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Cancelar alerta")
                    .setMessage("¿Segura que te encuentras bien?")
                    .setPositiveButton("Sí", (dialog, which) -> {

                        notificarCancelacionAContactos();

                        Toast.makeText(this,
                                "Todo está bien. Se notificó a tus contactos.",
                                Toast.LENGTH_LONG).show();

                        startActivity(new Intent(this, Principal.class));
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    // Método independiente fuera de onCreate
    private void notificarCancelacionAContactos() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        String msjCancelacion = "Estoy bien, fue una falsa alarma.";

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .collection("contactos")
                .get()
                .addOnSuccessListener(docs -> {
                    for (QueryDocumentSnapshot doc : docs) {
                        String numero = doc.getString("telefono");
                        if (numero != null && !numero.isEmpty()) {
                            try {
                                SmsManager sms = SmsManager.getDefault();
                                sms.sendTextMessage(numero, null, msjCancelacion, null, null);
                                Log.d(TAG, "SMS de cancelación enviado a: " + numero);
                            } catch (Exception e) {
                                Log.e(TAG, "Error enviando cancelación a " + numero, e);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al leer contactos para cancelación", e));
    }
}