package com.example.cfft.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.CircleTransform;
import com.example.cfft.R;
import com.example.cfft.enity.CommentVO;
import com.example.cfft.enity.Reply;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private Context mContext;
    private List<CommentVO> mReplies;

    public ReplyAdapter(Context context, List<CommentVO> replies) {
        mContext = context;
        mReplies = replies;
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.reply_item, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        CommentVO reply = mReplies.get(position);
        holder.bind(reply);
    }

    @Override
    public int getItemCount() {
        return mReplies.size();
    }

    public void setReplyList(List<CommentVO> replies) {
        mReplies = replies;
        notifyDataSetChanged();
    }

    public class ReplyViewHolder extends RecyclerView.ViewHolder {
        private ImageView userImageView;
        private TextView usernameTextView;
        private TextView contentTextView;
        private TextView publishTimeTextView;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            userImageView = itemView.findViewById(R.id.userImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            publishTimeTextView = itemView.findViewById(R.id.publishTimeTextView);
        }

        public void bind(CommentVO reply) {
            Picasso.get()
                    .load(reply.getUserImage())
                    .error(R.drawable.img_1)
                    .transform(new CircleTransform())
                    .into(userImageView);
            usernameTextView.setText(reply.getUsername());
            contentTextView.setText(reply.getContent());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String publishTime = sdf.format(reply.getPublishTime());
            publishTimeTextView.setText(publishTime);
        }
    }
}
