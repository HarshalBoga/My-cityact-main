package com.singhbrotherseducare.mycity_act;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Registration_Activity extends AppCompatActivity {
    TextView tvRegister,tvLogin;
    EditText etEmail,etPassword,etConfirmPassword;
    Button btnRegister;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_);
        FirebaseApp.initializeApp(this);
        progressDialog=new ProgressDialog(this);
        setup();
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validate()){
                    progressDialog.setMessage("Loading.....");
                    progressDialog.show();
                    String email=etEmail.getText().toString().trim();
                    String password=etPassword.getText().toString().trim();
                    mAuth=FirebaseAuth.getInstance();
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendEmail();
                                progressDialog.dismiss();
                            }else{
                                Toast.makeText(Registration_Activity.this, "!! Registration fail !!", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    } ).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Registration_Activity.this, "Error"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) ; } } } ) ;

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Registration_Activity.this,LogIn_Activity.class);
                startActivity(intent);
            }
        });


    }

    private void setup(){
        tvRegister=(TextView)findViewById(R.id.tvRegister);
        tvLogin=(TextView)findViewById(R.id.tvLogin);
        etEmail=(EditText)findViewById(R.id.etEmail);
        etPassword=(EditText)findViewById(R.id.etPass_word);
        etConfirmPassword=(EditText)findViewById(R.id.etConfirmPassword);
        btnRegister=(Button)findViewById(R.id.btnRegister);
    }
    private Boolean validate(){
        Boolean result=false;
        if (etEmail.length()==0){
            Toast.makeText(this, "This field cannot be empty", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
        }else  if (etPassword.length()==0){
            Toast.makeText(this, "This field cannot be empty", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
        }else if (etConfirmPassword.length()==0){
            Toast.makeText(this, "This field cannot be empty", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
        }else if(!(etPassword.getText().toString()).equals(etConfirmPassword.getText().toString())){
            etConfirmPassword.setText("");
            etConfirmPassword.requestFocus();
            Toast.makeText(this, "Password not matched", Toast.LENGTH_SHORT).show();
        }else {
            result=true;
        }

        return result;
    }

    private void sendEmail(){
        FirebaseUser firebaseUser=mAuth.getInstance().getCurrentUser();
        if (firebaseUser!=null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(Registration_Activity.this, "Successfully verified mail sent", Toast.LENGTH_SHORT).show();
                       // mAuth.signOut();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    Thread.sleep(5000);
                                }catch (InterruptedException e){
                                    e.printStackTrace();
                                }
                                Intent intent=new Intent(Registration_Activity.this,LogIn_Activity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).start();


                    }else{
                        Toast.makeText(Registration_Activity.this, "Verification Fail", Toast.LENGTH_SHORT).show();


                    }
                }
            });
        }
    }
}
