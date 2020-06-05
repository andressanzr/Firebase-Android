package com.example.firebase_proyect.Fragment;


import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.firebase_proyect.Activity.AgregarAsignatura;
import com.example.firebase_proyect.Models.Asignaturas;
import com.example.firebase_proyect.R;
import com.example.firebase_proyect.ViewHolder.AsignaturaHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AsignaturasFragment extends Fragment {

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    RecyclerView recyclerView;

    static String asignatura = "";

    public AsignaturasFragment() {
        // Required empty public constructor
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

                    asignatura = searchView.getQuery().toString();
                    onStart();
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    //Log.i("onQueryTextSubmit", query);
                    asignatura = searchView.getQuery().toString();
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
    public void onStart() {
        super.onStart();

        final DatabaseReference AsignaturasRef = FirebaseDatabase.getInstance().getReference().child("Asignaturas");

        FirebaseRecyclerOptions<Asignaturas> opciones = new FirebaseRecyclerOptions.Builder<Asignaturas>()
                .setQuery(AsignaturasRef.orderByChild("nombre").startAt(asignatura).endAt(asignatura + "\uf8ff"), Asignaturas.class)
                .build();

        FirebaseRecyclerAdapter<Asignaturas, AsignaturaHolder> adapter = new FirebaseRecyclerAdapter<Asignaturas, AsignaturaHolder>(opciones) {
            @Override
            protected void onBindViewHolder(@NonNull AsignaturaHolder holder, final int position, @NonNull final Asignaturas model) {

                holder.NombreAlumno.setText(model.getNombre());
                holder.Curso.setText(model.getCurso());

                if (model.getFoto() == null) {
                    Picasso.get().load(R.drawable.imagecurso).resize(80, 80).into(holder.foto);
                } else {
                    Picasso.get().load(model.getFoto()).resize(80, 80).into(holder.foto);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                                    asignatura = "";
                                    Intent intent = new Intent(getContext(), AgregarAsignatura.class);
                                    intent.putExtra("IDsubject", model.getID());
                                    startActivity(intent);
                                }
                                if (i == 1) {
                                    androidx.appcompat.app.AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                                    builder1.setTitle("Eliminar asignatura");
                                    builder1.setMessage("¿Estas seguro que quieres eliminar la asignatura de la lista?");
                                    LayoutInflater inflater = getActivity().getLayoutInflater();

                                    builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            AsignaturasRef
                                                    .child(model.getID())
                                                    .removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getContext(), "Asignatura eliminada de la lista", Toast.LENGTH_SHORT).show();
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
            public AsignaturaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_asignatura, parent, false);
                AsignaturaHolder holder = new AsignaturaHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Se infla el layout en este fragment
        View view = inflater.inflate(R.layout.fragment_asignaturas, container, false);

        RecyclerView.LayoutManager layoutManager;
        recyclerView = view.findViewById(R.id.recycler_asignaturas);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);


        FloatingActionButton addSubject_btn = view.findViewById(R.id.fabAddBoard);
        addSubject_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asignatura = "";
                Intent intent = new Intent(getContext(), AgregarAsignatura.class);
                startActivity(intent);
            }
        });

        return view;

    }


}