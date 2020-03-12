package com.official.ihome.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.official.ihome.HelperActivity.HttpDataHandler;
import com.official.ihome.MainActivity;
import com.official.ihome.Model.Analytic;
import com.official.ihome.Model.Property;
import com.official.ihome.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Promote extends Fragment {

    ImageView property_imageUrl;
    ImageButton enable;
    EditText property_name,property_address,property_detail,property_state,property_price;
    Button submit;
    TextView latitude,longitude;
    FirebaseUser firebaseUser;
    DatabaseReference reference,analytics;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat analyticDate = new SimpleDateFormat("yyyy-MM-dd");

    Spinner propTyType,propTyFor;

    StorageReference storageReference;
    String photoUrl = "default";

    public Promote() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_promote, container, false);
        storageReference = FirebaseStorage.getInstance().getReference("property");

        property_name = view.findViewById(R.id.property_nameBox);
        property_address = view.findViewById(R.id.property_addressBox);
        property_detail = view.findViewById(R.id.property_detailBox);
        property_state = view.findViewById(R.id.property_stateBox);
        property_imageUrl = view.findViewById(R.id.property_imageUrl);
        property_price = view.findViewById(R.id.property_price);

        submit = view.findViewById(R.id.submit);
        enable = view.findViewById(R.id.enableImg);
        latitude = view.findViewById(R.id.Property_latitude);
        longitude = view.findViewById(R.id.Property_longitude);

        final String validity = dateFormat.format(Calendar.getInstance().getTime());

        propTyType = view.findViewById(R.id.property_typeSpinner);
        ArrayAdapter<CharSequence> spinnerType = ArrayAdapter.createFromResource(view.getContext(),R.array.array_type,android.R.layout.simple_spinner_item);
        spinnerType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        propTyType.setAdapter(spinnerType);

        propTyFor = view.findViewById(R.id.property_forSpinner);
        ArrayAdapter<CharSequence> spinnerFor = ArrayAdapter.createFromResource(view.getContext(),R.array.array_sale,android.R.layout.simple_spinner_item);
        spinnerFor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        propTyFor.setAdapter(spinnerFor);

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ProgressDialog dialog = new ProgressDialog(getContext());

                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    reference = FirebaseDatabase.getInstance().getReference("Property").child(firebaseUser.getUid()).push();

                    final String propertyID = reference.getKey();
                    String OwnerID = firebaseUser.getUid();
                    String name = property_name.getText().toString();
                    String address = property_address.getText().toString();
                    String state = property_state.getText().toString();
                    String detail = property_detail.getText().toString();
                    String lat = latitude.getText().toString();
                    String lng = longitude.getText().toString();
                    String price = property_price.getText().toString();
                    String type = propTyType.getSelectedItem().toString();
                    String sale = propTyFor.getSelectedItem().toString();

                    Property property = new Property(propertyID,name,address,state,detail,photoUrl,OwnerID,lat,lng,validity,price,type,sale);

                    if (TextUtils.isEmpty(name)){
                        Toast.makeText(getActivity(),"Enter Property Name!",Toast.LENGTH_SHORT).show();
                        return;
                    }else if (TextUtils.isEmpty(address)){
                        Toast.makeText(getActivity(),"Enter Property Address!",Toast.LENGTH_SHORT).show();
                        return;
                    }else if (TextUtils.isEmpty(detail)){
                        Toast.makeText(getActivity(),"Enter Property Details!",Toast.LENGTH_SHORT).show();
                        return;
                    }else if (TextUtils.isEmpty(state)){
                        Toast.makeText(getActivity(),"Enter Property State/Province!",Toast.LENGTH_SHORT).show();
                        return;
                    }else if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lng)){
                        Toast.makeText(getActivity(),"Please press get property location!",Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        dialog.setMessage("Getting Location....");
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();

                        reference.setValue(property)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            final String recordDate = analyticDate.format(Calendar.getInstance().getTime());
                                            analytics = FirebaseDatabase.getInstance().getReference("Analytics").child(recordDate).push();
                                            Analytic analytic = new Analytic(propertyID);
                                            analytics.setValue(analytic).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        dialog.isShowing();
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(),"Advertisement published",Toast.LENGTH_LONG).show();
                                                        Intent toHome = new Intent(getContext(), MainActivity.class);
                                                        startActivity(toHome);
                                                    }
                                                }
                                            });

                                        }else {
                                            Toast.makeText(getContext(),"Failed! Something went wrong!",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                }
            });

        enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = property_address.getText().toString();
                String state = property_state.getText().toString();
                if (TextUtils.isEmpty(address)){
                    Toast.makeText(getActivity(),"Enter Property Address!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(state)){
                    Toast.makeText(getActivity(),"Enter Property State/Province!",Toast.LENGTH_SHORT).show();
                    return;
                }
                new Promote.GetCoordinates().execute(address.replace(" ","+"));
                enable.setVisibility(View.GONE);
                submit.setClickable(true);
            }
        });


        return view;
    }

    public class GetCoordinates extends AsyncTask<String,Void,String> {
        ProgressDialog dialog = new ProgressDialog(getContext());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait....");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response;
            try {
                String address = strings[0];
                HttpDataHandler http = new HttpDataHandler();
                String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=AIzaSyCGzRvi11XGIMWSiyjmeBDI-7XBPNncq-M", address);
                response = http.getHTTPData(url);
                return response;
            } catch (Exception ex) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);

                String lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lat").toString();
                String lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lng").toString();

                String addressHolder = ((JSONArray) jsonObject.get("results")).getJSONObject(0).get("formatted_address").toString();
                property_address.setText(addressHolder);
                latitude.setText(lat);
                longitude.setText(lng);


                if (dialog.isShowing())
                    dialog.dismiss();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}