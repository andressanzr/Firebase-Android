package com.example.firebase_proyect.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.firebase_proyect.Activity.Login;
import com.example.firebase_proyect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity  {
    private FirebaseAuth mAuth;
    private Button signout;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //instancia con firebase
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        setContentView(R.layout.activity_main);
        signout = findViewById(R.id.singout);

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }
    //muestra un mensaje de alerta si esta seguro que desea cerrar la cuenta
    private void signOut() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.logout);
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.signOut();
                //cierra la cuenta y le envia de nuevo al login
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //en caso contrario se cierra la alerta
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }


}
