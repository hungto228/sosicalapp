package com.example.ssocial_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ssocial_app.Adapter.AdapterChat;
import com.example.ssocial_app.Model.ModelChat;
import com.example.ssocial_app.Model.ModelUsers;
import com.example.ssocial_app.Notifications.APIService;
import com.example.ssocial_app.Notifications.Client;
import com.example.ssocial_app.Notifications.Data;
import com.example.ssocial_app.Notifications.Response;
import com.example.ssocial_app.Notifications.Sender;
import com.example.ssocial_app.Notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ChatActivity extends AppCompatActivity {
    static final String TAG="Chat Activity";
    //animation
    Animation animation;
    //firebase
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;
    //storage
//view xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView imgProfile;
    TextView mNameTv, mStatusUser;
    EditText mMessageEdt;
    ImageButton imgBtnSend;
    //checking if use  has seen message or not
    ValueEventListener seenListener;
    DatabaseReference referenceSeen;
    List<ModelChat> chatList;
    AdapterChat adapterChat;

    String hisUid;
    String myUid;
    String hisImage;

    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //init
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chats ");
//        toolbar.setTitle("");
        recyclerView = findViewById(R.id.users_recyclerview);
        imgProfile = findViewById(R.id.img_profile);
        imgBtnSend = findViewById(R.id.imgBtn_send);
        mNameTv = findViewById(R.id.tv_name);
        mStatusUser = findViewById(R.id.tv_statusUser);
        mMessageEdt = findViewById(R.id.edt_message);
        // Linearlayout for recycleview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recyclerview properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //create api service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");
        //firebase auth instance
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");
        //search ussers
        Query query = reference.orderByChild("id").equalTo(hisUid);
        //get picture  andname for "Usser"
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    String name = "" + ds.child("username").getValue();
                    hisImage = "" + ds.child("image").getValue();
                    String typingStatus = "" + ds.child("typing").getValue();
                    //check typing status
                    if (typingStatus.equals(myUid)) {
                        mStatusUser.setText("Đang nhập ...");
                    } else {
                        //get values of online status
                        String onlineStatus = "" + ds.child("onlinestatus").getValue();
                        if (onlineStatus.equals("online")) {
                            mStatusUser.setText(onlineStatus);
                        } else {
                            //convert  timestamp to proper time date
                            // convert timestamp to dd/mm/yy
                            Calendar calendar = Calendar.getInstance(Locale.CANADA);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            String datetime = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();
                            mStatusUser.setText("Nhìn thấy lần cuối từ " + datetime);
                        }
                    }


                    //set data
                    mNameTv.setText(name);

                    try {// image received,  set into image view toolbar
                        Glide.with(getApplicationContext()).load(hisImage).
                                apply(new RequestOptions().placeholder(R.drawable.ic_default_image_white)).into(imgProfile);
                    } catch (Exception e) {
                        //set default image
                        Glide.with(getApplicationContext()).load(R.drawable.ic_default_image_white).into(imgProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        imgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                //get text frorm editext
                String message = mMessageEdt.getText().toString().trim();
                //check text is empty
                if (TextUtils.isEmpty(message)) {
                    //text is empty
                    Toast.makeText(ChatActivity.this, "Không gửi được tin nhắn trống", Toast.LENGTH_SHORT).show();
                } else {
                    //text is not emptjy
                    sendMessage(message);
                }
                //reset editext after sending message
                mMessageEdt.setText("");
            }
        });
        readMessage();
        seenMessage();
        //check edit text change listener typing
        mMessageEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() == 0) {
                    checkTypingStatus("no");

                } else {
                    checkTypingStatus(hisUid);// uid of receiver
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    //TODO: seenMessage
    private void seenMessage() {
        referenceSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = referenceSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if (modelChat.getReceiver().equals(auth.getUid()) && modelChat.getSender().equals(hisUid)) {


                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //ToDO:readMessage
    private void readMessage() {
        chatList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if (modelChat.getReceiver().equals(myUid) && modelChat.getSender().equals(hisUid) ||
                            modelChat.getReceiver().equals(hisUid) && modelChat.getSender().equals(myUid)) {
                        chatList.add(modelChat);
                    }
                    //adapter
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    //set adapter  to recycleview
                    recyclerView.setAdapter(adapterChat);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //TODO: send message
    private void sendMessage(final String message) {
        reference = FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("isseen", false);
        reference.child("Chats").push().setValue(hashMap);


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUsers modelUsers = dataSnapshot.getValue(ModelUsers.class);
                if (notify) {

                    sendNotification(hisUid, modelUsers.getUsername(), message);

                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //TODO: sendnotification on chatActivity
    private void sendNotification(final String hisUid, final String username, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myUid, username + ":"+ message, "Có tin nhắn mới", hisUid, R.drawable.ic_image_default);

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender).enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                            Toast.makeText(ChatActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Response> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkOnlineStatus(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlinestatus", status);
        //update status of current "Users"
        reference.updateChildren(hashMap);

    }

    private void checkTypingStatus(String typing) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typing", typing);
        //update status of current "Users"
        reference.updateChildren(hashMap);

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu, menu);
//        //   menu.findItem(R.id.search).setVisible(false);
//        return super.onCreateOptionsMenu(menu);
//    }
//
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

    private void checkUserStatus() {
        user = auth.getCurrentUser();
        if (user != null) {
            myUid = user.getUid();


        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }


    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //set online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //get timestamp
        String timeStamp = String.valueOf(System.currentTimeMillis());
        //set off line with timeStamp
        checkOnlineStatus(timeStamp);
        checkTypingStatus("no");
        referenceSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        //set on line
        checkOnlineStatus("online");
        super.onResume();
    }
}
