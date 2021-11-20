package com.singhbrotherseducare.mycity_act;

import android.Manifest;
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
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Account_setup_NG extends AppCompatActivity {
    CircleImageView setupImage;
    EditText etName,etPhone,etPin;
    Button btnSave;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private Uri resultUri=null;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    String user_id,ng_name,ng_phone,ng_pincode;
    private  boolean isChanged=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup__ng);
        setupImage=findViewById(R.id.ivProfile_ng);
        etName=findViewById(R.id.etName_ng);
        etPhone=findViewById(R.id.etPhone_ng);
        etPin=findViewById(R.id.etPincode_ng);
        btnSave=findViewById(R.id.btnSave_ng);
        mAuth=FirebaseAuth.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        progressDialog=new ProgressDialog(this);
        user_id=mAuth.getCurrentUser().getUid();
        mStorageRef= FirebaseStorage.getInstance().getReference();
        progressDialog.show();
        btnSave.setEnabled(false);



        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String image=task.getResult().getString("image");
                        String name=task.getResult().getString("name");
                        String phone=task.getResult().getString("mobile_no");
                        String pincode=task.getResult().getString("pincode");
                        resultUri=Uri.parse(image);
                        etName.setText(name);
                        etPhone.setText(phone);
                        etPin.setText(pincode);
                        RequestOptions placeholderRequest=new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.df);
                        Glide.with(Account_setup_NG.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);
                        Toast.makeText(Account_setup_NG.this, "Data exists", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(Account_setup_NG.this, "Data doesn't exists", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    String error=task.getException().getMessage();
                    Toast.makeText(Account_setup_NG.this, "Error "+error, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
                btnSave.setEnabled(true);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               progressDialog.setMessage("Loading...");
                progressDialog.show();
                ng_name = etName.getText().toString().trim();
                ng_phone=etPhone.getText().toString().trim();
                ng_pincode=etPin.getText().toString().trim();
                if (validate()) {
                    if(isChanged) {
                        user_id = mAuth.getCurrentUser().getUid();
                        StorageReference image_path = mStorageRef.child("profile_images").child(user_id + ".jpg");
                        image_path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    storeFirestore(task,ng_name,ng_phone,ng_pincode);
                                } else {
                                    progressDialog.dismiss();
                                    String error = task.getException().getMessage();
                                    Toast.makeText(Account_setup_NG.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                } } } ) ;
                    }else{
                        storeFirestore(null,ng_name,ng_phone,ng_pincode);
                    } } } } ) ;


        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(Account_setup_NG.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(Account_setup_NG.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(Account_setup_NG.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }else{
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(Account_setup_NG.this);
                    }


                }else{
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(Account_setup_NG.this);
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
    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task,String ng_name,String ng_phone,String ng_pincode){
        Uri down_load ;
        if(task!=null) {
            down_load = task.getResult().getDownloadUrl();
        }else{
            down_load=resultUri;
        }

        Map<String,String> userMap=new HashMap<>();
        userMap.put("name",ng_name);
        userMap.put("mobile_no",ng_phone);
        userMap.put("pincode",ng_pincode);
        userMap.put("image",down_load.toString());
        userMap.put("type","n");
        Toast.makeText(Account_setup_NG.this, "Image is uploaded", Toast.LENGTH_SHORT).show();
        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(Account_setup_NG.this, "Succesfully updated", Toast.LENGTH_SHORT).show();
                }else{
                    String error=task.getException().getMessage();
                    Toast.makeText(Account_setup_NG.this, "Error "+error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        Intent intent=new Intent(Account_setup_NG.this,MainActivity.class);
        startActivity(intent);
        finish();

    }



    public boolean validate(){
        if (etName.length()==0){
            etName.requestFocus();
            Toast.makeText(this, "This field cannot be empkty", Toast.LENGTH_SHORT).show();
            return false;

        }else if(etPhone.length()!=10){
            etPhone.requestFocus();
            Toast.makeText(this, "This field cannot be empkty", Toast.LENGTH_SHORT).show();
            return false;

        }else if (etPin.length()!=6){
            etPin.setText(null);
            etPin.requestFocus();
            Toast.makeText(this, "This field cannot be empkty", Toast.LENGTH_SHORT).show();
            return false;

        }
        return true;
    }
}
