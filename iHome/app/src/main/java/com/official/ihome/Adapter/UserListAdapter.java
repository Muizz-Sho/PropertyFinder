package com.official.ihome.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.official.ihome.MessageActivity;
import com.official.ihome.Model.User;
import com.official.ihome.R;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private Context context;
    private List<User> users;

    public UserListAdapter(Context context,List<User> users){
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chatlist_layout,parent,false);
        return new UserListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = users.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageUrl().equals("default")){
            holder.profile_image.setImageResource(R.drawable.placeholder);
        }else {
            Glide.with(context).load(user.getImageUrl()).into(holder.profile_image);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent reply = new Intent(context, MessageActivity.class);
                reply.putExtra("userid",user.getId());
                reply.putExtra("username",user.getUsername());
                context.startActivity(reply);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView username;
        ImageView profile_image;

        public ViewHolder(View itemView){
            super(itemView);
            username = itemView.findViewById(R.id.chatList_name);
            profile_image = itemView.findViewById(R.id.chatList_Image);
        }
    }

    @Override
    public long getItemId(int position) {
        return users.size();
    }
}
