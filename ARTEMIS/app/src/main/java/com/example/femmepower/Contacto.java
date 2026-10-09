package com.example.femmepower;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
    RecyclerView recyclerView;
    Button btnAgregar;
    ArrayList<ContactoModelo> lista;
    ContactoAdapter adapter;

    // Firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacto);

        // Obtener usuario
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            finish();
            return;
        }

        recyclerView = findViewById(R.id.Contactos);
        btnAgregar = findViewById(R.id.btnAgregar);

        lista = new ArrayList<>();
        adapter = new ContactoAdapter(lista);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAgregar.setOnClickListener(v -> abrirContactos());

        //Cargar contactos del usuario
        cargarContactos();

        btninicio    = findViewById(R.id.btninicio);
        btncontactos = findViewById(R.id.btncontactos);
        btnmensaje   = findViewById(R.id.btnmensaje);
        btnperfil    = findViewById(R.id.btnperfil);

        seleccionar(btncontactos);

        btninicio.setOnClickListener(v ->
                startActivity(new Intent(Contacto.this, Principal.class)));

        btncontactos.setOnClickListener(v ->
                seleccionar(btncontactos));

        btnmensaje.setOnClickListener(v -> {
            seleccionar(btnmensaje);
            startActivity(new Intent(Contacto.this, Mensajes.class));
        });

        btnperfil.setOnClickListener(v -> {
            seleccionar(btnperfil);
            startActivity(new Intent(Contacto.this, Inicio.class));
        });
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
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int nombreIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numeroIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER);

                String nombre = cursor.getString(nombreIndex);
                String numero = cursor.getString(numeroIndex);

                lista.add(new ContactoModelo(nombre, numero));
                adapter.notifyItemInserted(lista.size() - 1);

                // Guardar en Firestore
                Map<String, Object> contacto = new HashMap<>();
                contacto.put("nombre", nombre);
                contacto.put("telefono", numero);

                db.collection("usuarios")
                        .document(userId)
                        .collection("contactos")
                        .add(contacto);

                cursor.close();
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
                        String nombre = doc.getString("nombre");
                        String telefono = doc.getString("telefono");

                        lista.add(new ContactoModelo(nombre, telefono));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void seleccionar(Button activo) {
        btninicio.setBackground(getDrawable(R.drawable.inactivo));
        btncontactos.setBackground(getDrawable(R.drawable.inactivo));
        btnmensaje.setBackground(getDrawable(R.drawable.inactivo));
        btnperfil.setBackground(getDrawable(R.drawable.inactivo));

        btninicio.setTextColor(getColor(R.color.morado1));
        btncontactos.setTextColor(getColor(R.color.morado1));
        btnmensaje.setTextColor(getColor(R.color.morado1));
        btnperfil.setTextColor(getColor(R.color.morado1));

        activo.setBackground(getDrawable(R.drawable.activo));
        activo.setTextColor(getColor(R.color.morado2));
    }
}