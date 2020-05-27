package com.example.firebase_proyect.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_proyect.Models.Users;
import com.example.firebase_proyect.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DialogInterface.OnClickListener, View.OnClickListener {
    private Button guardar;
    private EditText nombrepersona, apellidopersona, edadpersona, correopersona, nuevacontra;
    private CircleImageView imagenperfil;
    private FirebaseAuth mAuth;
    private TextView userName;
    private CircleImageView profileImage;

    private FirebaseUser user;
    Uri imageUri;
    String myUrl = "";
    StorageTask uploadTask;
    private AlertDialog.Builder builder;
    private EditText editTextPassword;

    static int emailRep = 0;
    static String ID;
    StorageReference storageProfilePictureRef;

    static String originalPassword;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;

    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        getSupportActionBar().setTitle("Configuración Usuario");
        References();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        drawerLayout = findViewById(R.id.drawer_perfil);
        navigationView = findViewById(R.id.nav_view_perfil);
        navigationView.setNavigationItemSelectedListener(this);

        //Funcionamiento del icono hamburguesa
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();


        userName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_username);
        profileImage = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.nav_user_photo);

        ID = user.getUid();


        mAuth = FirebaseAuth.getInstance();
        storageProfilePictureRef = FirebaseStorage.getInstance().getReference().child("Fotos de perfil");


        guardar.setOnClickListener(this);


        imagenperfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity(imageUri).setAspectRatio(1, 1)
                        .start(ConfiguracionActivity.this);
            }
        });
        getUsuarioInfo(user);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                imageUri = result.getUri();
                imagenperfil.setImageURI(imageUri);
            }
        } else {
            Toast.makeText(this, "Error, inténtelo de nuevo", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onClick(View view) {

        //VENTANA EMERGENTE
        builder = new AlertDialog.Builder(ConfiguracionActivity.this);
        builder.setTitle("Confirmar cambios");
        builder.setMessage("Debe introducir la contraseña actual, para que se pueda actualizar los datos");
        //EL TEXTO ESCRITO APARECERÁ EN EL CENTRO DE LA PANTALLA
        editTextPassword = new EditText(ConfiguracionActivity.this);
        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(editTextPassword);

        // BOTONES DE ACEPTAR/CANCELAR
        builder.setPositiveButton("Aceptar", ConfiguracionActivity.this);
        builder.setNegativeButton("Cancelar", ConfiguracionActivity.this);
        builder.show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            originalPassword = editTextPassword.getText().toString();
            if (!originalPassword.isEmpty()) {
                userInfoSaved();
            } else {
                Toast.makeText(ConfiguracionActivity.this, "Contraseña ACTUAL requerida para guardar cambios", Toast.LENGTH_SHORT).show();
            }
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            dialogInterface.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_perfil);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void userInfoSaved() {
        String nombre = nombrepersona.getText().toString().trim();
        String apellido = apellidopersona.getText().toString().trim();
        int edad = Integer.parseInt(edadpersona.getText().toString());
        String email = correopersona.getText().toString().trim();
        String password = nuevacontra.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) || edad < 0 ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(ConfiguracionActivity.this, "Error, rellene los campos vacíos", Toast.LENGTH_SHORT).show();
        } else {
            uploadImage(nombre, apellido, edad, email, password);
        }
    }


    private void uploadImage(final String nombre, final String apellido, final int edad, final String email, final String password) {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapShot : dataSnapshot.getChildren()) {
                    Users datosUsuario = snapShot.getValue(Users.class);
                    String emailBBDD = datosUsuario.getEmail();
                    String idBBDD = datosUsuario.getID();

                    if (!ID.equals(idBBDD)) {

                        if (email.equals(emailBBDD)) {
                            emailRep++;
                        }
                    }
                }
                boolean emailValido;
                boolean passValido;
                if (emailRep == 0) {
                    emailValido = true;
                } else {
                    emailValido = false;
                    Toast.makeText(ConfiguracionActivity.this, "email no disponible", Toast.LENGTH_SHORT).show();
                }
                if (password.length() < 6) {
                    passValido = false;
                    Toast.makeText(ConfiguracionActivity.this, "Debe introducir un password de al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                } else {
                    passValido = true;
                }
                if (emailValido && passValido) {

                    AuthCredential credential = EmailAuthProvider
                            .getCredential(user.getEmail(), originalPassword); // Current Login Credentials \\
                    // Prompt the user to re-provide their sign-in credentials
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        guardaEmail(email, RootRef);
                                        guardaPassword(password, RootRef);
                                        actualizaPerfil(nombre, apellido, edad, RootRef);
                                        actualizaImagen(imageUri, RootRef);
                                        mAuth.signOut();
                                        Toast.makeText(ConfiguracionActivity.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ConfiguracionActivity.this, Login.class));
                                    } else {
                                        Toast.makeText(ConfiguracionActivity.this, "Verifique la contraseña, por favor", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        emailRep = 0;
    }


    private void guardaEmail(final String email, final DatabaseReference rootRef) {
        final String ID = user.getUid();
        final HashMap<String, Object> userdataMap = new HashMap<>();

        //inputEmail.getText().toString.trim
        user.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            userdataMap.put("email", email);
                            rootRef.child("Usuarios").child(ID).updateChildren(userdataMap);
                        } else {
                            Toast.makeText(ConfiguracionActivity.this, "Error en la inserción de email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void guardaPassword(final String password, final DatabaseReference rootRef) {
        final String ID = user.getUid();
        final HashMap<String, Object> userdataMap = new HashMap<>();
        user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    userdataMap.put("password", password);
                    rootRef.child("Usuarios").child(ID).updateChildren(userdataMap);
                } else {
                    Toast.makeText(ConfiguracionActivity.this, "UPS. ocurrio un error al modificar contraseña", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void actualizaPerfil(String nombre, String apellido, int edad, DatabaseReference rootRef) {
        final String ID = user.getUid();
        HashMap<String, Object> userdataMap = new HashMap<>();
        userdataMap.put("nombre", nombre);
        userdataMap.put("apellido", apellido);
        userdataMap.put("edad", edad);
        rootRef.child("Usuarios").child(ID).updateChildren(userdataMap);
    }


    private void actualizaImagen(final Uri imageUri, final DatabaseReference rootRef) {

        if (imageUri != null) {
            final StorageReference fileref = storageProfilePictureRef.child(user.getUid() + ".jpg");
            uploadTask = fileref.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();
                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("foto", myUrl);
/*
                        //FIJAMOS FOTO DEL PERFIL DE FIREBASE
                        UserProfileChangeRequest profileUpdatesPhoto = new UserProfileChangeRequest.Builder().setPhotoUri(imageUri).build();
                        user.updateProfile(profileUpdatesPhoto);
                        */
                        //ACTUALIZAMOS LOS DATOS CUYO NODO PRINCIPAL SEA IDÉNTICO AL ID DEL USUARIO ACTUAL
                        rootRef.child("Usuarios").child(user.getUid()).updateChildren(userMap);
                    } else {

                        Toast.makeText(ConfiguracionActivity.this, "Error en la modificación de foto", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.menu_gestionar:
                Intent i = new Intent(this, ActivityGestionar.class);
                startActivity(i);
                break;
            case R.id.reuniones:
                Intent a = new Intent(this, ReunionesActivity.class);
                startActivity(a);
                break;
            case R.id.configuracion:
                Intent b = new Intent(this, ConfiguracionActivity.class);
                startActivity(b);
                break;
            case R.id.cerrar_sesion:
                signOut();
                break;
        }
        return true;
    }

    private void signOut() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(this);
        alert.setMessage(R.string.logout);
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.signOut();
                //cierra la cuenta y le envia de nuevo al login
                startActivity(new Intent(ConfiguracionActivity.this, Login.class));
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

    private void getUsuarioInfo(final FirebaseUser user) {

        //Ruta donde buscaremos la información asociada al usuario
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (final DataSnapshot snapShot : dataSnapshot.getChildren()) {
                    //Accedemos a la base de datos en la ruta indicada
                    RootRef.child("Usuarios").child(snapShot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //Para extraer los datos de la BBDD con ayuda de la clase Usuarios
                            Users datosUsuario = snapShot.getValue(Users.class);
                            //Se obtiene la ID del usuario actual
                            String id = user.getUid();
                            //Se obtienen los string que representan las IDs en la BBDD
                            String idBBDD = datosUsuario.getID();
                            //Si el ID del usuario actual se corresponde con alguna de las guardadas,
                            //se obtienen los datos
                            if (idBBDD.equals(id)) {

                                String fotoBBDD = null;
                                //Se obtiene el url de ubicación de la foto en caso de estar guardado
                                if (snapShot.child("foto").exists()) {
                                    fotoBBDD = datosUsuario.getFoto();
                                }
                                //Se obtienen nombre y apellidos
                                String nombreBBDD = datosUsuario.getNombre();
                                String apellidosBBDD = datosUsuario.getApellido();
                                int edadBBDD = datosUsuario.getEdad();
                                String emailBBDD = datosUsuario.getEmail();
                                String passwordBBDD = datosUsuario.getPassword();

                                if (fotoBBDD != null) {
                                    //Glide.with(context).load(fotoBBDD).into(navUserPhot);
                                    // TODO ARREGLAR CARGAR FOTO DE FIREBASE
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                    storageReference = storageReference.child("imagenesUsuarios/" + idBBDD + ".jpg");
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imagenperfil);
                                        }
                                    });

                                } else {
                                    imagenperfil.setImageResource(R.drawable.seusuario);
                                }
                                userName.setText(nombreBBDD + " " + apellidosBBDD);
                                //Rellenamos los campos con los datos actuales
                                nombrepersona.setText(nombreBBDD);
                                apellidopersona.setText(apellidosBBDD);
                                edadpersona.setText(edadBBDD + "");
                                correopersona.setText(emailBBDD);
                                nuevacontra.setText(passwordBBDD);
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

    private void References() {
        imagenperfil = (CircleImageView) findViewById(R.id.fotonueva);
        nombrepersona = (EditText) findViewById(R.id.nombrePersona);
        apellidopersona = (EditText) findViewById(R.id.apellidoPersona);
        edadpersona = (EditText) findViewById(R.id.edadPersona);
        correopersona = (EditText) findViewById(R.id.correoPersona);
        nuevacontra = (EditText) findViewById(R.id.nuevaContrasena);
        guardar = (Button) findViewById(R.id.guardarDatos);
    }
}