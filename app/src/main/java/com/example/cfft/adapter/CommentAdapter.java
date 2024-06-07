package com.example.cfft.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.CircleTransform;
import com.example.cfft.R;
import com.example.cfft.enity.CommentVO;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context mContext;
    private List<CommentVO> mComments;
    private int postId;
    private String token;
    private boolean isReplyVisible = false;
    private BottomSheetDialog bottomSheetDialog1;
    public CommentAdapter(Context context, List<CommentVO> commentList, int postId,String token) {
        this.mContext = context;
        this.mComments = commentList;
        this.postId = postId;
        this.token = token;
        bottomSheetDialog1 = new BottomSheetDialog(mContext);
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
        private  TextView toggleRepliesTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentUserImageView = itemView.findViewById(R.id.userImageView);
            commentContentTextView = itemView.findViewById(R.id.commentContentTextView);
            commentUsernameTextView = itemView.findViewById(R.id.commentUsernameTextView);
            commentPublishTimeTextView = itemView.findViewById(R.id.commentPublishTimeTextView);
            commentLikeCountTextView = itemView.findViewById(R.id.commentLikeCountTextView);
            commentReplyCountTextView = itemView.findViewById(R.id.commentReplyCountTextView);
            replyRecyclerView = itemView.findViewById(R.id.replyRecyclerView);
             toggleRepliesTextView = itemView.findViewById(R.id.toggleRepliesTextView);
            replyRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            replyAdapter = new ReplyAdapter(mContext, new ArrayList<>());
            replyRecyclerView.setAdapter(replyAdapter);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        CommentVO comment = mComments.get(position);
                        openBottomSheet1(comment.getCommentId());
                    }
                }
            });


            toggleRepliesTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        CommentVO comment = mComments.get(position);
                        fetchReplies(postId,comment.getCommentId());
                    }
                    if (isReplyVisible) {

                        replyRecyclerView.setVisibility(View.GONE);
                        toggleRepliesTextView.setText("查看回复");
                    } else {
                        replyRecyclerView.setVisibility(View.VISIBLE);
                        toggleRepliesTextView.setText("收起回复");
                    }
                    isReplyVisible = !isReplyVisible;
                }
            });

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
        private void openBottomSheet1(int commentId) {
            View bottomSheetView = LayoutInflater.from(mContext).inflate(R.layout.dialog_reply, null);
            EditText replyEditText = bottomSheetView.findViewById(R.id.replyEditText);
            Button sendButton = bottomSheetView.findViewById(R.id.sendButton);

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String replyContent = replyEditText.getText().toString().trim();
                    if (!replyContent.isEmpty()) {
                        sendReply(commentId, replyContent);
                        bottomSheetDialog1.dismiss();
                    } else {
                        Toast.makeText(mContext, "回复内容不能为空", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            bottomSheetDialog1.setContentView(bottomSheetView);
            bottomSheetDialog1.show();
        }

        private void sendReply(int commentId, String replyContent) {
            OkHttpClient client = new OkHttpClient();

            String url = "http://101.200.79.152:8080/comment/comment"; // 替换为你的API URL


            RequestBody body = new FormBody.Builder()
                    .add("commentId", String.valueOf(commentId))
                    .add("content", replyContent)
                    .add("token", token)
                    .add("typeId", String.valueOf(postId))
                    // 如果需要，可以添加其他必要的字段
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // 处理成功响应
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "回复已发送", Toast.LENGTH_SHORT).show();
                                // 可选：刷新回复列表
                                fetchReplies(postId, commentId);
                            }
                        });
                    } else {
                        // 处理失败响应
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "发送回复失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }

        private void fetchReplies(int postId,int commentId) {
            OkHttpClient client = new OkHttpClient();

            String url = "http://101.200.79.152:8080/comment/comments/" + commentId ; // 替换为你的API URL

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // 处理失败情况
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            List<CommentVO> replies = new ArrayList<>();
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject commentObject = dataArray.getJSONObject(i);
                                // 解析评论对象并添加到列表中
                                CommentVO commentVO = parseComment(commentObject);
                                replies.add(commentVO);
                            }

                            // 更新 UI
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    replyAdapter.setReplyList(replies);
                                    replyAdapter.notifyDataSetChanged();
                                }
                            });
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                private CommentVO parseComment(JSONObject commentObject) throws JSONException, ParseException {
                    int commentId = commentObject.getInt("commentId");
                    String content = commentObject.getString("content");
                    String publishTimeString = commentObject.getString("publishTime");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

                        Date publishTime = dateFormat.parse(publishTimeString);


//                    int userId = commentObject.getInt("userId");
                    int postId = commentObject.getInt("typeId");
                    String userImg = commentObject.getString("userImage");
                    String username = commentObject.getString("username");
                    int parentCommentId = commentObject.getInt("parentCommentId");
                    int likeCount = commentObject.getInt("likeCount");
                    int replyCount = commentObject.getInt("replyCount");

                    // 创建 CommentVO 对象并返回
                    return new CommentVO(userImg,commentId, content, publishTime,username, postId, parentCommentId, likeCount, replyCount);
                }

            });

        }
    }
}
