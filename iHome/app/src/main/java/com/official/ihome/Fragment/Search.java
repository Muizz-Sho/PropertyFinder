package com.official.ihome.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.official.ihome.Adapter.PropertyAdapter;
import com.official.ihome.Model.Property;
import com.official.ihome.R;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Search extends Fragment {

    SearchView searchView;
    RecyclerView resultList;
    ArrayList<Property> arrayList;
    PropertyAdapter adapter;
    TextView empty;

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public Search() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        resultList = view.findViewById(R.id.searchList);
        resultList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        resultList.setLayoutManager(layoutManager);
        empty = view.findViewById(R.id.emptylist);

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference geoFenceUidRef = rootRef.child("Geo-fence").child(uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final double geoFenceLat = dataSnapshot.child("Latitude").getValue(Double.class);
                final double geoFenceLng = dataSnapshot.child("Longitude").getValue(Double.class);
                final double radius = dataSnapshot.child("Radius").getValue(Double.class);

                DatabaseReference propertyUidRef = rootRef.child("Property");
                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        arrayList = new ArrayList<>();
                        for(DataSnapshot key : dataSnapshot.getChildren()) {
                            for (DataSnapshot ds : key.getChildren()){
                                Property prop = ds.getValue(Property.class);
                                assert prop != null;
                                double propertyLat = Double.parseDouble(prop.getLatitude());
                                double propertyLng = Double.parseDouble(prop.getLongitude());
                                String propertyDate = prop.getValidity();

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

                                if (calculateDistanceInKilometer(geoFenceLat,geoFenceLng,propertyLat,propertyLng) <= radius){
                                    if (diff <= 7) //7 days availability
                                        arrayList.add(prop);

                                }
                            }
                        }
                        adapter = new PropertyAdapter(getContext(),arrayList);
                        resultList.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("property", databaseError.getMessage());
                    }
                };
                propertyUidRef.addValueEventListener(eventListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("geo", databaseError.getMessage());
            }
        };
        geoFenceUidRef.addValueEventListener(valueEventListener);

        searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter !=null){
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });

        return view;
    }

    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
    public double calculateDistanceInKilometer(double userLat, double userLng,double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (double) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
    }

    public static long getUnitBetweenDates(Date startDate, Date endDate, TimeUnit unit) {
        long timeDiff = endDate.getTime() - startDate.getTime();
        return unit.convert(timeDiff, TimeUnit.MILLISECONDS);
    }
}
