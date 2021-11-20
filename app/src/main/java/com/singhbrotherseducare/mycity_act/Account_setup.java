package com.singhbrotherseducare.mycity_act;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Account_setup extends AppCompatActivity {
    CircleImageView setupImage;
    EditText etName,etPhone,etPincode;
    TextView tvDate;
    Button btnSave;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private Uri resultUri=null;
    private  FirebaseStorage firebaseStorage;
    private  FirebaseFirestore firebaseFirestore;
    String user_id,user_name,mobile_no,dob,pincode;
    private  boolean isChanged=false;
    private int mYear, mMonth, mDay;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);
        tvDate=findViewById(R.id.etB_Date);
        setupImage=(CircleImageView)findViewById(R.id.ivProfile);
        etName=(EditText)findViewById(R.id.etName);
        etPhone=findViewById(R.id.etPhone);
        etPincode=findViewById(R.id.etPincode);
        btnSave=(Button)findViewById(R.id.btnSave);
        mAuth=FirebaseAuth.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.show();
        user_id=mAuth.getCurrentUser().getUid();
        mStorageRef= FirebaseStorage.getInstance().getReference();

        btnSave.setEnabled(false);


        firebaseFirestore.collection("Users").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String name=task.getResult().getString("name");
                        String phone=task.getResult().getString("mobile_no");
                        String dob=task.getResult().getString("dob");
                        String pincode=task.getResult().getString("pincode");
                        String image=task.getResult().getString("image");
                        resultUri=Uri.parse(image);
                        etName.setText(name);
                        etPhone.setText(phone);
                        tvDate.setText(dob);
                        etPincode.setText(pincode);
                        RequestOptions placeholderRequest=new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.df);
                        Glide.with(Account_setup.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);
                        Toast.makeText(Account_setup.this, "Data exists", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(Account_setup.this, "Data doesn't exists", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    String error=task.getException().getMessage();
                    Toast.makeText(Account_setup.this, "Error "+error, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
                btnSave.setEnabled(true);
            }
        });



tvDate.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(Account_setup.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        String date=dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;

                        tvDate.setText("DOB: "+date);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
});

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Loading");
                progressDialog.show();
                user_name = etName.getText().toString().trim();
                mobile_no=etPhone.getText().toString().trim();
                dob=tvDate.getText().toString().trim();
                pincode=etPincode.getText().toString().trim();

                if (validate()) {
                    if(isChanged) {
                        user_id = mAuth.getCurrentUser().getUid();
                        StorageReference image_path = mStorageRef.child("profile_images").child(user_id + ".jpg");
                        image_path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    storeFirestore(task, user_name,mobile_no,dob,pincode);

                                } else {
                                    progressDialog.dismiss();
                                    String error = task.getException().getMessage();
                                    Toast.makeText(Account_setup.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                    storeFirestore(null,user_name,mobile_no,dob,pincode);
                }
                }
            }
        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(Account_setup.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(Account_setup.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(Account_setup.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }else{
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(Account_setup.this);
                    }


                }else{
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(Account_setup.this);
                }



            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                setupImage.setImageURI(resultUri);
                isChanged=true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task,String user_name,String mobile_no,String dob,String pincode){
       Uri down_load ;
       if(task!=null) {
          down_load = task.getResult().getDownloadUrl();
       }else{
           down_load=resultUri;
       }
       Map<String,String> userMap=new HashMap<>();
       userMap.put("name",user_name);
       userMap.put("mobile_no",mobile_no);
       userMap.put("dob",dob);
       userMap.put("pincode",pincode);
       userMap.put("image",down_load.toString());
       userMap.put("type","u");
       firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
           @Override
           public void onComplete(@NonNull Task<Void> task) {
               if (task.isSuccessful()){
                   Toast.makeText(Account_setup.this, "Succesfully updated", Toast.LENGTH_SHORT).show();
               }else{
                   String error=task.getException().getMessage();
                   Toast.makeText(Account_setup.this, "Error "+error, Toast.LENGTH_SHORT).show();
               }
           }
       });
       Toast.makeText(Account_setup.this, "Image is uploaded", Toast.LENGTH_SHORT).show();
       Intent intent=new Intent(Account_setup.this,MainActivity.class);
       startActivity(intent);
    }



   public boolean validate(){
        if (etName.length()==0){
            etName.requestFocus();
            Toast.makeText(this, "This field cannot be empkty", Toast.LENGTH_SHORT).show();
            return false;

        }else if(etPhone.length()!=10){
            etPhone.requestFocus();
            Toast.makeText(this, "This field cannot be empty", Toast.LENGTH_SHORT).show();
            return false;

        }else if (etPincode.length()!=6){
            etPincode.setText(null);
            etPincode.requestFocus();
            Toast.makeText(this, "This field cannot be empty", Toast.LENGTH_SHORT).show();
            return false;

        }else if (tvDate.length()==0){
            tvDate.requestFocus();
            Toast.makeText(this, "This field cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
