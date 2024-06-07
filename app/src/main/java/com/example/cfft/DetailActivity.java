package com.example.cfft;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.adapter.CommentAdapter;
import com.example.cfft.adapter.ReplyAdapter;
import com.example.cfft.enity.CommentVO;
import com.example.cfft.enity.CommunityItem;
import com.example.cfft.enity.Reply;
import com.example.cfft.enity.ResultVO;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity {

    // 声明适配器和评论列表
    // 声明 RecyclerView 和适配器
    private RecyclerView mRecyclerView;
    private CommentAdapter mAdapter;
    private ImageView imageView;
    private TextView lickText;

    private CommunityItem item;
    private String token;
    private BottomSheetDialog bottomSheetDialog;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bottomSheetDialog = new BottomSheetDialog(this);
        setContentView(R.layout.activity_detail);

        // 获取从上一个 Activity 传递过来的数据
        item = (CommunityItem) getIntent().getSerializableExtra("itemData");
        token =  getIntent().getStringExtra("token");
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout likeTextView = findViewById(R.id.like);
//        commentEditText = findViewById(R.id.commentEditText);
        lickText = findViewById(R.id.likeCountTextView);
//        Button submitCommentButton = findViewById(R.id.submitCommentButton);
        imageView = findViewById(R.id.commentImageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    openBottomSheet();

            }
        });


        // 创建并设置适配器
//        replyAdapter = new ReplyAdapter();
//        replyRecyclerView.setAdapter(replyAdapter);

//        // 添加示例数据到回复列表
//        List<Reply> replyList = new ArrayList<>();
//        replyList.add(new Reply("User1", "Content1", "1 hour ago"));
//        replyList.add(new Reply("User2", "Content2", "2 hours ago"));
//        // 添加更多回复...

        // 更新适配器的数据集
