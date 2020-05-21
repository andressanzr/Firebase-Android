package com.example.firebase_proyect.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.firebase_proyect.Interface.ItemClickListener;
import com.example.firebase_proyect.R;

public class AsignaturaHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView Curso, NombreAlumno;
    public ImageView foto;
    public ItemClickListener listener;


    public AsignaturaHolder(View v) {
        super(v);
        Curso=(TextView) v.findViewById(R.id.curso);
        NombreAlumno=(TextView) v.findViewById(R.id.nombreAlumno);
        foto = (ImageView)v.findViewById(R.id.profile_image);
    }
    public void setItemClickListener(ItemClickListener listener){this.listener = listener;}

    @Override
    public void onClick(View v) {
        listener.onClick(v, getAdapterPosition(), false);

    }

}