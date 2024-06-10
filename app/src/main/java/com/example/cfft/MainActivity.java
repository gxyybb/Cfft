package com.example.cfft;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.cfft.fragment.CommunityFragment;
import com.example.cfft.fragment.MyFragment;
import com.example.cfft.fragment.StudyFragment;
import com.example.cfft.fragment.VideoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private FloatingActionButton floatingButton;
    private String audioFilePath;
    private boolean isFirstClick = true;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取传递的 token
        token = getIntent().getStringExtra("msg");
        Log.d("MainActivity", "Received token: " + token);

        DragFloatActionButton mBtn = findViewById(R.id.floating_button);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFirstClick) {
                    // 第一次点击
                    showToast("开始拖拽按钮");
                    startRecording();
                    isFirstClick = false;
                } else {
                    // 第二次点击
                    showToast("结束拖拽按钮");
                    stopRecording();
                    isFirstClick = true;
                }
            }
        });

        // 设置底部导航栏点击事件
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    openHomeFragment();
                    return true;
                } else if (item.getItemId() == R.id.navigation_dashboard) {
                    openDashboardFragment();
                    return true;
                } else if (item.getItemId() == R.id.navigation_notifications) {
                    openNotificationsFragment();
                    return true;
                } else if (item.getItemId() == R.id.navigation_community) {
                    openCommunityFragment();
                    return true;
                }
                return false;
            }
        });

        // 默认打开社区 Fragment
        openCommunityFragment();
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // 使用THREE_GPP格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // 使用AMR_NB编码
        audioFilePath = getExternalCacheDir().getAbsolutePath() + "/audio.3gp"; // 保存为3GP文件
        mediaRecorder.setOutputFile(audioFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            showToast("正在录音，滑动选择操作");
        } catch (IOException e) {
            e.printStackTrace();
            isRecording = false;
            showToast("录音失败");
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                Log.e("Recording", "停止录音失败", e);
                showToast("录音停止失败，请重试");
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                return;
            }
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;

            sendMessageToFileServer();

        } else {
            showToast("录音未开始或已停止");
        }
    }

    private void sendMessageToFileServer() {
        OkHttpClient client = new OkHttpClient();
        File audioFile = new File(audioFilePath);
        String mimeType = "audio/mp4"; // 确保使用正确的MIME类型

        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), audioFile);

        MultipartBody.Part body = MultipartBody.Part.createFormData("file", audioFile.getName(), requestBody);

        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(body)
                .addFormDataPart("token", token)
                .addFormDataPart("use4", "true")
                .build();

        Request request = new Request.Builder()
                .url("http://101.200.79.152:8080/chat/file") // 确保使用正确的URL格式
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showToast("发送到/chat/file失败: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject responseData = new JSONObject(response.body().string());
                        String msg = responseData.getString("msg");
                        playAudio(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void playAudio(String audioUrl) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            showToast("正在播放音频");
        } catch (IOException e) {
            e.printStackTrace();
            showToast("播放音频时出错");
        }

        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
            showToast("音频播放完成");
        });
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void openHomeFragment() {
        String token = getIntent().getStringExtra("msg");
        Bundle bundle = new Bundle();
        bundle.putString("token", token);

        MyFragment myFragment = new MyFragment();
        myFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, myFragment)
                .commit();
    }

    private void openDashboardFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new StudyFragment())
                .commit();
    }

    private void openNotificationsFragment() {
        String token = getIntent().getStringExtra("msg");
        Bundle bundle = new Bundle();
        bundle.putString("token", token);
        VideoFragment videoFragment = new VideoFragment();
        videoFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, videoFragment)
                .commit();
    }

    private void openCommunityFragment() {
        String token = getIntent().getStringExtra("msg");
        Bundle bundle = new Bundle();
        bundle.putString("token", token);

        CommunityFragment communityFragment = new CommunityFragment();
        communityFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, communityFragment)
                .commit();
    }
}
