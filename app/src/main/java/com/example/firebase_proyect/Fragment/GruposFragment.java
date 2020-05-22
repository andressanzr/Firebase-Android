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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebase_proyect.Activity.AgregarGrupos;
import com.example.firebase_proyect.Models.Grupos;
import com.example.firebase_proyect.R;
import com.example.firebase_proyect.ViewHolder.GrupoHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GruposFragment extends Fragment {
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    RecyclerView recyclerView;
    static String grupo="";
    public GruposFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);

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
                    //Log.i("onQueryTextChange", newText);

                    grupo=searchView.getQuery().toString();
                    onStart();
                    return false;
                }
                @Override
                public boolean onQueryTextSubmit(String query) {
                    grupo=searchView.getQuery().toString();
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

        final DatabaseReference GruposRef = FirebaseDatabase.getInstance().getReference().child("Grupos");

        FirebaseRecyclerOptions<Grupos> opciones= new FirebaseRecyclerOptions.
                Builder<Grupos>().setQuery(GruposRef.orderByChild("nombre").startAt(grupo), Grupos.class).build();
        FirebaseRecyclerAdapter<Grupos, GrupoHolder> adapter = new FirebaseRecyclerAdapter<Grupos, GrupoHolder>(opciones) {
            @Override
            protected void onBindViewHolder(@NonNull GrupoHolder holder, final int position, @NonNull final Grupos model) {

                //Direcciona y devuelve el nombre con el numero
                holder.nombregrupo.setText(model.getNombre());
                holder.numero.setText(model.getNumero());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //te muestra las dos opciones que tiene de modificar y eliminar
                        final CharSequence opciones[]= new CharSequence[]{
                                "Modificar",
                                "Eliminar"
                        };
                        //muestra un mensaje con
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Opciones:");
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {

                                if(i==0){
                                    grupo="";
                                    Intent intent= new Intent(getContext(), AgregarGrupos.class);
                                    intent.putExtra("IDgroup", model.getID());
                                    startActivity(intent);
                                }

                                if(i==1){
                        //Alerta de dialogo donde te indica si lo quieres eliminar
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                                    builder1.setTitle("Eliminar grupo");
                                    builder1.setMessage("¿Esta seguro de que quieres eliminar el grupo de la lista?");
                                    LayoutInflater inflater = getActivity().getLayoutInflater();

                                    builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            GruposRef
                                                    .child(model.getID())
                                                    .removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(getContext(),"Grupo eliminado de la lista",Toast.LENGTH_SHORT).show();
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
            public GrupoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_item_layout, parent, false);
                GrupoHolder holder= new GrupoHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // se infla el layout en este fragment
        View view=inflater.inflate(R.layout.fragment_grupos, container, false);
        //recyclerview donde contendrá el cardview
        RecyclerView.LayoutManager layoutManager;
        recyclerView = view.findViewById(R.id.recycler_groups);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        FloatingActionButton addGroup_btn= view.findViewById(R.id.gruposagregar);
        addGroup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grupo="";
                Intent intent= new Intent(getContext(), AgregarGrupos.class);
                startActivity(intent);

            }
        });

        return view;
    }
}
