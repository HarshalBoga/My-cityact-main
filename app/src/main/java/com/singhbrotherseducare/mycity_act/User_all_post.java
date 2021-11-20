package com.singhbrotherseducare.mycity_act;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class User_all_post extends Fragment {
    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPage=true;
    String user_id;
    CollectionReference post_ref;
    Query q1;

    public User_all_post() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_user_detail_, container, false);

        blog_list=new ArrayList<>();
        blog_list_view=view.findViewById(R.id.user_blog_list_view);
        blogRecyclerAdapter=new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        Bundle bundle = this.getArguments();
        user_id = bundle.getString("user_id");
        post_ref=firebaseFirestore.collection("Posts");
        q1=post_ref
                .whereEqualTo("user_id",user_id)
                .limit(3);
        q1.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (documentSnapshots.size()!=0){
                if (isFirstPage){
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                }
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        String blogPostId=doc.getDocument().getId();
                        BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                        if (isFirstPage) {
                            blog_list.add(blogPost);
                        }else{
                            blog_list.add(0,blogPost);
                        }
                        blogRecyclerAdapter.notifyDataSetChanged();
                    } }
                isFirstPage=false;

            }else{
                    Toast.makeText(getContext(), "No data to show", Toast.LENGTH_SHORT).show();
                }
            }
        });


        blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean reachedBottom=!recyclerView.canScrollVertically(-1);
                if (reachedBottom){
                    loadMore();
                }
            }
        });

        return view;

    }
    public void loadMore(){
        Query q2;
        q2=post_ref
                .whereEqualTo("user_id",user_id)
                //.whereEqualTo("status","1")
                .startAfter(lastVisible)
                .limit(3);
        q2.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()){
                    lastVisible=documentSnapshots.getDocuments().get(documentSnapshots.size()-1);

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String blogPostId=doc.getDocument().getId();
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                            blog_list.add(blogPost);
                            blogRecyclerAdapter.notifyDataSetChanged();
                        } }
                }else{
                    // Toast.makeText(getContext(), "No more feeds", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

}
