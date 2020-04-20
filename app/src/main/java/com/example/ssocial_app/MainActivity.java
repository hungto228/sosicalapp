package com.example.ssocial_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;


import com.example.ssocial_app.Fragment.ChatListFragment;
import com.example.ssocial_app.Fragment.HomeFragment;
import com.example.ssocial_app.Fragment.ProfileFragment;
import com.example.ssocial_app.Fragment.UsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.FragmentTransaction;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    FirebaseAuth auth;
    Toolbar toolbar;

    //ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //actionBar=getSupportActionBar();
        //  actionBar.setTitle("profile");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Main ");
        //init
        auth = FirebaseAuth.getInstance();
        //bottom vavigation
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
// default on start
        //  actionBar.setTitle("Home");
        getSupportActionBar().setTitle("Home");
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, homeFragment, "");
        ft1.commit();
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
                            ChatListFragment chatListFragment=new ChatListFragment();
                            FragmentTransaction ft4=getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content,chatListFragment,"");
                            ft4.commit();
                            return true;
                    }
                    return false;
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            case R.id.exit:
                // start main
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);


                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startActivity(startMain);
                finish();



        }
        return false;
    }
}
