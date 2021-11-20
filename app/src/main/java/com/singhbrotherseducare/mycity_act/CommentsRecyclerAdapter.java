package com.singhbrotherseducare.mycity_act;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder>{
    public List<Comments> commentsList;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    public CommentsRecyclerAdapter(List<Comments> commentsList){

        this.commentsList = commentsList;

    }

    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list, parent, false);
        context = parent.getContext();
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentsRecyclerAdapter.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);
        String commentMessage = commentsList.get(position).getMessage();
        holder.setComment_message(commentMessage);

        String userId=commentsList.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    String user_name=task.getResult().getString("name");
                    String image=task.getResult().getString("image");
                    holder.setUserData(user_name,image);
                }else{

                }
            }
        });

        long millisecond=commentsList.get(position).getTimestamp().getTime();
        Date date = new Date(millisecond);
        DateFormat formatter=DateFormat
                .getDateInstance(DateFormat.MEDIUM);
        String dateString=formatter.format(date);
        DateFormat df = new SimpleDateFormat("hh:mm a");
        String timeString = df.format(date);
        holder.setDate(dateString,timeString);




    }


    @Override
    public int getItemCount() {

        if(commentsList != null) {

            return commentsList.size();

        } else {

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView comment_message;
        private CircleImageView blogUserProfile;
        private TextView blogUsername;
        private TextView commentDate;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment_message(String message){

            comment_message = mView.findViewById(R.id.com_comment);
            comment_message.setText(message);

        }
        public void setDate(String date,String time){
            commentDate=mView.findViewById(R.id.comment_time);
            String dt=time+" . "+date;
            commentDate.setText(dt);

        }



        public void setUserData(String name,String image){
            blogUserProfile=mView.findViewById(R.id.com_profile);
            blogUsername=mView.findViewById(R.id.com_username);
            blogUsername.setText(name);
            RequestOptions placeholderOption=new RequestOptions();
            placeholderOption.placeholder(R.drawable.df);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserProfile);
        }

    }

}