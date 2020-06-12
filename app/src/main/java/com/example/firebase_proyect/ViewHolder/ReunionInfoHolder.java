package com.example.firebase_proyect.ViewHolder;

import android.view.View;
import android.widget.TextView;

import com.example.firebase_proyect.Interface.ItemClickListener;
import com.example.firebase_proyect.R;

import androidx.recyclerview.widget.RecyclerView;

public class ReunionInfoHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    public TextView NombreGrupo, Fecha;
    private ItemClickListener listener;

    public ReunionInfoHolder(View v) {
        super(v);
        NombreGrupo = (TextView) v.findViewById(R.id.nombreGrupo);
        Fecha = (TextView) v.findViewById(R.id.fechaReunion);

    }

    @Override
    public void onClick(View v) {
        listener.onClick(v, getAdapterPosition(), false);
    }

    @Override
    public boolean onLongClick(View view) {
        listener.onClick(view, getAdapterPosition(), true);
        return true;
    }
}
