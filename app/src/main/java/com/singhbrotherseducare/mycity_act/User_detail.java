package com.singhbrotherseducare.mycity_act;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

public class User_detail extends AppCompatActivity {
    CircleImageView setupImage;
    TextView tvName,tvSolvedPost,tvUnSolvedPost;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;
    String user_id;
    private Uri resultUri=null;
    private User_detail_F unSolvedFragment;
    private Solved solvedFragment;
    private  User_all_post user_all_post;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user_id=getIntent().getStringExtra("user_id");
        tvName=findViewById(R.id.tvName);
        tvSolvedPost=findViewById(R.id.tvSolvedPost);
        tvUnSolvedPost=findViewById(R.id.tvUnSolvedPost);
        setupImage=findViewById(R.id.ivProfile_pic);
        unSolvedFragment=new User_detail_F();
        solvedFragment=new Solved();
        user_all_post=new User_all_post();
        if (user_id==null){
            user_id=mAuth.getCurrentUser().getUid();
        }


        tvSolvedPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replace(solvedFragment);
            }
        });
        tvUnSolvedPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replace(unSolvedFragment);
            }
        });
        firebaseFirestore.collection("Posts").whereEqualTo("user_id",user_id)
                .whereEqualTo("status","1").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshot , FirebaseFirestoreException e) {
                if (!documentSnapshot.isEmpty()){
                    tvSolvedPost.setText("Solved= "+documentSnapshot.size());
                }else{
                    tvSolvedPost.setText("Solved= 0");
                }

            }
        });
        firebaseFirestore.collection("Posts").whereEqualTo("user_id",user_id)
                .whereEqualTo("status","0").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshot , FirebaseFirestoreException e) {
                if (!documentSnapshot.isEmpty()){
                    tvUnSolvedPost.setText("Pending= "+documentSnapshot.size());
                }else{
                    tvUnSolvedPost.setText("Pending= 0");
                }

            }
        });

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String name=task.getResult().getString("name");
                        String image=task.getResult().getString("image");
                        resultUri= Uri.parse(image);
                        tvName.setText("Name: "+name);
                        RequestOptions placeholderRequest=new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.df);
                        Glide.with(User_detail.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);
                    }else{
                        Toast.makeText(User_detail.this, "Data doesn't exists", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    String error=task.getException().getMessage();
                    Toast.makeText(User_detail.this, "Error "+error, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        replace(user_all_post);
    }

    private void replace(Fragment fragment){
        Bundle bundle = new Bundle();
        String userId =user_id;
        bundle.putString("user_id",userId );
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.mainContainer_user,fragment);
        fragmentTransaction.commit();
    }
}
