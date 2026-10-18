package com.example.femmepower;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ViewHolder> {

    private ArrayList<ContactoModelo> lista;
    private Context context;
    private String userId;

    public ContactoAdapter(ArrayList<ContactoModelo> lista, Context context, String userId) {
        this.lista = lista;
        this.context = context;
        this.userId = userId;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombre, btnEliminar;

        public ViewHolder(View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contacto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactoModelo contacto = lista.get(position);

        holder.txtNombre.setText(contacto.getNombre());

        holder.btnEliminar.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                ContactoModelo itemAEliminar = lista.get(pos);

                // Eliminar de Firebase si existe userId e id del doc
                if (userId != null && !userId.isEmpty() && itemAEliminar.getId() != null) {
                    FirebaseFirestore.getInstance()
                            .collection("usuarios")
                            .document(userId)
                            .collection("contactos")
                            .document(itemAEliminar.getId())
                            .delete();
                }

                lista.remove(pos);
                notifyItemRemoved(pos);
                Toast.makeText(context, "Contacto eliminado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}