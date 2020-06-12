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

import com.example.firebase_proyect.Models.Grupos;
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

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class AgregarGrupos extends AppCompatActivity {
    private EditText gruponumero, gruponombre;
    private String ID;
    String IDexistente = "";

    private DatabaseReference GroupsRef;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private Button guardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_grupos);
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
                        Intent i = new Intent(AgregarGrupos.this, MiGestion.class);
                        startActivity(i);
                        break;
                    case R.id.reuniones:
                        Intent a = new Intent(AgregarGrupos.this, ReunionesProfesorActivity.class);
                        startActivity(a);
                        break;
                    case R.id.configuracion:
                        Intent b = new Intent(AgregarGrupos.this, ConfiguracionActivity.class);
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
            IDexistente = getIntent().getStringExtra("IDgroup");
            getGroupInfo(IDexistente);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Grupos");


        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int groupNumber = Integer.parseInt(gruponumero.getText().toString().trim());
                String groupName = gruponombre.getText().toString().trim();
                //si el número es mayor que 3 le myestra un mensaje
                if (groupNumber <= 0) {
                    Toast.makeText(AgregarGrupos.this, "Introduzca un número valido", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(AgregarGrupos.this, "Se requiere un nombre para el curso", Toast.LENGTH_SHORT).show();
                } else {
                    saveInfoGroupinBBDD(groupNumber, groupName);
                }
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
                startActivity(new Intent(AgregarGrupos.this, Login.class));
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

    private void setToolbar() {
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getGroupInfo(final String IDexistente) {
        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Grupos");

        GroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (final DataSnapshot snapShot : dataSnapshot.getChildren()) {
                    GroupsRef.child(snapShot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Grupos datosGrupo = snapShot.getValue(Grupos.class);
                            String id = IDexistente;
                            String idBd = datosGrupo.getID();
                            if (idBd.equals(id)) {


                                int numero = datosGrupo.getNumero();
                                String nombregrup = datosGrupo.getNombre();

                                gruponumero.setText(numero + "");
                                gruponombre.setText(nombregrup);
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

    private void saveInfoGroupinBBDD(final int groupNumber, final String groupName) {
        if (IDexistente != null) {
            ID = IDexistente;
        } else {
            ID = GroupsRef.push().getKey();
        }

        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Grupos");
        GroupsRef.orderByChild("numero").equalTo(groupNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(AgregarGrupos.this, "Numero de grupo repetido, introduzca otro", Toast.LENGTH_SHORT).show();
                        } else {
                            HashMap<String, Object> groupMap = new HashMap<>();
                            groupMap.put("ID", ID);
                            groupMap.put("numero", groupNumber);
                            groupMap.put("nombre", groupName);

                            GroupsRef.child(ID).updateChildren(groupMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(AgregarGrupos.this, "Grupos actualizadas correctamente", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(AgregarGrupos.this, MiGestion.class).putExtra("fragNumber", 2);
                                                startActivity(intent);
                                            } else {
                                                String mensaje = task.getException().toString();
                                                Toast.makeText(AgregarGrupos.this, "Error: " + mensaje, Toast.LENGTH_SHORT).show();
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

    private void References() {
        gruponumero = (EditText) findViewById(R.id.textnumero);
        gruponombre = (EditText) findViewById(R.id.textnombreGrupo);
        guardar = (Button) findViewById(R.id.guardarDatos);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_agregar_grupo);
        navigationView = (NavigationView) findViewById(R.id.navview_agregar_grupo);
    }
}