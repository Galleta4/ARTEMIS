package com.example.femmepower;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.bluetooth.*;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Service {

    public static boolean isRunning;
    private boolean running = true;
    private static final String TAG = "BT_SERVICE";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (isRunning) {
            return START_STICKY;
        }

        isRunning = true;

        Log.d("BT_SERVICE", "🔥 Servicio iniciado");

        new Thread(this::escucharBluetooth).start();

        return START_STICKY;
    }

    private void escucharBluetooth() {
        while (running) { // 👈 bucle que reintenta siempre
            try {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

                if (adapter == null || !adapter.isEnabled()) {
                    Log.d(TAG, "❌ Bluetooth apagado");
                    Thread.sleep(5000);
                    continue;
                }

                if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) return;

                BluetoothDevice device = null;
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                for (BluetoothDevice d : devices) {
                    if (d.getName() != null && d.getName().equals("ESP32_Alerta")) {
                        device = d;
                        break;
                    }
                }

                if (device == null) {
                    Log.d(TAG, "❌ ESP32 no vinculado, reintentando en 10s...");
                    Thread.sleep(10000);
                    continue; // 👈 reintenta
                }

                Log.d(TAG, "Conectando...");
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

                adapter.cancelDiscovery();
                socket.connect();
                Log.d(TAG, "✅ CONECTADO AL ESP32");

                InputStream input = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytes;

                while (running) {
                    bytes = input.read(buffer);
                    String mensaje = new String(buffer, 0, bytes).trim();
                    Log.d(TAG, "📩 Mensaje: " + mensaje);

                    if (mensaje.toUpperCase().contains("ALERTA")) {
                        Log.d(TAG, "🚨 ALERTA DETECTADA");
                        enviarSMS();
                        Intent intent = new Intent(this, Alarma.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Conexión perdida, reintentando en 5s...", e);
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                // 👈 el while exterior reconecta automáticamente
            }
        }
    }
    // 🔥 ENVÍO DE SMS (FORMA SEGURA)
    private void enviarSMS() {
        SharedPreferences prefs = getSharedPreferences("femme_power", MODE_PRIVATE);
        String mensaje = prefs.getString("mensaje", "Estoy en peligro, necesito ayuda.");

        String uid = com.google.firebase.auth.FirebaseAuth.getInstance()
                .getCurrentUser().getUid();

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .collection("contactos")
                .get()
                .addOnSuccessListener(docs -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : docs) {
                        String numero = doc.getString("telefono");
                        if (numero != null && !numero.isEmpty()) {
                            try {
                                android.telephony.SmsManager sms =
                                        android.telephony.SmsManager.getDefault();
                                sms.sendTextMessage(numero, null, mensaje, null, null);
                                Log.d(TAG, "SMS enviado a: " + numero);
                            } catch (Exception e) {
                                Log.e(TAG, "Error enviando SMS a " + numero, e);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error leyendo contactos", e));
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        running = false;
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}