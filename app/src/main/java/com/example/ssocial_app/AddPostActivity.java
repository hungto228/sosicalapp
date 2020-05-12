package com.example.ssocial_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import java.io.ByteArrayOutputStream;
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

    // info of post  edited
    String mTitle, mDescription, imgEditPost;

    ProgressDialog pd;
//    private int progressStatus = 0;
////    private Handler handler = new Handler();

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

        //init view
        mTitleEdt = findViewById(R.id.edt_title);
        mDescriptionEdt = findViewById(R.id.edt_desc);
        imgPost = findViewById(R.id.img_post);
        mUploadPostBtn = findViewById(R.id.btn_uploadPost);

        //get data intent from ActivityAdapter
        Intent intent = getIntent();
        final String isUpdatekey = "" + intent.getStringExtra("key");
        final String editPostId = "" + intent.getStringExtra("editPostId");

        //validate update post  from AdapterPost
        if (isUpdatekey.equals("editPost")) {
            //update
            actionBar.setTitle("Cập nhật bài đăng");
            mUploadPostBtn.setText("Cập nhật");
            loadPostData(editPostId);
        } else {
            //add
            actionBar.setTitle("Thêm bài mới");
            mUploadPostBtn.setText("Đăng");

        }

        actionBar.setSubtitle(email);
        //init permission array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // get info of curent user include to post //name, email,image
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
                if (isUpdatekey.equals("editPost")) {
                    beginUpdate(title, description, editPostId);
                } else {
                    //post without image
                    uploadData(title, description);
                }

            }
        });


    }

    private void beginUpdate(String title, String description, String editPostId) {
        pd.setMessage("Đang cập nhật bài đăng..");
        pd.show();

        if (!imgEditPost.equals("noImage")) {
            //without image
            //TODO:updateWasWith
            updateWasWithImage(title, description, editPostId);
        } else if (imgPost.getDrawable() != null) {
            //with image
            updateWithNowImage(title, description, editPostId);
        } else {
            updateWithoutImage(title, description, editPostId);
        }
    }

    private void updateWithoutImage(String title, String description, String editPostId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        //put post info uid;uname,uemail,udp,ptitle,pdescr,\\pimage with dowloadUri
        hashMap.put("uid", uid);
        hashMap.put("uname", name);
        hashMap.put("umail", email);
        hashMap.put("udp", dp);
        hashMap.put("ptitle", title);
        hashMap.put("pdescr", description);
        hashMap.put("pimage", "noImage");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.child(editPostId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "Đã cập nhật..", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void updateWithNowImage(final String title, final String description, final String editPostId) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;
        //get image from imageview
        Bitmap bitmap = ((BitmapDrawable) imgPost.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        storageReference1.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image uploaded get it url
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;

                String dowloadUri = uriTask.getResult().toString();
                if (uriTask.isSuccessful()) {
                    //url is recived ,up load to firebase
                    HashMap<String, Object> hashMap = new HashMap<>();
                    //put post info uid;uname,uemail,udp,ptitle,pdescr,\\pimage with dowloadUri
                    hashMap.put("uid", uid);
                    hashMap.put("uname", name);
                    hashMap.put("umail", email);
                    hashMap.put("udp", dp);
                    hashMap.put("ptitle", title);
                    hashMap.put("pdescr", description);
                    hashMap.put("pimage", dowloadUri);
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                    reference.child(editPostId).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Đã cập nhật..", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //image not up load
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateWasWithImage(final String title, final String description, final String editPostId) {
        //post with image , delete previous image first
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imgEditPost);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //image deleted ,upload new image//
                //for post again post_image name,post_id,publish_time
                String timeStamp = String.valueOf(System.currentTimeMillis());
                String filePathAndName = "Posts/" + "post_" + timeStamp;
                //get image from imageview
                Bitmap bitmap = ((BitmapDrawable) imgPost.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //image compress
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                storageReference1.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded get it url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        String dowloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            //url is recived ,up load to firebase
                            HashMap<String, Object> hashMap = new HashMap<>();
                            //put post info uid;uname,uemail,udp,ptitle,pdescr,\\pimage with dowloadUri
                            hashMap.put("uid", uid);
                            hashMap.put("uname", name);
                            hashMap.put("umail", email);
                            hashMap.put("udp", dp);
                            hashMap.put("ptitle", title);
                            hashMap.put("pdescr", description);
                            hashMap.put("pimage", dowloadUri);
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                            reference.child(editPostId).updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Đã cập nhật..", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //image not up load
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //get detail of post using id of post
        Query query = reference.orderByChild("pid").equalTo(editPostId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    mTitle = "" + ds.child("ptitle").getValue();
                    mDescription = "" + ds.child("pdescr").getValue();
                    imgEditPost = "" + ds.child("pimage").getValue();
                    // set data to edt
                    mTitleEdt.setText(mTitle);
                    mDescriptionEdt.setText(mDescription);
                    //set data image
                    if (!imgEditPost.equals("noImage")) {
                        try {
                            Glide.with(getApplicationContext()).load(imgEditPost).into(imgPost);
                        } catch (Exception e) {

                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //TODO: up load to firebase POSTs
    private void uploadData(final String title, final String description) {
        pd.setMessage("Đang đăng bài ...");
// Set progress dialog style horizontal
//        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//
//        // Set the custom drawable for progress bar
//        pd.setProgressDrawable(getDrawable(R.drawable.progressbar_states));
//        // Set the progress status zero on each button click
//        progressStatus = 0;
//
//        // Start the lengthy operation in a background thread
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (progressStatus < 100) {
//                    // Update the progress status
//                    progressStatus += 1;
//
//                    // Try to sleep the thread for 20 milliseconds
//                    try {
//                        Thread.sleep(20);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    // Update the progress bar
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            // Update the progress status
//                            pd.setProgress(progressStatus);
//                            // If task execution completed
//                            if (progressStatus == 100) {
//                                // Dismiss/hide the progress dialog
//                                pd.dismiss();
//                            }
//                        }
//                    });
//                }
//            }
//        }).start(); // Start the operation


    // Change the background color of progress dialog
    // pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.YELLOW));
        pd.show();


    //post -image name,post-id,post-publish-time
    final String timestamp = String.valueOf(System.currentTimeMillis());
    String filePathAndName = "Post/" + "post_" + timestamp;
        if(imgPost.getDrawable()!=null)

    {
        //get image from imageview
        Bitmap bitmap = ((BitmapDrawable) imgPost.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        //post with image
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        storageReference.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is upload to firebase Storage
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String dowloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            //uri is received up load post to firebase
                            HashMap<Object, String> hashMap = new HashMap<>();
                            //put post info
                            hashMap.put("uid", uid);
                            hashMap.put("uname", name);
                            hashMap.put("uemail", email);
                            hashMap.put("udp", dp);
                            hashMap.put("pid", timestamp);
                            hashMap.put("ptitle", title);
                            hashMap.put("pdescr", description);
                            hashMap.put("pimage", dowloadUri);
                            hashMap.put("ptime", timestamp);
                            //path to store Post data
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
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
                                    image_uri = null;
                                    pd.dismiss();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed adding post
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed uploading image
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    } else

    {
        //post without image
        //uri is received up load post to firebase
        HashMap<Object, String> hashMap = new HashMap<>();
        //put post info
        hashMap.put("uid", uid);
        hashMap.put("uname", name);
        hashMap.put("uemail", email);
        hashMap.put("udp", dp);
        hashMap.put("pid", timestamp);
        hashMap.put("ptitle", title);
        hashMap.put("pdescr", description);
        hashMap.put("pimage", "noImage");
        hashMap.put("ptime", timestamp);
        //path to store Post data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
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
                image_uri = null;
                pd.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed adding post
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            case R.id.menu_Mpost:
                startActivity(new Intent(this, AddPostActivity.class));
        }
        return false;
    }
}
