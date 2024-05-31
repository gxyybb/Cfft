package com.example.cfft.fragment;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.ChatAiActivity;
import com.example.cfft.CircleTransform;
import com.example.cfft.EditProfileActivity;
import com.example.cfft.LikesActivity;
import com.example.cfft.R;
import com.example.cfft.adapter.CommunityAdapter;
import com.example.cfft.enity.CommunityItem;
import com.example.cfft.enity.UserProfile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyFragment extends Fragment {
    private ImageView profileImage, themeBackground;
    private TextView profileName,profileNickname,profileInfo, ilike, iliked, commentLiked, reply;
    private TextView emailTextView, addressTextView, registerTimeTextView, genderTextView, birthdayTextView;
    private RecyclerView recyclerView;
    private FloatingActionButton fabChatItem;
    private OkHttpClient client;
    private Gson gson;
    private CommunityAdapter postAdapter;
    private ImageButton settingsButton;
    private UserProfile userProfile;
    private String token;
    private Context context;
    private ArrayList<CommunityItem> dataList = new ArrayList<>();
    private static final int REQUEST_EDIT_PROFILE = 1;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, container, false);

        // 获取传递的 token
        Bundle bundle = getArguments();

        if (bundle != null) {
            token = bundle.getString("token");
            Log.d("MyFragment", "Token: " + token);
        } else {
            Log.e("MyFragment", "Token not found");
        }

        // 初始化视图
        themeBackground = view.findViewById(R.id.theme_background);
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileNickname = view.findViewById(R.id.profile_info);
        profileInfo = view.findViewById(R.id.bio);
        ilike = view.findViewById(R.id.Ilike);
        iliked = view.findViewById(R.id.Iliked);

        String finalToken = token;
        ilike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到新页面并传递信息
                Intent intent = new Intent(getActivity(), LikesActivity.class);
                intent.putExtra("type", "like");
                intent.putExtra("token", finalToken);
                startActivity(intent);
            }
        });

        iliked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到新页面并传递信息
                Intent intent = new Intent(getActivity(), LikesActivity.class);
                intent.putExtra("type", "liked");
                intent.putExtra("token", finalToken);
                startActivity(intent);
            }
        });
        commentLiked = view.findViewById(R.id.commentLiked);
        reply = view.findViewById(R.id.reply);
        emailTextView = view.findViewById(R.id.email);
        addressTextView = view.findViewById(R.id.address);
        registerTimeTextView = view.findViewById(R.id.register_time);
        genderTextView = view.findViewById(R.id.gender);
        birthdayTextView = view.findViewById(R.id.birthday);
        recyclerView = view.findViewById(R.id.recyclerview);
        fabChatItem = view.findViewById(R.id.fab_chat_item);
        // 初始化视图
        settingsButton = view.findViewById(R.id.settings_button);
        // 其他视图初始化

        // 设置点击事件监听器
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                intent.putExtra("userProfile", userProfile);
                intent.putExtra("token", finalToken);
                Log.d("nihao", String.valueOf(userProfile));
                startActivityForResult(intent, REQUEST_EDIT_PROFILE);
            }
        });
        // 找到 FloatingActionButton
        FloatingActionButton fabChatItem = view.findViewById(R.id.fab_chat_item);

        // 设置 FloatingActionButton 的点击事件监听器
        fabChatItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动 ChatAIActivity
                Intent intent = new Intent(getActivity(), ChatAiActivity.class);
                intent.putExtra("token",token);
                startActivity(intent);
            }
        });

        // 初始化 RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postAdapter = new CommunityAdapter(getActivity(), getContext(), dataList, token); // 初始化适配器
        recyclerView.setAdapter(postAdapter);

        // 初始化 OkHttpClient 和 Gson
        client = new OkHttpClient();
        gson = new Gson();

        // 获取用户数据
        if (token != null) {
            getUserProfile(token);
            getUserPosts(token);
        }

        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == RESULT_OK) {
            // 处理从 EditProfileActivity 返回的结果
            if (token != null) {
                getUserProfile(token);
            }
        }
    }
    private void getUserProfile(String token) {
        Request request = new Request.Builder()
                .url("http://101.200.79.152:8080/user?token=" + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                        JsonObject data = jsonObject.getAsJsonObject("data");
                        userProfile = gson.fromJson(data, UserProfile.class);

                        getActivity().runOnUiThread(() -> {
                            profileName.setText(userProfile.getUsername());
                            profileNickname.setText(userProfile.getNickName());
                            profileInfo.setText(userProfile.getBio() != null ? userProfile.getBio() : "No bio available");
                            ilike.setText(String.valueOf(userProfile.getLikeCount()));
                            iliked.setText(String.valueOf(userProfile.getPostZanCount()));
                            commentLiked.setText(String.valueOf(userProfile.getCommentZanCount()));
                            reply.setText(String.valueOf(userProfile.getCommentCommentCount()));
                            emailTextView.setText(userProfile.getEmail());
                            addressTextView.setText(userProfile.getAddress());


                            String registrationTimeString = userProfile.getRegistrationTime();

                            // 解析和格式化日期时间字符串
                            DateTimeFormatter inputFormatter = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
                            }
                            DateTimeFormatter outputFormatter = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
                            }

                            try {
                                // 将字符串解析为 ZonedDateTime 对象
                                ZonedDateTime registrationTime = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    registrationTime = ZonedDateTime.parse(registrationTimeString, inputFormatter);
                                }
                                // 格式化 ZonedDateTime 对象为需要的字符串格式
                                String formattedTime = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    formattedTime = registrationTime.format(outputFormatter);
                                }
                                // 设置文本
                                registerTimeTextView.setText(formattedTime);
                            } catch (Exception e) {
                                e.printStackTrace();
                                // 处理解析异常，例如设置一个默认值或者显示错误信息
                                registerTimeTextView.setText("Invalid date");
                            }

                            genderTextView.setText(userProfile.getGender());
                            birthdayTextView.setText(userProfile.getBirthdate());
                            String birTimeString = userProfile.getBirthdate();
                            // 解析和格式化日期时间字符串
                            DateTimeFormatter inputFormatter1 = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                inputFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
                            }
                            DateTimeFormatter outputFormatter1 = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                outputFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
                            }

                            try {
                                // 将字符串解析为 ZonedDateTime 对象
                                ZonedDateTime registrationTime1 = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    registrationTime1 = ZonedDateTime.parse(birTimeString, inputFormatter);
                                }
                                // 格式化 ZonedDateTime 对象为需要的字符串格式
                                String formattedTime1 = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    formattedTime1 = registrationTime1.format(outputFormatter);
                                }
                                // 设置文本
                                birthdayTextView.setText(formattedTime1);
                            } catch (Exception e) {
                                e.printStackTrace();
                                // 处理解析异常，例如设置一个默认值或者显示错误信息
                                birthdayTextView.setText("Invalid date");
                            }
                            Picasso.get().load(userProfile.getUserImage()).transform(new CircleTransform()).into(profileImage);

                            Picasso.get().load(userProfile.getBackImg()).into(themeBackground);
                        });
                    }
                }
            }
        });
    }

    private void getUserPosts(String token) {
        Request request = new Request.Builder()
                .url("http://101.200.79.152:8080/user/post?token=" + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                        JsonObject data = jsonObject.getAsJsonObject("data");
                        if (data.has("data") && !data.get("data").isJsonNull()) {
                            Type postListType = new TypeToken<List<CommunityItem>>() {}.getType();
                            List<CommunityItem> newDataList = gson.fromJson(data.getAsJsonArray("data"), postListType);

                            getActivity().runOnUiThread(() -> {
                                postAdapter.updateData(newDataList); // 使用适配器的更新方法
                            });
                        }
                    }
                }
            }
        });
    }
}
