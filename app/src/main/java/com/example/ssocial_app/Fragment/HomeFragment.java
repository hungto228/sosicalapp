package com.example.ssocial_app.Fragment;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ssocial_app.Adapter.AdapterPosts;
import com.example.ssocial_app.AddPostActivity;
import com.example.ssocial_app.Model.ModelPost;
import com.example.ssocial_app.R;
import com.example.ssocial_app.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    //firebase auth
    FirebaseAuth auth;
    DatabaseReference reference;

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //init
        auth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.post_recycleView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show  post fist ,for this load from load
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layaout to recycleview
        recyclerView.setLayoutManager(layoutManager);
        //init post list
        postList = new ArrayList<>();

        loadPost();
        if (!checknet()) {

            Toast.makeText(getActivity(), "Không kết nối internet", Toast.LENGTH_SHORT).show();

        }
        return view;

    }
    //TODO: load post
    private void loadPost() {
        //path all post
        reference = FirebaseDatabase.getInstance().getReference("Posts");
        //get  all data from this reference
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    postList.add(modelPost);
                    //adapter
                    adapterPosts=new AdapterPosts(getActivity(),postList);
                    //set adapter in to recycleview
                    recyclerView.setAdapter(adapterPosts);
                    
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //case error
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    //TODO: search post

    private  void searchPost(final String searchQuery){
        //path all post
        reference = FirebaseDatabase.getInstance().getReference("Posts");
        //get  all data from this reference
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    //get title, descr, => search
                    if(modelPost.getPtitle().toLowerCase().contains(searchQuery.toLowerCase())
                            ||modelPost.getPdescr().toLowerCase().contains(searchQuery.toLowerCase()))
                    {
                        postList.add(modelPost);
                    }
                    //adapter
                    adapterPosts=new AdapterPosts(getActivity(),postList);
                    //set adapter in to recycleview
                    recyclerView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //case error
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void checkUserStatus() {
        FirebaseUser user;
        user = auth.getCurrentUser();
        if (user != null) {

        } else {
            startActivity(new Intent(getActivity(), StartActivity.class));
            getActivity().finish();
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//show menu in option in fragment
        super.onCreate(savedInstanceState);
    }
    //inflate option menu


   @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_search,menu);
        //hide add post
       menu.findItem(R.id.menu_Spost).setVisible(false);
        //searchView to search post by post
       MenuItem menuItem=menu.findItem(R.id.menu_Ssearch);
       SearchView searchView=(SearchView) MenuItemCompat.getActionView(menuItem);
       //search listener
       searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String query) {
               //call when user press search button
               if(!TextUtils.isEmpty(query)){
                   searchPost(query);
               }else {
                   loadPost();
               }
               return false;
           }

           @Override
           public boolean onQueryTextChange(String newText) {
               //call ad and when user press any letter
               if(!TextUtils.isEmpty(newText)){
                   searchPost(newText);
               }else {
                   loadPost();
               }
               return false;
           }
       });
        super.onCreateOptionsMenu(menu, inflater);
    }

    //handle menu item click
//    @Override
//    public boolean onContextItemSelected(@NonNull MenuItem item) {
//        //get item id
//        int id=item.getItemId();
//        if(id==R.id.menu_Mlogout){
//            auth.signOut();
//            checkUserStatus();
//
//        }
//        if(id==R.id.menu_Mpost){
//           startActivity(new Intent(getActivity(), AddPostActivity.class));
//        }
//        return super.onContextItemSelected(item);
//    }
    private boolean checknet() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkinfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkinfo != null && activeNetworkinfo.isConnected();

//write below methodcall in every button click event wherever u want to check internet connection


    }
}
