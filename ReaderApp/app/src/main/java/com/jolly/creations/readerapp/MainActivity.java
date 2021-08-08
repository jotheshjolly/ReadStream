package com.jolly.creations.readerapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    ListView lv_pdf;
    public static ArrayList<File> fileList = new ArrayList<File>();
    PDFAdapter obj_adapter;
    public static int REQUEST_PERMISSIONS = 1;
    boolean boolean_permission;
    File dir;
    EditText rid;
    DatabaseReference myRef;
    
    private ProgressDialog progress;
    
    public static final String MyPREFERENCES = "readapp" ;
    public static final String loginkey = "loginkey";
    public static final String uname = "uname";
    
    String un,lk;
    
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        

    }

    private void init() {
    
        progress=new ProgressDialog(this);
        progress.setMessage("uploading Document");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
       

        lv_pdf = (ListView) findViewById(R.id.lv_pdf);
        rid = (EditText) findViewById(R.id.rid);
    
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    
        lk = sharedpreferences.getString(loginkey,null);
        un = sharedpreferences.getString(uname,null);
    
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://bingojs-default-rtdb.firebaseio.com/");
        myRef = database.getReference();
        
        dir = new File(Environment.getExternalStorageDirectory().toString());
      //  dir = new File(String.valueOf(Environment.getExternalStorageDirectory()));
        fn_permission();

        lv_pdf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                
                uploadpdf(i);
               

        
            }
        });
    
        
        
    }
    
    void uploadpdf(final int pos)
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://bingojs.appspot.com");
    
        Uri file = Uri.fromFile(new File(String.valueOf(fileList.get(pos))));
        String pdfFileName = fileList.get(pos).getName();
        final StorageReference riversRef = storageRef.child(pdfFileName);
    
        
    
        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
    
                        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progress.dismiss();
                                Intent intent = new Intent(getApplicationContext(), PdfActivity.class);
                                intent.putExtra("position", pos);
    
                                String id = getRandomNumberString();
    
                                final int[] presnt = {0};
    
                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(dataSnapshot.child("users").child(un).child("rid").getValue().toString())) {
                                            presnt[0] = 1;
                                        }
                                        else
                                        {
                                            presnt[0] = 0;
                                        }
            
                                    }
        
                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        // Failed to read value
                                        //Log.w(TAG, "Failed to read value.", error.toException());
                                    }
                                });
    
                                if (presnt[0]==1) {
                                    String id1 = getRandomNumberString();
                                    myRef.child("users").child(un).child("rid").setValue(id1);
                                    myRef.child(id1+"").child("filelink").setValue(uri.toString());
                                }
                                else
                                {
                                    myRef.child("users").child(un).child("rid").setValue(id);
                                    myRef.child(id+"").child("filelink").setValue(uri.toString());
                                }
                                
    
                                intent.putExtra("file_code", id);
                                
                                //myRef.child("users").child(un).child("filelink").setValue(uri+"");
                                
                                Toast.makeText(getBaseContext(), "Upload success! URL - " + uri , Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                            }
                        });
                        
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @SuppressWarnings("VisibleForTests")
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progs = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progress.setProgress((int)progs);
                    progress.show();
                   
                }
                });
    }

    public ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {
                    getfile(listFile[i]);

                } else {

                    boolean booleanpdf = false;
                    if (listFile[i].getName().endsWith(".pdf")) {

                        for (int j = 0; j < fileList.size(); j++) {
                            if (fileList.get(j).getName().equals(listFile[i].getName())) {
                                booleanpdf = true;
                            } else {

                            }
                        }

                        if (booleanpdf) {
                            booleanpdf = false;
                        } else {
                            fileList.add(listFile[i]);

                        }
                    }
                }
            }
        }
        return fileList;
    }
    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }
    
            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
        
            }
        } else {
            boolean_permission = true;

            getfile(dir);

            obj_adapter = new PDFAdapter(getApplicationContext(), fileList);
            lv_pdf.setAdapter(obj_adapter);

        }
    }
    
    public void enter(View view) {
        
        String id = rid.getText().toString();
        myRef.child("users").child(un).child("rid").setValue(id);
        //myRef.child(id).child("btn1").setValue("");
        
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //String value = dataSnapshot.getValue(String.class);
                //Log.d(TAG, "Value is: " + value);
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        
    }
    
    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        
        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }
    
    public void logout(View view) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        
        editor.putString(loginkey, "false");
        editor.commit();
        startActivity(new Intent(MainActivity.this, login.class));
        
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                boolean_permission = true;
                getfile(dir);

                obj_adapter = new PDFAdapter(getApplicationContext(), fileList);
                lv_pdf.setAdapter(obj_adapter);

            } else {
                Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();

            }
        }
    }

}