package com.example.femmepower;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.InputStream;
import java.util.UUID;

import android.bluetooth.*;

public class BluetoothService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(() -> escucharBluetooth()).start();

        return START_STICKY; // importante (se mantiene activo)
    }

    private void escucharBluetooth() {
        try {

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = adapter.getRemoteDevice("XX:XX:XX:XX:XX:XX"); // MAC ESP32

            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            );

            socket.connect();

            InputStream input = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                bytes = input.read(buffer);
                String mensaje = new String(buffer, 0, bytes);

                if (mensaje.contains("ALERTA DISPARADA")) {

                    Intent intent = new Intent(this, Alarma.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}