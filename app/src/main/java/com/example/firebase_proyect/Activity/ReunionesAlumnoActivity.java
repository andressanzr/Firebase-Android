package com.example.firebase_proyect.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_proyect.Models.Reunion;
import com.example.firebase_proyect.Models.Users;
import com.example.firebase_proyect.R;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


public class ReunionesAlumnoActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ListView listView;
    private Users usuario;
    private DatabaseReference ReunionesRef;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ArrayList<String> listaAsignaturas = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reuniones);
        getSupportActionBar().setTitle("Reuniones");
        setToolbar();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        updateNavHeader(user);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_reuniones);
        navigationView = (NavigationView) findViewById(R.id.navview_reuniones);
        listView = findViewById(R.id.list_view_asignaturas_alumno);

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
                    case R.id.reunionesAlumno:
                        Intent a = new Intent(ReunionesAlumnoActivity.this, ReunionesAlumnoActivity.class);
                        startActivity(a);
                        break;
                    case R.id.configuracionAlumno:
                        Intent b = new Intent(ReunionesAlumnoActivity.this, ConfiguracionActivity.class);
                        startActivity(b);
                        break;
                    case R.id.cerrar_sesionAlumno:
                        signOut();
                        break;
                }
                return true;
            }
        });


        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaAsignaturas);

        listView.setAdapter(arrayAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, final int i, final long l) {
                ReunionesRef = FirebaseDatabase.getInstance().getReference().child("Reuniones");
                // boolean causado por al añadir reunion salta ondataChange y elimina la reunion
                final boolean borrarOK = false;
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ReunionesAlumnoActivity.this);
                builder1.setTitle("Eliminar reunion");
                builder1.setMessage("¿Estas seguro que quieres eliminar la reunion?");
                builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ReunionesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (final DataSnapshot snapShot : dataSnapshot.getChildren()) {
                                    ReunionesRef.child(snapShot.getKey()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Reunion datosReunion = snapShot.getValue(Reunion.class);
                                            if (datosReunion.getAsignatura().equals(listaAsignaturas.get(i))) {
                                                ReunionesRef.child(datosReunion.getID()).removeValue()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(ReunionesAlumnoActivity.this, "Reunion eliminada", Toast.LENGTH_SHORT).show();
                                                                view.setBackgroundColor(Color.WHITE);

                                                            }
                                                        });
                                            }
                                            ReunionesRef.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                                ReunionesRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                });

                builder1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder1.show();
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                ReunionesRef = FirebaseDatabase.getInstance().getReference().child("Reuniones");
                String ID = ReunionesRef.push().getKey();

                if (view.getBackground() != null && ((ColorDrawable) view.getBackground()).getColor() == Color.GREEN) {
                    Toast.makeText(ReunionesAlumnoActivity.this, "Reunion ya existente", Toast.LENGTH_SHORT).show();
                } else {
                    if (usuario.getGrupoUser().equals("")) {
                        Toast.makeText(ReunionesAlumnoActivity.this, "No tienes grupo no puedes reunirte", Toast.LENGTH_SHORT).show();
                    } else {
                        //Guarda todos los datos ingresados con las siguientes características
                        HashMap<String, Object> reunionMap = new HashMap<>();
                        reunionMap.put("ID", ID);
                        reunionMap.put("asignatura", listaAsignaturas.get(i));
                        reunionMap.put("grupo", usuario.getGrupoUser());
                        reunionMap.put("fecha", new Date());

                        ReunionesRef.child(ID).updateChildren(reunionMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ReunionesAlumnoActivity.this, "Reunion guardada en la base de datos", Toast.LENGTH_SHORT).show();
                                            view.setBackgroundColor(Color.GREEN);
                                        } else {
                                            String mensaje = task.getException().toString();
                                            Toast.makeText(ReunionesAlumnoActivity.this, "Error: " + mensaje, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }

            }

        });
        final String userEmail = user.getEmail();

        final DatabaseReference Usersref = FirebaseDatabase.getInstance().getReference().child("Usuarios");

        Usersref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapShot : dataSnapshot.getChildren()) {
                    Usersref.child(snapShot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final Users datosUser = snapShot.getValue(Users.class);
                            String emailBd = datosUser.getEmail();

                            if (emailBd.equals(userEmail) && datosUser.getAsignaturasUser() != null) {
                                for (String data : datosUser.getAsignaturasUser()
                                ) {
                                    listaAsignaturas.add(data);
                                }
                                usuario = datosUser;
                                ReunionesRef = FirebaseDatabase.getInstance().getReference().child("Reuniones");
                                ReunionesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (final DataSnapshot snapShot : dataSnapshot.getChildren()) {
                                            ReunionesRef.child(snapShot.getKey()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    Reunion datosReunion = snapShot.getValue(Reunion.class);
                                                    for (int t = 0; t < datosUser.getAsignaturasUser().size(); t++) {
                                                        if (datosReunion.getAsignatura().equals(usuario.getAsignaturasUser().get(t)) && datosReunion.getGrupo().equals(usuario.getGrupoUser())) {
                                                            listView.getChildAt(t).setBackgroundColor(Color.GREEN);
                                                        }
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

                                arrayAdapter.notifyDataSetChanged();
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
                            try {
                                //Para extraer los datos de la BBDD con ayuda de la clase Usuarios
                                Users datosUsuario = snapShot.getValue(Users.class);
                                NavigationView navigationView = (NavigationView) findViewById(R.id.navview_reuniones);
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
                            } catch (NullPointerException e) {
                                e.printStackTrace();
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
                startActivity(new Intent(ReunionesAlumnoActivity.this, Login.class));
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


    private void setToolbar() {
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
}
