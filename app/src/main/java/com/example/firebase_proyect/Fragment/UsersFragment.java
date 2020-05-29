package com.example.firebase_proyect.Fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.firebase_proyect.Activity.AgregarUser;
import com.example.firebase_proyect.Interface.ItemClickListener;
import com.example.firebase_proyect.Models.Users;
import com.example.firebase_proyect.R;
import com.example.firebase_proyect.ViewHolder.UserHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class UsersFragment extends Fragment {

    private FloatingActionButton fab;

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    RecyclerView recyclerView;

    static String user = "";

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
        View view = inflater.inflate(R.layout.fragment_alumnos, container, false);

        RecyclerView.LayoutManager layoutManager;
        recyclerView = view.findViewById(R.id.recycler_alumnos);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        fab = (FloatingActionButton) view.findViewById(R.id.fabAddUser);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AgregarUser.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        final DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");

        FirebaseRecyclerOptions<Users> opciones = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(UsersRef.orderByChild("nombre").startAt(user), Users.class)
                .build();
        FirebaseRecyclerAdapter<Users, UserHolder> adapter = new FirebaseRecyclerAdapter<Users, UserHolder>(opciones) {
            @Override
            protected void onBindViewHolder(@NonNull final UserHolder holder, final int position, final @NonNull Users model) {
                holder.NombreApellidosAlumno.setText(model.getNombre() + " " + model.getApellido());
                if (model.getTipoUsuario() == 0) {
                    holder.TipoAlumno.setText("Alumno");
                } else if (model.getTipoUsuario() == 1) {
                    holder.TipoAlumno.setText("Profesor");
                }
                if (model.getFoto() == null) {
                    holder.FotoAlumno.setImageResource(R.drawable.seusuario);
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

                holder.itemView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        //Muestra las dos opciones que tiene de modificar y eliminar
                        final CharSequence opciones[] = new CharSequence[]{
                                "Modificar",
                                "Eliminar"
                        };

                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                        builder.setTitle("Opciones:");
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    user = "";
                                    Intent intent = new Intent(getContext(), AgregarUser.class);
                                    intent.putExtra("IDsubject", model.getID());
                                    startActivity(intent);
                                }
                                if (i == 1) {
                                    androidx.appcompat.app.AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                                    builder1.setTitle("Eliminar asignatura");
                                    builder1.setMessage("¿Estas seguro que quieres eliminar al usuario de la lista?");
                                    LayoutInflater inflater = getActivity().getLayoutInflater();

                                    builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            UsersRef
                                                    .child(model.getID())
                                                    .removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getContext(), "Usuario eliminada de la lista", Toast.LENGTH_SHORT).show();
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
                        builder.show();
                    }
                });

            }

            @NonNull
            @Override
            public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_user, parent, false);
                UserHolder holder = new UserHolder(view);
                return holder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

}