//        replyAdapter.setReplyList(replyList);
        likeTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                updateLikeCountOnServer(item.getPostId(),likeTextView);
            }
        });

            fetchCommunityItemFromServer(item.getPostId());

            // 请求评论数据
            fetchCommentsFromServer(item.getPostId());
            // 设置发送评论按钮点击事件

        }

    private void openBottomSheet() {
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.dialog_reply, null);
        EditText replyEditText = bottomSheetView.findViewById(R.id.replyEditText);
        Button sendButton = bottomSheetView.findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String replyEditText1 = replyEditText.getText().toString();
                if (!replyEditText1.isEmpty()) {
                    // 发送评论到服务器

                    sendCommentToServer(item.getPostId(), replyEditText1);
                    bottomSheetDialog.dismiss();
                } else {
                    Toast.makeText(DetailActivity.this, "评论内容不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    private void fetchCommunityItemFromServer(Integer postId) {
        OkHttpClient client = new OkHttpClient();
        String url = UrlConstants.POST_DETAIL_URL + postId; // 替换为实际的服务器地址和端点

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // 在子线程中解析 JSON 数据
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 使用 Gson 解析 JSON 数据
                            Gson gson = new Gson();
                            ResultVO resultVO = gson.fromJson(responseData, ResultVO.class);
                            if (resultVO.getCode() == 200) {
                                // 解析成功，获取数据并填充视图
                                CommunityItem item = gson.fromJson(gson.toJson(resultVO.getData()), CommunityItem.class);
                                if (item != null) {
                                    // 打印item
                                    Log.d("CommunityItem", String.valueOf(item));
                                    // 填充视图
                                    fillViews(item);
                                    // 请求评论数据
                                    fetchCommentsFromServer(item.getPostId());
                                }
                            } else {
                                // 解析失败，处理异常情况
                                Log.e("NetworkResponse", "Request failed with code: " + resultVO.getCode());
                            }
                        }
                    });
                } else {
                    // 请求失败
                    Log.e("NetworkResponse", "Request failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 请求失败
                e.printStackTrace();
                Log.e("NetworkResponse", "Request failed: " + e.getMessage());
            }
        });
    }

    // 更新服务器上的点赞数
    private void updateLikeCountOnServer(int postId, final LinearLayout likeTextView) {
        OkHttpClient client = new OkHttpClient();
        String token = getIntent().getStringExtra("token");
        // 构建请求体
        RequestBody formBody = new FormBody.Builder()
                .add("postId", String.valueOf(postId))
                .add("token", token)
                .build();

        // 创建请求
        Request request = new Request.Builder()
                .url("http://101.200.79.152:8080/post/like") // 替换为您的服务器端点
                .post(formBody)
                .build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        if (!jsonObject.isNull("data")) {
                            final int updatedLikeCount = jsonObject.getInt("data");
                            // 更新 UI 上的点赞数
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 更新点赞文本视图
                                    lickText.setText(String.valueOf(updatedLikeCount));
                                }
                            });
                        } else {
                            Log.e("UpdateLikeCount", "JSON object has null or missing 'data' field");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    // 处理请求失败或其他错误情况
                    Log.e("UpdateLikeCount", "Request failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                // 处理请求失败，如果需要的话
                Log.e("UpdateLikeCount", "Request failed: " + e.getMessage());
            }
        });}
    private void fillViews(CommunityItem item) {
        // 找到布局中的视图
        TextView titleTextView = findViewById(R.id.detailTitleTextView);
        TextView timeTextView = findViewById(R.id.publishTextView);
        TextView descriptionTextView = findViewById(R.id.detailDescriptionTextView);
        LinearLayout imageLayout = findViewById(R.id.imageLayout1);
        ImageView userAvatarImageView = findViewById(R.id.userAvatarImageView);
        TextView usernameTextView = findViewById(R.id.usernameTextView);
        TextView contentTextView = findViewById(R.id.contentTextView);
        ImageView imageResourceImageView = findViewById(R.id.imageResourceImageView);
        TextView lickCountTextView = findViewById(R.id.likeCountTextView);
        TextView commentCountTextView = findViewById(R.id.commentCountTextView);


        // 设置标题和描述
        titleTextView.setText(item.getTitle());
        // 原始格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String publishTime = sdf.format(item.getPublishTime());
        timeTextView.setText(publishTime);

        descriptionTextView.setText(item.getContent());

        // 使用 Picasso 加载图片
        List<String> imageUrls = item.getImg();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                ImageView imageView = new ImageView(this);
                int imageSize = (int) (150 * this.getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
                layoutParams.setMargins(0, 0, 8, 0); // 设置图片之间的间距
                imageView.setLayoutParams(layoutParams);

                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                // 使用Picasso加载图片，并裁剪为方形
                Picasso.get()
                        .load(imageUrl)
                        .resize(imageSize, imageSize) // 设置宽高
                        .centerCrop() // 中心裁剪
                        .into(imageView);

                imageLayout.addView(imageView);

                // 设置点击事件
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImageDialog(imageUrl);
                    }
                });
            }
        }

        // 设置用户头像
        Picasso.get().load(item.getUserImg()).transform(new CircleTransform()).into(userAvatarImageView);

        // 设置用户名
        usernameTextView.setText(item.getUserName());

        // 设置内容
        contentTextView.setText(item.getContent());

        // 设置图片资源
        imageResourceImageView.setImageResource(item.getImageResource());
        lickCountTextView.setText(String.valueOf(item.getLikeCount()));
        commentCountTextView.setText(String.valueOf(item.getCommentCount()));

    }

    // 显示图片的对话框方法
    private void showImageDialog(String imageUrl) {
        Dialog dialog = new Dialog(this, R.style.FullScreenDialog);
        dialog.setContentView(R.layout.dialog_image);

        ImageView imageView = dialog.findViewById(R.id.dialogImageView);
        Picasso.get().load(imageUrl).into(imageView);

        // 点击对话框的任意地方关闭对话框
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void fetchCommentsFromServer(Integer postId) {
        OkHttpClient client = new OkHttpClient();
        String url = UrlConstants.COMMENT_LIST_URL + postId; // 替换为实际的服务器地址和端点

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // 在子线程中解析 JSON 数据
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 使用 Gson 解析 JSON 数据
                            Gson gson = new Gson();
                            ResultVO resultVO = gson.fromJson(responseData, ResultVO.class);

// 将 Object 类型的数据转换为 List<CommentVO>
                            Object data = resultVO.getData();

// 将 Object 类型的数据转换为 List<CommentVO>
                            List<CommentVO> commentList = new ArrayList<>();
                            Map<Integer, CommentVO> commentMap = new HashMap<>();

                            if (data instanceof List) {
                                for (Object obj : (List<?>) data) {
                                    if (obj instanceof LinkedTreeMap) {
                                        LinkedTreeMap<String, Object> map = (LinkedTreeMap<String, Object>) obj;
                                        CommentVO commentVO = new CommentVO();
                                        commentVO.setCommentId(((Double) map.get("commentId")).intValue());
                                        commentVO.setContent((String) map.get("content"));
                                        String publishTimeString = (String) map.get("publishTime");
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                                        try {
                                            Date publishTime = dateFormat.parse(publishTimeString);
                                            commentVO.setPublishTime(publishTime);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        commentVO.setUserImage((String) map.get("userImage"));
                                        commentVO.setUsername((String) map.get("username"));
                                        commentVO.setPostId(((Double) map.get("typeId")).intValue());
                                        Object parentCommentIdObj = map.get("parentCommentId");
                                        if (parentCommentIdObj != null) {
                                            commentVO.setParentCommentId(((Double) parentCommentIdObj).intValue());
                                        } else {
                                            // 如果允许 parentCommentId 为 null，则在这种情况下设置为 null
                                            commentVO.setParentCommentId(null); // 确保 ParentCommentId 的类型允许 null 值
                                        }

                                        commentVO.setLikeCount(((Double) map.get("likeCount")).intValue());
                                        commentVO.setReplyCount(((Double) map.get("replyCount")).intValue());
                                        commentVO.setReplies(new ArrayList<>()); // 初始化子评论列表

                                        commentMap.put(commentVO.getCommentId(), commentVO);
                                    }
                                }

                                for (CommentVO comment : commentMap.values()) {
                                    if (comment.getParentCommentId() == null) { // 顶层评论
                                        commentList.add(comment);
                                    } else { // 子评论
                                        CommentVO parentComment = commentMap.get(comment.getParentCommentId());
                                        if (parentComment != null) {
                                            parentComment.getReplies().add(comment);
                                        }
                                    }
                                }
                            }


                            // 初始化适配器并绑定到 ListView


// 找到 RecyclerView
                            mRecyclerView = findViewById(R.id.commentRecyclerView);
                            mAdapter = new CommentAdapter(DetailActivity.this, commentList,item.getPostId(),token);
                            mRecyclerView.setAdapter(mAdapter);
//// 获取每个评论的回复数据并更新 RecyclerView
//                            fetchRepliesForComments(commentList);
                        }
                    });
                } else {
                    // 请求失败
                }
            }


            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 请求失败
            }
        });
    }

    // 更新 RecyclerView 中的回复数据
    private void updateReplyList(CommentVO comment, List<Reply> replyList) {
        // 找到评论对应的 RecyclerView ViewHolder
        RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForItemId(comment.getCommentId());

        // 更新评论对应的回复列表数据
        if (viewHolder instanceof CommentAdapter.CommentViewHolder) {
            CommentAdapter.CommentViewHolder commentViewHolder = (CommentAdapter.CommentViewHolder) viewHolder;
//            commentViewHolder.setReplyList(replyList);
        }
    }
    private void sendCommentToServer(Integer postId, String commentText) {
        OkHttpClient client = new OkHttpClient();
        String url = UrlConstants.SEND_COMMENT_URL; // 替换为实际的服务器地址和端点
        String token = getIntent().getStringExtra("token");

        RequestBody requestBody = new FormBody.Builder()
                .add("token",token)
                .add("typeId", String.valueOf(postId))
                .add("content", commentText)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 评论成功添加到服务器，重新获取数据
                    fetchCommentsFromServer(postId);
                    // 清空评论输入框
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            commentEditText.setText("");
//                        }
//                    });
                } else {
                    // 请求失败
                    // 获取响应状态码
                    int statusCode = response.code();
                    if (statusCode == 500) {
                        // 请求失败，提示用户
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 在这里弹出一个提示框告诉用户请求失败了
                                // 代码省略...
                            }
                        });
                    }
                    Log.e("NetworkResponse", "Request failed with code: " + statusCode);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 请求失败
                e.printStackTrace();
                Log.e("NetworkResponse", "Request failed: " + e.getMessage());
            }
        });

    }
}

