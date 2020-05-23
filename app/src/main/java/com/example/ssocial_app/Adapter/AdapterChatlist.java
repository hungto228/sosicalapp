package com.example.ssocial_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ssocial_app.ChatActivity;
import com.example.ssocial_app.Model.ModelChatlist;
import com.example.ssocial_app.Model.ModelUsers;
import com.example.ssocial_app.R;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.myHolder> {
    Context context;
    List<ModelUsers> usersList;
    private HashMap<String, String> lastMessageHasmap;

    public AdapterChatlist(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
        this.lastMessageHasmap = new HashMap<>();
    }



    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row-chatlist
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new myHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
        //get data
        final String hisUid = usersList.get(position).getId();
        String userAvatar = usersList.get(position).getImage();
        String userName = usersList.get(position).getUsername();
        String lastmessage = lastMessageHasmap.get(hisUid);

        //setdata
        holder.mNameTV.setText(userName);
        if (lastmessage == null || lastmessage.equals("default")) {
            holder.mLastMessageTV.setVisibility(View.GONE);
        } else {
            holder.mLastMessageTV.setVisibility(View.VISIBLE);
            holder.mLastMessageTV.setText(lastmessage);
        }
        try {
            Glide.with(context).load(userAvatar).apply(new RequestOptions().placeholder(R.drawable.ic_image_default1)).into(holder.imgAvatar);
        } catch (Exception e) {
            Glide.with(context).load(R.drawable.ic_image_default1).into(holder.imgAvatar);
        }
//        //set online status  chat list
//        if(usersList.get(position).getOnlinestatus().equals("online")){
//            // status online
//           holder.imgSatusOnline.setImageResource(R.drawable.cricle_online);
//        }else {
//            //status offline
//            holder.imgSatusOnline.setImageResource(R.drawable.cricle_offline);
//        //    Glide.with(context).load(R.drawable.cricle_offline).into(holder.imgSatusOnline);
//        }
        if (usersList.get(position).getOnlinestatus().equals("online")){
            holder.img_on.setVisibility(View.VISIBLE);
            holder.img_off.setVisibility(View.GONE);
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.VISIBLE);
        }
        //handle click of usser
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity  with that usser
                Intent intent=new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUid);
                context.startActivity(intent);
            }
        });

    }
    //set last message last
    public  void setLastMessageHasmap(String userId,String lastMessage){
        lastMessageHasmap.put(userId,lastMessage);
    }

    @Override
    public int getItemCount() {
        //size of list
        return usersList.size();

    }


    class myHolder extends RecyclerView.ViewHolder {
        //View of chat list row_chatlist
        ImageView  imgAvatar;
        TextView mNameTV, mLastMessageTV;
        private ImageView img_on;
        private ImageView img_off;

        public myHolder(@NonNull View itemView) {
            super(itemView);
            //init view
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            imgAvatar = itemView.findViewById(R.id.img_profile);

            mNameTV = itemView.findViewById(R.id.tv_name);
            mLastMessageTV = itemView.findViewById(R.id.tv_lastMessage);
        }
    }
}
