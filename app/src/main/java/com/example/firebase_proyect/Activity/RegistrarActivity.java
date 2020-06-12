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
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_proyect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrarActivity extends AppCompatActivity {
    private Button IniciarSesion;
    private Button registro;
    private FirebaseAuth mAuth;
    private TextView Foto;
    static int PReqCode = 1;
    static int REQUESCODE = 1;
    private Uri pickedImgUri;
    CircleImageView ImgUserPhoto;
    String myUrl = "";
    StorageTask uploadTask;

    FirebaseStorage fileStorage;

    //Para guardar info en el storage de Firebase
    StorageReference storageProfilePictureRef;
    private EditText Nombre, Apellidos, edad, MailInicial, PasswordInicial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);
        References();
        //inicia la autenticación con Firebase
        mAuth = FirebaseAuth.getInstance();

        fileStorage = FirebaseStorage.getInstance();
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
                String email = MailInicial.getText().toString();
                String password = PasswordInicial.getText().toString();
                String name = Nombre.getText().toString();
                int age;
                if (edad.getText().toString().equals("")) {
                    age = 0;
                } else {
                    age = Integer.parseInt(edad.getText().toString());
                }
                String lastname = Apellidos.getText().toString();

                if (email.isEmpty() || name.isEmpty() || password.isEmpty() || age <= 0 || lastname.isEmpty()) {
                    // si algo sale mal: todos los campos deben llenarse
                    // necesitamos mostrar un mensaje de error
                    showMessage("Por favor. Verifica tus datos");
                } else {
                    //todo está bien y todos los campos están llenos ahora podemos comenzar a crear una cuenta de usuario
                    // El método CreateUserAccount intentará crear el usuario si el correo electrónico es válido
                    // showMessage("Te has registrado correctamente, bienvenido");
                    CreateUserAccount(name, lastname, age, email, password);
                }
            }

        });

        Foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestForPermission();
                } else {
                    openGallery();
                }
            }
        });
    }

    private void CreateUserAccount(final String nombre, final String apellido, final int edad, final String email, final String password) {
        //este método crea una cuenta de usuario con correo electrónico y contraseña específicos
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        if (password.length() < 6) {
            Toast.makeText(RegistrarActivity.this, "Debe introducir un password de al menos 6 caracteres", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrarActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // el usuario creo la cuenta correctamente
                        showMessage("Se creo la cuenta");
                        // after we created user account we need to update his profile picture and name
                        updateUserInfo(nombre, apellido, edad, email, password, RootRef);
                    } else {
                        // account creation failed
                        showMessage("account creation failed" + task.getException().getMessage());
                    }
                }
            });
        }


    }

    // actualiza la foto y el nombre del usuario
    private void updateUserInfo(String nombre, String apellido, int edad, String email, String password, final DatabaseReference rootRef) {
        final String ID = mAuth.getCurrentUser().getUid();

        HashMap<String, Object> userdataMap = new HashMap<>();
        userdataMap.put("ID", ID);
        userdataMap.put("nombre", nombre);
        userdataMap.put("apellido", apellido);
        userdataMap.put("edad", edad);
        userdataMap.put("email", email);
        userdataMap.put("password", password);
        //tipo por defecto alumno
        userdataMap.put("tipoUsuario", 1);

        rootRef.child("Usuarios").child(ID).updateChildren(userdataMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) { //En caso exitoso
                            showMessage("Datos guardados");
                            try {
                                actualizaImagen(pickedImgUri, ID, rootRef);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            updateUI();

                        } else { //En caso de error
                            showMessage("Error en guardar los datos");
                        }
                    }
                });

        mAuth.signOut();

    }

    private void actualizaImagen(final Uri imageUri, final String userID, final DatabaseReference rootRef) {
        if (imageUri != null) {
            //Ruta donde se guarda la foto de usuario en Firebase Storage
            storageProfilePictureRef = fileStorage.getReference();
            final StorageReference fileref = storageProfilePictureRef.child("imagenesUsuarios/" + userID + ".jpg");
            uploadTask = fileref.putFile(imageUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showMessage("error al subir img" + e.getMessage());
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    myUrl = taskSnapshot.getMetadata().getPath();
                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("foto", myUrl);
/*
                    //FIJAMOS FOTO DEL PERFIL DE FIREBASE
                    UserProfileChangeRequest profileUpdatesPhoto = new UserProfileChangeRequest.Builder().setPhotoUri(imageUri).build();
                    user.updateProfile(profileUpdatesPhoto);

 */
                    //ACTUALIZAMOS LOS DATOS CUYO NODO PRINCIPAL SEA IDÉNTICO AL ID DEL USUARIO ACTUAL
                    rootRef.child("Usuarios").child(userID).updateChildren(userMap);
                }
            });

        }
    }

    private void openGallery() {
        //abre la intención de la galería y espera a que el usuario elija una imagen
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);
    }

    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(RegistrarActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                //permiso para poder acceder la galería
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegistrarActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(RegistrarActivity.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();

            } else {
                ActivityCompat.requestPermissions(RegistrarActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }

        } else
            openGallery();

    }

    //referencia
    private void References() {
        Foto = (TextView) findViewById(R.id.cambiarFoto);
        ImgUserPhoto = (CircleImageView) findViewById(R.id.reUser);
        Nombre = (EditText) findViewById(R.id.Nombre);
        Apellidos = (EditText) findViewById(R.id.Apellidos);
        edad = (EditText) findViewById(R.id.edad);
        MailInicial = (EditText) findViewById(R.id.MailInicial);
        PasswordInicial = (EditText) findViewById(R.id.PasswordInicial);
        IniciarSesion = (Button) findViewById(R.id.botonIniciarSesion);
        registro = (Button) findViewById(R.id.botonCrearCuenta);
        storageProfilePictureRef = FirebaseStorage.getInstance().getReference().child("imagenesUsuarios");
    }

    //toast que se mostrará
    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void updateUI() {
        Intent mainActivity = new Intent(getApplicationContext(), Login.class);
        startActivity(mainActivity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {
            //el usuario ha elegido con éxito una imagen
            // necesitamos guardar su referencia a una variable Uri
            pickedImgUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImgUri);
        }
    }
}