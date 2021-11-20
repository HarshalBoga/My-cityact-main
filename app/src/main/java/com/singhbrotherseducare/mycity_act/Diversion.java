package com.singhbrotherseducare.mycity_act;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Diversion extends AppCompatActivity {
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diversion);
        mAuth=FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if (firebaseUser==null){
            Intent intent=new Intent(Diversion.this,LogIn_Activity.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent=new Intent(Diversion.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
