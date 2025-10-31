package com.example.appelite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClientesAdapter extends RecyclerView.Adapter<ClientesAdapter.ViewHolderClientes> {

    private List<Clientes> listaClientes;
    private OnClienteClickListener listener;

    public interface OnClienteClickListener {
        void onEditarCliente(Clientes cliente);
        void onEliminarCliente(Clientes cliente);
    }

    public ClientesAdapter(List<Clientes> listaClientes) {
        this.listaClientes = listaClientes;
    }

    public void setOnClienteClickListener(OnClienteClickListener listener) {
        this.listener = listener;
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

        // Mostrar documento completo (tipo + número)
        String documentoCompleto = cliente.getTipoDocumento() + ": " + cliente.getNumeroDocumento();
        holder.txtDocumentoCompleto.setText(documentoCompleto);

        // Mostrar nombre completo
        holder.txtNombreCompleto.setText(cliente.getNombreCompleto());

        // Mostrar teléfono y correo
        holder.txtTelefono.setText(cliente.getTelefono());
        holder.txtCorreo.setText(cliente.getCorreo());

        // Configurar click listeners para los botones
        holder.btnEditarCliente.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditarCliente(cliente);
            }
        });

        holder.btnEliminarCliente.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarCliente(cliente);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaClientes.size();
    }

    public static class ViewHolderClientes extends RecyclerView.ViewHolder {
        TextView txtDocumentoCompleto, txtNombreCompleto, txtTelefono, txtCorreo;
        ImageView imgTelefono, imgCorreo;
        ImageButton btnEditarCliente, btnEliminarCliente;

        public ViewHolderClientes(@NonNull View itemView) {
            super(itemView);

            txtDocumentoCompleto = itemView.findViewById(R.id.txtDocumentoCompleto);
            txtNombreCompleto = itemView.findViewById(R.id.txtNombreCompleto);
            txtTelefono = itemView.findViewById(R.id.txtTelefono);
            txtCorreo = itemView.findViewById(R.id.txtCorreo);

            imgTelefono = itemView.findViewById(R.id.imgTelefono);
            imgCorreo = itemView.findViewById(R.id.imgCorreo);
            
            btnEditarCliente = itemView.findViewById(R.id.btnEditarCliente);
            btnEliminarCliente = itemView.findViewById(R.id.btnEliminarCliente);
        }
    }
}
