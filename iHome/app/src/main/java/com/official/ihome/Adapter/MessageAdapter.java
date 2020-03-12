package com.official.ihome.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.official.ihome.Model.Chat;
import com.official.ihome.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Chat> chatList;

    private static final int MSG_TYPE_RECEIVED = 0;
    private static final int MSG_TYPE_SEND = 1;

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public MessageAdapter(Context context, List<Chat> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_SEND){
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_right,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_left,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Chat chat = chatList.get(position);
        holder.show_message.setText(chat.getMessage());

        //user image under here
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView show_message;

        private ViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.text_message_body);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_SEND;
        }else
            return MSG_TYPE_RECEIVED;
    }
}
