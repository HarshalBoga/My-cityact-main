package com.singhbrotherseducare.mycity_act;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comment_Activity extends AppCompatActivity {
    EditText comment_field;
    ImageView comment_post_btn;
    private String blog_post_id;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    String current_user_id;
    private RecyclerView comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        blog_post_id = getIntent().getStringExtra("blog_post_id");
        comment_field = findViewById(R.id.etComment);
        comment_post_btn = findViewById(R.id.btnSend);
        comment_list = findViewById(R.id.comment_list_item);
        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(commentsRecyclerAdapter);
        firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                .addSnapshotListener(Comment_Activity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String commentId = doc.getDocument().getId();
                                    Comments comments = doc.getDocument().toObject(Comments.class);
                                    commentsList.add(comments);
                                } } } } });

        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment_message = comment_field.getText().toString();
                Map<String, Object> commentsMap = new HashMap<>();
                commentsMap.put("message", comment_message);
                commentsMap.put("user_id", current_user_id);
                commentsMap.put("timestamp", FieldValue.serverTimestamp());
                firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(Comment_Activity.this, "Error Posting Comment : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Comment_Activity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(Comment_Activity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    }
                } );} } );


    }
}