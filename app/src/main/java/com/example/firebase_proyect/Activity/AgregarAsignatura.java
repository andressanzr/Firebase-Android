package com.example.firebase_proyect.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.firebase_proyect.Models.Asignaturas;
import com.example.firebase_proyect.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class AgregarAsignatura extends AppCompatActivity {
    private EditText inputSubjectName, inputSubjectCourse, inputSubjectDescription;

    private String ID;
    private ImageView input_fotoAsignatura;
    private static final int GalleryPick = 1;
    private Uri ImagenUri;
    String IDexistente="";
    String myUrl="";
    StorageTask uploadTask;

    private StorageReference SubjectsImagesRef;
    private DatabaseReference SubjectsRef;

    private Button createSubject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_asignatura);

        try{
            IDexistente=getIntent().getStringExtra("IDsubject");
            getSubjectInfo(IDexistente);
        }catch (NullPointerException e){
            e.printStackTrace();
        }


        SubjectsRef= FirebaseDatabase.getInstance().getReference().child("Asignaturas");
        SubjectsImagesRef= FirebaseStorage.getInstance().getReference().child("Imagenes asignaturas");

        input_fotoAsignatura= (ImageView) findViewById(R.id.imageCurso);
        inputSubjectName= (EditText)findViewById(R.id.nombrePersona);
        inputSubjectCourse= (EditText)findViewById(R.id.txtCurso);
        inputSubjectDescription= (EditText)findViewById(R.id.txtDescripcion);
        createSubject=(Button) findViewById(R.id.guardarDatos);


        input_fotoAsignatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
            }
        });


        createSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String subjectName= inputSubjectName.getText().toString().trim();
                String subjectCourse= inputSubjectCourse.getText().toString().trim();
                String subjectDescription= inputSubjectDescription.getText().toString().trim();

                if(TextUtils.isEmpty(subjectName)){
                    Toast.makeText(AgregarAsignatura.this,"Se requiere un nombre para la asignatura",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(subjectCourse)){
                    Toast.makeText(AgregarAsignatura.this,"Se requiere un curso para la asignatura",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(subjectDescription)){
                    Toast.makeText(AgregarAsignatura.this,"Se requiere una descripción para la asignatura",Toast.LENGTH_SHORT).show();
                }else{
                    addSubjectIntoDB(subjectName,subjectCourse,subjectDescription);
                }
            }
        });

    }


    private void getSubjectInfo(final String IDexistente) {


        SubjectsRef= FirebaseDatabase.getInstance().getReference().child("Asignaturas");

        SubjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (final DataSnapshot snapShot : dataSnapshot.getChildren()) {
                    SubjectsRef.child(snapShot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Asignaturas datosAsignatura = snapShot.getValue(Asignaturas.class);
                            String id = IDexistente;
                            String idBBDD = datosAsignatura.getID();
                            if (idBBDD.equals(id)) {

                                String fotoBBDD = null;


                                if(snapShot.child("foto").exists()){
                                    fotoBBDD=datosAsignatura.getFoto();
                                }


                                String nameBBDD = datosAsignatura.getNombre();
                                String courseBBDD = datosAsignatura.getCurso();
                                String descriptionBBDD = datosAsignatura.getDescripcion();


                                if(fotoBBDD!=null){
                                    Picasso.get().load(fotoBBDD).resize(250,250).into(input_fotoAsignatura);
                                }

                                inputSubjectName.setText(nameBBDD);
                                inputSubjectCourse.setText(courseBBDD);
                                inputSubjectDescription.setText(descriptionBBDD);

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

    private void addSubjectIntoDB(String subjectName, String subjectCourse, String subjectDescription) {

        saveInfoSubjectinBBDD(subjectName,subjectCourse,subjectDescription);
    }


    private void addPhoto(final Uri imageUri, final DatabaseReference rootRef, final String ID) {

        if(imageUri!=null){
            //Ruta donde se guarda la foto de asignatura en Firebase Storage
            final StorageReference fileref=SubjectsImagesRef.child(ID +".jpg");
            uploadTask=fileref.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUrl=task.getResult();
                        myUrl=downloadUrl.toString();
                        HashMap<String,Object> subjectMap= new HashMap<>();
                        subjectMap.put("foto",myUrl);

                        //ACTUALIZAMOS LOS DATOS CUYO NODO PRINCIPAL SEA IDÉNTICO AL ID DEL USUARIO ACTUAL
                        rootRef.child(ID).updateChildren(subjectMap);

                    }else{
                        Toast.makeText(AgregarAsignatura.this,"Error al actualizar perfil", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }



    private void saveInfoSubjectinBBDD(String subjectName, String subjectCourse, String subjectDescription) {

        //OBTENEMOS LA FECHA ACTUAL
        /*Calendar calendar= Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM,yyyy");
        fechaActual=currentDate.format(calendar.getTime());

        //OBTENEMOS LA HORA ACTUAL
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        horaActual=currentTime.format(calendar.getTime());*/

        //CREAMOS UNA CLAVE CON LA FECHA Y HORA OBTENIDA QUE SERVIRÁ PARA CREAR LA ID DEL PRODUCTO




        if(IDexistente!=null){
            ID=IDexistente;
        }else{
            ID = SubjectsRef.push().getKey();
        }


        HashMap<String,Object> subjectMap= new HashMap<>();
        subjectMap.put("ID",ID);
        subjectMap.put("nombre",subjectName);
        subjectMap.put("curso",subjectCourse);
        subjectMap.put("descripcion",subjectDescription);

        try{
            addPhoto(ImagenUri,SubjectsRef,ID);
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        SubjectsRef.child(ID).updateChildren(subjectMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(AgregarAsignatura.this,"Asignaturas actualizadas en la base de datos",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(AgregarAsignatura.this,NavigationActivity.class).putExtra("fragNumber",2);
                            startActivity(intent);
                        }else{
                            String mensaje= task.getException().toString();
                            Toast.makeText(AgregarAsignatura.this,"Error: "+ mensaje,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void abrirGaleria(){
        Intent galeriaIntent= new Intent();
        galeriaIntent.setAction(Intent.ACTION_GET_CONTENT);
        galeriaIntent.setType("image/*");
        startActivityForResult(galeriaIntent,GalleryPick);
    }
    //SE OBTIENE LA RUTA DE LA FOTO SELECCIONADA, NECESARIA PARA SUBIRLA A LA BBDD
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==GalleryPick&&resultCode==RESULT_OK&&data!=null){
            ImagenUri=data.getData();
            Picasso.get().load(ImagenUri).resize(250,250).into(input_fotoAsignatura);

            //input_fotoAsignatura.setImageURI(ImagenUri);
        }
    }

}

