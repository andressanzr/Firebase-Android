package com.example.firebase_proyect.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebase_proyect.Interface.ItemClickListener;
import com.example.firebase_proyect.R;

public class GrupoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView numero, nombregrupo;

    public ItemClickListener listener;
    public GrupoHolder(@NonNull View itemView) {
        super(itemView);
        numero=(TextView) itemView.findViewById(R.id.numeroGrupo);
        nombregrupo=(TextView) itemView.findViewById(R.id.nombreGrupo);

    }
    public void setItemClickListener(ItemClickListener listener){this.listener = listener;}

    @Override
    public void onClick(View v) {
        listener.onClick(v, getAdapterPosition(), false);

    }
}
