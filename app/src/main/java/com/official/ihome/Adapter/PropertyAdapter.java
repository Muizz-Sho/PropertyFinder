package com.official.ihome.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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
import com.official.ihome.Model.Circle;
import com.official.ihome.Model.Property;
import com.official.ihome.Model.User;
import com.official.ihome.PropertyActivity;
import com.official.ihome.R;

import java.util.ArrayList;
import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyAdapterViewHolder> implements Filterable {

    Context context;
    ArrayList<Property> arrayList;
    ArrayList<Property> arrayListFiltered;

    FirebaseUser firebaseUser =  FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Geo-fence").child(firebaseUser.getUid());

    public PropertyAdapter(Context context, ArrayList<Property> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.arrayListFiltered = new ArrayList<>(arrayList);
    }

    @NonNull
    @Override
    public PropertyAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout,parent,false);
        PropertyAdapterViewHolder viewHolder = new PropertyAdapterViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PropertyAdapterViewHolder holder, final int position) {

        final Property property = arrayList.get(position);
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
        holder.sale.setText("For "+property.getSale());
        holder.price.setText("RM"+property.getPrice());
        holder.type.setText("per "+property.getType());
        holder.detail.setText(property.getDetail());
        if (property.getImageUrl().equals("default")){
            holder.image.setImageResource(R.drawable.noimage);
        }else {
            Glide.with(context).load(property.getImageUrl()).into(holder.image);
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return propertyFilter;
    }

    private Filter propertyFilter = new Filter(){

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Property> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0){
                filteredList.addAll(arrayListFiltered);
            }else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Property item : arrayListFiltered){
                    if (item.getAddress().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            arrayList.clear();
            arrayList.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };

    class PropertyAdapterViewHolder extends RecyclerView.ViewHolder{

       TextView name,sale,detail,price,type;
       ImageView image;

       PropertyAdapterViewHolder(@NonNull View itemView) {
           super(itemView);

           name = itemView.findViewById(R.id.list_Name);
           sale = itemView.findViewById(R.id.list_Sale);
           detail = itemView.findViewById(R.id.list_Detail);
           image = itemView.findViewById(R.id.list_Image);
           price = itemView.findViewById(R.id.list_price);
           type = itemView.findViewById(R.id.list_type);
       }

   }
}
