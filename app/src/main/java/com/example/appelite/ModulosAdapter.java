package com.example.appelite;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ModulosAdapter extends RecyclerView.Adapter<ModulosAdapter.ModuloViewHolder> {
    private List<Modulo> modulos;
    private Context context;

    public ModulosAdapter(List<Modulo> modulos, Context context) {
        this.modulos = modulos;
        this.context = context;
    }

    @NonNull
    @Override
    public ModuloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_modulo, parent, false);
        return new ModuloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuloViewHolder holder, int position) {
        Modulo modulo = modulos.get(position);
        holder.tvTitulo.setText(modulo.getTitulo());
        holder.tvDescripcion.setText(modulo.getDescripcion());
        holder.imgModulo.setImageResource(modulo.getIcono());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, modulo.getActivityClass());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return modulos.size();
    }

    static class ModuloViewHolder extends RecyclerView.ViewHolder {
        ImageView imgModulo;
        TextView tvTitulo, tvDescripcion;

        ModuloViewHolder(@NonNull View itemView) {
            super(itemView);
            imgModulo = itemView.findViewById(R.id.imgModulo);
            tvTitulo = itemView.findViewById(R.id.tvTituloModulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionModulo);
        }
    }
}
