package com.example.ssocial_app.Fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.ssocial_app.Adapter.AdapterChatlist;
import com.example.ssocial_app.MainActivity;
import com.example.ssocial_app.Model.ModelChat;
import com.example.ssocial_app.Model.ModelChatlist;
import com.example.ssocial_app.Model.ModelUsers;
import com.example.ssocial_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {
    //Recyclerview
    RecyclerView recyclerView;
    //firebase auth
    FirebaseAuth auth;
    DatabaseReference reference;
    FirebaseUser currentUser;

    List<ModelChatlist> chatlistList;
    List<ModelUsers> usersList;

    AdapterChatlist adapterChatlist;


    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        //init
        auth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        recyclerView = view.findViewById(R.id.recycleView);
        chatlistList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                chatlistList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChatlist modelChatlist = ds.getValue(ModelChatlist.class);
                    chatlistList.add(modelChatlist);
                }
                loadChat();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    //TODO: loadChat with user
    private void loadChat() {
        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                    for (ModelChatlist chatlist : chatlistList) {
                        if (modelUsers.getId() != null && modelUsers.getId().equals(chatlist.getId()))
                            ;
                        usersList.add(modelUsers);
                        break;
                    }
                }
                //adapter
                adapterChatlist = new AdapterChatlist(getContext(), usersList);
                recyclerView.setAdapter(adapterChatlist);

                //set last message
                for (int i = 0; i <usersList.size() ; i++) {
                    lastMessage(usersList.get(i).getId());

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void lastMessage(final String userId) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lastmessage="default";
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);
                    if(chat==null){
                        continue;
                    }
                    String sender=chat.getSender();
                    String receiver=chat.getReceiver();
                    if(sender==null||receiver==null){
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid())&&chat.getSender().equals(userId)
                    ||chat.getReceiver().equals(userId)&&chat.getSender().equals(currentUser.getUid())
                    ){
                        lastmessage=chat.getMessage();
                    }
                }
                adapterChatlist.setLastMessageHasmap(userId,lastmessage);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user;
        user = auth.getCurrentUser();
        if (user != null) {

        } else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//toshow menu option
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        //hide add post icon form fragment
        menu.findItem(R.id.menu_Spost).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.logout) {
//            auth.signOut();
//            checkUserStatus();
//
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


}
