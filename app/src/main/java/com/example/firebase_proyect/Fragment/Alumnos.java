package com.example.firebase_proyect.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.firebase_proyect.R;

public class Alumnos extends Fragment implements View.OnClickListener{



    public Alumnos() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_alumnos, container, false);

        return v;
    }


    @Override
    public void onClick(View view){



    }

}