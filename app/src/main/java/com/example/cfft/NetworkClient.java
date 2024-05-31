package com.example.cfft;

import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.cfft.adapter.UserProfile1;
import com.example.cfft.enity.CommentVO;
import com.example.cfft.enity.CommunityItem;
import com.example.cfft.enity.Post;
import com.example.cfft.enity.UserProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NetworkClient {
    private static final String BASE_URL = "http://101.200.79.152:8080/user";
    private OkHttpClient client;
    private Gson gson;

    public NetworkClient(String token) {
        this.client = new OkHttpClient.Builder().build();
        this.gson = new Gson();
    }

    public interface ApiResponseCallback<T> {
        void onSuccess(T result);

        void onFailure(String errorMessage);
    }

    // 方法用于获取喜欢的帖子列表，并通过回调返回结果
    public void getLikedPosts(String token, final ApiResponseCallback<ArrayList<CommunityItem>> callback) {
        // 构建请求的 URL，包括主机地址和端口号，并在其中包含要发送的消息作为参数
        String url = BASE_URL + "/likePost?token=" + token;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("Unexpected code " + response);
                    return;
                }

                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray dataArray = jsonObject.getJSONArray("data");

                    Type listType = new TypeToken<ArrayList<CommunityItem>>() {
                    }.getType();
                    ArrayList<CommunityItem> posts = gson.fromJson(dataArray.toString(), listType);

                    callback.onSuccess(posts);
                } catch (JSONException e) {
                    callback.onFailure("Error parsing JSON: " + e.getMessage());
                }
            }
        });
    }


    public void getLikedUsers(String token, final ApiResponseCallback<ArrayList<UserProfile1>> callback) {
        // 构建请求的 URL，包括主机地址和端口号，并在其中包含要发送的消息作为参数
        String url = BASE_URL + "/getLikeUser?token=" + token;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("Unexpected code " + response);
                    return;
                }

                String responseData = response.body().string();

                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    if (!jsonObject.isNull("data")) {
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        ArrayList<UserProfile1> users = new ArrayList<>();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject userObject = dataArray.getJSONObject(i);
                            int userId = userObject.getInt("userId");
                            String username = userObject.getString("username");
                            int level = userObject.getInt("level");
                            String gender = userObject.optString("gender", null); // 可能为空
                            String bio = userObject.optString("bio", null); // 可能为空
                            String userImage = userObject.getString("userImage");

                            UserProfile1 userProfile = new UserProfile1(userId, username, level, gender, bio, userImage);
                            users.add(userProfile);
                        }
//                    Type listType = new TypeToken<ArrayList<UserProfile>>() {
//                    }.getType();
//                    ArrayList<UserProfile> users = gson.fromJson(dataArray.toString(), listType);

                    callback.onSuccess(users);
                    } else {
                        callback.onFailure("No data found in response");
                    }
                } catch (JSONException e) {
                    callback.onFailure("Error parsing JSON: " + e.getMessage());
                    Log.e("cuo",e.getMessage());
                }
            }
        });
    }
}