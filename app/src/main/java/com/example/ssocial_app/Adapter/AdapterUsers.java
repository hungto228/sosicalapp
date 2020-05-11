package com.example.ssocial_app.Adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ssocial_app.ChatActivity;
import com.example.ssocial_app.Model.ModelUsers;
import com.example.ssocial_app.R;
import com.example.ssocial_app.ThereProfileActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHoler> {
    public static final String TAG="AdapterUsers ";
    //animation
    Animation animation;
    private boolean on_attach = true;
    long DURATION = 200;

    Context context;
    AdapterUsers adapterUsers;
    List<ModelUsers> usersList;
    // contructor

    public AdapterUsers(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public MyHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //layout (row_users.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent,false);

        return new MyHoler(view);
    }
    //TODO: onBindView AdapterUser
    @Override
    public void onBindViewHolder(@NonNull MyHoler holder, int position) {
       //get data
        final String hisUid=usersList.get(position).getId();
        String userImage = usersList.get(position).getImage();
        String userName = usersList.get(position).getUsername();
        final String userEmail = usersList.get(position).getEmail();
        //set data
        holder.mNameTV.setText(userName);
        holder.mEmailTV.setText(userEmail);
        Log.d(TAG, "set data: ");
        try{
//            RequestOptions requestOptions = new RequestOptions();
//            requestOptions.placeholder(R.drawable.ic_image_default1);
//            Glide.with(context).setDefaultRequestOptions(requestOptions).load(userImage).into(holder.imgAvatar);
           Glide.with(context).load(userImage).apply(new RequestOptions().placeholder(R.drawable.ic_image_default1)).into(holder.imgAvatar);
        }catch (Exception e){

        }
        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                //remote
//                Intent intent=new Intent(context, ChatActivity.class);
//                intent.putExtra("hisUid",hisUid);
//                context.startActivity(intent);

            //    Toast.makeText(context, ""+userEmail, Toast.LENGTH_SHORT).show();
                //show dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Xem trang cá nhân", "Nhắn tin"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    if(which==0){
                        //profile clicked
                        //click go to ThereProfileActivity with uid,uid of user
                        //show specific user  data/post
                        Intent intent=new Intent(context, ThereProfileActivity.class);
                        intent.putExtra("uid",hisUid);
                        context.startActivity(intent);
                    }
                    if(which==1){
                        //chat clicked
                        //click go to ChatActivity with uid,uid of user
                        //show specific user  data/post
                        Intent intent=new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid",hisUid);
                        context.startActivity(intent);
                    }
                    }
                });
                builder.create().show();
            }
        });
        // animation is update because is bug
//setAnimation(holder.itemView,position);
    }
    //animation fade
        private void setAnimation(View itemView, int i) {
        if(!on_attach){
            i = -1;
        }
        boolean isNotFirstItem = i == -1;
        i++;
        itemView.setAlpha(0.f);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animator = ObjectAnimator.ofFloat(itemView, "alpha", 0.f, 0.5f, 1.0f);
        ObjectAnimator.ofFloat(itemView, "alpha", 0.f).start();
        animator.setStartDelay(isNotFirstItem ? DURATION / 2 : (i * DURATION / 3));
        animator.setDuration(500);
        animatorSet.play(animator);
        animator.start();
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    //view holder class
    class MyHoler extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView mNameTV, mEmailTV;

        public MyHoler(@NonNull View itemView) {
            super(itemView);
            //init view
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            mNameTV = itemView.findViewById(R.id.tv_name);
            mEmailTV = itemView.findViewById(R.id.tv_email);

        }
    }
}
