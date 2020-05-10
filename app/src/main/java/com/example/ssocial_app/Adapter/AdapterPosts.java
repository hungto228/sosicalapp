package com.example.ssocial_app.Adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ssocial_app.Model.ModelPost;
import com.example.ssocial_app.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.Myholder> {
    public static final String TAG="AdapterPosts";
    Context context;
    List<ModelPost> postList;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout  row_post
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);

        return new Myholder(view);
    }
    //TODO: onBindView AdapterPost
    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
            //get data

        String uid=postList.get(position).getUid();
        String uemail=postList.get(position).getUemail();
        String uname=postList.get(position).getUname();
        String udp=postList.get(position).getUdp();

        String pid=postList.get(position).getPid();
        String ptitle=postList.get(position).getPtitle();
        String pdescription=postList.get(position).getPdescr();
        String pimage=postList.get(position).getPimage();
        String ptimestamp=postList.get(position).getPtime();

        // convert timestamp to dd/mm/yy
        Calendar calendar = Calendar.getInstance(Locale.CANADA);
        calendar.setTimeInMillis(Long.parseLong(ptimestamp));
        String datetime = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();
        //yyyy-MM-dd hh:mm:ss a     dd/mm/yyyy hh:mm aa

        //set data
        holder.mUNameTv.setText(uname);
        holder.mPtimeTv.setText(datetime);
        holder.mPtitleTv.setText(ptitle);
        holder.mPdescriptionTv.setText(pdescription);
        Log.d(TAG, "set data: ");

        //set user dp
        try {
            Glide.with(context).load(udp).apply(new RequestOptions().placeholder(R.drawable.ic_image_default1)).into(holder.imgUpicture);
        }catch (Exception e){
        }
        //set post image
        //if there no image imgPicture.equal("noImage") => hide imageview
        if(pimage.equals("noImage")){
            //hide image view
            holder.imgPpicture.setVisibility(View.GONE);

        }else {
            try {
                Glide.with(context).load(pimage).into(holder.imgPpicture);
            } catch (Exception e) {
            }
        }

        //handle button click
        holder.imgMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          Toast.makeText(context, "m  ore", Toast.LENGTH_SHORT).show();
            }
        });
        holder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Likes", Toast.LENGTH_SHORT).show();
            }
        });
        holder.mCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "comment", Toast.LENGTH_SHORT).show();
            }
        });
        holder.mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "share", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holdel class
    class Myholder extends RecyclerView.ViewHolder {
        //view from row_post
        ImageView imgUpicture, imgPpicture;
        TextView mUNameTv, mPtimeTv, mPtitleTv, mPdescriptionTv, mPlikesTV;
        ImageButton imgMoreBtn;
        Button mLikeBtn, mCommentBtn, mShareBtn;

        public Myholder(@NonNull View itemView) {
            super(itemView);
            //init view
            imgUpicture = itemView.findViewById(R.id.img_uPicture);
            imgPpicture = itemView.findViewById(R.id.img_pPicture);

            imgMoreBtn = itemView.findViewById(R.id.btn_more);

            mUNameTv = itemView.findViewById(R.id.tv_name);
            mPtimeTv = itemView.findViewById(R.id.tv_time);
            mPtitleTv = itemView.findViewById(R.id.tv_Ptitle);
            mPdescriptionTv = itemView.findViewById(R.id.tv_Pdescripsion);
            mPlikesTV = itemView.findViewById(R.id.tv_Plike);

            mLikeBtn = itemView.findViewById(R.id.btn_PLike);
            mCommentBtn = itemView.findViewById(R.id.btn_Pcomment);
            mShareBtn = itemView.findViewById(R.id.btn_Pshare);

        }
    }
}
