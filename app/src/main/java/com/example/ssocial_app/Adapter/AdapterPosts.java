package com.example.ssocial_app.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import com.example.ssocial_app.PostDetailActivity;
import com.example.ssocial_app.R;
import com.example.ssocial_app.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.Myholder> {
    public static final String TAG = "AdapterPosts";
    Context context;
    List<ModelPost> postList;
    String myUid;
    //for  like database  node
    private DatabaseReference likeReference;
    //reference of post
    private DatabaseReference postReference;

    boolean mProgressLike = false;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeReference = FirebaseDatabase.getInstance().getReference("Likes");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
       likeReference.keepSynced(true);
       postReference.keepSynced(true);
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
    public void onBindViewHolder(@NonNull final Myholder holder, final int position) {
        //get data

        final String uid = postList.get(position).getUid();
        String uemail = postList.get(position).getUemail();
        String uname = postList.get(position).getUname();
        String udp = postList.get(position).getUdp();

        final String pid = postList.get(position).getPid();
        final String ptitle = postList.get(position).getPtitle();
        final String pdescription = postList.get(position).getPdescr();
        final String pimage = postList.get(position).getPimage();
        final String ptimestamp = postList.get(position).getPtime();
        final String plikes = postList.get(position).getPlikes();
        String pcomment = postList.get(position).getPcomment();


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
        holder.mPlikesTV.setText(plikes + "Thích");
        holder.mPcommentTV.setText(pcomment + "Bình luận");

        //set like for  post
        setLike(holder, pid);

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

        //TODO:handle button  more click
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
//                Toast.makeText(context, "Likes", Toast.LENGTH_SHORT).show();
                //get total number of likes for the post ,whose like  button clicked
                //increase values by1,otherwire decreale values by 1
                final int pLikes = Integer.parseInt(postList.get(position).getPlikes());
                mProgressLike = true;
                //get id of the post clicked
                final String postIde = postList.get(position).getPid();
                likeReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mProgressLike) {
                            if (dataSnapshot.child(postIde).hasChild(myUid)) {
                                // is liked  , so remove likeL
                                postReference.child(postIde).child("plikes").setValue("" + (pLikes - 1));
                                likeReference.child(postIde).child(myUid).removeValue();
                                //bug remove values
                                mProgressLike = false;
                            } else {
                                //not like ,like it;
                                postReference.child(postIde).child("plikes").setValue("" + (pLikes + 1));
                                likeReference.child(postIde).child(myUid).setValue("Liked");
                                mProgressLike = false;
                            }
                        }

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        //TODO:hander button Comment click
        holder.mCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "comment", Toast.LENGTH_SHORT).show();
                //start PostDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                //get delail  of post using id,id of post cliked
                intent.putExtra("postId", pid);
                context.startActivity(intent);
            }
        });
        //TODO: hander button Share click
        holder.mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "share", Toast.LENGTH_SHORT).show();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.imgPpicture.getDrawable();
                if (bitmapDrawable == null) {
                    //post without image
                    shareTextOnly(ptitle, pdescription);
                } else {
                    //post with image
                    //convert to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(ptitle, pdescription, bitmap);

                }
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

    private void shareImageAndText(String ptitle, String pdescription, Bitmap bitmap) {
        String shareBody = ptitle + "\n" + pdescription;
        //  save image image in cache ,get the saved in image uri
        Uri uri = saveImageToShare(bitmap);
        //share intent
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.putExtra(Intent.EXTRA_TEXT,shareBody);
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject");
        intent.setType("image/png");
        context.startActivity(Intent.createChooser(intent,"Share via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        //child images from res xml:path
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            //create if not exits, directory named by this  pathname.
            imageFolder.mkdir();
            File file=new File(imageFolder,"shared_image.png");
            FileOutputStream stream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();

            uri= FileProvider.getUriForFile(context,"com.example.ssocial_app.fileprovider",file);

        } catch (Exception e) {
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void shareTextOnly(String ptitle, String pdescription) {
        String shareBody = ptitle + "\n" + pdescription;

        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        //case you share via email app
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        //text body
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        //message to show in share dialog
        context.startActivity(Intent.createChooser(intent, "Share via"));
    }

    //TODO:set like for
    private void setLike(final Myholder holder, final String posKey) {
        likeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(posKey).hasChild(myUid)) {
                    // user ha not liked this post
                    //change icon button from like => liked
                    //change text like button  from like to liked
                    holder.mLikeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_green, 0, 0, 0);
                    holder.mLikeBtn.setText("Đã Thích");
                } else {
                    //user ha not liked this post
                    //change icon button from like => liked
                    //change text like button  from like to liked
                    holder.mLikeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_back, 0, 0, 0);
                    holder.mLikeBtn.setText("Thích");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Chỉnh sửa");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "Xem chi tiết");
        //item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    //delete click
                    beginDelete(pid, pimage);
                } else if (id == 1) {
                    //edit clicked
                    //start AddPostActivity with key"editPost" and id  of the post clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pid);
                    context.startActivity(intent);
                } else if (id == 2) {
                    //start PostDetailActivity
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    //get delail  of post using id,id of post cliked
                    intent.putExtra("posiID", pid);
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
            //TODO:deleteWithoutImage
            deleteWithoutImage(pid, pimage);

        } else {
            //post with image
            //TODO:deleteWithImage
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
        TextView mUNameTv, mPtimeTv, mPtitleTv, mPdescriptionTv, mPlikesTV, mPcommentTV;
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
            mPcommentTV = itemView.findViewById(R.id.tv_Pcomment);

            mLikeBtn = itemView.findViewById(R.id.btn_PLike);
            mCommentBtn = itemView.findViewById(R.id.btn_Pcomment);
            mShareBtn = itemView.findViewById(R.id.btn_Pshare);

            proflieLayut = itemView.findViewById(R.id.profileLayout);

        }
    }
}
