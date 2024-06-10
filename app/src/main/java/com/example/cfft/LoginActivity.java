package com.example.cfft;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cfft.enity.ResultVO;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private TextView regiestText;
    private ImageButton buttonLogin;
    private OkHttpClient client;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        client = new OkHttpClient();
        regiestText = findViewById(R.id.regist);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        regiestText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                // Start the new activity
                startActivity(intent);
            }
        });


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // 发送POST请求
                sendPostRequest(username, password);
            }
        });
    }

    private void sendPostRequest(String username, String password) {
        // 创建请求体
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();

        // 创建POST请求
        Request request = new Request.Builder()
                .url("http://101.200.79.152:8080/user/login") // 替换成你的服务器URL
                .post(formBody)
                .build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // 请求失败处理
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "Request failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    // 解析JSON响应数据并封装到LoginResponse对象中
                    Gson gson = new Gson();
                    final ResultVO loginResponse = gson.fromJson(responseData, ResultVO.class);

                    // 处理服务器响应
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 在UI线程中更新UI
//                            Toast.makeText(LoginActivity.this, loginResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            // 如果响应为登录成功，启动MainActivity
                            if (loginResponse.getCode() == 200) {
                                // 处理返回的data数据，这里假设data是一个字符串类型
                                String dataString = loginResponse.getMsg();
                                // 处理data数据，这里可以根据实际情况做进一步的处理
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("msg", dataString); // 将data数据传递到MainActivity
                                startActivity(intent);
                                finish(); // 关闭当前活动
                            }
                        }
                    });
                } else {
                    // 请求失败处理
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Request failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        });
    }
}
