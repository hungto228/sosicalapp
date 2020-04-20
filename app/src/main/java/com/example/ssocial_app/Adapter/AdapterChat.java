package com.example.ssocial_app.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ssocial_app.Model.ModelChat;
import com.example.ssocial_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.Myholder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;
    //firebase user
    FirebaseUser firebaseUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    //TODO: chat left,right
    @NonNull
    @Override
    public AdapterChat.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //layout row_chat_left for receiver , row_layout_chat_right for sender
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new Myholder(view);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new Myholder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, final int position) {
//get data
        String message = chatList.get(position).getMessage();
        String timestamp = chatList.get(position).getTimestamp();

        // convert timestamp to dd/mm/yy
        Calendar calendar = Calendar.getInstance(Locale.CANADA);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String datetime = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();
        //yyyy-MM-dd hh:mm:ss a     dd/mm/yyyy hh:mm aa
        //set data
        holder.mMessege.setText(message);
        holder.mTimeStamp.setText(datetime);
        try {
            Glide.with(context).load(imageUrl).into(holder.imgProfile);
        } catch (Exception e) {
        }

        //click show delete  dialog
        holder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //show delete message  dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xóa");
                builder.setMessage("Bạn muốn xóa tin nhắn!!");

                //delete button
                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(position);
                    }
                });

                // cancel buton
                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                //create show dialog
                builder.create().show();
                return true;
            }
        });

        //set seen/delivered  status message
        if (position == chatList.size() - 1) {
            if (chatList.get(position).isIsseen()) {
                holder.mIsSeen.setText("Đã xem");
            } else {
                holder.mIsSeen.setText("Đã gửi");
            }
        } else {
            holder.mIsSeen.setVisibility(View.GONE);
        }
    }
//TODO: deleteMessage
    private void deleteMessage(int position) {
        final String myUid=FirebaseAuth.getInstance().getCurrentUser().getUid();

//        get timestamp of click message
//        compare the time of the click message with all message in chat
//        where both  values matches delete that message
        String mgsTmestamp=chatList.get(position).getTimestamp();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");
        Query query=reference.orderByChild("timestamp").equalTo(mgsTmestamp);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                  //  if you want to allow sender to delete only his message then
                    //  compare sender values with current user
                    if(ds.child("sender").getValue().equals(myUid))
                    {
                        //choose 1 or 2
                        //1 remove message  from chat
                        ds.getRef().removeValue();
                        //2 set th values of message
//                        HashMap<String,Object> hashMap=new HashMap<>();
//                        hashMap.put("message","this message delete");
//                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "Tin nhắn đã bị xóa!!", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "Bạn chỉ có thể xóa tin nhắn của bạn", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }

    //view holder class
    class Myholder extends RecyclerView.ViewHolder {
        //view
        ImageView imgProfile;
        TextView mMessege, mTimeStamp, mIsSeen;
        LinearLayout messageLayout; //click show  delete

        public Myholder(@NonNull View itemView) {

            super(itemView);
            //init view
            imgProfile = itemView.findViewById(R.id.img_profile);
            mMessege = itemView.findViewById(R.id.tv_message);
            mTimeStamp = itemView.findViewById(R.id.tv_time);
            mIsSeen = itemView.findViewById(R.id.tv_isseen);
            messageLayout = itemView.findViewById(R.id.messagelayout);


        }
    }
}
