package com.example.ssocial_app.Adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ssocial_app.Model.ModelComment;
import com.example.ssocial_app.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class AdapterComments extends RecyclerView.Adapter<AdapterComments.myHolder> {
    Context context;
    List<ModelComment> commentList;

    public AdapterComments(Context context, List<ModelComment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row_comments
        View view= LayoutInflater.from(context).inflate(R.layout.row_comments,parent,false);
        return new myHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {

        //get data
        String uid=commentList.get(position).getUid();
        String name=commentList.get(position).getUname();
        String email=commentList.get(position).getUemail();
        String image=commentList.get(position).getUdp();

        String cid=commentList.get(position).getCid();
        String comment=commentList.get(position).getComment();
        String timestamp=commentList.get(position).getTimestamp();
//
//        // convert timestamp to dd/mm/yy
//        Calendar calendar = Calendar.getInstance(Locale.CANADA);
//        calendar.setTimeInMillis(Long.parseLong(timestamp));
//        String datetime = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();

        //set data
        holder.mNameTV.setText(name);
      //  holder.mTimeTV.setText(datetime);
        holder.mCommentTV.setText(comment);

        //set user dp
            try{
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.ic_image_default1)).into(holder.imgAvatar);
            }catch (Exception e){

            }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class myHolder extends RecyclerView.ViewHolder{
        ImageView imgAvatar;
        TextView mNameTV,mCommentTV,mTimeTV;
        public myHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar =itemView.findViewById(R.id.img_avatar);
            mNameTV=itemView.findViewById(R.id.tv_name);
            mCommentTV=itemView.findViewById(R.id.tv_Pcomment);
        //    mTimeTV=itemView.findViewById(R.id.tv_time);

        }
    }




}
