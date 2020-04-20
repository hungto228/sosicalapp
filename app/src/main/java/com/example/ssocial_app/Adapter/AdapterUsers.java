package com.example.ssocial_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ssocial_app.ChatActivity;
import com.example.ssocial_app.Model.ModelUsers;
import com.example.ssocial_app.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHoler> {

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
                Intent intent=new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUid);
                context.startActivity(intent);
                Toast.makeText(context, ""+userEmail, Toast.LENGTH_SHORT).show();
            }
        });

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
