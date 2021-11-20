package com.singhbrotherseducare.mycity_act;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class Post_detail_Activity extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore;
    TextView tvName,tvAddress,tvTime,tvStatus,tvNagasevak,tvDescription,tvLocation,tvDepartment;
    ImageView ivStatus,ivMap, ivPostImage;
    CircleImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail_);

        tvName=findViewById(R.id.tvName);
        tvAddress=findViewById(R.id.tvAddress);
        tvTime=findViewById(R.id.tvTime);
        tvStatus=findViewById(R.id.tvStatus);
        tvNagasevak=findViewById(R.id.tvNagarsevak);
        tvDescription=findViewById(R.id.tvPinCode);
        tvLocation=findViewById(R.id.tvLocation);
        tvDepartment=findViewById(R.id.tvDepartment);
        ivStatus=findViewById(R.id.ivStatus);
        ivMap=findViewById(R.id.ivMap);
        ivPostImage=findViewById(R.id.ivPostImage);
        ivProfile=findViewById(R.id.ivUserProfile);


        firebaseFirestore=FirebaseFirestore.getInstance();
        final String blogID=getIntent().getStringExtra("ID");

        firebaseFirestore.collection("Posts").document(blogID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                           // Toast.makeText(Post_detail_Activity.this, "jay shree ram", Toast.LENGTH_SHORT).show();
                            String user_id=task.getResult().getString("user_id");
                            //String pincode="Pincode: "+task.getResult().getString("pin");
                            int status=Integer.parseInt(task.getResult().getString("status"));
                            String image_url=task.getResult().getString("image_url");
                            String description="Description: "+task.getResult().getString("desc");
                            String address="Address: "+task.getResult().getString("address");
                            String department="Department: "+task.getResult().getString("dep");
                            Date timestamp=task.getResult().getDate("timestamp");
                            tvAddress.setText(address);
                            tvDepartment.setText(department);
                            tvDescription.setText(description);

                           if (status==0){
                               String s="Status: Un_Solved";
                               tvStatus.setText(s);
                               ivStatus.setImageResource(R.drawable.red);
                            }else{
                               String s="Status: Solved";
                               tvStatus.setText(s);
                                ivStatus.setImageResource(R.drawable.green);
                            }
                            long millisecond=timestamp.getTime();
                            Date date = new Date(millisecond);
                            DateFormat formatter=DateFormat
                                    .getDateInstance(DateFormat.MEDIUM);
                            String dateString=formatter.format(date);
                            DateFormat df = new SimpleDateFormat("hh:mm a");
                            String timeString = df.format(date);
                            String msg="Posted on "+dateString+" at "+timeString;
                            tvTime.setText(msg);

                            RequestOptions placeholderRequest=new RequestOptions();
                            placeholderRequest.placeholder(R.drawable.df);
                            Glide.with(Post_detail_Activity.this).setDefaultRequestOptions(placeholderRequest)
                                    .load(image_url).into(ivPostImage);

                            firebaseFirestore.collection("Users").document(user_id).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                       if (task.isSuccessful()){
                                           String name=task.getResult().getString("name");
                                           String image=task.getResult().getString("image");
                                           tvName.setText(name);
                                           RequestOptions placeholderRequest=new RequestOptions();
                                           placeholderRequest.placeholder(R.drawable.df);
                                           Glide.with(Post_detail_Activity.this).setDefaultRequestOptions(placeholderRequest)
                                                   .load(image).into(ivProfile);


                                       }
                                        }
                                    });



                        }

                    }
                });
        ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Posts").document(blogID).get()
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
                                        startActivity(new Intent(Intent.ACTION_VIEW, directionsBuilder.build()));
                                    }else{
                                        Toast.makeText(Post_detail_Activity.this, "No data", Toast.LENGTH_SHORT).show();
                                    } }else {
                                    Toast.makeText(Post_detail_Activity.this, "task fail", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });
    }
}
