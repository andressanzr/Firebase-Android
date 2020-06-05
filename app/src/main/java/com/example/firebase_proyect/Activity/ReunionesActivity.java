package com.example.firebase_proyect.Activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.firebase_proyect.Models.Reunion;
import com.example.firebase_proyect.Models.Users;
import com.example.firebase_proyect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class ReunionesActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ListView listView;
    private Users usuario;
    private DatabaseReference ReunionesRef;

    private ArrayList<String> listaAsignaturas = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reuniones);
        getSupportActionBar().setTitle("Reuniones");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        listView = findViewById(R.id.list_view_asignaturas_alumno);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaAsignaturas);

        listView.setAdapter(arrayAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, final int i, final long l) {
                ReunionesRef = FirebaseDatabase.getInstance().getReference().child("Reuniones");
                // boolean causado por al añadir reunion salta ondataChange y elimina la reunion
                final boolean borrarOK = false;
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ReunionesActivity.this);
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
                                                                Toast.makeText(ReunionesActivity.this, "Reunion eliminada", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ReunionesActivity.this, "Reunion ya existente", Toast.LENGTH_SHORT).show();
                } else {
                    if (usuario.getGrupoUser().equals("")) {
                        Toast.makeText(ReunionesActivity.this, "No tienes grupo no puedes reunirte", Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(ReunionesActivity.this, "Reunion guardada en la base de datos", Toast.LENGTH_SHORT).show();
                                            view.setBackgroundColor(Color.GREEN);
                                        } else {
                                            String mensaje = task.getException().toString();
                                            Toast.makeText(ReunionesActivity.this, "Error: " + mensaje, Toast.LENGTH_SHORT).show();
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


                            if (emailBd.equals(userEmail)) {
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
}
