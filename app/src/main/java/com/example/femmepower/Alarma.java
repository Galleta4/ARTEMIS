package com.example.femmepower;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Alarma extends AppCompatActivity {

    Button btnCancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alerta);

        btnCancelar = findViewById(R.id.btnCancelar);

        //Permiso SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, 1);
        }

        //LEER CONFIGURACIÓN DEL USUARIO
        SharedPreferences prefs = getSharedPreferences("femme_power", MODE_PRIVATE);

        String mensaje = prefs.getString("mensaje", "Necesito ayuda");
        boolean gps = prefs.getBoolean("gps", false);
        boolean ritmo = prefs.getBoolean("ritmo", false);

        //CONSTRUIR MENSAJE
        String mensajeFinal = mensaje;

        if (gps) {
            mensajeFinal += "\nUbicación: https://maps.google.com/?q=20.5,-100.3";
        }

        if (ritmo) {
            mensajeFinal += "\nRitmo cardiaco: 80 bpm";
        }

        //ENVIAR AUTOMÁTICAMENTE
        enviarSMS(mensajeFinal);

        // BOTÓN CANCELAR
        btnCancelar.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Cancelar alerta")
                    .setMessage("¿Seguro que estás bien?")
                    .setPositiveButton("Sí", (dialog, which) -> {

                        enviarSMS("Estoy bien, fue una falsa alarma.");

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

    private void enviarSMS(String mensaje) {
        try {
            SmsManager sms = SmsManager.getDefault();

            //aquí enlace con pantalla de andi //String numero = "4420000000";
            String numero = "4421234567";

            sms.sendTextMessage(numero, null, mensaje, null, null);

        } catch (Exception e) {
            Toast.makeText(this, "Error al enviar SMS", Toast.LENGTH_SHORT).show();
        }
    }
}