package com.example.ssocial_app.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ssocial_app.AddPostActivity;
import com.example.ssocial_app.Model.ModelPost;
import com.example.ssocial_app.R;
import com.example.ssocial_app.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.Myholder> {
    public static final String TAG = "AdapterPosts";
    Context context;
    List<ModelPost> postList;
    String myUid;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
    public void onBindViewHolder(@NonNull final Myholder holder, int position) {
        //get data

        final String uid = postList.get(position).getUid();
        String uemail = postList.get(position).getUemail();
        String uname = postList.get(position).getUname();
        String udp = postList.get(position).getUdp();

        final String pid = postList.get(position).getPid();
        String ptitle = postList.get(position).getPtitle();
        String pdescription = postList.get(position).getPdescr();
        final String pimage = postList.get(position).getPimage();
        String ptimestamp = postList.get(position).getPtime();

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
        } catch (Exception e) {
        }
        //set post image
        //if there no image imgPicture.equal("noImage") => hide imageview
        if (pimage.equals("noImage")) {
                //hide image view
                holder.imgPpicture.setVisibility(View.GONE);

        } else {
            //show  image view
            holder.imgPpicture.setVisibility(View.VISIBLE);
            try {
                Glide.with(context).load(pimage).into(holder.imgPpicture);
            } catch (Exception e) {
            }
        }

        //handle button  more click
        holder.imgMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   Toast.makeText(context, "m  ore", Toast.LENGTH_SHORT).show();
                showMoreOption(holder.imgMoreBtn, uid, myUid, pid, pimage);
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
        holder.proflieLayut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "Profle", Toast.LENGTH_SHORT).show();
                //click go to ThereProfileActivity with uid,uid of user
                //show specific user  data/post
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });


    }

    //
    private void showMoreOption(ImageButton imgMoreBtn, String uid, String myUid, final String pid, final String pimage) {
        //create popup menu having option delete // use add more option later
        PopupMenu popupMenu = new PopupMenu(context, imgMoreBtn, Gravity.END);
       // Context wrapper = new ContextThemeWrapper(context, R.style.popUP);

     //   PopupMenu popup = new PopupMenu(wrapper, view);
        //show delete option in only post with user
        if (uid.equals(myUid)) {
            //add item menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Xóa");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Chỉnh sửa");
        }
        //item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0){
                    //delete click
                    beginDelete(pid, pimage);}
                else {
                    //edit clicked
                    //start AddPostActivity with key"editPost" and id  of the post clicked
                    Intent intent=new Intent(context, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",pid);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //popup menu show
        popupMenu.show();
    }

    private void beginDelete(String pid, String pimage) {
        // post can be with  or without image
        if (pimage.equals("noImage")) {
            //post without image
            deleteWithoutImage(pid, pimage);

        } else {
            //post with image
            deleteWithImage(pid, pimage);

        }
    }

    private void deleteWithImage(final String pid, String pimage) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Đang xóa bài");

        //step 1:delete image using url
        //     2: delete from database using post id;
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pimage);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete database
                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pid").equalTo(pid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    //remove values firebase from where pid
                                    ds.getRef().removeValue();
                                }
                                //delete
                                Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                pd.dismiss();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed
                pd.dismiss();
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void deleteWithoutImage(String pid, String pimage) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Đang xóa bài");
        //image deleted, now delete database
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pid").equalTo(pid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //remove values firebase from where pid
                    ds.getRef().removeValue();

                }
                //delete 
                Toast.makeText(context, "Xóa thành công ", Toast.LENGTH_SHORT).show();
                pd.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        LinearLayout proflieLayut;

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

            proflieLayut = itemView.findViewById(R.id.profileLayout);

        }
    }
}
