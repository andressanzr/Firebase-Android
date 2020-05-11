package com.example.firebase_proyect.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.firebase_proyect.Fragment.Alumnos;
import com.example.firebase_proyect.Fragment.Asignaturas;
import com.example.firebase_proyect.Fragment.Grupos;
import com.google.android.material.navigation.NavigationView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebase_proyect.Models.Users;
import com.example.firebase_proyect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityGestionar extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    //private RecyclerView recycler;
  //  private FloatingActionButton fab;
    boolean isDark = false;
    EditText searchInput ;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    //private RecyclerView.LayoutManager Manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar);
        setToolbar();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navview);


        updateNavHeader();

        setFragmentByDefault();

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
                Fragment fragment = null;


                switch (item.getItemId()) {

                    case R.id.menu_alumnos:
                        fragment = new Alumnos();
                        fragmentTransaction = true;
                        break;
                    case R.id.menu_grupos:
                        fragment = new Asignaturas();
                        fragmentTransaction = true;
                        break;
                    case R.id.menu_asignaturas:
                        fragment = new Grupos();
                        fragmentTransaction = true;
                        break;

                    case R.id.cerrar_sesion:
                        signOut();
                        break;
                }


                if (fragmentTransaction) {
                    changeFragment(fragment, item);
                    drawerLayout.closeDrawers();
                }
                return true;
            }
        });



    }

    public void updateNavHeader() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.navview);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_username);
        TextView navUserMail = headerView.findViewById(R.id.nav_gmail);
        ImageView navUserPhot = headerView.findViewById(R.id.nav_user_photo);

       // navUserMail.setText(Users.getEmail());
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
        changeFragment(new Alumnos(), navigationView.getMenu().getItem(0));
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
                startActivity(new Intent(ActivityGestionar.this, Login.class));
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
