package com.example.firebase_proyect.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.firebase_proyect.Fragment.AlumnosFragment;
import com.example.firebase_proyect.Models.Users;
import com.google.android.material.navigation.NavigationView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebase_proyect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

public class NavigationActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    boolean isDark = false;
    private FirebaseAuth mAuth;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar);
        setToolbar();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navview);


        updateNavHeader(user);

        setFragmentByDefault();
        getSupportActionBar().setTitle("Gestionar");
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
                boolean fragmentTransaction = false;
                Activity fragment = null;


                switch (item.getItemId()) {

                    case R.id.menu_gestionar:
                        Intent i = new Intent(NavigationActivity.this, ActivityGestionar.class);
                        startActivity(i);
                        break;
                    case R.id.reuniones:
                        Intent a = new Intent(NavigationActivity.this, ReunionesActivity.class);
                        startActivity(a);
                        break;
                    case R.id.configuracion:
                        Intent b = new Intent(NavigationActivity.this, ConfiguracionActivity.class);
                        startActivity(b);
                        break;

                    case R.id.cerrar_sesion:
                        signOut();
                        break;
                }



              /*  if (fragmentTransaction) {
                    changeFragment(fragment, item);
                    drawerLayout.closeDrawers();
                }*/
                return true;
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
                            NavigationView navigationView = (NavigationView) findViewById(R.id.navview);
                            View headerView = navigationView.getHeaderView(0);
                            TextView navUsername = headerView.findViewById(R.id.nav_username);
                            TextView navUserLastname = headerView.findViewById(R.id.nav_lastname);
                            ImageView navUserPhot = headerView.findViewById(R.id.nav_user_photo);
                            //Se obtiene la ID del usuario actual
                            String id = user.getUid();
                            //Se obtienen los string que representan las IDs en la BBDD
                            String idBBDD = datosUsuario.getID();
                            //Si el ID del usuario actual se corresponde con alguna de las guardadas,
                            //se obtienen los datos
                            if (idBBDD.equals(id)) {
                                String fotoBBDD = null;
                                /*
                                //Se obtiene el url de ubicación de la foto en caso de estar guardado
                                if(snapShot.child("foto").exists()){
                                    fotoBBDD=datosUsuario.getFoto();
                                }

                                 */
                                //Se obtienen nombre y apellidos
                                String nombreBBDD = datosUsuario.getNombre();
                                String apellidosBBDD = datosUsuario.getApellido();

                                fotoBBDD = datosUsuario.getFoto();
                                //Se introducen los datos obtenidos en los elementos de la vista
                                if(fotoBBDD!=null){
                                    //Glide.with(context).load(fotoBBDD).into(navUserPhot);
                                    // TODO ARREGLAR CARGAR FOTO DE FIREBASE
                                    Picasso.get().load(fotoBBDD).into(navUserPhot);
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



       // navUserMail.setText(user.getEmail());
        //navUsername.setText(Users.getNombre());

        // now we will use Glide to load user image
        // first we need to import the library

        // Glide.with(this).load(currentUser.getPhotoUrl()).into(navUserPhot);




    }
    private void setToolbar() {
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setFragmentByDefault() {
        changeFragment(new AlumnosFragment(), navigationView.getMenu().getItem(0));
    }
    private void signOut() {
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.logout);
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.signOut();
                //cierra la cuenta y le envia de nuevo al login
                startActivity(new Intent(NavigationActivity.this, Login.class));
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
    private void changeFragment(Fragment fragment, MenuItem item) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
        item.setChecked(true);
        getSupportActionBar().setTitle(item.getTitle());
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
