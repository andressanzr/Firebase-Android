package com.example.firebase_proyect.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.firebase_proyect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarcontraActivity extends AppCompatActivity {
    private EditText rstEmail;
    private Button rstBoton;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar);
        //Para inicializar la instancia de autenticación
        mAuth= FirebaseAuth.getInstance();

        //Referenciamos los elementos de la vista
        rstEmail=(EditText) findViewById(R.id.editartexto);
        rstBoton=(Button) findViewById(R.id.botoneditar);

        //Botón de reseteo de contraseña
        rstBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email= rstEmail.getText().toString().trim();
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(RecuperarcontraActivity.this,"Debe escribir una dirección",Toast.LENGTH_SHORT).show();
                }else{

                    //método que manda un email automático a la dirección proporcionada donde se
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //En caso exitoso, volvemos al activity principal
                                Toast.makeText(RecuperarcontraActivity.this,"Se ha enviado un email a tu dirección",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RecuperarcontraActivity.this,Login.class));
                            }else{
                                //Si la dirección de la cuenta no existe
                                Toast.makeText(RecuperarcontraActivity.this,"Error: no hay ninguna cuenta registrada con la dirección proporcionada",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
