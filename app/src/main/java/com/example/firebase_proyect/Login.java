package com.example.firebase_proyect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private Button login;
    private Button registro;
    private EditText emailInicial;
    private EditText passwordInicial;
    private FirebaseAuth mAuth;
    private Intent MainActivity;
    private ImageView loginPhoto;
    private SharedPreferences mSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        References();
        //conexión con la base de datos en firebase
        mAuth = FirebaseAuth.getInstance();
        MainActivity = new Intent(this,com.example.firebase_proyect.MainActivity.class);
        loginPhoto = findViewById(R.id.login_photo);
        //inicia el shared Preference
        initSharedPreferences();
        //al seleccionar la foto te manda a registrar
        loginPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent registerActivity = new Intent(getApplicationContext(),RegistrarActivity.class);
                startActivity(registerActivity);
                finish();


            }
        });
        //comprueba los datos introducidos para que se puedan loguearse
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String mail = emailInicial.getText().toString();
                final String password = passwordInicial.getText().toString();

                if (mail.isEmpty() || password.isEmpty()) {
                    showMessage("Porfavor. Verifique los campos");

                }
                else
                {
                    signIn(mail,password);
                }
            }
        });

        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loggy = new Intent(Login.this, RegistrarActivity.class);
                startActivity(loggy);

            }
        });
    }

    private void initSharedPreferences() {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
    //declaración variables
    private void References() {
        login = (Button) findViewById(R.id.botonLogin);
        registro = (Button) findViewById(R.id.botonRegistro);
        emailInicial = (EditText) findViewById(R.id.MailInicial);
        passwordInicial = (EditText) findViewById(R.id.PasswordInicial);

    }
    //comprueba los datos
    private boolean isValidData() {
        if (emailInicial.getText().toString().length() > 0 &&
                passwordInicial.getText().toString().length() > 0
                ){
            return true;
        } else{
            return false;
        }
    }
    private  boolean validar(){
        String correo=emailInicial.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(correo).matches()){
            Toast.makeText(this, "Ingresa un email válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //método para corroborar que el usuario se encuentre en la base de datos
    private void signIn(String mail, String password) {


        mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {


                if (task.isSuccessful()) {

                    updateUI();

                }
                else {
                    showMessage(task.getException().getMessage());

                }


            }
        });
    }
    //Termina el activity
    private void updateUI() {

        startActivity(MainActivity);
        finish();

    }

    private void showMessage(String text) {

        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
    }

    //mantiene la sesión del usuario abierta una vez registrado
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            //user is already connected  so we need to redirect him to home page
            updateUI();

        }
    }

}
