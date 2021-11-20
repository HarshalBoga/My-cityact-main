package com.singhbrotherseducare.mycity_act;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogIn_Activity extends AppCompatActivity {

    TextView tvLogin,tvForgetPassword;
    Button btnLogin,btnRegister;
    EditText etEmail,etPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_);
        mAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        setup();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Loading");
                if(validate()){
                    progressDialog.show();
                    String email=etEmail.getText().toString().trim();
                    String password=etPassword.getText().toString().trim();

                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                checkVerification();
                            }else{
                                progressDialog.dismiss();
                                Toast.makeText(LogIn_Activity.this, "Email or Password is incorrect", Toast.LENGTH_SHORT).show();
                            }
                        }});
                }}});

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LogIn_Activity.this,Select_Profile_Activity.class);
                startActivity(intent);
            }
        });

        tvForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LogIn_Activity.this,Reset_Password.class);
                startActivity(intent);
            }
        } ) ; }

    private void setup(){
        tvLogin=findViewById(R.id.tvLogIn);
        tvForgetPassword=findViewById(R.id.tvForget);
        btnLogin=findViewById(R.id.btnLogIn);
        btnRegister=findViewById(R.id.btnRegister);
        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);

    }
    private Boolean validate(){
        Boolean result;
        result=false;
        if (etEmail.length()==0){
            Toast.makeText(this, "This field cannot be empty", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
        }else if(etPassword.length()==0){
            Toast.makeText(this, "This field cannot be empty", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
        }else{
            result=true;
        }
        return  result;
    }
    private void checkVerification(){
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        Boolean flag=firebaseUser.isEmailVerified();
        if (flag==true){
            progressDialog.dismiss();
            Intent intent=new Intent(LogIn_Activity.this,MainActivity.class);
            startActivity(intent);
        }else{
            progressDialog.dismiss();
            Toast.makeText(this, "Verify your email first", Toast.LENGTH_SHORT).show();
            mAuth.signOut();;
        }
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit ?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(LogIn_Activity.this,Splash_end_Activity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("NO",null)
                .show();

    }

}
