package com.singhbrotherseducare.mycity_act;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPost_Activity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    ImageView postImage;
    EditText etDesc;
    TextView tvDep;
    Button btnPost;
    private Uri postImageUri=null;
    private ProgressDialog  progressDialog;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String current_user_id;
    private static final int MAX_LENGTH=100;
    private Bitmap compressedImageFile;
    String randomName;
    String lat,lan,addr;
    String pin="400049";


    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post_);

        postImage=findViewById(R.id.newPostImage);
        etDesc=findViewById(R.id.etDesc);
        tvDep=findViewById(R.id.tvDep);
        btnPost=findViewById(R.id.btnPost);

        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        current_user_id=mAuth.getCurrentUser().getUid();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);


        tvDep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(NewPost_Activity.this,tvDep);
                popupMenu.getMenuInflater().inflate(R.menu.m3,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                       String  dep= (String) item.getTitle();
                        tvDep.setText("Department: "+dep);
                        tvDep.setTextColor(Color.BLACK);
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String desc=etDesc.getText().toString().trim();
                if ((!TextUtils.isEmpty(desc)) && postImageUri!=null){
                    progressDialog.setMessage("Loading");
                    progressDialog.show();
                //postPB.setVisibility(View.VISIBLE);

                randomName= UUID.randomUUID().toString();

                StorageReference file_path=storageReference.child("post_image").child(randomName+".jpg");
                file_path.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        final String downloadUri=task.getResult().getDownloadUrl().toString();
                        if (task.isSuccessful()){
                            File newImageFile=new File(postImageUri.getPath());
                            try {
                                compressedImageFile = new Compressor(NewPost_Activity.this)
                                        .setMaxHeight(100)
                                        .setMaxWidth(100)
                                        .setQuality(2)
                                        .compressToBitmap(newImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ByteArrayOutputStream baos=new ByteArrayOutputStream();
                            compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                            byte[]thumbData=baos.toByteArray();
                            UploadTask thumb_filepath=storageReference.child("post_image/thumbs")
                                    .child(randomName+".jpg").putBytes(thumbData);
                            thumb_filepath.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    String downloadthumbUri= taskSnapshot.getDownloadUrl().toString();
                                    String department=tvDep.getText().toString().trim();

                                    String status="0";
                                    Map<String,Object>postMap=new HashMap<>();
                                    postMap.put("image_url",downloadUri);
                                    postMap.put("image_thumb", downloadthumbUri);
                                    postMap.put("desc",desc);
                                    postMap.put("user_id",current_user_id);
                                    postMap.put("timestamp",FieldValue.serverTimestamp());
                                    postMap.put("dep",department);
                                    postMap.put("lat",lat);
                                    postMap.put("lon",lan);
                                    postMap.put("pin",pin);
                                    postMap.put("address",addr);
                                    postMap.put("status",status);
                                    firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(NewPost_Activity.this, "Post was added", Toast.LENGTH_SHORT).show();
                                                Intent intent=new Intent(NewPost_Activity.this,MainActivity.class);
                                                startActivity(intent);

                                            }else{

                                            }
                                            progressDialog.dismiss();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                } } ) ;
                        }else{
                            progressDialog.dismiss();
                            String error=task.getException().getMessage();
                            Toast.makeText(NewPost_Activity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                        } } } ) ;
                } } } ) ;

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(3,2)
                        .start(NewPost_Activity.this);
            }
        });
        mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        checkLocation();

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
            postImageUri=result.getUri();
            postImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(NewPost_Activity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startLocationUpdates();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLocation == null){
            startLocationUpdates();
        }
        if (mLocation != null) {
             lat=Double.toString(mLocation.getLatitude());
             lan=Double.toString(mLocation.getLongitude());


    }
    }

    @Override
    public void onConnectionSuspended(int i) {
       // Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
       // Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        } }
    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }
    @Override
    public void onLocationChanged(Location location) {
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Geocoder geocoder=new Geocoder(this,Locale.ENGLISH);
        Address address=null;
        try {
            List<Address> addresses=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if (addresses !=null && addresses.size()>0){

                String add = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                String zip=addresses.get(0).getPostalCode();
                addr=add;
                pin=zip;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //Toast.makeText(this, "Address:"+addr, Toast.LENGTH_LONG).show();

    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
       // Log.d("reque", "--->>>>");
    }

}
