package com.example.femmepower;

import android.app.Service;
import android.content.Intent;
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
    private BluetoothSocket socket = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (isRunning) {
            return START_STICKY;
        }

        isRunning = true;
        Log.d(TAG, "🔥 Servicio de Bluetooth iniciado");

        new Thread(this::escucharBluetooth).start();
        return START_STICKY;
    }

    private void escucharBluetooth() {
        while (running) { // Bucle de reconexión continua
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
                    continue;
                }

                Log.d(TAG, "Conectando al ESP32...");
                socket = device.createRfcommSocketToServiceRecord( UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

                adapter.cancelDiscovery();
                socket.connect();
                Log.d(TAG, "✅ CONECTADO AL ESP32");

                InputStream input = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytes;

                while (running) {
                    bytes = input.read(buffer);
                    if (bytes == -1) break;

                    String mensaje = new String(buffer, 0, bytes).trim();
                    Log.d(TAG, "📩 Mensaje del ESP32: " + mensaje);

                    if (mensaje.toUpperCase().contains("ALERTA")) {
                        Log.d(TAG, "🚨 ALERTA DETECTADA");

                        // 1. Envía el SMS a todos los contactos leyendo la plantilla y Firestore mediante SmsHelper
                        SmsHelper.enviarAlertaContactos(this);

                        // 2. Despliega la pantalla de la Alarma
                        Intent intentAlarma = new Intent(this, Alarma.class);
                        intentAlarma.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentAlarma);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Conexión perdida, reintentando en 5s...", e);
                cerrarSocket();
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void cerrarSocket() {
        if (socket != null) {
            try { socket.close(); } catch (Exception ignored) {}
            socket = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        isRunning = false;
        cerrarSocket();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

