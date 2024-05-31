package com.example.cfft;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.*;

import java.io.IOException;

public class ChatApi {

    private static final OkHttpClient client = new OkHttpClient();

    // 自定义回调接口
    public interface ApiResponseCallback {
        void onFailure(String errorMessage);
        void onSuccess(JSONObject responseData);
    }

    // 发送消息方法
    public static void sendMessage(String messageText, String gptVersion, final ApiResponseCallback callback) {
        // 构建请求的 URL，包括主机地址和端口号，并在其中包含要发送的消息作为参数
        String url = "http://101.200.79.152:8080/chat?message=" + messageText;

        // 如果 gptVersion 不为空，则添加 gptVersion 参数
        if (gptVersion != null && !gptVersion.isEmpty()) {
            url += "&" + gptVersion +"=true";
        }

        // 创建请求对象
        Request request = new Request.Builder()
                .url(url)
                .get() // 使用 GET 请求方式
                .build();

        // 发送请求
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 在此处处理网络请求失败的情况
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // 如果响应不成功，调用回调接口的 onFailure 方法，并传递错误消息
                    callback.onFailure("Unexpected code " + response);
                    return;
                }
                // 解析响应数据
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    int code = jsonObject.getInt("code");
                    // 如果响应码为 200，表示成功，可以更新 UI
                    if (code == 200) {
                        callback.onSuccess(jsonObject);
                    } else {
                        // 如果响应码不是 200，表示失败，调用回调接口的 onFailure 方法，并传递错误消息
                        callback.onFailure("Response code is not 200: " + code);
                    }
                } catch (JSONException e) {
                    // JSON 解析失败，调用回调接口的 onFailure 方法，并传递错误消息
                    callback.onFailure("Error parsing JSON: " + e.getMessage());
                }
            }

        });
    }

}
