package com.example.appelite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder> {
    private List<Producto> productos;
    private OnProductoClickListener listener;

    public interface OnProductoClickListener {
        void onEditar(Producto producto);
        void onEliminar(Producto producto);
    }

    public ProductosAdapter(List<Producto> productos, OnProductoClickListener listener) {
        this.productos = productos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);
        
        // Mostrar código del producto
        String codigo = producto.getCodigo();
        if (codigo != null && !codigo.isEmpty()) {
            holder.tvCodigo.setText("Código: " + codigo);
            holder.tvCodigo.setVisibility(View.VISIBLE);
        } else {
            holder.tvCodigo.setVisibility(View.GONE);
        }
        
        // Información básica
        holder.tvNombre.setText(producto.getNombre());
        holder.tvDescripcion.setText(producto.getDescripcion() != null ? producto.getDescripcion() : "Sin descripción");
        
        // Precio con formato mejorado
        String moneda = producto.getMoneda() != null ? producto.getMoneda() : "PEN";
        String simbolo = moneda.equals("USD") ? "$" : "S/";
        holder.tvPrecio.setText("Precio: " + simbolo + String.format("%.2f", producto.getPrecio()));
        
        // Stock con indicador de stock bajo
        String stockText = "Stock: " + producto.getStock();
        if (producto.tieneStockBajo() && producto.getStockMinimo() > 0) {
            stockText += " (¡Bajo!)";
            holder.tvStock.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
        } else {
            holder.tvStock.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.primary_text_light));
        }
        holder.tvStock.setText(stockText);
        
        // Información adicional en lugar de solo moneda
        String infoAdicional = "";
        if (producto.getCategoria() != null && !producto.getCategoria().isEmpty()) {
            infoAdicional = "Categoría: " + producto.getCategoria();
        } else {
            infoAdicional = "Moneda: " + (moneda.equals("USD") ? "Dólares" : "Soles");
        }
        holder.tvMoneda.setText(infoAdicional);
        
        // Listeners
        holder.btnEditar.setOnClickListener(v -> listener.onEditar(producto));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(producto));
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
        notifyDataSetChanged();
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvCodigo, tvNombre, tvDescripcion, tvPrecio, tvStock, tvMoneda;
        ImageButton btnEditar, btnEliminar;
        ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCodigo = itemView.findViewById(R.id.tvCodigoProducto);
            tvNombre = itemView.findViewById(R.id.tvNombreProducto);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionProducto);
            tvPrecio = itemView.findViewById(R.id.tvPrecioProducto);
            tvStock = itemView.findViewById(R.id.tvStockProducto);
            tvMoneda = itemView.findViewById(R.id.tvMonedaProducto);
            btnEditar = itemView.findViewById(R.id.btnEditarProducto);
            btnEliminar = itemView.findViewById(R.id.btnEliminarProducto);
        }
    }
}
