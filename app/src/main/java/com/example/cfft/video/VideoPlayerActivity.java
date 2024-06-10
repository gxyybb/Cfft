package com.example.cfft.video;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.cfft.DetailActivity;
import com.example.cfft.R;
import com.example.cfft.UrlConstants;
import com.example.cfft.adapter.CommentAdapter;
import com.example.cfft.enity.CommentVO;
import com.example.cfft.enity.ResultVO;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
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

public class VideoPlayerActivity extends AppCompatActivity {
    private VideoData videoData;
    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private TextView textViewTitle;
    private TextView textViewDescription;
    private TextView textViewPublishTime;
    private boolean isFullScreen = false; // 添加 isFullScreen 变量并初始化为 false
    private long lastClickTime = 0; // 记录上次点击的时间
    private BottomSheetDialog bottomSheetDialog;
    private String token;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        bottomSheetDialog = new BottomSheetDialog(this);
        // 获取视频 URL 和 VideoData 对象
        Intent intent = getIntent();
        String videoUrl = intent.getStringExtra("videoUrl");
        long uploadTimeMillis = intent.getLongExtra("uploadtime", 0);
        Date uploadTime = new Date(uploadTimeMillis);
        videoData = intent.getParcelableExtra("videoData");
         token = intent.getStringExtra("token");
        ArrayList<CommentVO> commentList = (ArrayList<CommentVO>) intent.getSerializableExtra("commentList");
        VideoView videoView = findViewById(R.id.video);
//        MediaController mediaController = new MediaController(this);
//        mediaController.setAnchorView(videoView);
//        videoView.setMediaController(mediaController);


        // 设置视频路径

        // 设置视频标题、描述和发布时间
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewPublishTime = findViewById(R.id.textViewPublishTime);
        if (videoData != null) {
            textViewTitle.setText(videoData.getTitle());
            textViewDescription.setText(videoData.getDescription());
            textViewPublishTime.setText(formatPublishTime(uploadTime));
        } else {
            textViewTitle.setText("Title");
            textViewDescription.setText("Description");
            textViewPublishTime.setText("Publish Time");
        }

        // 初始化 RecyclerView
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));

        // 设置评论列表适配器
        List<CommentVO> comments = commentList;
        commentAdapter = new CommentAdapter(this, comments, videoData.getVideoid(), token);
        recyclerViewComments.setAdapter(commentAdapter);
        // 添加点击标题区域的监听器
        textViewTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("VideoPlayerActivity", "Title clicked");
                openBottomSheet();
            }
        });
        textViewDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("VideoPlayerActivity", "Title clicked");
                openBottomSheet();
            }
        });
//        videoView.setVideoPath(videoUrl);
        // 延迟设置 MediaController 以确保 VideoView 完全初始化
        if (videoView != null) {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.setVideoPath(videoUrl);
            videoView.start();

            // 显示 MediaController
            videoView.post(new Runnable() {
                @Override
                public void run() {
                    mediaController.show(0);
                }
            });
        } else {
            Log.e("MainActivity", "VideoView is null");
        }

        // 添加视频播放完成监听器
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 播放完成后退出全屏模式
                exitFullScreen();
            }
        });
        // 添加全屏切换监听器
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取当前点击的时间
                long now = System.currentTimeMillis();
                // 判断两次点击的时间间隔
                if (now - lastClickTime < 500) {
                    // 双击事件，切换全屏状态
                    if (isFullScreen) {
                        exitFullScreen();
                    } else {
                        enterFullScreen();
                    }
                    isFullScreen = !isFullScreen;
                }
                lastClickTime = now;
            }
        });
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

                    sendCommentToServer(videoData.getVideoid(), replyEditText1);
                    bottomSheetDialog.dismiss();
                } else {
                    Toast.makeText(VideoPlayerActivity.this, "评论内容不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    // 格式化发布时间的方法
    private String formatPublishTime(Date publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(publishTime);
    }

    // 添加退出全屏方法
    private void exitFullScreen() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().show(); // 显示标题栏
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE); // 显示系统UI
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 切换为竖屏模式
    }

    // 添加进入全屏方法
    private void enterFullScreen() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // 隐藏标题栏
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        ); // 隐藏系统UI
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // 切换为横屏模式
    }
    private void sendCommentToServer(Integer postId, String commentText) {
        OkHttpClient client = new OkHttpClient();
        String url = UrlConstants.SEND_VIDEO_COMMENT_URL; // 替换为实际的服务器地址和端点
        String token = getIntent().getStringExtra("token");

        RequestBody requestBody = new FormBody.Builder()
                .add("token",token)
                .add("videoId", String.valueOf(postId))
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
                            recyclerViewComments = findViewById(R.id.commentRecyclerView);
                            commentAdapter = new CommentAdapter(VideoPlayerActivity.this, commentList,videoData.getVideoid(),token);
                            recyclerViewComments.setAdapter(commentAdapter);
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

}

