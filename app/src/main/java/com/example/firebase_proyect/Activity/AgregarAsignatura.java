package com.example.firebase_proyect.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_proyect.Models.Asignaturas;
import com.example.firebase_proyect.Models.Users;
import com.example.firebase_proyect.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
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

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class AgregarAsignatura extends AppCompatActivity {

    private String ID;
    private ImageView fotoAsignatura;
    private static final int GalleryPick = 1;
    String myUrl = "";
    StorageTask uploadTask;
    private StorageReference SubjectsImagesRef;
    private DatabaseReference SubjectsRef;
    private EditText nombreAsig, nombreCurs, description;
    private Button guardar;
    private Uri ImagenUri;
    String IDexistente = "";

    private FirebaseAuth mAuth;
    private FirebaseUser user;


    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_asignatura);
        setToolbar();
        References();

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
                        Intent i = new Intent(AgregarAsignatura.this, MiGestion.class);
                        startActivity(i);
                        break;
                    case R.id.reuniones:
                        Intent a = new Intent(AgregarAsignatura.this, ReunionesProfesorActivity.class);
                        startActivity(a);
                        break;
                    case R.id.configuracion:
                        Intent b = new Intent(AgregarAsignatura.this, ConfiguracionActivity.class);
                        startActivity(b);
                        break;
                    case R.id.cerrar_sesion:
                        signOut();
                        break;
                }
                return true;
            }
        });
        try {
            IDexistente = getIntent().getStringExtra("IDsubject");
            getSubjectInfo(IDexistente);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        SubjectsRef = FirebaseDatabase.getInstance().getReference().child("Asignaturas");
        SubjectsImagesRef = FirebaseStorage.getInstance().getReference().child("Imagenes asignaturas");


        fotoAsignatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
            }
        });


        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subjectName = nombreAsig.getText().toString().trim();
                String subjectCourse = nombreCurs.getText().toString().trim();
                String subjectDescription = description.getText().toString().trim();

                if (TextUtils.isEmpty(subjectName)) {
                    Toast.makeText(AgregarAsignatura.this, "Se requiere un nombre para la asignatura", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(subjectCourse)) {
                    Toast.makeText(AgregarAsignatura.this, "Se requiere un curso para la asignatura", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(subjectDescription)) {
                    Toast.makeText(AgregarAsignatura.this, "Se requiere una descripción para la asignatura", Toast.LENGTH_SHORT).show();
                } else {
                    addSubjectIntoDB(subjectName, subjectCourse, subjectDescription);
                }
            }
        });

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
                startActivity(new Intent(AgregarAsignatura.this, Login.class));
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

    private void getSubjectInfo(final String IDexistente) {

        SubjectsRef = FirebaseDatabase.getInstance().getReference().child("Asignaturas");

        SubjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (final DataSnapshot snapShot : dataSnapshot.getChildren()) {
                    SubjectsRef.child(snapShot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Asignaturas datosAsignatura = snapShot.getValue(Asignaturas.class);
                            String id = IDexistente;
                            String idBd = datosAsignatura.getID();
                            if (idBd.equals(id)) {

                                String fotoBd = null;
                                fotoBd = datosAsignatura.getFoto();
                                //Se introducen los datos obtenidos en los elementos de la vista
                                if (fotoBd != null) {
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                    storageReference = storageReference.child("Imagenes Asignaturas/" + IDexistente + ".jpg");
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(fotoAsignatura);
                                        }
                                    });
                                } else {
                                    fotoAsignatura.setImageResource(R.drawable.imgcurso);
                                }

                                //declaración para que devuelva los balores
                                String name = datosAsignatura.getNombre();
                                String course = datosAsignatura.getCurso();
                                String descripcion = datosAsignatura.getDescripcion();


                                nombreAsig.setText(name);
                                nombreCurs.setText(course);
                                description.setText(descripcion);

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

    private void addSubjectIntoDB(String subjectName, String subjectCourse, String subjectDescription) {

        saveInfoSubjectinBBDD(subjectName, subjectCourse, subjectDescription);
    }


    private void addPhoto(final Uri imageUri, final DatabaseReference rootRef, final String ID) {

        if (imageUri != null) {
            //Busca mediante la Uri en el FireStore
            final StorageReference fileref = SubjectsImagesRef.child(ID + ".jpg");
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
                        HashMap<String, Object> subjectMap = new HashMap<>();
                        subjectMap.put("foto", myUrl);

                        //ACTUALIZAMOS LOS DATOS CUYO NODO PRINCIPAL SEA IDÉNTICO AL ID DEL USUARIO ACTUAL
                        rootRef.child(ID).updateChildren(subjectMap);

                    } else {
                        Toast.makeText(AgregarAsignatura.this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void saveInfoSubjectinBBDD(String subjectName, String subjectCourse, String subjectDescription) {
        if (IDexistente != null) {
            ID = IDexistente;
        } else {
            ID = SubjectsRef.push().getKey();
        }
        //Guarda todos los datos ingresados con las siguientes características
        HashMap<String, Object> subjectMap = new HashMap<>();
        subjectMap.put("ID", ID);
        subjectMap.put("nombre", subjectName);
        subjectMap.put("curso", subjectCourse);
        subjectMap.put("descripcion", subjectDescription);
        try {
            addPhoto(ImagenUri, SubjectsRef, ID);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        SubjectsRef.child(ID).updateChildren(subjectMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AgregarAsignatura.this, "Asignaturas actualizadas en la base de datos", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AgregarAsignatura.this, MiGestion.class).putExtra("fragNumber", 1);
                            startActivity(intent);
                        } else {
                            String mensaje = task.getException().toString();
                            Toast.makeText(AgregarAsignatura.this, "Error: " + mensaje, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //se muestra la galería
    private void abrirGaleria() {
        Intent galeriaIntent = new Intent();
        galeriaIntent.setAction(Intent.ACTION_GET_CONTENT);
        galeriaIntent.setType("image/*");
        startActivityForResult(galeriaIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            ImagenUri = data.getData();
            Picasso.get().load(ImagenUri).resize(250, 250).into(fotoAsignatura);

        }
    }

    private void setToolbar() {
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //referencia los datos
    private void References() {
        fotoAsignatura = (ImageView) findViewById(R.id.imageCurso);
        nombreAsig = (EditText) findViewById(R.id.textNombreAsig);
        nombreCurs = (EditText) findViewById(R.id.txtCurso);
        description = (EditText) findViewById(R.id.txtDescripcion);
        guardar = (Button) findViewById(R.id.guardarDatos);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_agregar_asignatura);
        navigationView = (NavigationView) findViewById(R.id.navview_agregar_asignatura);
    }

}

