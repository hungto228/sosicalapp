package com.example.ssocial_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ssocial_app.Adapter.AdapterComments;
import com.example.ssocial_app.Model.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PostDetailActivity extends AppCompatActivity {
    //progress dialog
    ProgressDialog pd;
    // get detail of usser  and post
    String pimage,hisUid,myUid, myEmail, myName, myDp,
            postId, pLikes, hisDp, hisName;
    boolean mProgressComment = false;
    boolean mProgressLike=false;
    //view
    ImageView imgUpicture, imgPpicture;
    TextView mNameTV, mPtimeTV, mPtitleTV, mPdescriptionTv, mPlikesTV,mPcommentTV;
    ImageButton imgMoreBtn;
    Button mLikeBtn, mShareBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    //add comment view
    ImageButton imgSendBtn;
    EditText mCommentEdt;
    ImageView imgCAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        //action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Chi tiết bài đăng");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2E9AFE")));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // get id of post  using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        //init view
        imgUpicture = findViewById(R.id.img_uPicture);
        imgPpicture = findViewById(R.id.img_pPicture);

        mNameTV = findViewById(R.id.tv_name);
        mPtimeTV = findViewById(R.id.tv_time);
        mPtitleTV = findViewById(R.id.tv_Ptitle);
        mPdescriptionTv = findViewById(R.id.tv_Pdescripsion);
        mPlikesTV = findViewById(R.id.tv_Plike);
        mPcommentTV = findViewById(R.id.tv_Pcomment);

        imgMoreBtn = findViewById(R.id.btn_more);
        mLikeBtn = findViewById(R.id.btn_PLike);
        mShareBtn = findViewById(R.id.btn_Pshare);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView=findViewById(R.id.recycleView);

        //init add comment
        imgSendBtn = findViewById(R.id.imgBtn_send);
        mCommentEdt = findViewById(R.id.edt_comment);
        imgCAvatar = findViewById(R.id.img_avatar);


        //load post info
        loadPostInfo();
        checkUserStatus();
        //loadUserInfo
        loadUserInfo();
        //set likes
        setLikes();
        //subtitle of actionbar
        actionBar.setSubtitle("Đăng nhập bằng " + myEmail);
        //TODO: load comment
        loadComments();
        //TODO:sendComment button click
        imgSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });
        //TODO:like hander click on PostDetailActivity
        mLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });
        //TODO:More hander button click
        imgMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOption();
            }
        });
        //TODO:Share hander button click
        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ptitle =mPtitleTV.getText().toString().trim();
                String pdescription=mPdescriptionTv.getText().toString().trim();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imgPpicture.getDrawable();
                if (bitmapDrawable == null) {
                    //post without image
                    shareTextOnly(ptitle, pdescription);
                } else {
                    //post with image
                    //convert to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(ptitle, pdescription, bitmap);

                }
            }
        });
    }


    private void shareImageAndText(String ptitle, String pdescription, Bitmap bitmap) {
        String shareBody = ptitle + "\n" + pdescription;
        //  save image image in cache ,get the saved in image uri
        Uri uri = saveImageToShare(bitmap);
        //share intent
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.putExtra(Intent.EXTRA_TEXT,shareBody);
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject");
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent,"Share via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        //child images from res xml:path
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            //create if not exits, directory named by this  pathname.
            imageFolder.mkdir();
            File file=new File(imageFolder,"shared_image.png");
            FileOutputStream stream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();

            uri= FileProvider.getUriForFile(this,"com.example.ssocial_app.fileprovider",file);

        } catch (Exception e) {
            Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void shareTextOnly(String ptitle, String pdescription) {
        String shareBody = ptitle + "\n" + pdescription;

        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        //case you share via email app
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        //text body
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        //message to show in share dialog
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void loadComments() {
        //layout for  recycleview
        LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        //set layout to recycleView
        recyclerView.setLayoutManager(layoutManager);

        // commment list
        commentList=new ArrayList<>();
        // path posts , get comment
       DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
       reference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               commentList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                  ModelComment modelComment=ds.getValue(ModelComment.class);

                   commentList.add(modelComment);

                    //setup data
                    adapterComments=new AdapterComments(getApplicationContext(),commentList);
                    recyclerView.setAdapter(adapterComments);
                }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

    }

    private void showMoreOption() {
        //create popup menu having option delete // use add more option later
        PopupMenu popupMenu = new PopupMenu(this, imgMoreBtn, Gravity.END);
        // Context wrapper = new ContextThemeWrapper(context, R.style.popUP);

        //   PopupMenu popup = new PopupMenu(wrapper, view);
        //show delete option in only post with user
        if (hisUid.equals(myUid)) {
            //add item menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Xóa");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Chỉnh sửa");
        }

        //item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0){
                    //delete click
                    beginDelete();}
                else if(id==1) {
                    //edit clicked
                    //start AddPostActivity with key"editPost" and id  of the post clicked
                    Intent intent=new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",postId);
                    startActivity(intent);

                }
                return false;
            }
        });
        //popup menu show
        popupMenu.show();
    }

    private void beginDelete() {
        // post can be with  or without image
        if (pimage.equals("noImage")) {
            //post without image
            //TODO:deleteWithoutImage on postdetailActivity
            deleteWithoutImage();

        } else {
            //post with image
            //TODO:deleteWithImage
            deleteWithImage();

        }
    }

    private void deleteWithImage() {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang xóa bài");

        //step 1:delete image using url
        //     2: delete from database using post id;
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pimage);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete database
                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pid").equalTo(postId);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    //remove values firebase from where pid
                                    ds.getRef().removeValue();
                                }
                                //delete
                                Toast.makeText(PostDetailActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                pd.dismiss();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void deleteWithoutImage() {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang xóa bài");
        //image deleted, now delete database
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pid").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //remove values firebase from where pid
                    ds.getRef().removeValue();

                }
                //delete
                Toast.makeText(PostDetailActivity.this, "Xóa thành công ", Toast.LENGTH_SHORT).show();
                pd.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikes() {
        //when the detail  of post  is loading , check of current user has liked or not like
        final DatabaseReference likeReference=FirebaseDatabase.getInstance().getReference().child("Likes");
        likeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postId).hasChild(myUid)){
                    // user ha not liked this post
                    //change icon button from like => liked
                    //change text like button  from like to liked
                    mLikeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_green,0,0,0);
                    mLikeBtn.setText("Đã Thích");
                }else {
                    //user ha not liked this post
                    //change icon button from like => liked
                    //change text like button  from like to liked
                    mLikeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_back,0,0,0);
                    mLikeBtn.setText("Thích");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {
        //                Toast.makeText(context, "Likes", Toast.LENGTH_SHORT).show();
        //get total number of likes for the post ,whose like  button clicked
        //increase values by1,otherwire decreale values by 1

        mProgressLike=true;
        //get id of the post clicked
      final DatabaseReference likeReference=FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postReference=FirebaseDatabase.getInstance().getReference().child("Posts");
        likeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mProgressLike){
                    if (dataSnapshot.child(postId).hasChild(myUid)){
                        // is liked  , so remove likeL
                        postReference.child(postId).child("plikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likeReference.child(postId).child(myUid).removeValue();
                        //bug remove values
                        mProgressLike=false;

                    }else{
                        //not like ,like it;
                        postReference.child(postId).child("plikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likeReference.child(postId).child(myUid).setValue("Liked");
                        mProgressLike=false;

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Đang thêm bình luận");

        //get data  from coment editext
        final String comment = mCommentEdt.getText().toString().trim();
        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(this, "Bình luận đang trống", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        //get reffrence Post,child "comment" contain  in Posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
//TODO: is problem error timestamp,uname
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cid", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("udp",myDp);
        hashMap.put("uname", myEmail);
        hashMap.put("uid", myUid);
        hashMap.put("uemail", myEmail);

        // put this data in database, child time, setvalues hashmap
        reference.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //added
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "Đã thêm bình luận", Toast.LENGTH_SHORT).show();
                mCommentEdt.setText("");
                upDataCommentCount();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed ,no added
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    //TODO:upDataCommentCount
    private void upDataCommentCount() {
        //whenever  user  adds  comment increase the comment count
        mProgressComment = true;
        //DatabaseReference use with post, child postId
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProgressComment) {
                    String comment=""+dataSnapshot.child("pcomment").getValue();
                    int newComentValues=Integer.parseInt(comment)+1;
                    reference.child("pcomment").setValue(""+newComentValues);
                    mProgressComment=false;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    //TODO: loadUser Infomation
    private void loadUserInfo() {
        //get current info

        Query query = FirebaseDatabase.getInstance().getReference("Users");
        query.orderByChild("uid").equalTo(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    myName = "" + ds.child("name").getValue();
                    myDp = "" + ds.child("image").getValue();
                    //set data
                    try {
                        Glide.with(getApplication()).load(myDp).apply(new RequestOptions().placeholder(R.drawable.ic_image_default1)).into(imgCAvatar);
                    } catch (Exception e) {
                        Glide.with(getApplication()).load(R.drawable.ic_image_default1).into(imgCAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //TODO: loadPost Infomation
    private void loadPostInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("pid").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // keep checking post until  get the required
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String ptitle = "" + ds.child("ptitle").getValue();
                    String pdescr = "" + ds.child("pdescr").getValue();
                    pLikes = "" + ds.child("plikes").getValue();
                    String ptimestamp = "" + ds.child("ptime").getValue();
                    pimage = "" + ds.child("pimage").getValue();
                    hisDp = "" + ds.child("udp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uemail = "" + ds.child("uemail").getValue();
                    hisName = "" + ds.child("uname").getValue();
                    String commentCount=""+ds.child("pcomment").getValue();


                    // convert timestamp to dd/mm/yy
                    Calendar calendar = Calendar.getInstance(Locale.CANADA);
                    calendar.setTimeInMillis(Long.parseLong(ptimestamp));
                    String datetime = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();
                    //yyyy-MM-dd hh:mm:ss a     dd/mm/yyyy hh:mm aa

                    //set data
                    mPtitleTV.setText(ptitle);
                    mPdescriptionTv.setText(pdescr);
                    mPlikesTV.setText(pLikes+"Thích");
                    mPtimeTV.setText(datetime);
                    mPcommentTV.setText(commentCount+"Bình luận");

                    mNameTV.setText(hisName);

                    //set image of the user   poster
                    //set post image
                    //if there no image imgPicture.equal("noImage") => hide imageview
                    if (pimage.equals("noImage")) {
                        //hide image view
                        imgPpicture.setVisibility(View.GONE);

                    } else {
                        //show  image view
                        imgPpicture.setVisibility(View.VISIBLE);
                        try {
                            Glide.with(getApplication()).load(pimage).into(imgPpicture);
                        } catch (Exception e) {
                        }
                    }
                    //set up user image in comment
                    try {
                        Glide.with(getApplication()).load(hisDp).apply(new RequestOptions().placeholder(R.drawable.ic_image_default1)).into(imgUpicture);

                    } catch (Exception e) {
                        Glide.with(getApplication()).load(R.drawable.ic_image_default1).into(imgUpicture);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            myEmail = user.getEmail();
            myUid = user.getUid();

        } else {
            //user not signed , back main Activity
            startActivity(new Intent(this, StartActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide
        menu.findItem(R.id.menu_Mpost).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.menu_Mlogout) {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();

        }
        return super.onOptionsItemSelected(item);
    }
}
