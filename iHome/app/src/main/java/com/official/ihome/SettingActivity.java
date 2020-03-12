package com.official.ihome;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.official.ihome.Fragment.Search;
import com.official.ihome.Model.Circle;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SettingActivity extends AppCompatActivity {

    private SeekBar rangeBar;
    private TextView rangeProgress;
    private  TextView rangeInfo;

    FusedLocationProviderClient client;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        reference = FirebaseDatabase.getInstance().getReference("Geo-fence");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        seekBar();
        onStart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(SettingActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            return;
        }
        client.getLastLocation().addOnSuccessListener(SettingActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location!=null){
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    reference.child(firebaseUser.getUid()).child("Latitude").setValue(lat);
                    reference.child(firebaseUser.getUid()).child("Longitude").setValue(lng);
                }
            }
        });
    }

    public void seekBar(){

        rangeBar = findViewById(R.id.seekBar);
        rangeProgress = findViewById(R.id.seekBarPosition);
        rangeInfo = findViewById(R.id.seekBarInfo);

        rangeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progressValue;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                rangeProgress.setText(rangeBar.getProgress()+"/"+rangeBar.getMax());

                if (rangeBar.getProgress() <=25){
                    rangeInfo.setText("10km from here");
                    reference.child(firebaseUser.getUid()).child("Radius").setValue(10);

                }else if (rangeBar.getProgress() <=50){
                    rangeInfo.setText("25km from here");
                    reference.child(firebaseUser.getUid()).child("Radius").setValue(25);

                }else if (rangeBar.getProgress() <=75) {
                    rangeInfo.setText("50km from here");
                    reference.child(firebaseUser.getUid()).child("Radius").setValue(50);

                }else if (rangeBar.getProgress() <=100) {
                    rangeInfo.setText("Entire Malaysia");
                    reference.child(firebaseUser.getUid()).child("Radius").setValue(500);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                rangeProgress.setText(rangeBar.getProgress()+"/"+rangeBar.getMax());
            }
        });
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},1);
    }
}
