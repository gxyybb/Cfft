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

import com.example.cfft.R;
import com.example.cfft.UrlConstants;
import com.example.cfft.adapter.CommentAdapter;
import com.example.cfft.enity.CommentVO;
import com.example.cfft.enity.ResultVO;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.jetbrains.annotations.NotNull;

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
    private boolean isFullScreen = false;
    private long lastClickTime = 0;
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
        commentAdapter = new CommentAdapter(this, comments, videoData.getVideoid(), token,"video");
        recyclerViewComments.setAdapter(commentAdapter);

        // 添加点击标题和描述区域的监听器
        View.OnClickListener openBottomSheetListener = v -> openBottomSheet();
        textViewTitle.setOnClickListener(openBottomSheetListener);
        textViewDescription.setOnClickListener(openBottomSheetListener);

        // 设置视频路径并播放
        if (videoView != null) {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.setVideoPath(videoUrl);
            videoView.start();

            // 显示 MediaController
            videoView.post(() -> mediaController.show(0));

            // 添加视频播放完成监听器
            videoView.setOnCompletionListener(mp -> exitFullScreen());

            // 添加全屏切换监听器
            videoView.setOnClickListener(v -> {
                long now = System.currentTimeMillis();
                if (now - lastClickTime < 500) {
                    if (isFullScreen) {
                        exitFullScreen();
                    } else {
                        enterFullScreen();
                    }
                    isFullScreen = !isFullScreen;
                }
                lastClickTime = now;
            });
        } else {
            Log.e("MainActivity", "VideoView is null");
        }
        fetchCommentsFromServer(videoData.getVideoid());
    }

    private void openBottomSheet() {
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.dialog_reply, null);
        EditText replyEditText = bottomSheetView.findViewById(R.id.replyEditText);
        Button sendButton = bottomSheetView.findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String replyText = replyEditText.getText().toString();
            if (!replyText.isEmpty()) {
                sendCommentToServer(videoData.getVideoid(), replyText);
                bottomSheetDialog.dismiss();
            } else {
                Toast.makeText(VideoPlayerActivity.this, "评论内容不能为空", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private String formatPublishTime(Date publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(publishTime);
    }

    private void exitFullScreen() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void enterFullScreen() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void sendCommentToServer(Integer videoId, String commentText) {
        OkHttpClient client = new OkHttpClient();
        String url = UrlConstants.SEND_VIDEO_COMMENT_URL;

        RequestBody requestBody = new FormBody.Builder()
                .add("token", token)
                .add("videoId", String.valueOf(videoId))
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
                    fetchCommentsFromServer(videoId);
                } else {
                    int statusCode = response.code();
                    if (statusCode == 500) {
                        runOnUiThread(() -> Toast.makeText(VideoPlayerActivity.this, "服务器错误，请稍后再试", Toast.LENGTH_SHORT).show());
                    }
                    Log.e("NetworkResponse", "Request failed with code: " + statusCode);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.e("NetworkResponse", "Request failed: " + e.getMessage());
            }
        });
    }

    private void fetchCommentsFromServer(Integer videoId) {
        OkHttpClient client = new OkHttpClient();
        String url = UrlConstants.COMMENT_LIST_URL + videoId+"?type=video";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> {
                        Gson gson = new Gson();
                        ResultVO resultVO = gson.fromJson(responseData, ResultVO.class);

                        Object data = resultVO.getData();
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
                                        commentVO.setParentCommentId(null);
                                    }
                                    commentVO.setLikeCount(((Double) map.get("likeCount")).intValue());
                                    commentVO.setReplyCount(((Double) map.get("replyCount")).intValue());
                                    commentVO.setReplies(new ArrayList<>());

                                    commentMap.put(commentVO.getCommentId(), commentVO);
                                }
                            }

                            for (CommentVO comment : commentMap.values()) {
                                if (comment.getParentCommentId() == null) {
                                    commentList.add(comment);
                                } else {
                                    CommentVO parentComment = commentMap.get(comment.getParentCommentId());
                                    if (parentComment != null) {
                                        parentComment.getReplies().add(comment);
                                    }
                                }
                            }
                        }

                        commentAdapter.updateComments(commentList);
                    });
                } else {
                    Log.e("NetworkResponse", "Failed to fetch comments");
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("NetworkResponse", "Request failed: " + e.getMessage());
            }
        });
    }
}
