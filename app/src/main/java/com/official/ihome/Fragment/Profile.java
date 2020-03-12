package com.official.ihome.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.official.ihome.Adapter.OutdatedAdapter;
import com.official.ihome.Adapter.OwnedAdapter;
import com.official.ihome.DeleteAccountActivity;
import com.official.ihome.EditProfileActivity;
import com.official.ihome.FeedbackActivity;
import com.official.ihome.LoginActivity;
import com.official.ihome.MainActivity;
import com.official.ihome.Model.Analytic;
import com.official.ihome.Model.Property;
import com.official.ihome.Model.User;
import com.official.ihome.R;
import com.official.ihome.SettingActivity;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;


public class Profile extends Fragment {

    CircleImageView image_profile;
    TextView username,contact,email;
    Button edit_profile;
    ImageButton options;
    ToggleButton available,outdated;
    RecyclerView ownedAvailable,ownedOutdated;
    OwnedAdapter ownedAdapter;
    OutdatedAdapter outdatedAdapter;
    ArrayList<Property> arrayListAvailable,arrayListOutdated;

    FirebaseUser firebaseUser;
    String profileID;
    FirebaseAuth firebaseAuth;
    DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ownedAvailable = view.findViewById(R.id.owned_available);
        ownedAvailable.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerAvailable = new LinearLayoutManager(getActivity());
        ownedAvailable.setLayoutManager(layoutManagerAvailable);
        arrayListAvailable = new ArrayList<>();
        ownedAdapter = new OwnedAdapter(getContext(),arrayListAvailable);
        ownedAvailable.setAdapter(ownedAdapter);
        ownedAdapter.notifyDataSetChanged();

        ownedOutdated = view.findViewById(R.id.owned_outdated);
        ownedOutdated.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerOutdated = new LinearLayoutManager(getActivity());
        ownedOutdated.setLayoutManager(layoutManagerOutdated);
        arrayListOutdated = new ArrayList<>();
        outdatedAdapter = new OutdatedAdapter(getContext(),arrayListOutdated);
        ownedOutdated.setAdapter(outdatedAdapter);
        outdatedAdapter.notifyDataSetChanged();

        ownedAvailable.setVisibility(View.VISIBLE);
        ownedOutdated.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences preferences = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileID = preferences.getString("profileID","none");

        image_profile = view.findViewById(R.id.profile_image);
        options = view.findViewById(R.id.optionButton);
        username = view.findViewById(R.id.username);
        contact = view.findViewById(R.id.contact);
        edit_profile = view.findViewById(R.id.editButton);
        email = view.findViewById(R.id.emailAddress);
        available = view.findViewById(R.id.property_available);
        outdated = view.findViewById(R.id.property_outdated);


        userInfo();
        propertyAvailable();
        propertyOutdated();

        available.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                available.setTextColor(getResources().getColor(R.color.Red));
                outdated.setTextColor(getResources().getColor(R.color.colorPrimary));
                ownedAvailable.setVisibility(View.VISIBLE);
                ownedOutdated.setVisibility(View.GONE);
        }
        });

        outdated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outdated.setTextColor(getResources().getColor(R.color.Red));
                available.setTextColor(getResources().getColor(R.color.colorPrimary));
                ownedAvailable.setVisibility(View.GONE);
                ownedOutdated.setVisibility(View.VISIBLE);
            }
        });

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent edit = new Intent(getContext(),EditProfileActivity.class);
                startActivity(edit);
            }
        });


        final PopupMenu dropMenu = new PopupMenu(getContext(),options);
        final Menu menu = dropMenu.getMenu();

        dropMenu.getMenuInflater().inflate(R.menu.profile_dropdown,menu);
        dropMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){

                    case R.id.action_Feedback:
                        Intent feed = new Intent(getContext(),FeedbackActivity.class);
                        startActivity(feed);
                        return true;

                    case R.id.action_signOut:
                        firebaseAuth.signOut();
                        Intent intent = new Intent(getContext(),LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        return true;

                    case R.id.action_DeleteAccount:
                        Intent delete = new Intent(getContext(), DeleteAccountActivity.class);
                        startActivity(delete);
                        return true;

                    case R.id.action_setting:
                        Intent setting = new Intent(getContext(), SettingActivity.class);
                        startActivity(setting);
                        return true;

                }
                return false;
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropMenu.show();
            }
        });

        return view;
    }

    private void userInfo(){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(profileID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                username.setText(user.getUsername());
                contact.setText(user.getContact());
                email.setText(user.getEmail());
                if (user.getImageUrl().equals("default")){
                    image_profile.setImageResource(R.drawable.placeholder);
                }else {
                    Glide.with(getContext()).load(user.getImageUrl()).into(image_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void propertyAvailable(){

        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        reference = FirebaseDatabase.getInstance().getReference("Property").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayListAvailable.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Property property = snapshot.getValue(Property.class);
                    String propertyDate = property.getValidity();

                    String currentDate = dateFormat.format(Calendar.getInstance().getTime());
                    Date currentDateValue,propertyDateValue;
                    long diff = 0;
                    try {
                        currentDateValue = dateFormat.parse(currentDate);
                        propertyDateValue = dateFormat.parse(propertyDate);
                        diff = getUnitBetweenDates(propertyDateValue,currentDateValue, TimeUnit.DAYS);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (diff <= 7){
                        arrayListAvailable.add(property);
                    }

                }
                ownedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void propertyOutdated(){

        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        reference = FirebaseDatabase.getInstance().getReference("Property").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayListOutdated.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Property property = snapshot.getValue(Property.class);
                    String propertyDate = property.getValidity();

                    String currentDate = dateFormat.format(Calendar.getInstance().getTime());
                    Date currentDateValue,propertyDateValue;
                    long diff = 0;
                    try {
                        currentDateValue = dateFormat.parse(currentDate);
                        propertyDateValue = dateFormat.parse(propertyDate);
                        diff = getUnitBetweenDates(propertyDateValue,currentDateValue, TimeUnit.DAYS);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (diff > 7){
                        arrayListOutdated.add(property);
                    }
                }
                outdatedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static long getUnitBetweenDates(Date startDate, Date endDate, TimeUnit unit) {
        long timeDiff = endDate.getTime() - startDate.getTime();
        return unit.convert(timeDiff, TimeUnit.MILLISECONDS);
    }
}
