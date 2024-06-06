package com.example.cfft.video;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.cfft.R;
import com.example.cfft.adapter.CommentAdapter;
import com.example.cfft.enity.CommentVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VideoPlayerActivity extends AppCompatActivity {
    private VideoData videoData;
    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private TextView textViewTitle;
    private TextView textViewDescription;
    private TextView textViewPublishTime;
    private boolean isFullScreen = false; // 添加 isFullScreen 变量并初始化为 false
    private long lastClickTime = 0; // 记录上次点击的时间

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // 获取视频 URL 和 VideoData 对象
        Intent intent = getIntent();
        String videoUrl = intent.getStringExtra("videoUrl");
        long uploadTimeMillis = intent.getLongExtra("uploadtime", 0);
        Date uploadTime = new Date(uploadTimeMillis);
        videoData = intent.getParcelableExtra("videoData");
        ArrayList<CommentVO> commentList = (ArrayList<CommentVO>) intent.getSerializableExtra("commentList");
        VideoView videoView = findViewById(R.id.video);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);



        // 设置视频路径
        videoView.setVideoPath(videoUrl);
        // 开始播放视频
        videoView.start();

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
        commentAdapter = new CommentAdapter(this, comments,1);
        recyclerViewComments.setAdapter(commentAdapter);
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
}
