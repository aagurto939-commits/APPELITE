package com.example.appelite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClientesAdapter extends RecyclerView.Adapter<ClientesAdapter.ViewHolderClientes> {

    private List<Clientes> listaClientes;

    public ClientesAdapter(List<Clientes> listaClientes) {
        this.listaClientes = listaClientes;
    }

    @NonNull
    @Override
    public ViewHolderClientes onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new ViewHolderClientes(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderClientes holder, int position) {
        Clientes cliente = listaClientes.get(position);

        // Mostrar tipo y número de documento
        holder.txtTipoDocumento.setText(cliente.getTipoDocumento());
        holder.txtNumeroDocumento.setText(": " + cliente.getNumeroDocumento());

        // Mostrar nombre completo
        holder.txtNombreCompleto.setText(cliente.getNombreCompleto());

        // Mostrar teléfono y correo
        holder.txtTelefono.setText(cliente.getTelefono());
        holder.txtCorreo.setText(cliente.getCorreo());
    }

    @Override
    public int getItemCount() {
        return listaClientes.size();
    }

    public static class ViewHolderClientes extends RecyclerView.ViewHolder {
        TextView txtTipoDocumento, txtNumeroDocumento, txtNombreCompleto, txtTelefono, txtCorreo;
        ImageView imgTelefono, imgCorreo;

        public ViewHolderClientes(@NonNull View itemView) {
            super(itemView);

            txtTipoDocumento = itemView.findViewById(R.id.txtTipoDocumento);
            txtNumeroDocumento = itemView.findViewById(R.id.txtNumeroDocumento);
            txtNombreCompleto = itemView.findViewById(R.id.txtNombreCompleto);
            txtTelefono = itemView.findViewById(R.id.txtTelefono);
            txtCorreo = itemView.findViewById(R.id.txtCorreo);

            imgTelefono = itemView.findViewById(R.id.imgTelefono);
            imgCorreo = itemView.findViewById(R.id.imgCorreo);
        }
    }
}
