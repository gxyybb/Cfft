package com.example.cfft;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.adapter.ChatAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author 14847
 */
public class ChatAiActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private RecyclerView recyclerView;
    private EditText editText;
    private Button sendButton;
    private ImageButton toggleInputButton;
    private Button recordButton;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private Button changeButton;
    private LinearLayout swipeHintLayout;
    private TextView swipeHintLeft, swipeHintRight; //swipeHintDown;
    private Markwon markwon;
    private boolean isAi3 = true;
    private boolean isAi4 = false;
    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private OkHttpClient client;
    private String token;
    private GestureDetector gestureDetector;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        token = getIntent().getStringExtra("token");

        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editText);
        sendButton = findViewById(R.id.sendButton);
        toggleInputButton = findViewById(R.id.toggleInputButton);
        recordButton = findViewById(R.id.recordButton);
        changeButton = findViewById(R.id.changeButton);
        swipeHintLayout = findViewById(R.id.swipeHintLayout);
        swipeHintLeft = findViewById(R.id.swipeHintLeft);
        swipeHintRight = findViewById(R.id.swipeHintRight);
//        swipeHintDown = findViewById(R.id.swipeHintDown);

        markwon = Markwon.create(this);
        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        client = new OkHttpClient();

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAi3) {
                    changeButton.setText("ChaiAiPlus");
                    isAi4 = true;
                } else {
                    changeButton.setText("ChatAi");
                    isAi4 = false;
                }
                isAi3 = !isAi3;
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = editText.getText().toString().trim();
                if (!messageText.isEmpty()) {
                    ChatMessage outgoingMessage = new ChatMessage(messageText, ChatMessage.TYPE_OUTGOING);
                    messageList.add(outgoingMessage);
                    adapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                    editText.setText("");
                    sendMessageToServer(messageText);
                }
            }
        });

        toggleInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getVisibility() == View.VISIBLE) {
                    editText.setVisibility(View.GONE);
                    recordButton.setVisibility(View.VISIBLE);
                } else {
                    editText.setVisibility(View.VISIBLE);
                    recordButton.setVisibility(View.GONE);
                }
            }
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                Log.d("Gesture", "onScroll - diffX: " + diffX + " diffY: " + diffY);

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (diffX > 0) {
                        Log.d("Gesture", "Scrolling Right");
                        swipeHintRight.setTextColor(Color.GREEN);
                        swipeHintLeft.setTextColor(Color.BLACK);
//                        swipeHintDown.setTextColor(Color.BLACK);
                    } else {
                        Log.d("Gesture", "Scrolling Left");
                        swipeHintLeft.setTextColor(Color.RED);
                        swipeHintRight.setTextColor(Color.BLACK);
//                        swipeHintDown.setTextColor(Color.BLACK);
                    }
                } else {
                    if (diffY > 0) {
                        Log.d("Gesture", "Scrolling Down");
//                        swipeHintDown.setTextColor(Color.BLUE);
                        swipeHintLeft.setTextColor(Color.BLACK);
                        swipeHintRight.setTextColor(Color.BLACK);
                    }
                }
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                Log.d("Gesture", "onFling - diffX: " + diffX + " diffY: " + diffY + " velocityX: " + velocityX + " velocityY: " + velocityY);

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (diffX > 0) {
                        Log.d("Gesture", "Fling Right");
                        onSwipeRight();
                    } else {
                        Log.d("Gesture", "Fling Left");
                        onSwipeLeft();
                    }
                } else {
                    if (diffY > 0) {
                        Log.d("Gesture", "Fling Down");
//                        onSwipeDown();
                    }
                }
                return true;
            }
        });

        recordButton.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                startRecording();
                swipeHintLayout.setVisibility(View.VISIBLE);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                stopRecording(event.getX(), event.getY());
                swipeHintLayout.setVisibility(View.GONE);
                // 重置文字颜色
                swipeHintLeft.setTextColor(Color.BLACK);
                swipeHintRight.setTextColor(Color.BLACK);
//                swipeHintDown.setTextColor(Color.BLACK);
            }
            return true;
        });

        checkPermissions();
    }

    private void onSwipeRight() {
        Toast.makeText(this, "右滑 - 发送到convert", Toast.LENGTH_SHORT).show();
        sendAudioToServer();
    }

    private void onSwipeLeft() {
        Toast.makeText(this, "左滑 - 取消", Toast.LENGTH_SHORT).show();
    }

