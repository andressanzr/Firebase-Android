package com.example.firebase_proyect.Fragment;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.firebase_proyect.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Asignaturas extends Fragment implements View.OnClickListener {

    private AlertDialog.Builder builder;

    private FloatingActionButton fab;

    public Asignaturas() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_asignaturas, container, false);

        return v;
    }

    @Override
    public void onClick(View view){


    }
}