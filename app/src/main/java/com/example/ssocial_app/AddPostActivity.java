package com.example.ssocial_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {
    //permission
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //choose image
//    private static final int REQUEST_GALLERY_SELECT = 0;
    private static final int IMAGE_PINK_CAMERA_CODE = 300;
    private static final int IMAGE_PINK_GALLERY_CODE = 400;
    //ủi picked image
    Uri image_uri = null;
    // array permission
    String cameraPermissions[];
    String storagePermissions[];
    //firebase auth
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    ActionBar actionBar;
    //view
    EditText mTitleEdt, mDescriptionEdt;
    ImageView imgPost;
    Button mUploadPostBtn;
    //user info
    String name, email, uid, dp;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Thêm bài mới");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2E9AFE")));
        //enabed back button in actionbar
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
//init  permissions array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        pd = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        checkUserStatus();

        actionBar.setSubtitle(email);
        //init permission array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // get info of curent user include to post
        reference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" + ds.child("image").getValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //init view
        mTitleEdt = findViewById(R.id.edt_title);
        mDescriptionEdt = findViewById(R.id.edt_desc);
        imgPost = findViewById(R.id.img_post);
        mUploadPostBtn = findViewById(R.id.btn_uploadPost);
        //get image form gallery /camera
        imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }


        });
        // up load button click
        mUploadPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data editext title, description;
                String title = mTitleEdt.getText().toString().trim();
                String description = mDescriptionEdt.getText().toString().trim();
                //check title isEmpty
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(AddPostActivity.this, "Nhập tiêu đề...", Toast.LENGTH_SHORT).show();
                    return;
                }
                //check description isEmpty
                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(AddPostActivity.this, "Nhập Nội dung..", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (image_uri == null) {
                    //post without image
                    uploadData(title, description, "noImage");
                } else {
                    //post with image
                    uploadData(title, description, String.valueOf(image_uri));
                }
            }
        });


    }

    private void uploadData(final String title, final String description, String uri) {
        pd.setMessage("Đang đăng bài ...");
        pd.show();
        //post -image name,post-id,post-publish-time
        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Post/" + "post_" + timestamp;
        if (!uri.equals("noImage")) {
            //post with image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            storageReference.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //image is upload to firebase Storage
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String dowloadUri = uriTask.getResult().toString();
                    if (uriTask.isSuccessful()) {
                            //uri is received up load post to firebase
                        HashMap<Object, String> hashMap=new HashMap<>();
                        //put post info
                        hashMap.put("uid",uid);
                        hashMap.put("uname",name);
                        hashMap.put("uemail",email);
                        hashMap.put("udp",dp);
                        hashMap.put("pid",timestamp);
                        hashMap.put("ptitle",title);
                        hashMap.put("pdescr",description);
                        hashMap.put("pimage",dowloadUri);
                        hashMap.put("ptime",timestamp);
                        //path to store Post data
                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts");
                        //put data in reference
                        reference.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //added in database
                                Toast.makeText(AddPostActivity.this, "Đã đăng bài", Toast.LENGTH_SHORT).show();
                                //reset view
                                mTitleEdt.setText("");
                                mDescriptionEdt.setText("");
                                imgPost.setImageURI(null);
                                image_uri=null;
                                pd.dismiss();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //failed adding post
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed uploading image
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            //post without image
            //uri is received up load post to firebase
            HashMap<Object, String> hashMap=new HashMap<>();
            //put post info
            hashMap.put("uid",uid);
            hashMap.put("uname",name);
            hashMap.put("uemail",email);
            hashMap.put("udp",dp);
            hashMap.put("pid",timestamp);
            hashMap.put("ptitle",title);
            hashMap.put("pdescr",description);
            hashMap.put("pimage","noImage");
            hashMap.put("ptime",timestamp);
            //path to store Post data
            DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts");
            //put data in reference
            reference.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //added in database
                    Toast.makeText(AddPostActivity.this, "Đã đăng bài", Toast.LENGTH_SHORT).show();
                    //reset view
                    mTitleEdt.setText("");
                    mDescriptionEdt.setText("");
                    imgPost.setImageURI(null);
                    image_uri=null;
                    pd.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed adding post
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showImagePickDialog() {
//lựa chon thu viện

        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn hình ảnh từ");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                // item hander click
                if (position == 0) {
                    // camera
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                }
                if (position == 1) {
                    //gallery
                    if (!checkStoragePermission()) {
                        reaquestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.show();
    }

    private void pickFromCamera() {
        //intent pick camera form  camera
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr ");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        //  Intent intent = new Intent();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //  Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);

        startActivityForResult(intent, IMAGE_PINK_CAMERA_CODE);

    }

    private void pickFromGallery() {
        // intent pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PINK_GALLERY_CODE);


    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void reaquestStoragePermission() {
        //request runtime storage permission, in top
        // ActivityCompat.requestPermissions(getActivity(), storagePermissions, STORAGE_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);


    }

    private boolean checkCameraPermission() {
        // check camera baatj
        // trowr laij khi chuwa bataj
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        // ActivityCompat.requestPermissions(getActivity(), cameraPermissions, CAMERA_REQUEST_CODE);
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    //cấp quyền cấp nhận
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera &&Gallery car hai cần thiết", Toast.LENGTH_SHORT).show();
                    }
                } else {
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    if (grantResults.length > 0) {
                        boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                        if (storageAccepted) {
                            pickFromGallery();
                        } else {
                            Toast.makeText(this, "Gallery  cần thiết", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                    }
                }
            }
            break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PINK_GALLERY_CODE) {
                //get uri image
                image_uri = data.getData();
                //set to imageview
                imgPost.setImageURI(image_uri);


            }


        } else if (requestCode == IMAGE_PINK_CAMERA_CODE) {
            imgPost.setImageURI(image_uri);


        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {
        user = auth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
        } else {
            startActivity(new Intent(AddPostActivity.this, MainActivity.class));
            finish();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();// goto previous activity


        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.menu_Mpost).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.logout) {
//            auth.signOut();
//            checkUserStatus();
//
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_Mlogout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AddPostActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            case R.id.menu_Mexit:
                // start main
                Intent intent = new Intent(getApplicationContext(), AddPostActivity.class);
                startActivity(intent);


                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startActivity(startMain);
                finish();
            case  R.id.menu_Mpost:
                startActivity(new Intent(this, AddPostActivity.class));
        }
        return false;
    }
}
