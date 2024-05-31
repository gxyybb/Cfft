package com.example.cfft;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.adapter.CommunityAdapter;

import com.example.cfft.adapter.UserProfile1;
import com.example.cfft.enity.CommunityItem;
import com.example.cfft.enity.UserProfile;
import com.example.cfft.adapter.UserAdapter; // 导入 com.example.cfft.adapter.UserAdapter
import java.util.ArrayList;

public class LikesActivity extends AppCompatActivity {

    private NetworkClient networkClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);

        String type = getIntent().getStringExtra("type");
        String token = getIntent().getStringExtra("token");
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        networkClient = new NetworkClient(token);
        if ("like".equals(type)) {
            fetchLikedPosts(recyclerView, token);
        } else if ("liked".equals(type)) {
            fetchLikedUsers(recyclerView, token);

        }
    }

    private void fetchLikedPosts(RecyclerView recyclerView, String token) {
        networkClient.getLikedPosts(token, new NetworkClient.ApiResponseCallback<ArrayList<CommunityItem>>() {
            @Override
            public void onSuccess(ArrayList<CommunityItem> posts) {
                runOnUiThread(() -> {
                    CommunityAdapter postsAdapter = new CommunityAdapter(LikesActivity.this, LikesActivity.this, posts, token);
                    recyclerView.setAdapter(postsAdapter);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    // 显示错误消息
                    Toast.makeText(LikesActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchLikedUsers(RecyclerView recyclerView, String token) {
        Log.d("to",token);
        networkClient.getLikedUsers(token, new NetworkClient.ApiResponseCallback<ArrayList<UserProfile1>>() {
            @Override
            public void onSuccess(ArrayList<UserProfile1> users) {
                runOnUiThread(() -> {
                    // 使用适合用户数据的适配器
                    UserAdapter userAdapter = new UserAdapter(LikesActivity.this, users);
                    recyclerView.setAdapter(userAdapter);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    // 显示错误消息
                    Toast.makeText(LikesActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}