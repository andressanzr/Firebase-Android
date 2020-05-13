package com.example.firebase_proyect.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.firebase_proyect.R;

public class ConfiguracionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        getSupportActionBar().setTitle("Configuración Usuario");

    }
}
