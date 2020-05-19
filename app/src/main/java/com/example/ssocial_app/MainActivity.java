package com.example.ssocial_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.example.ssocial_app.Fragment.ChatListFragment;
import com.example.ssocial_app.Fragment.HomeFragment;
import com.example.ssocial_app.Fragment.ProfileFragment;
import com.example.ssocial_app.Fragment.UsersFragment;
import com.example.ssocial_app.Notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    FirebaseAuth auth;
    Toolbar toolbar;
    FirebaseUser user;

    ActionBar actionBar;
    String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTheme(R.style.light_noActionBar);
        actionBar = getSupportActionBar();

        // actionBar.setTitle("Main");
        //enabed back button in actionbar
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2E9AFE")));
        //  setActionBarTextColor(actionBar,R.color.white);


        if (!checknet()) {

            Toast.makeText(this, "Không kết nối internet", Toast.LENGTH_SHORT).show();

        }


        //init
        auth = FirebaseAuth.getInstance();
        //bottom vavigation
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
// default on start

        getSupportActionBar().setTitle("Home");
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, homeFragment, "");
        ft1.commit();
        checkUserStatus();

    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    //TODO: update token
    public void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        reference.child(mUID).setValue(mToken);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    // hander item click
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            // home fragment
                            //  actionBar.setTitle("Home");
                            getSupportActionBar().setTitle("Home ");
                            HomeFragment homeFragment = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, homeFragment, "");
                            ft1.commit();
                            return true;
                        case R.id.nav_profile:
                            // profile fragment
                            // actionBar.setTitle("Profile");
                            getSupportActionBar().setTitle("Profile ");
                            ProfileFragment profileFragment = new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, profileFragment, "");
                            ft2.commit();

                            return true;
                        case R.id.nav_users:
                            // users fragment
                            // actionBar.setTitle("Users");
                            getSupportActionBar().setTitle("Users ");
                            UsersFragment usersFragment = new UsersFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, usersFragment, "");
                            ft3.commit();
                            return true;
                        case R.id.nav_chat:
                            getSupportActionBar().setTitle("Chat");
                            ChatListFragment chatListFragment = new ChatListFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content, chatListFragment, "");
                            ft4.commit();
                            return true;
                    }
                    return false;
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_Mlogout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            case R.id.menu_Mexit:
                // start main
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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

    private void checkUserStatus() {
        user = auth.getCurrentUser();
        if (user != null) {
            mUID = user.getUid();
            //TODO:SharedPreferences save uid
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();
            //update token
            updateToken(FirebaseInstanceId.getInstance().getToken());
        } else {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

    }

    //    private Boolean isOnline() {
//        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo ni = cm.getActiveNetworkInfo();
//        if(ni != null && ni.isConnected()) {
//            return true;
//        }
//        return false;
//    }
    private boolean checknet() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkinfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkinfo != null && activeNetworkinfo.isConnected();

//write below methodcall in every button click event wherever u want to check internet connection


    }
}
