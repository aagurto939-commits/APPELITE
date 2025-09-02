package com.example.appelite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VentaProductosAdapter extends RecyclerView.Adapter<VentaProductosAdapter.VentaProductoViewHolder> {
    private List<Producto> productos;
    private List<VentaItem> ventaItems;
    private OnCantidadChangeListener listener;

    public interface OnCantidadChangeListener {
        void onCantidadChanged();
    }

    public VentaProductosAdapter(List<Producto> productos, List<VentaItem> ventaItems, OnCantidadChangeListener listener) {
        this.productos = productos;
        this.ventaItems = ventaItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VentaProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venta_producto, parent, false);
        return new VentaProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VentaProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);
        VentaItem ventaItem = encontrarVentaItem(producto.getId());
        
        holder.tvNombre.setText(producto.getNombre());
        holder.tvPrecio.setText((producto.getMoneda().equals("USD") ? "$" : "S/") + producto.getPrecio());
        holder.tvStock.setText("Stock: " + producto.getStock());
        
        if (ventaItem != null) {
            holder.etCantidad.setText(String.valueOf(ventaItem.getCantidad()));
        } else {
            holder.etCantidad.setText("0");
        }

        holder.btnAgregar.setOnClickListener(v -> {
            int cantidad = Integer.parseInt(holder.etCantidad.getText().toString().trim());
            if (cantidad > 0 && cantidad <= producto.getStock()) {
                agregarOActualizarItem(producto, cantidad);
                listener.onCantidadChanged();
            }
        });

        holder.btnQuitar.setOnClickListener(v -> {
            quitarItem(producto.getId());
            holder.etCantidad.setText("0");
            listener.onCantidadChanged();
        });
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    private VentaItem encontrarVentaItem(String productoId) {
        for (VentaItem item : ventaItems) {
            if (item.getProductoId().equals(productoId)) {
                return item;
            }
        }
        return null;
    }

    private void agregarOActualizarItem(Producto producto, int cantidad) {
        VentaItem existente = encontrarVentaItem(producto.getId());
        if (existente != null) {
            existente.setCantidad(cantidad);
        } else {
            ventaItems.add(new VentaItem(producto.getId(), producto.getNombre(), cantidad, producto.getPrecio(), producto.getMoneda()));
        }
    }

    private void quitarItem(String productoId) {
        for (int i = ventaItems.size() - 1; i >= 0; i--) {
            if (ventaItems.get(i).getProductoId().equals(productoId)) {
                ventaItems.remove(i);
                break;
            }
        }
    }

    static class VentaProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPrecio, tvStock;
        EditText etCantidad;
        Button btnAgregar, btnQuitar;

        VentaProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreProductoVenta);
            tvPrecio = itemView.findViewById(R.id.tvPrecioProductoVenta);
            tvStock = itemView.findViewById(R.id.tvStockProductoVenta);
            etCantidad = itemView.findViewById(R.id.etCantidadVenta);
            btnAgregar = itemView.findViewById(R.id.btnAgregarVenta);
            btnQuitar = itemView.findViewById(R.id.btnQuitarVenta);
        }
    }
}
