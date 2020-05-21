package com.example.firebase_proyect.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.firebase_proyect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConfiguracionActivity extends AppCompatActivity {
    private Button guardar;
    private EditText nombrepersona,apellidopersona,edadpersona,correopersona;
    private ImageView loginPhoto;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        getSupportActionBar().setTitle("Configuración Usuario");
        References();
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   String email = correopersona.getText().toString();
                String name = nombrepersona.getText().toString();
                String age = edadpersona.getText().toString();
                String lastname = apellidopersona.getText().toString();

                if( email.isEmpty() || name.isEmpty() ||age.isEmpty() || lastname.isEmpty() ) {

                    // si algo sale mal: todos los campos deben llenarse
                    // necesitamos mostrar un mensaje de error
                    showMessage("Por favor. Verifica tus datos") ;

                }
                else {
                    //todo está bien y todos los campos están llenos ahora podemos comenzar a crear una cuenta de usuario
                    // El método CreateUserAccount intentará crear el usuario si el correo electrónico es válido
                    showMessage("Te has registrado correctamente, bienvenido");

                    CreateUserAccount(name,lastname,age,email);
                }*/
            }

        });
    }
    private void References() {
        nombrepersona = (EditText) findViewById(R.id.nombrePersona);
        apellidopersona = (EditText) findViewById(R.id.apellidoPersona);
        edadpersona = (EditText) findViewById(R.id.edadPersona);
        correopersona = (EditText) findViewById(R.id.correoPersona);
        guardar= (Button) findViewById(R.id.guardarDatos);
        loginPhoto = (ImageView) findViewById(R.id.login_photo);
    }
    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

    }
    private void CreateUserAccount(final String nombre, final String apellido,final String edad, final String email, final String password) {


        //este método crea una cuenta de usuario con correo electrónico y contraseña específicos
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.child("Usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Nos aseguramos de que la contraseña tenga 6 caracteres
                if (password.length() < 6) {
                    Toast.makeText(ConfiguracionActivity.this, "Debe introducir un password de al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(ConfiguracionActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                // el usuario creo la cuenta correctamente
                                showMessage("Se creo la cuenta");
                                // after we created user account we need to update his profile picture and name
                         //       updateUserInfo(nombre, apellido, edad, email, password, RootRef);
                            } else {

                                // account creation failed
                                showMessage("account creation failed" + task.getException().getMessage());


                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
