package com.jolly.creations.readerapp;

import android.content.Intent;
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

public class Registeration extends AppCompatActivity {
    EditText Username,Email,Password;
    String user,pass,email;
    Button signup;
    TextView login;
    DatabaseReference databaseReference;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        Username=findViewById(R.id.name);
        Email=findViewById(R.id.emailid);
        signup=findViewById(R.id.signup);
        Password=findViewById(R.id.password);
        login=findViewById(R.id.login);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://bingojs-default-rtdb.firebaseio.com/");
        databaseReference = database.getReference();
        
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user=Username.getText().toString();
                pass=Password.getText().toString();
                email=Email.getText().toString();
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
                else if(!(email.length()<35 && email.length()>18)){
                    Email.setError("Email must be atleast 18 characters long");
                }
                else {
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild(user)) {
                                databaseReference.child("users").child(user).child("password").setValue(pass);
                                databaseReference.child("users").child(user).child("email").setValue(email);
                                
                                Toast.makeText(Registeration.this, "registration successful", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(Registeration.this, login.class));
                            } else {
                                Toast.makeText(Registeration.this, "username already exists", Toast.LENGTH_LONG).show();
                            }
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
            }
        });
        
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),login.class));
            }
        });
    }
}