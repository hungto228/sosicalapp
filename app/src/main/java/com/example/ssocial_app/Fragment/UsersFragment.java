package com.example.ssocial_app.Fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ssocial_app.Adapter.AdapterUsers;
import com.example.ssocial_app.MainActivity;
import com.example.ssocial_app.Model.ModelUsers;
import com.example.ssocial_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {
    public static final String TAG = "User Fragment";
    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUsers> usersList;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;
    //progress dialog
    ProgressDialog pd;


    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        auth = FirebaseAuth.getInstance();
        //init recyclerview

        auth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.users_recyclerview);
        //set propretions
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //init user list
        usersList = new ArrayList<>();
        //get all users
        getAllUser();

    return view;
    }

    private void getAllUser() {
        //get current usser

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                usersList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                    //get all user i
                    Log.d(TAG, "check in ");
                    if(!modelUsers.getId().equals(firebaseUser.getUid())){

                         usersList.add(modelUsers);
                       }
                    //adapter
                    adapterUsers = new AdapterUsers(getContext(), usersList);
                    //set adapter to  recycler view
                    recyclerView.setAdapter(adapterUsers);
                    Log.d(TAG, " set adapter");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


}
//TODO: search user
    private void searchUser(final String query) {
        //get current usser
        Log.d(TAG, "check in ");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                usersList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                    //get all user i
                    if(!modelUsers.getId().equals(user.getUid())) {
                        if (modelUsers.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                                modelUsers.getEmail().toLowerCase().contains(query.toLowerCase())) {
                            usersList.add(modelUsers);
                        }
                    }
                }
                //adapter
                adapterUsers = new AdapterUsers(getContext(), usersList);
                //refresh  adapter
                adapterUsers.notifyDataSetChanged();
                //set adapter to  recycler view
                recyclerView.setAdapter(adapterUsers);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void checkUserStatus() {
        user=auth.getCurrentUser();
        if(user!=null)
        {

        }else {
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
        inflater.inflate(R.menu.menu_search , menu);
        //hide add post icon form fragment
        menu.findItem(R.id.menu_Spost).setVisible(false);
        //searchview
        MenuItem menuItem=menu.findItem(R.id.menu_Ssearch);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        //search    listenner
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query.trim())){
                    searchUser(query);
                }else {
                    getAllUser();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText.trim())){
                    searchUser(newText);
                }else {
                    getAllUser();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       int id=item.getItemId();
       if(id==R.id.menu_Mlogout)
       {auth.signOut();
       checkUserStatus();

}

        return super.onOptionsItemSelected(item );
    }


}





