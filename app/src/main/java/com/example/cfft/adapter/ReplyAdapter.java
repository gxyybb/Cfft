package com.example.cfft.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.R;
import com.example.cfft.enity.Reply;

import java.util.List;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private List<Reply> replyList;

    public void setReplyList(List<Reply> replyList) {
        this.replyList = replyList;
        notifyDataSetChanged();
    }

    @Override
    public ReplyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_item_layout, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReplyViewHolder holder, int position) {
        Reply reply = replyList.get(position);
        holder.usernameTextView.setText(reply.getUsername());
        holder.contentTextView.setText(reply.getContent());
        holder.publishTimeTextView.setText(reply.getPublishTime());
        // 设置用户头像...
    }

    @Override
    public int getItemCount() {
        return replyList != null ? replyList.size() : 0;
    }

    // 创建ViewHolder来保存视图组件的引用
    static class ReplyViewHolder extends RecyclerView.ViewHolder {
        ImageView userImageView;
        TextView usernameTextView;
        TextView replyedUsernameTextView;
        TextView contentTextView;
        TextView publishTimeTextView;

        public ReplyViewHolder(View itemView) {
            super(itemView);
            userImageView = itemView.findViewById(R.id.replyUserImageView);
            usernameTextView = itemView.findViewById(R.id.replyUsernameTextViewTextView);
            replyedUsernameTextView = itemView.findViewById(R.id.replyedUsernameTextView);
            contentTextView = itemView.findViewById(R.id.replyContentTextView);
            publishTimeTextView = itemView.findViewById(R.id.replyPublishTimeTextView);
        }
    }
}