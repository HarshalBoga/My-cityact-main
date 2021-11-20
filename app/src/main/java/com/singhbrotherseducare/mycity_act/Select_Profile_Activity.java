package com.singhbrotherseducare.mycity_act;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

public class Select_Profile_Activity extends AppCompatActivity {
    ImageView ivUser,ivNg,ivDepartment;
    SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select__profile_);

        ivUser=findViewById(R.id.ivUser);
        ivNg=findViewById(R.id.ivNG);
        ivDepartment=findViewById(R.id.ivDepartment);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences=getSharedPreferences("MyP1",MODE_PRIVATE);



        ivUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Type","U");
                editor.commit();
                Intent intent=new Intent(Select_Profile_Activity.this,Registration_Activity.class);
                startActivity(intent);

            }
        });
        ivNg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Type","N");
                editor.commit();
                Intent intent=new Intent(Select_Profile_Activity.this,Registration_Activity.class);
                startActivity(intent);

            }
        });
        ivDepartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Type","D");
                editor.commit();
                Intent intent=new Intent(Select_Profile_Activity.this, Registration_Activity.class);
                startActivity(intent);

            }
        });

    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser!=null){
//            Intent intent=new Intent(Select_Profile_Activity.this,MainActivity.class);
//            startActivity(intent);
//        }
//    }
}
