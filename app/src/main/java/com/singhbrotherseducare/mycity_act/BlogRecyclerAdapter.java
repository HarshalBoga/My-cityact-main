package com.singhbrotherseducare.mycity_act;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    String blog_user_id;
    public BlogRecyclerAdapter(List<BlogPost> blog_list){
        this.blog_list=blog_list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        context=view.getContext();
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        final String blogPostId=blog_list.get(position).BlogPostId;
        final String current_user_id=mAuth.getCurrentUser().getUid();
        String desc_data=blog_list.get(position).getDesc();
        holder.setDescText(desc_data);
        String imageUri=blog_list.get(position).getImage_url();
        String thumbUri=blog_list.get(position).getImage_thumb();
        holder.setBlogImage(imageUri,thumbUri);




        blog_user_id=blog_list.get(position).getUser_id();
        if (blog_user_id.equals(current_user_id)){
            holder.deletepost.setClickable(true);
            holder.deletepost.setVisibility(View.VISIBLE);
        }
        String ID=mAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Users").document(ID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()){
                String type=task.getResult().getString("type");
                if (type.equals("n")){
                    holder.swStatus.setVisibility(View.VISIBLE);
                    holder.swStatus.setClickable(true);
                } } } } ) ;

        firebaseFirestore.collection("Users").document(blog_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        String user_name=task.getResult().getString("name");
                        String image=task.getResult().getString("image");
                        holder.setUserData(user_name,image);

                    }else{

                    } } } ) ;
        firebaseFirestore.collection("Posts").document(blogPostId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
           if (task.isSuccessful()){
               int s=Integer.parseInt(task.getResult().getString("status"));
               if (s==0)
                    holder.ivStatus.setImageResource(R.drawable.red);
               else
                    holder.ivStatus.setImageResource(R.drawable.green);

           }
            }
        });


        long millisecond=blog_list.get(position).getTimestamp().getTime();
        Date date = new Date(millisecond);
        DateFormat formatter=DateFormat
                .getDateInstance(DateFormat.MEDIUM);
        String dateString=formatter.format(date);
        DateFormat df = new SimpleDateFormat("hh:mm a");
        String timeString = df.format(date);
        holder.setDate(dateString,timeString);

        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {


            @Override
            public void onEvent(QuerySnapshot documentSnapshot , FirebaseFirestoreException e) {

                if (!documentSnapshot.isEmpty()){
                    int count=documentSnapshot.size();
                    holder.updateLike(count);
                }else{
                    holder.updateLike(0);
                }

            }
        });
        firebaseFirestore.collection("Posts/"+blogPostId+"/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {


            @Override
            public void onEvent(QuerySnapshot documentSnapshot , FirebaseFirestoreException e) {

                if (!documentSnapshot.isEmpty()){
                    int count=documentSnapshot.size();
                    holder.updateComment(count);
                }else{
                    holder.updateComment(0);
                }

            }
        });

        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(current_user_id)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()){
                            holder.blogLike.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_action_like_accent));
                        }else{
                            holder.blogLike.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_action_like));
                        }

                    }
                });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,Post_detail_Activity.class);
                intent.putExtra("ID",blogPostId);
                context.startActivity(intent);
            }
        });

        holder.blogUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,User_detail.class);
                intent.putExtra("user_id",blog_user_id);
                context.startActivity(intent);
            }
        });

        holder.swStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    Map<String, Object> data = new HashMap<>();
                    data.put("status", "1");
                    firebaseFirestore.collection("Posts")
                            .document(blogPostId)
                            .set(data, SetOptions.merge());
                    holder.ivStatus.setImageResource(R.drawable.green);

                }else{
                    Map<String, Object> data = new HashMap<>();
                    data.put("status", "0");
                    firebaseFirestore.collection("Posts")
                            .document(blogPostId)
                            .set(data, SetOptions.merge());
                    holder.ivStatus.setImageResource(R.drawable.red);

                }
            }
        });


        holder.blogCommentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent=new Intent(context,Comment_Activity.class);
                commentIntent.putExtra("blog_post_id",blogPostId);
                context.startActivity(commentIntent);
            }
        });

        holder.ivLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Posts").document(blogPostId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            if (task.getResult().exists()){
                                String lat=task.getResult().getString("lat");
                                String lon=task.getResult().getString("lon");
                                Uri.Builder directionsBuilder = new Uri.Builder()
                                        .scheme("https")
                                        .authority("www.google.com")
                                        .appendPath("maps")
                                        .appendPath("dir")
                                        .appendPath("")
                                        .appendQueryParameter("api", "1")
                                        .appendQueryParameter("destination", lat + "," + lon);
                                context.startActivity(new Intent(Intent.ACTION_VIEW, directionsBuilder.build()));
                            }else{
                                Toast.makeText(context, "No data", Toast.LENGTH_SHORT).show();
                            } }else {
                            Toast.makeText(context, "task fail", Toast.LENGTH_SHORT).show();

                        } } } ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error firebase", Toast.LENGTH_SHORT).show();
                    } } ) ; } } ) ;

        holder.blogLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(current_user_id).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (!task.getResult().exists()){
                                    Map<String,Object> likeMap=new HashMap<>();
                                    likeMap.put("timestamp", FieldValue.serverTimestamp());
                                    firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(current_user_id).set(likeMap);

                                }else {
                                    firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(current_user_id).delete();
                                }
                            }
                        });

            }
        });

        holder.deletepost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to delete this post ?")
                        .setCancelable(false)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                firebaseFirestore.collection("Posts").document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        blog_list.remove(position);
                                        Intent intent=new Intent(context,MainActivity.class);
                                        context.startActivity(intent);


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(context, "Error"+e, Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        })
                        .setNegativeButton("NO",null)
                        .show();
            } } ) ;
    }

    @Override
    public int getItemCount()
    {
        return blog_list.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descView;
        private ImageView blogImage;
        private TextView blogDate;
        private TextView blogUsername;
        private ImageView blogLike;
        private TextView blog_like_count;
        private CircleImageView blogUserProfile;
        private ImageView blogCommentbtn;
        private TextView blog_comment_count;
        private ImageView deletepost;
        private ImageView ivLocation,ivStatus;
        private Switch swStatus;



        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            blogLike=mView.findViewById(R.id.blog_like);
            blogCommentbtn=mView.findViewById(R.id.blog_Comment);
            deletepost=mView.findViewById(R.id.delete_post);
            ivLocation=mView.findViewById(R.id.ivLocaction);
            swStatus=mView.findViewById(R.id.swStatus);
            ivStatus=mView.findViewById(R.id.ivStatus);
            blogUserProfile=mView.findViewById(R.id.profile);

        }
        public void setDescText(String descText){
            descView=mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
        }
        public void setBlogImage(String downloadUri,String thumbUri){
            blogImage=mView.findViewById(R.id.postImage);
            Glide.with(context).load(downloadUri)
                    .thumbnail(Glide.with(context).load(thumbUri))
                    .into(blogImage);
        }
        public void setDate(String date,String time){
            blogDate=mView.findViewById(R.id.postdate);
            String dt=date+" at "+time;
            blogDate.setText(dt);

        }
        public void updateLike(int like){
            blog_like_count=mView.findViewById(R.id.blog_like_count);
            blog_like_count.setText(like+" Upvotes");
        }
        public void updateComment(int like){
            blog_comment_count=mView.findViewById(R.id.comment_count);
            blog_comment_count.setText(like+" Comments");
        }
        public void setUserData(String name,String image){
            blogUserProfile=mView.findViewById(R.id.profile);
            blogUsername=mView.findViewById(R.id.username);
            blogUsername.setText(name);
            RequestOptions placeholderOption=new RequestOptions();
            placeholderOption.placeholder(R.drawable.df);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserProfile);
        }
    }
}
