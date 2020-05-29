package com.example.firebase_proyect.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.firebase_proyect.Models.Users;
import com.example.firebase_proyect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class AgregarUser extends AppCompatActivity {
    private EditText Nombre, Apellidos, edad, MailInicial, PasswordInicial;
    private Button registro;
    private FirebaseAuth mAuth;
    private CircleImageView ImgUserPhoto;
    FirebaseStorage fileStorage;

    private StorageReference UsersImagesRef;
    private DatabaseReference UsersRef;

    private Uri pickedImgUri;
    StorageTask uploadTask;
    String myUrl = "";

    //Para guardar info en el storage de Firebase
    StorageReference storageProfilePictureRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_user);
        References();
        mAuth = FirebaseAuth.getInstance();

        fileStorage = FirebaseStorage.getInstance();

        if (getIntent().getExtras().getString("IDsubject") != null) {
            getUserInfo(getIntent().getExtras().getString("IDsubject"));
        }

        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = MailInicial.getText().toString();
                String password = PasswordInicial.getText().toString();
                String name = Nombre.getText().toString();
                int age = Integer.parseInt(edad.getText().toString());
                String lastname = Apellidos.getText().toString();

                if (email.isEmpty() || name.isEmpty() || password.isEmpty() || age <= 0 || lastname.isEmpty()) {
                    // si algo sale mal: todos los campos deben llenarse
                    // necesitamos mostrar un mensaje de error
                    showMessage("Por favor. Verifica tus datos");
                } else {
                    //todo está bien y todos los campos están llenos ahora podemos comenzar a crear una cuenta de usuario
                    // El método CreateUserAccount intentará crear el usuario si el correo electrónico es válido

                    CreateUserAccount(name, lastname, age, email, password);
                }
            }

        });
    }

    private void getUserInfo(final String IdUser) {

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (final DataSnapshot snapShot : dataSnapshot.getChildren()) {
                    UsersRef.child(snapShot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Users datosUser = snapShot.getValue(Users.class);

                            String idBd = datosUser.getID();
                            if (idBd.equals(IdUser)) {
                                String fotoBd = null;
                                fotoBd = datosUser.getFoto();
                                //Se introducen los datos obtenidos en los elementos de la vista
                                if (fotoBd != null) {
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                    storageReference = storageReference.child("imagenesUsuarios/" + IdUser + ".jpg");
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(ImgUserPhoto);
                                        }
                                    });
                                } else {
                                    ImgUserPhoto.setImageResource(R.drawable.seusuario);
                                }
                                String nombreBd = datosUser.getNombre();
                                String apellidosBd = datosUser.getApellido();
                                int edadBd = datosUser.getEdad();
                                String emailBd = datosUser.getEmail();


                                Nombre.setText(nombreBd);
                                Apellidos.setText(apellidosBd);
                                MailInicial.setText(emailBd);
                                edad.setText(edadBd);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void CreateUserAccount(final String nombre, final String apellido, final int edad, final String email, final String password) {
        //este método crea una cuenta de usuario con correo electrónico y contraseña específicos
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        if (password.length() < 6) {
            Toast.makeText(AgregarUser.this, "Debe introducir un password de al menos 6 caracteres", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(AgregarUser.this, new OnCompleteListener<AuthResult>() {
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
        userdataMap.put("tipoUsuario", 0);

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
                        } else { //En caso de error
                            showMessage("Error en guardar los datos");
                        }
                    }
                });


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

                    //ACTUALIZAMOS LOS DATOS CUYO NODO PRINCIPAL SEA IDÉNTICO AL ID DEL USUARIO ACTUAL
                    rootRef.child("Usuarios").child(userID).updateChildren(userMap);
                }
            });

        }
    }

    private void References() {
        ImgUserPhoto = (CircleImageView) findViewById(R.id.reUser);
        Nombre = (EditText) findViewById(R.id.Nombre);
        Apellidos = (EditText) findViewById(R.id.Apellidos);
        edad = (EditText) findViewById(R.id.edad);
        MailInicial = (EditText) findViewById(R.id.MailInicial);
        PasswordInicial = (EditText) findViewById(R.id.PasswordInicial);
        registro = (Button) findViewById(R.id.botonCrearUsuario);
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
