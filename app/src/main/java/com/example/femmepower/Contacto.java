package com.example.femmepower;

import com.example.femmepower.R;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Contacto extends AppCompatActivity {

    private Button btninicio, btncontactos, btnmensaje, btnperfil;
    private RecyclerView recyclerView;
    private Button btnAgregar;
    private ArrayList<ContactoModelo> lista;
    private ContactoAdapter adapter;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contacto);

        // Insets para pantalla completa / EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase Setup
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }

        recyclerView = findViewById(R.id.Contactos);
        btnAgregar = findViewById(R.id.btnAgregar);

        lista = new ArrayList<>();
        adapter = new ContactoAdapter(lista, this, userId);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }

        if (btnAgregar != null) {
            btnAgregar.setOnClickListener(v -> abrirContactos());
        }

        // Cargar contactos
        if (!userId.isEmpty()) {
            cargarContactos();
        } else {
            // Contactos de prueba si no hay sesión iniciada
            lista.add(new ContactoModelo("test_1", "Contacto 1", "123456789"));
            lista.add(new ContactoModelo("test_2", "Contacto 2", "987654321"));
            adapter.notifyDataSetChanged();
        }

        // Navegación Inferior
        btninicio    = findViewById(R.id.btninicio);
        btncontactos = findViewById(R.id.btncontactos);
        btnmensaje   = findViewById(R.id.btnmensaje);
        btnperfil    = findViewById(R.id.btnperfil);

        if (btncontactos != null) seleccionar(btncontactos);

        if (btninicio != null) {
            btninicio.setOnClickListener(v -> startActivity(new Intent(Contacto.this, Principal.class)));
        }

        if (btnmensaje != null) {
            btnmensaje.setOnClickListener(v -> startActivity(new Intent(Contacto.this, Mensajes.class)));
        }

        if (btnperfil != null) {
            btnperfil.setOnClickListener(v -> startActivity(new Intent(Contacto.this, Inicio.class)));
        }
    }

    private void abrirContactos() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        );
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int nombreIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int numeroIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    String nombre = (nombreIndex != -1) ? cursor.getString(nombreIndex) : "Contacto sin nombre";
                    String numero = (numeroIndex != -1) ? cursor.getString(numeroIndex) : "Sin número";

                    cursor.close();

                    // Guardar en Firestore si hay usuario
                    if (!userId.isEmpty()) {
                        Map<String, Object> contacto = new HashMap<>();
                        contacto.put("nombre", nombre);
                        contacto.put("telefono", numero);

                        db.collection("usuarios")
                                .document(userId)
                                .collection("contactos")
                                .add(contacto)
                                .addOnSuccessListener(documentReference -> {
                                    String idDoc = documentReference.getId();
                                    lista.add(new ContactoModelo(idDoc, nombre, numero));
                                    adapter.notifyItemInserted(lista.size() - 1);
                                    Toast.makeText(Contacto.this, "Contacto guardado", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        lista.add(new ContactoModelo(String.valueOf(System.currentTimeMillis()), nombre, numero));
                        adapter.notifyItemInserted(lista.size() - 1);
                        Toast.makeText(Contacto.this, "Contacto agregado (Modo prueba)", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void cargarContactos() {
        db.collection("usuarios")
                .document(userId)
                .collection("contactos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    lista.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String nombre = doc.getString("nombre");
                        String telefono = doc.getString("telefono");

                        lista.add(new ContactoModelo(id, nombre, telefono));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void seleccionar(Button activo) {
        if (btninicio != null) btninicio.setBackground(ContextCompat.getDrawable(this, R.drawable.inactivo));
        if (btncontactos != null) btncontactos.setBackground(ContextCompat.getDrawable(this, R.drawable.inactivo));
        if (btnmensaje != null) btnmensaje.setBackground(ContextCompat.getDrawable(this, R.drawable.inactivo));
        if (btnperfil != null) btnperfil.setBackground(ContextCompat.getDrawable(this, R.drawable.inactivo));

        if (activo != null) {
            activo.setBackground(ContextCompat.getDrawable(this, R.drawable.activo));
        }
    }
}