//    private void onSwipeDown() {
//        Toast.makeText(this, "下滑 - 发送到/chat/file", Toast.LENGTH_SHORT).show();
//        sendMessageToFileServer();
//    }

    private void sendMessageToServer(String messageText) {
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", messageText)
                .addFormDataPart("use4", isAi4 ? "true" : "false")
                .build();

        Request request = new Request.Builder()
                .url("http://101.200.79.152:8080/chat")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatAiActivity.this, "请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject responseData = new JSONObject(response.body().string());
                        String msg = responseData.getString("msg");
                        String parsedMsg = parseMsgToMarkdown(msg);
                        ChatMessage chatMessage = new ChatMessage(parsedMsg, ChatMessage.TYPE_INCOMING);
                        runOnUiThread(() -> {
                            messageList.add(chatMessage);
                            adapter.notifyItemInserted(messageList.size() - 1);
                            recyclerView.scrollToPosition(messageList.size() - 1);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendMessageToFileServer() {
        File audioFile = new File(audioFilePath);
        String mimeType = "audio/mp4"; // 确保使用正确的MIME类型

        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), audioFile);

        MultipartBody.Part body = MultipartBody.Part.createFormData("file", audioFile.getName(), requestBody);

        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(body)
                .addFormDataPart("token", token)
                .build();

        Request request = new Request.Builder()
                .url("http://101.200.79.152:8080/chat/file") // 确保使用正确的URL格式
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatAiActivity.this, "发送到/chat/file失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject responseData = new JSONObject(response.body().string());
                        String msg = responseData.getString("msg");
                        runOnUiThread(() -> new AlertDialog.Builder(ChatAiActivity.this)
                                .setMessage(msg)
                                .setPositiveButton("发送", (dialog, which) -> {
                                    ChatMessage outgoingMessage = new ChatMessage(msg, ChatMessage.TYPE_OUTGOING);
                                    messageList.add(outgoingMessage);
                                    adapter.notifyItemInserted(messageList.size() - 1);
                                    recyclerView.scrollToPosition(messageList.size() - 1);
                                    sendMessageToServer(msg);
                                })
                                .setNegativeButton("取消", null)
                                .show());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendAudioToServer() {
        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            Log.e("Audio File", "音频文件不存在: " + audioFilePath);
            return;
        }

        long fileSize = audioFile.length();
        Log.d("Audio File", "音频文件大小: " + fileSize + " bytes");

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("video/3gpp"), audioFile); // 使用video/3gpp MIME类型
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", audioFile.getName(), requestBody);

        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(body)
                .addFormDataPart("token", token)
                .build();

        Request request = new Request.Builder()
                .url("http://101.200.79.152:8080/audio/convert")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Send Audio", "音频转换失败: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(ChatAiActivity.this, "音频转换失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("Send Audio", "服务器返回错误: " + response.code());
                    runOnUiThread(() -> Toast.makeText(ChatAiActivity.this, "服务器返回错误: " + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }

                String responseData = response.body().string();
                Log.d("Send Audio", "服务器响应: " + responseData);
                runOnUiThread(() -> Toast.makeText(ChatAiActivity.this, "音频转换成功", Toast.LENGTH_SHORT).show());
                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    String msg = jsonResponse.getString("msg");
                    JSONArray msgArray = new JSONArray(msg);
                    String messageText = msgArray.getString(0);

                    // 创建并显示弹窗
                    runOnUiThread(() -> new AlertDialog.Builder(ChatAiActivity.this)
                            .setMessage(messageText)
                            .setPositiveButton("发送", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ChatMessage outgoingMessage = new ChatMessage(messageText, ChatMessage.TYPE_OUTGOING);
                                    messageList.add(outgoingMessage);
                                    adapter.notifyItemInserted(messageList.size() - 1);
                                    recyclerView.scrollToPosition(messageList.size() - 1);
                                    sendMessageToServer(messageText);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String parseMsgToMarkdown(String msg) {
        return markwon.toMarkdown(msg).toString();
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
            Toast.makeText(this, "正在录音，滑动选择操作", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            isRecording = false;
            Toast.makeText(this, "录音失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording(float x, float y) {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                Log.e("Recording", "停止录音失败", e);
                Toast.makeText(this, "录音停止失败，请重试", Toast.LENGTH_SHORT).show();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                return;
            }
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;

            // 调试输出手势坐标
            Log.d("Gesture", "stopRecording - X: " + x + " Y: " + y);
            Log.d("Gesture", "stopRecording - screenWidth: " + screenWidth + " screenHeight: " + screenHeight);

            if (x < screenWidth / 3) {
                Log.d("Gesture", "Detected Left Swipe in stopRecording");
                onSwipeLeft();
            } else if (x > 2 * screenWidth / 3) {
                Log.d("Gesture", "Detected Right Swipe in stopRecording");
                onSwipeRight();
            } else if (y > 2 * screenHeight / 3) {
                Log.d("Gesture", "Detected Down Swipe in stopRecording");
//                onSwipeDown();
            } else {
                Toast.makeText(this, "操作取消", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "录音未开始或已停止", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予
            } else {
                Toast.makeText(this, "需要录音和存储权限才能使用此功能", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
