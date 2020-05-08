package com.example.firebase_proyect.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.firebase_proyect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegistrarActivity extends AppCompatActivity {
    private Button IniciarSesion;
    private Button registro;
    private FirebaseAuth mAuth;
    static int PReqCode = 1 ;
    static int REQUESCODE = 1 ;
    private Uri pickedImgUri ;
    private ImageView ImgUserPhoto;
    private EditText Nombre, Apellidos, edad, MailInicial,PasswordInicial;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);
        References();
        //inicia la autenticación con Firebase
        mAuth = FirebaseAuth.getInstance();
        //boton de inciar sesión
        IniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent IniciarSesion = new Intent(RegistrarActivity.this, Login.class);
                startActivity(IniciarSesion);
            }
        });
        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = MailInicial.getText().toString();
                final String password = PasswordInicial.getText().toString();
                final String name = Nombre.getText().toString();
                final String Edad = edad.getText().toString();
                final String apellidos = Apellidos.getText().toString();

                if( email.isEmpty() || name.isEmpty() || password.isEmpty()  ||Edad.isEmpty() || apellidos.isEmpty() ) {


                    // si algo sale mal: todos los campos deben llenarse
                    // necesitamos mostrar un mensaje de error
                    showMessage("Por favor. Verifica tus datos") ;

                }
                else {
                    //todo está bien y todos los campos están llenos ahora podemos comenzar a crear una cuenta de usuario
                    // El método CreateUserAccount intentará crear el usuario si el correo electrónico es válido
                    showMessage("Te has registrado correctamente, bienvenido");

                    CreateUserAccount(email,name,password);
                }
                }

        });
        ImgUserPhoto = findViewById(R.id.reUser) ;

        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= 22) {

                    checkAndRequestForPermission();

                }
                else
                {
                    openGallery();
                }
            }
        });
    }

    private void CreateUserAccount(String email, final String name, String password) {


        //este método crea una cuenta de usuario con correo electrónico y contraseña específicos

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // el usuario creo la cuenta correctamente
                            showMessage("Se creo la cuenta");
                            // after we created user account we need to update his profile picture and name
                            updateUserInfo( name ,pickedImgUri,mAuth.getCurrentUser());
 }
                        else
                        {

                            // account creation failed
                            showMessage("account creation failed" + task.getException().getMessage());


                        }
                    }
                });
    }

    // actualiza la foto y el nombre del usuario
    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {


        //primero tenemos que subir la foto del usuario al almacenamiento de Firebase y obtener la URL

        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // imagen cargada con éxito
                // ahora podemos obtener nuestra url de imagen

                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // uri contain user image url


                        UserProfileChangeRequest profleUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();


                        currentUser.updateProfile(profleUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            // información del usuario actualizada con éxito
                                            showMessage("Registrado completamente");
                                            updateUI();
                                        }

                                    }
                                });

                    }
                });





            }
        });
    }
    private void openGallery() {
        //abre la intención de la galería y espera a que el usuario elija una imagen

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESCODE);
    }

    private void checkAndRequestForPermission() {


        if (ContextCompat.checkSelfPermission(RegistrarActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                //permiso para poder acceder la galería
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegistrarActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(RegistrarActivity.this,"Please accept for required permission",Toast.LENGTH_SHORT).show();

            }

            else
            {
                ActivityCompat.requestPermissions(RegistrarActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }

        }
        else
            openGallery();

    }
    //referencia
    private void References() {
        Nombre = (EditText) findViewById(R.id.Nombre);
        Apellidos = (EditText) findViewById(R.id.Apellidos);
        edad = (EditText) findViewById(R.id.edad);
        MailInicial = (EditText) findViewById(R.id.MailInicial);
        PasswordInicial = (EditText) findViewById(R.id.PasswordInicial);
        IniciarSesion = (Button) findViewById(R.id.botonIniciarSesion);
        registro = (Button) findViewById(R.id.botonCrearCuenta);
    }
    //toast que se mostrará
    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

    }
    private void updateUI() {

        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivity);
        finish();


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null ) {

            //el usuario ha elegido con éxito una imagen
            // necesitamos guardar su referencia a una variable Uri
            pickedImgUri = data.getData() ;
            ImgUserPhoto.setImageURI(pickedImgUri);


        }


    }
}