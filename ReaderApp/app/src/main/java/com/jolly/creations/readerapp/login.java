package com.jolly.creations.readerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {
    EditText Username,Password;
    String user,pass;
    Button login;
    TextView signup;
    DatabaseReference databaseReference;
    public static final String MyPREFERENCES = "readapp" ;
    public static final String loginkey = "loginkey";
    public static final String uname = "uname";
   
    SharedPreferences sharedpreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        
        Username=findViewById(R.id.uname);
        signup=findViewById(R.id.register);
        Password=findViewById(R.id.pword);
        login=findViewById(R.id.Login);
    
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://bingojs-default-rtdb.firebaseio.com/");
        databaseReference = database.getReference();
        
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Registeration.class));
    
            }
        });
    
        String lk = sharedpreferences.getString(loginkey,null);
        String un = sharedpreferences.getString(uname,null);
    
        if (lk != null && un != null )
        {
            if (lk.equals("true"))
            {
                startActivity(new Intent(login.this, MainActivity.class));
    
            }
            
        }
        
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user=Username.getText().toString();
                pass=Password.getText().toString();
                //email=Email.getText().toString();
                if(user.equals("")){
                    Username.setError("can't be blank");
                }
                else if(pass.equals("")){
                    Password.setError("can't be blank");
                }
                else if(!user.matches("[A-Za-z0-9]+")){
                    Username.setError("only alphabet or number allowed");
                }
                else if(user.length()<6){
                    Username.setError("at least 6 characters long");
                }
                else if(pass.length()<8){
                    Password.setError("at least 8 characters long");
                }
                else {
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("users").hasChild(user)) {
                                if(pass.equals(dataSnapshot.child("users").child(user).child("password").getValue()))
                                {
                                    SharedPreferences.Editor editor = sharedpreferences.edit();
    
                                    editor.putString(loginkey, "true");
                                    editor.putString(uname, user);
                                    editor.commit();
                                    
                                    Toast.makeText(login.this, "login successful", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(login.this, MainActivity.class));
                                }
                                else
                                {
                                    Toast.makeText(login.this, " password wrong", Toast.LENGTH_LONG).show();
    
                                }
                                
                            } else {
                                Toast.makeText(login.this, "username already exists", Toast.LENGTH_LONG).show();
                            }
                
                        }
            
                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            //Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });
        
        
        
        
        
                }
            }
        });
    }
}