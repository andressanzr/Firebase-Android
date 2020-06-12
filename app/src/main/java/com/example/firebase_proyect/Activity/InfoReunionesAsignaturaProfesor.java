package com.example.firebase_proyect.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_proyect.Models.Reunion;
import com.example.firebase_proyect.Models.Users;
import com.example.firebase_proyect.R;
import com.example.firebase_proyect.ViewHolder.ReunionInfoHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class InfoReunionesAsignaturaProfesor extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private RecyclerView recyclerView;

    private ArrayList<Reunion> listaReuniones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_reuniones_asignatura_profesor);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_info_reuniones);
        navigationView = (NavigationView) findViewById(R.id.navview_info_reuniones);
        recyclerView = findViewById(R.id.recycler_info_reuniones);

        updateNavHeader(user);

        setToolbar();

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
                        Intent c = new Intent(InfoReunionesAsignaturaProfesor.this, MiGestion.class);
                        startActivity(c);
                        break;
                    case R.id.reuniones:
                        Intent a = new Intent(InfoReunionesAsignaturaProfesor.this, ReunionesProfesorActivity.class);
                        startActivity(a);
                        break;
                    case R.id.configuracion:
                        Intent b = new Intent(InfoReunionesAsignaturaProfesor.this, ConfiguracionActivity.class);
                        startActivity(b);
                        break;
                    case R.id.cerrar_sesion:
                        signOut();
                        break;
                }
                return true;
            }
        });

        RecyclerView.LayoutManager layoutManager;
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("nombreAsig")) {
            final String asig = bundle.getString("nombreAsig");
            final DatabaseReference Reunionesref = FirebaseDatabase.getInstance().getReference().child("Reuniones");

            FirebaseRecyclerOptions<Reunion> opciones = new FirebaseRecyclerOptions.
                    Builder<Reunion>().setQuery(Reunionesref.orderByChild("asignatura").equalTo(asig), Reunion.class).build();
            FirebaseRecyclerAdapter<Reunion, ReunionInfoHolder> adapter = new FirebaseRecyclerAdapter<Reunion, ReunionInfoHolder>(opciones) {
                @Override
                protected void onBindViewHolder(@NonNull ReunionInfoHolder holder, final int position, @NonNull final Reunion model) {
                    //model.getAsignatura().equals(asig)
                    if (1 == 1) {
                        listaReuniones.add(model);
                        //Direcciona y devuelve el nombre con el numero
                        holder.NombreGrupo.setText(model.getGrupo());
                        holder.Fecha.setText(model.getFecha().toString());
                        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                //te muestra las dos opciones que tiene de modificar y eliminar
                                final CharSequence opciones[] = new CharSequence[]{
                                        "Eliminar"
                                };
                                //muestra un mensaje con
                                AlertDialog.Builder builder = new AlertDialog.Builder(InfoReunionesAsignaturaProfesor.this);
                                builder.setTitle("Opciones:");
                                builder.setItems(opciones, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        if (i == 0) {
                                            //Alerta de dialogo donde te indica si lo quieres eliminar
                                            AlertDialog.Builder builder1 = new AlertDialog.Builder(InfoReunionesAsignaturaProfesor.this);
                                            builder1.setTitle("Eliminar grupo");
                                            builder1.setMessage("¿Esta seguro de que quieres eliminar la reunion?");

                                            builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Reunionesref
                                                            .child(model.getID())
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(getApplicationContext(), "Reunion eliminada de la lista", Toast.LENGTH_SHORT).show();
                                                                        notifyItemRemoved(position);
                                                                    }
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
                                        }
                                    }
                                });
                                builder.setCancelable(true);
                                builder.show();
                                return true;
                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final ArrayList<String> listaUsuarios = new ArrayList<>();
                                final AlertDialog.Builder builder = new AlertDialog.Builder(InfoReunionesAsignaturaProfesor.this);

                                DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
                                RootRef.child("Usuarios").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (final DataSnapshot snapShot : dataSnapshot.getChildren()) {
                                            Users datosUsuario = snapShot.getValue(Users.class);
                                            if (datosUsuario.getGrupoUser() != null) {
                                                if (listaReuniones != null) {
                                                    if (datosUsuario.getGrupoUser().equals(listaReuniones.get(position).getGrupo())) {
                                                        listaUsuarios.add(datosUsuario.getNombre() + " " + datosUsuario.getApellido());
                                                        final CharSequence[] chars = listaUsuarios.toArray(new CharSequence[listaUsuarios.size()]);
                                                        builder.setTitle("Integrantes grupo").setItems(chars, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                            }
                                                        });


                                                    }
                                                }
                                            }
                                        }
                                        builder.setCancelable(true);
                                        builder.show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                if (listaUsuarios.size() > 0) {


                                }
                            }

                        });
                    }
                }

                @NonNull
                @Override
                public ReunionInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_reunion_info, parent, false);
                    ReunionInfoHolder holder = new ReunionInfoHolder(view);
                    return holder;
                }
            };
            recyclerView.setAdapter(adapter);
            adapter.startListening();
        }
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
                startActivity(new Intent(InfoReunionesAsignaturaProfesor.this, Login.class));
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
}
