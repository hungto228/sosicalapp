package com.example.ssocial_app.Fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ssocial_app.Adapter.AdapterPosts;
import com.example.ssocial_app.AddPostActivity;
import com.example.ssocial_app.Model.ModelPost;
import com.example.ssocial_app.R;
import com.example.ssocial_app.SplashScreen;
import com.example.ssocial_app.StartActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.security.Permissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    //permission
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //choose image
//    private static final int REQUEST_GALLERY_SELECT = 0;
    private static final int IMAGE_PINK_CAMERA_CODE = 300;
    private static final int IMAGE_PINK_GALLERY_CODE = 400;
    //ủi picked image
    Uri image_uri = null;
    // checking  profile  or photo cover
    String profileOrCoverphoto;
    // array permission
    String cameraPermissions[];
    String storagePermissions[];

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;
    //firebase
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;
    //storage
    StorageReference storageReference;
    String storagepath = "Users_PhotoFile_Cover_img/";

    //view from xml
    ImageView imgAvatar, imgCover;
    TextView tvName, tvEmail, tvPhone;
    FloatingActionButton fab;
    RecyclerView postRecycleview;


    //progressdialog
    ProgressDialog pd;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        // Inflate the layout for this fragment
        //init firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");
        storageReference = getInstance().getReference();//firebase Storage reference
