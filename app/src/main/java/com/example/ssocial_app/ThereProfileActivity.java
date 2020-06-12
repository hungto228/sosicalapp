package com.example.ssocial_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ssocial_app.Adapter.AdapterPosts;
import com.example.ssocial_app.Model.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {
    FirebaseAuth auth;

    RecyclerView postRecycleview;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    //view from xml
    ImageView imgAvatar, imgCover;
    TextView tvName, tvEmail, tvPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);
   //     setTheme(R.style.light_noActionBar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Thông tin người dùng");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2E9AFE")));
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //init
        imgAvatar = findViewById(R.id.img_avatar);
        imgCover = findViewById(R.id.img_cover);
        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        postRecycleview=findViewById(R.id.post_recycleView);
        auth=FirebaseAuth.getInstance();

        //get uid of clicked usser to his post, inten from adapter usser
        Intent intent=getIntent();
       // inten uid from adapter usser
        uid=intent.getStringExtra("uid");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(uid);
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
                        Glide.with(getApplicationContext()).load(image).into(imgAvatar);
                    } catch (Exception e) {
                        //if exception getting then image dafault
                        //  Glide.with(getContext()).load(R.drawable.ic_image_default).into(imgAvatar);
                    }
                    try {
                        Glide.with(getApplicationContext()).load(cover).into(imgCover);
                    } catch (Exception e) {
                        //if exception getting then image dafault
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        postList=new ArrayList<>();

        checkUserStatus();
        //load hisPost
        loadHisPosts();

    }
    //TODO: loadHis post

    // copy in profilefragment is faster
    private void loadHisPosts() {
//linearlayout for recycleview
        LinearLayoutManager layoutManager=new LinearLayoutManager(ThereProfileActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to rycleview
        postRecycleview.setLayoutManager(layoutManager);
        //init post list
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts");
        //query to  load photo
        Query query=reference.orderByChild("uid").equalTo(uid);
        //get all data from  this referece
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    // add to list
                    postList.add(modelPost);
                    //adapter
                    adapterPosts=new AdapterPosts(ThereProfileActivity.this,postList);
                    //set this to recycleview
                    postRecycleview.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    //TODO: search his post
    private void seachHispost(final String searchQuery){
        //linearlayout for recycleview
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to rycleview
        postRecycleview.setLayoutManager(layoutManager);
        //init post list
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts");
        //query to  load photo
        Query query=reference.orderByChild("uid").equalTo(uid);
        //get all data from  this referece
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    //search
                    if(modelPost.getPtitle().toLowerCase().contains(searchQuery.toLowerCase())
                            ||modelPost.getPdescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        // add to list
                        postList.add(modelPost);
                    }

                    //adapter
                    adapterPosts=new AdapterPosts(ThereProfileActivity.this,postList);
                    //set this to recycleview
                    postRecycleview.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkUserStatus() {
        FirebaseUser user;
        user = auth.getCurrentUser();
        if (user != null) {
//            uid=user.getUid();
        } else {
            startActivity(new Intent(ThereProfileActivity.this, StartActivity.class));
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
        getMenuInflater().inflate(R.menu.menu_search,menu);
        //hide menuMpost
       // menu.findItem(R.id.menu_Mpost).setVisible(false);
        //search view
        MenuItem menuItem=menu.findItem(R.id.menu_Ssearch);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button
                if(!TextUtils.isEmpty(query)){
                    //seach
                    seachHispost(query);
                }else {
                    loadHisPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called whenever  user  type any letter
                if(!TextUtils.isEmpty(newText)){
                    //seach
                    seachHispost(newText);
                }else {
                    loadHisPosts();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
//        if (id == R.id.menu_Mlogout) {
//            auth.signOut();
//            checkUserStatus();
//
//        }

        return super.onOptionsItemSelected(item);
    }

}
