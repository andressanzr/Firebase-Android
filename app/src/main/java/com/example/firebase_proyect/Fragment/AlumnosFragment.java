package com.example.firebase_proyect.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.firebase_proyect.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AlumnosFragment extends Fragment {

    private FloatingActionButton fab;

    public AlumnosFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_alumnos, container, false);
        fab=(FloatingActionButton) v.findViewById(R.id.fabAddBoard);



        return v;
    }


}