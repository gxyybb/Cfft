package com.example.cfft.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.CircleTransform;
import com.example.cfft.R;
import com.example.cfft.enity.CommentVO;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context mContext;
    private List<CommentVO> mComments;

    public CommentAdapter(Context context, List<CommentVO> comments) {
        mContext = context;
        mComments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentVO comment = mComments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView commentUserImageView;
        private TextView commentContentTextView;
        private TextView commentUsernameTextView;
        private TextView commentPublishTimeTextView;
        private TextView commentLikeCountTextView;
        private TextView commentReplyCountTextView;
        private RecyclerView replyRecyclerView;
        private ReplyAdapter replyAdapter;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentUserImageView = itemView.findViewById(R.id.userImageView);
            commentContentTextView = itemView.findViewById(R.id.commentContentTextView);
            commentUsernameTextView = itemView.findViewById(R.id.commentUsernameTextView);
            commentPublishTimeTextView = itemView.findViewById(R.id.commentPublishTimeTextView);
            commentLikeCountTextView = itemView.findViewById(R.id.commentLikeCountTextView);
            commentReplyCountTextView = itemView.findViewById(R.id.commentReplyCountTextView);
            replyRecyclerView = itemView.findViewById(R.id.replyRecyclerView);

            replyRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            replyAdapter = new ReplyAdapter(mContext, new ArrayList<>());
            replyRecyclerView.setAdapter(replyAdapter);
        }

        public void bind(CommentVO comment) {
            Picasso.get()
                    .load(comment.getUserImage())
                    .error(R.drawable.img_1)
                    .transform(new CircleTransform())
                    .into(commentUserImageView);
            commentContentTextView.setText(comment.getContent());
            commentUsernameTextView.setText(comment.getUsername());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String publishTime = sdf.format(comment.getPublishTime());
            commentPublishTimeTextView.setText(publishTime);

            commentLikeCountTextView.setText(String.valueOf(comment.getLikeCount()));
            commentReplyCountTextView.setText(String.valueOf(comment.getReplyCount()));

            // 更新回复列表
            replyAdapter.setReplyList(comment.getReplies());
        }
    }
}
