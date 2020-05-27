package com.example.firebase_proyect.Fragment;

import android.app.SearchManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebase_proyect.Models.Users;
import com.example.firebase_proyect.R;
import com.example.firebase_proyect.ViewHolder.UserHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class UsersFragment extends Fragment {

    private FloatingActionButton fab;

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    RecyclerView recyclerView;

    static String UserQuery = "";

    public UsersFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        //menu donde contiene el buscador
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {

                    UserQuery = searchView.getQuery().toString();
                    onStart();
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {

                    UserQuery = searchView.getQuery().toString();
                    onStart();
                    return false;
                }

            };

            searchView.setOnQueryTextListener(queryTextListener);


        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                // Not implemented here
                return false;
            default:
                break;
        }
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_alumnos, container, false);
        fab=(FloatingActionButton) v.findViewById(R.id.fabAddBoard);



        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        final DatabaseReference UserssRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");

        FirebaseRecyclerOptions<Users> opciones = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(UserssRef.orderByChild("nombre").startAt(""), Users.class)
                .build();
        FirebaseRecyclerAdapter<Users, UserHolder> adapter = new FirebaseRecyclerAdapter<Users, UserHolder>(opciones) {
            @Override
            protected void onBindViewHolder(@NonNull final UserHolder holder, int position, @NonNull Users model) {
                holder.NombreApellidosAlumno.setText(model.getNombre() + " " + model.getApellido());
                if (model.getTipoUsuario() == 0) {
                    holder.TipoAlumno.setText("Alumno");
                } else if (model.getTipoUsuario() == 1) {
                    holder.TipoAlumno.setText("Profesor");
                }
                if (model.getFoto() == null) {
                    holder.FotoAlumno.setImageResource(R.drawable.imagecurso);
                } else {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                    storageReference = storageReference.child("imagenesUsuarios/" + model.getID() + ".jpg");
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(holder.FotoAlumno);
                        }
                    });
                }

            }

            @NonNull
            @Override
            public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return null;
            }
        };
    }

}