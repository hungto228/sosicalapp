package com.example.ssocial_app;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class SplashScreen extends AppCompatActivity {
    private static final int SPLASH_TIME = 1000;
    TextView mAppname,mNameCreator;
    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        //        Transparent Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }




        setContentView(R.layout.activity_launch_screen);
        //animation for appname ,name creator
//animation
        animation= AnimationUtils.loadAnimation(this,R.anim.left_in);
        animation.setDuration(500);
        mNameCreator=findViewById(R.id.tv_nameCreator);
        mAppname=findViewById(R.id.tv_nameApp);
        mNameCreator.startAnimation(animation);
       mAppname.startAnimation(animation);
//        imgGirl=findViewById(R.id.img_girl);
//        try {
//            Glide.with(getApplicationContext()).load(R.drawable.img2).into(imgGirl);
//
//        }catch (Exception e){
//
//        }
//        Glide.with(this)
//                .load(R.drawable.img1)
//                .into(imgGirl);
//        AnimationDrawable splashAnimation = (AnimationDrawable)
//                ivBgSplash.getBackground();
//        splashAnimation.start();


        new BackgroundTask().execute();
    }

    private class BackgroundTask extends AsyncTask {
        Intent intent;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            intent = new Intent(SplashScreen.this, StartActivity.class);
        }

        @Override
        protected Object doInBackground(Object[] params) {

            /*  Use this method to load background
             * data that your app needs. */

            try {
                Thread.sleep(SPLASH_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
//            Pass your loaded data here using Intent

//            intent.putExtra("data_key", "");
            startActivity(intent);
            finish();
        }
    }
}
