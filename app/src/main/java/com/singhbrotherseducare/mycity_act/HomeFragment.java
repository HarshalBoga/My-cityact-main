package com.singhbrotherseducare.mycity_act;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class HomeFragment extends Fragment {
    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPage=true;
    private String account_type,pincode,department,user_id;
    CollectionReference postsRef,userRef;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_home, container, false);

        blog_list=new ArrayList<>();
        blog_list_view=view.findViewById(R.id.blog_list_view);
        blogRecyclerAdapter=new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();
        postsRef = firebaseFirestore.collection("Posts");
        userRef = firebaseFirestore.collection("Users");

        userRef.document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        account_type=task.getResult().getString("type");
                        Query first_Query=null;
                        if(account_type.equals("u")) {
                            first_Query=postsRef
                                    .orderBy("timestamp",Query.Direction.DESCENDING)
                                    .limit(3);
                            first_Query.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                    if (documentSnapshots.size() != 0) {
                                        if (isFirstPage) {
                                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                        }
                                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                                String blogPostId = doc.getDocument().getId();
                                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                                if (isFirstPage) {
                                                    blog_list.add(blogPost);
                                                } else {
                                                    blog_list.add(0, blogPost);
                                                }
                                                blogRecyclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                        isFirstPage = false;
                                    }else{
                                        Toast.makeText(getContext(), "No data to show", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } ) ;
                        }else if (account_type.equals("n")){
                            pincode = task.getResult().getString("pincode");
                            first_Query=postsRef
                                    .whereEqualTo("pin",pincode)
//                                    .orderBy("timestamp",Query.Direction.ASCENDING)
                                    .limit(3);
                            first_Query.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                    if (documentSnapshots.size()!=0) {
                                        if (isFirstPage) {
                                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                        }
                                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                                String blogPostId = doc.getDocument().getId();
                                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                                if (isFirstPage) {
                                                    blog_list.add(blogPost);
                                                } else {
                                                    blog_list.add(0, blogPost);
                                                }
                                                blogRecyclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                        isFirstPage = false;
                                    }else{
                                        Toast.makeText(getContext(), "No data to show", Toast.LENGTH_SHORT).show();
                                    }
                                } } ) ;
                        }else if (account_type.equals("d")) {
                            department=task.getResult().getString("department");
                            first_Query=postsRef
                                    .whereEqualTo("dep",department)
  //                                  .orderBy("timestamp",Query.Direction.ASCENDING)
                                    .limit(3);
                            first_Query.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                    if (isFirstPage) {
                                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                        if (documentSnapshots.size() != 0) {
                                        }
                                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                                String blogPostId = doc.getDocument().getId();
                                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                                if (isFirstPage) {
                                                    blog_list.add(blogPost);
                                                } else {
                                                    blog_list.add(0, blogPost);
                                                }
                                                blogRecyclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                        isFirstPage = false;
                                    }else{
                                        Toast.makeText(getContext(), "No data to show", Toast.LENGTH_SHORT).show();
                                    }
                                } } ) ;  }
                    }else{
                        Toast.makeText(getContext(), "Data doesn't exists"+user_id, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    String error=task.getException().getMessage();
                    Toast.makeText(getContext(), "Error "+error, Toast.LENGTH_SHORT).show();
                } } } );


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

        userRef.document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        account_type=task.getResult().getString("type");
                        Query next_Query=null;
                        if(account_type.equals("u")) {
                            next_Query=postsRef
                                    .orderBy("timestamp",Query.Direction.DESCENDING)
                                    .startAfter(lastVisible)
                                    .limit(3);
                            next_Query.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
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
                                    } } } ) ;

                        }else if (account_type.equals("n")){
                            pincode = task.getResult().getString("pincode");
                            next_Query=postsRef
                                    .whereEqualTo("pin",pincode)
 //                                   .orderBy("timestamp",Query.Direction.ASCENDING)
                                    .startAfter(lastVisible)
                                    .limit(3);


                            next_Query.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
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
                                    } } } ) ;
                        }else if (account_type.equals("d")) {
                            department=task.getResult().getString("department");
                            next_Query=postsRef
                                    .whereEqualTo("dep",department)
                                    .startAfter(lastVisible)
                                    .limit(3);
                            next_Query.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
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
                                            }
                                        }

                                    }else{
                                        // Toast.makeText(getContext(), "No more feeds", Toast.LENGTH_SHORT).show();
                                    } } } ) ; } }
                }else{

                } } } ) ;

    }
}
