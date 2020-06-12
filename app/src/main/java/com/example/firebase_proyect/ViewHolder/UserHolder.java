package com.example.firebase_proyect.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebase_proyect.Interface.ItemClickListener;
import com.example.firebase_proyect.R;

import androidx.recyclerview.widget.RecyclerView;

public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView NombreApellidosAlumno, TipoAlumno;
    public ImageView FotoAlumno;
    public ItemClickListener listener;


    public UserHolder(View v) {
        super(v);
        NombreApellidosAlumno = (TextView) v.findViewById(R.id.nombreApellidoAlumno);
        TipoAlumno = (TextView) v.findViewById(R.id.tipoAlumno);
        FotoAlumno = (ImageView) v.findViewById(R.id.profile_image);
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v, getAdapterPosition(), false);
    }
}
