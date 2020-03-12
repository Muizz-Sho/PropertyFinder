package com.official.ihome.Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.official.ihome.MainActivity;
import com.official.ihome.Model.Analytic;
import com.official.ihome.Model.Property;
import com.official.ihome.PropertyActivity;
import com.official.ihome.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class OwnedAdapter extends RecyclerView.Adapter<OwnedAdapter.OwnedAdapterViewHolder>{

    Context context;
    ArrayList<Property> arrayList;

    public OwnedAdapter(Context context, ArrayList<Property> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public OwnedAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.owned_layout,parent,false);
        OwnedAdapterViewHolder viewHolder = new OwnedAdapterViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull OwnedAdapterViewHolder holder, int position) {

        final Property property = arrayList.get(position);
        if (property.getImageUrl().equals("default")){
            holder.image.setImageResource(R.drawable.noimage);
        }else {
            Glide.with(context).load(property.getImageUrl()).into(holder.image);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent redirect = new Intent(context, PropertyActivity.class);
                redirect.putExtra("propertyID", property.getPropertyID());
                redirect.putExtra("ownerID",property.getOwnerID());
                context.startActivity(redirect);
            }
        });

        holder.name.setText(property.getName());
        holder.state.setText(property.getState());
        holder.date.setText(property.getValidity());
        holder.action_extend.setVisibility(View.VISIBLE);

        holder.action_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Property Deletion");
                builder.setMessage("Are you sure want to proceed?");

                builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RemoveProperty(property.getPropertyID());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        holder.action_extend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtendProperty(property.getPropertyID());
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    class OwnedAdapterViewHolder extends RecyclerView.ViewHolder{

        TextView name,state,date;
        ImageView image;
        Button action;
        ImageButton action_extend,action_remove;

        public OwnedAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.list_Name);
            state = itemView.findViewById(R.id.list_State);
            date = itemView.findViewById(R.id.list_date);
            image = itemView.findViewById(R.id.list_Image);
            action = itemView.findViewById(R.id.list_action);
            action_extend = itemView.findViewById(R.id.action_extend);
            action_remove = itemView.findViewById(R.id.action_remove);

        }
    }

    public void ExtendProperty(final String propertyID){
        final ProgressDialog pd;
        pd = new ProgressDialog(context);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Property").child(firebaseUser.getUid()).child(propertyID);

        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("validity",currentDate);

        pd.setMessage("Advertising Property");
        pd.show();

        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    pd.dismiss();
                    Toast.makeText(context,"Data Updated!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        final DateFormat analyticsDate = new SimpleDateFormat("yyyy-MM-dd");
        final String updateAnalytics = analyticsDate.format(Calendar.getInstance().getTime());
        final DatabaseReference refAnalytics = FirebaseDatabase.getInstance().getReference("Analytics").child(updateAnalytics).push();
        refAnalytics.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Analytic analytic = new Analytic(propertyID);
                refAnalytics.setValue(analytic).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(context,"Data Renewed!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void RemoveProperty(String propertyID){
        final ProgressDialog pd;
        pd = new ProgressDialog(context);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Property").child(firebaseUser.getUid()).child(propertyID);

        reference.removeValue();
    }
}


