package com.singhbrotherseducare.mycity_act;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Reset_Password extends AppCompatActivity {
    TextView tvResetPassword;
    EditText etEmail;
    Button btnReset;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset__password);

        tvResetPassword=(TextView)findViewById(R.id.tvResetPassword);
        etEmail=(EditText)findViewById(R.id.etEmail);
        btnReset=(Button)findViewById(R.id.btnReset);



        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etEmail.length()==0){
                    Toast.makeText(Reset_Password.this, "This field cannot be empty", Toast.LENGTH_SHORT).show();
                    etEmail.requestFocus();
                }else{
                    String email=etEmail.getText().toString().trim();
                    mAuth=FirebaseAuth.getInstance();
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(Reset_Password.this, "Reset email successfully sent.", Toast.LENGTH_SHORT).show();
                            finish();
                            Intent i=new Intent(Reset_Password.this,LogIn_Activity.class);
                            startActivity(i);
                        }
                    });
                }

            }
        });

    }
}
