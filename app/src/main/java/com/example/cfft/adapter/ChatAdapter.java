package com.example.cfft.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.ChatMessage;
import com.example.cfft.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<com.example.cfft.adapter.ChatAdapter.ViewHolder> {

    private List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public com.example.cfft.adapter.ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ChatMessage.TYPE_INCOMING) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outgoing_message, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incoming_message, parent, false);
        }
        return new com.example.cfft.adapter.ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull com.example.cfft.adapter.ChatAdapter.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        holder.messageTextView.setText(message.getText());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getType();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.textViewMessage);
        }
    }
}

