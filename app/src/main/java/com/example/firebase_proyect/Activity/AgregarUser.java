package com.example.firebase_proyect.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_proyect.Models.Asignaturas;
import com.example.firebase_proyect.Models.Grupos;
import com.example.firebase_proyect.Models.Users;
import com.example.firebase_proyect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import de.hdodenhof.circleimageview.CircleImageView;

public class AgregarUser extends AppCompatActivity {
    private EditText Nombre, Apellidos, edad, MailInicial, PasswordInicial;
    private Button registro;
    private CircleImageView ImgUserPhoto;
    FirebaseStorage fileStorage;
    private RadioGroup radioGroup;
    private Spinner spinnerGrupo;
    private LinearLayout linearLayoutAsignaturas;

    private ArrayList<View> listaCheckboxAsignaturas = new ArrayList<>();

    private ArrayList<String> listaGrupos = new ArrayList<>();
    private ArrayList<String> listaAsignaturas = new ArrayList<>();
    private ArrayList<String> listaAsignaturasSeleccionadas = new ArrayList<>();

    private StorageReference UsersImagesRef;
    private DatabaseReference UsersRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private Uri pickedImgUri;
    StorageTask uploadTask;
    String myUrl = "";
    Bundle intentExtras;
    //Para guardar info en el storage de Firebase
    StorageReference storageProfilePictureRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_user);
        References();

        setToolbar();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        updateNavHeader(user);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().hide();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                getSupportActionBar().show();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_gestionar:
                        Intent i = new Intent(AgregarUser.this, MiGestion.class);
                        startActivity(i);
                        break;
                    case R.id.reuniones:
                        Intent a = new Intent(AgregarUser.this, ReunionesProfesorActivity.class);
                        startActivity(a);
                        break;
                    case R.id.configuracion:
                        Intent b = new Intent(AgregarUser.this, ConfiguracionActivity.class);
                        startActivity(b);
                        break;
                    case R.id.cerrar_sesion:
                        signOut();
                        break;
                }
                return true;
            }
        });
        fileStorage = FirebaseStorage.getInstance();
        intentExtras = getIntent().getExtras();
        if (intentExtras != null && intentExtras.containsKey("IDuser")) {
            getUserInfo(getIntent().getExtras().getString("IDuser"));
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
                    for (View vista :
                            listaCheckboxAsignaturas) {
                        CheckBox check = (CheckBox) vista;
                        if (check.isChecked()) {
                            listaAsignaturasSeleccionadas.add((String) check.getText());
                        }

                    }
                    CreateUserAccount(name, lastname, age, email, password);
                }
            }

        });
        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestForPermission();
                } else {
                    CropImage.activity(pickedImgUri).setAspectRatio(1, 1).start(AgregarUser.this);
                    openGallery();
                }
            }
        });

        // annadir grupos a spinner
        listaGrupos.add("Sin grupo");
        final DatabaseReference GruposRef = FirebaseDatabase.getInstance().getReference().child("Grupos");
        final ArrayAdapter<String> adapterGrupos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaGrupos);
        spinnerGrupo.setAdapter(adapterGrupos);

        GruposRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Grupos grupos = childSnapshot.getValue(Grupos.class);
                    listaGrupos.add(grupos.getNombre());
                    adapterGrupos.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // annadir checkboxs de asignaturas
        listaAsignaturas.add("Sin asignatura");
        addCheckbox("Sin asignatura");
        final DatabaseReference AsignaturasRef = FirebaseDatabase.getInstance().getReference().child("Asignaturas");
        AsignaturasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Asignaturas asignaturas = childSnapshot.getValue(Asignaturas.class);
                    listaAsignaturas.add(asignaturas.getNombre());
                    addCheckbox(asignaturas.getNombre());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addCheckbox(String nombre) {
        CheckBox checkBox = new CheckBox(getApplicationContext());
        checkBox.setText(nombre);
        checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // Add Checkbox to LinearLayout
        if (linearLayoutAsignaturas != null) {
            linearLayoutAsignaturas.addView(checkBox);
            listaCheckboxAsignaturas.add(checkBox);
        }
    }

    public void updateNavHeader(final FirebaseUser user) {
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

                            View headerView = navigationView.getHeaderView(0);
                            TextView navUsername = headerView.findViewById(R.id.nav_username);
                            TextView navUserLastname = headerView.findViewById(R.id.nav_lastname);
                            final ImageView navUserPhot = headerView.findViewById(R.id.nav_user_photo);
                            //Se obtiene la ID del usuario actual
                            String id = user.getUid();
                            //Se obtienen los string que representan las IDs en la BBDD
                            String idBBDD = datosUsuario.getID();
                            //Si el ID del usuario actual se corresponde con alguna de las guardadas,
                            //se obtienen los datos
                            if (idBBDD.equals(id)) {
                                String fotoBBDD = null;
                                //Se obtienen nombre y apellidos
                                String nombreBBDD = datosUsuario.getNombre();
                                String apellidosBBDD = datosUsuario.getApellido();

                                fotoBBDD = datosUsuario.getFoto();
                                //Se introducen los datos obtenidos en los elementos de la vista
                                if (fotoBBDD != null) {
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                    storageReference = storageReference.child("imagenesUsuarios/" + idBBDD + ".jpg");
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(navUserPhot);
                                        }
                                    });
                                } else {
                                    navUserPhot.setImageResource(R.drawable.seusuario);
                                }
                                navUserLastname.setText(apellidosBBDD);
                                navUsername.setText(nombreBBDD);
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

    private void signOut() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.logout);
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.signOut();
                //cierra la cuenta y le envia de nuevo al login
                startActivity(new Intent(AgregarUser.this, Login.class));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                // abrir el menu lateral
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(AgregarUser.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                //permiso para poder acceder la galería
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AgregarUser.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(AgregarUser.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();

            } else {
                ActivityCompat.requestPermissions(AgregarUser.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }

        } else
            openGallery();
    }

    private void openGallery() {
        //abre la intención de la galería y espera a que el usuario elija una imagen
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1 && data != null) {
            //el usuario ha elegido con éxito una imagen
            // necesitamos guardar su referencia a una variable Uri
            pickedImgUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImgUri);
        }
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

                                //spinner grupo seleccionar del user
                                for (int i = 0; i < listaGrupos.size(); i++) {
                                    if (datosUser.getGrupoUser() != null && datosUser.getGrupoUser().equals(listaGrupos.get(i))) {
                                        spinnerGrupo.setSelection(i);
                                        break;
                                    }
                                }


                                // check checkbox asignaturas
                                ArrayList<String> listaAsigUser = datosUser.getAsignaturasUser();
                                if (listaCheckboxAsignaturas.size() > 0) {
                                    if (listaAsigUser != null) {
                                        for (String asig :
                                                listaAsigUser) {
                                            for (View vista :
                                                    listaCheckboxAsignaturas) {
                                                if (((CheckBox) vista).getText().equals(asig)) {
                                                    ((CheckBox) vista).setChecked(true);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (datosUser.getTipoUsuario() == 2) {
                                    radioGroup.check(R.id.radioButtonProfesor);
                                } else {
                                    radioGroup.check(R.id.radioButtonAlumno);
                                }
                                Nombre.setText(nombreBd);
                                Apellidos.setText(apellidosBd);
                                MailInicial.setText(emailBd);
                                edad.setText(edadBd + "");
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
            if (intentExtras != null && intentExtras.containsKey("IDuser")) {
                updateUserInfo(nombre, apellido, edad, email, password, RootRef);
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
    }

    private void updateUserInfo(String nombre, String apellido, int edad, String email, String password, final DatabaseReference rootRef) {
        final String ID;
        if (intentExtras != null && intentExtras.containsKey("IDuser")) {
            ID = intentExtras.getString("IDuser");
        } else {
            ID = mAuth.getCurrentUser().getUid();
        }

        boolean sinAsig = false;
        HashMap<String, Object> userdataMap = new HashMap<>();
        userdataMap.put("ID", ID);
        userdataMap.put("nombre", nombre);
        userdataMap.put("apellido", apellido);
        userdataMap.put("edad", edad);
        userdataMap.put("email", email);
        userdataMap.put("password", password);
        if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonProfesor) {
            userdataMap.put("grupoUser", "");
            Toast.makeText(getApplicationContext(), "Profesor no puede estar en un grupo", Toast.LENGTH_LONG).show();
        } else {
            userdataMap.put("grupoUser", spinnerGrupo.getSelectedItem().toString());
        }

        for (String asig :
                listaAsignaturasSeleccionadas) {
            if (asig.equals("Sin asignatura")) {
                sinAsig = true;
                Toast.makeText(getApplicationContext(), "Sin asignaturas", Toast.LENGTH_LONG).show();
            }
        }

        if (sinAsig) {
            userdataMap.put("asignaturasUser", new ArrayList<>());
        } else {
            userdataMap.put("asignaturasUser", listaAsignaturasSeleccionadas);
        }
        if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonAlumno) {
            userdataMap.put("tipoUsuario", 1);
        } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonProfesor) {
            userdataMap.put("tipoUsuario", 2);
        } else {
            userdataMap.put("tipoUsuario", 1);
        }
        //tipo por defecto alumno

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

                            Intent intent = new Intent(AgregarUser.this, MiGestion.class).putExtra("fragNumber", 0);
                            ;

                            startActivity(intent);

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

    private void setToolbar() {
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void References() {
        ImgUserPhoto = (CircleImageView) findViewById(R.id.reUser);
        Nombre = (EditText) findViewById(R.id.Nombre);
        Apellidos = (EditText) findViewById(R.id.Apellidos);
        edad = (EditText) findViewById(R.id.edad);
        MailInicial = (EditText) findViewById(R.id.MailInicial);
        PasswordInicial = (EditText) findViewById(R.id.PasswordInicial);
        registro = (Button) findViewById(R.id.botonCrearUsuario);
        radioGroup = findViewById(R.id.radioButtonGroup);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_agregar_user);
        navigationView = (NavigationView) findViewById(R.id.navview_agregar_user);
        spinnerGrupo = findViewById(R.id.spinner_grupo);
        linearLayoutAsignaturas = findViewById(R.id.layout_asignaturas_user);
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