//init  permissions array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //init
        imgAvatar = view.findViewById(R.id.img_avatar);
        imgCover = view.findViewById(R.id.img_cover);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhone = view.findViewById(R.id.tv_phone);
        fab = view.findViewById(R.id.fab);
        postRecycleview = view.findViewById(R.id.post_recycleView);
        //progress dialog
        pd = new ProgressDialog(getActivity());


        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check until  data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    //set data
                    tvName.setText(name);
                    tvEmail.setText(email);
                    tvPhone.setText(phone);
                    try {
                        Glide.with(getContext()).load(image).into(imgAvatar);
                    } catch (Exception e) {
                        //if exception getting then image dafault
                        //  Glide.with(getContext()).load(R.drawable.ic_image_default).into(imgAvatar);
                    }
                    try {
                        Glide.with(getContext()).load(cover).into(imgCover);
                    } catch (Exception e) {
                        //if exception getting then image dafault
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //floatting button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProfileDialog();
            }
        });
        postList = new ArrayList<>();
        checkUserStatus();
        loadMyPosts();
        return view;
    }

    private void loadMyPosts() {
        //linearlayout for recycleview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to rycleview
        postRecycleview.setLayoutManager(layoutManager);
        //init post list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //query to  load photo
        Query query = reference.orderByChild("uid").equalTo(uid);
        //get all data from  this referece
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    // add to list
                    postList.add(modelPost);
                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set this to recycleview
                    postRecycleview.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void searchMyPosts(final String searchQuery) {
        //linearlayout for recycleview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to rycleview
        postRecycleview.setLayoutManager(layoutManager);
        //init post list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //query to  load photo
        Query query = reference.orderByChild("uid").equalTo(uid);
        //get all data from  this referece
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    //search
                    if (modelPost.getPtitle().toLowerCase().contains(searchQuery.toLowerCase())
                            || modelPost.getPdescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        // add to list
                        postList.add(modelPost);
                    }

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set this to recycleview
                    postRecycleview.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void showProfileDialog() {
        //TODO: dialog option
        //edit profile picture,cover photo,name,phone
//       final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
//       arrayAdapter.add("Sửa ảnh đại diện");
        String options[] = {"Sửa ảnh đại diện", "Sửa ảnh bìa", "Sửa tên", "Sửa số điện thoại"};//alert dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//settitle

        builder.setTitle("Chọn hành động");//setitem
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Log.d("tag", "onClick: ");
                // hander dialog click
                if (which == 0) {
                    //edit profile picture
                    pd.setMessage("Đang cập nhật ảnh đại diện");
                    profileOrCoverphoto = "image";
                    Log.e("check", "ok");
                    showImagePickDialog();
                } else if (which == 1) {
                    //edit cover
                    pd.setMessage("Đang cập nhật ảnh bìa");
                    profileOrCoverphoto = "cover";
                    showImagePickDialog();
                } else if (which == 2) {
                    //edit name
                    pd.setMessage("Đang cập nhật tên");
                    //calling  method  and pass key "name"
                    showNamePhoneUpdateDialog("name");
                } else if (which == 3) {
                    //edit phone number
                    pd.setMessage("đang cập nhật số điện thoại");
                    //calling  method  and pass key "phone"
                    showNamePhoneUpdateDialog("phone");
                }

            }
        });
        builder.create().show();//create and show dialog

    }


    //TODO:dialog camere gallery
    private void showImagePickDialog() {
        //lựa chon thu viện

        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        //  Intent intent = new Intent();

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
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
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void reaquestStoragePermission() {
        //request runtime storage permission, in top
        // ActivityCompat.requestPermissions(getActivity(), storagePermissions, STORAGE_REQUEST_CODE);
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);


    }

    private boolean checkCameraPermission() {
        // check camera baatj
        // trowr laij khi chuwa bataj
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                        Toast.makeText(getActivity(), "Camera &&Gallery car hai cần thiết", Toast.LENGTH_SHORT).show();
                    }
                } else {
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(getActivity(), "Gallery  cần thiết", Toast.LENGTH_SHORT).show();
                    }


                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PINK_GALLERY_CODE) {
                //get uri image
                image_uri = data.getData();
                //up load image  leen fire base
                uploadProfileCoverPhoto(image_uri);


            }


        } else if (requestCode == IMAGE_PINK_CAMERA_CODE) {
            uploadProfileCoverPhoto(image_uri);


        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        pd.show();
        //path and name of image  to be store in firebase storage
        String filePathAndName = storagepath + "" + profileOrCoverphoto + "_" + user.getUid();
        StorageReference storageReference2 = storageReference.child(filePathAndName);
        storageReference2.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image update to storage
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                final Uri dowload = uriTask.getResult();
                // check if image up loaded or not
                if (uriTask.isSuccessful()) {
                    //imaage uploaded
                    // add/update  url  in user database
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(profileOrCoverphoto, dowload.toString());
                    reference.child(user.getUid()).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //url database  added sucssesfuly
                                    //dismis progress dialog
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Hình ảnh đang cập nhật...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //error adding
                            //dismiss progress dialog
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Lỗi  cập nhật..", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //if usser edit  his name , => change it from his post
                    if (profileOrCoverphoto.equals("image")) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = reference.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("udp").setValue(dowload.toString());

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                } else {
                    // load image error
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Có lỗi !! ", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //is error ,get and show message , dis miss to  progress dialog
                pd.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNamePhoneUpdateDialog(final String key) {
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Cập nhật " + key);
        //set layout dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        //add edittext
        final EditText editText = new EditText(getActivity());
        editText.setHint("Nhập " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        //add button  in dialog up date
        builder.setPositiveButton("Cập nhật ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //input text from edittext
                final String values = editText.getText().toString().trim();
                //validate if user has enterd sothing or not
                if (!TextUtils.isEmpty(values)) {
                    pd.show();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(key, values);

                    reference.child(user.getUid()).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // update , dismis progress dialog
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Cập nhật...", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed    , dismiss progress dialog , get and show error message
                            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    //if usser edit  his name , => change it from his post
                    if (key.equals("name")) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = reference.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uname").setValue(values);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    Toast.makeText(getActivity(), "Nhấn enter", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // ađ buton in dialog cancel
        builder.setNegativeButton("Trở lại", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();

    }


    private void checkUserStatus() {
        FirebaseUser user;
        user = auth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        } else {
            startActivity(new Intent(getActivity(), StartActivity.class));
            getActivity().finish();
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//show menu in option in fragment
        super.onCreate(savedInstanceState);
    }
    //inflate option menu


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_search, menu);
        menu.findItem(R.id.menu_Mexit).setVisible(false);
        menu.findItem(R.id.menu_Mlogout).setVisible(false);
        menu.findItem(R.id.menu_Mpost).setVisible(false);
        menu.findItem(R.id.menu_Spost).setVisible(false);

        //search view
        MenuItem menuItem = menu.findItem(R.id.menu_Ssearch);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button
                if (!TextUtils.isEmpty(query)) {
                    //seach
                    searchMyPosts(query);
                } else {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called whenever  user  type any letter
                if (!TextUtils.isEmpty(newText)) {
                    //seach
                    searchMyPosts(newText);
                } else {
                    loadMyPosts();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_Plogout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            case R.id.menu_Pexit:
                // start main
                Intent intent = new Intent(getContext(), SplashScreen.class);
                startActivity(intent);


                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startActivity(startMain);
                getActivity().finish();
                return true;
            case R.id.menu_Ppost:
                startActivity(new Intent(getContext(), AddPostActivity.class));
                return true;
            default:
                break;
        }
        return false;
    }

    //handle menu item click
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.menu_Plogout) {
            auth.signOut();
            checkUserStatus();

        }
        if (id == R.id.menu_Ppost) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onContextItemSelected(item);
    }


}
