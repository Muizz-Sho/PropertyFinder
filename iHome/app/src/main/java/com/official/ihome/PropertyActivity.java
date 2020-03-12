package com.official.ihome;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.official.ihome.Fragment.Search;
import com.official.ihome.Model.Property;
import com.official.ihome.Model.User;

public class PropertyActivity extends AppCompatActivity {

    TextView name,state,address,postal,owner,detail,latitude,longitude,price;
    Button message;
    ImageButton navigation;
    ImageView imageProperty;

    DatabaseReference reference,refOwner;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property);

        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        name = findViewById(R.id.PropertyView_Name);
        state = findViewById(R.id.PropertyView_State);
        address = findViewById(R.id.PropertyView_address);
        postal = findViewById(R.id.PropertyView_postal);
        owner = findViewById(R.id.PropertyView_owner);
        detail = findViewById(R.id.PropertyView_detail);
        imageProperty = findViewById(R.id.PropertyView_image);
        price = findViewById(R.id.PropertyView_price);

        message = findViewById(R.id.PropertyMessage);
        navigation = findViewById(R.id.PropertyNavigation);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        Bundle data = intent.getExtras();

        final String propertyID = data.getString("propertyID");
        final String ownerID = data.getString("ownerID");

        if (data!=null){
            final String dataHold = (String) data.get("propertyID");
            final String dataHoldOwner = (String) data.get("ownerID");

            if (dataHoldOwner.equals(firebaseUser.getUid())){
                message.setText("Edit Property");
            }else{
                message.setText("Message Owner");
            }


            refOwner = FirebaseDatabase.getInstance().getReference("Users");
            refOwner.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User ownerData = snapshot.getValue(User.class);

                        if (dataHoldOwner.equals(ownerData.getId())){
                            owner.setText(ownerData.getUsername());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            reference = FirebaseDatabase.getInstance().getReference("Property");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                        for (DataSnapshot propertySnapshot : userSnapshot.getChildren()){
                            Property ads = propertySnapshot.getValue(Property.class);

                            if (ads.getPropertyID().equals(dataHold)){
                                name.setText(ads.getName().toUpperCase());
                                state.setText(ads.getState());
                                address.setText(ads.getAddress());
                                //postal.setText(ads.getPostal());
                                //owner.setText(ads.getOwnerID());
                                detail.setText(ads.getDetail());
                                price.setText("RM"+ads.getPrice());
                                latitude.setText(ads.getLatitude());
                                longitude.setText(ads.getLongitude());
                                if (ads.getImageUrl().equals("default")){
                                    imageProperty.setImageResource(R.drawable.noimage);
                                }else
                                    Glide.with(getApplicationContext()).load(ads.getImageUrl()).into(imageProperty);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }else{
            Toast.makeText(PropertyActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PropertyActivity.this, Search.class));
        }


        String btn = message.getText().toString();
        if (btn.equals("Message Owner")){
            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent messageOwner = new Intent(PropertyActivity.this,MessageActivity.class);
                    messageOwner.putExtra("userid", ownerID);
                    startActivity(messageOwner);
                }
            });
        }else {
            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editProperty = new Intent(PropertyActivity.this,EditPropertyActivity.class);
                    editProperty.putExtra("propertyID",propertyID);
                    startActivity(editProperty);
                }
            });
        }

        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat = Double.parseDouble(latitude.getText().toString());
                double lng = Double.parseDouble(longitude.getText().toString());

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr="+lat+","+lng));
                startActivity(intent);
            }
        });
    }
}
