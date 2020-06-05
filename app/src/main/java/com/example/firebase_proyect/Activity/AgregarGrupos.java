package com.example.firebase_proyect.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.firebase_proyect.Models.Grupos;
import com.example.firebase_proyect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AgregarGrupos extends AppCompatActivity {
    private EditText gruponumero, gruponombre;
    private String ID;
    String IDexistente = "";

    private DatabaseReference GroupsRef;


    private Button guardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_grupos);

        try {
            IDexistente = getIntent().getStringExtra("IDgroup");
            getGroupInfo(IDexistente);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Grupos");

        References();
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

    private void References() {
        gruponumero = (EditText) findViewById(R.id.textnumero);
        gruponombre = (EditText) findViewById(R.id.textnombreGrupo);
        guardar = (Button) findViewById(R.id.guardarDatos);
    }
}