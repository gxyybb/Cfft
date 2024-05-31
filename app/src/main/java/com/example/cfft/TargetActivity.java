package com.example.cfft;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cfft.adapter.TargetAdapter;
import com.example.cfft.enity.MushRoomVO;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TargetActivity extends AppCompatActivity {

    private Integer isEat;
    private Integer isPosion;
    private RecyclerView recyclerView;
    private TargetAdapter adapter;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private ImageView imgView;
    private static final int PAGE_SIZE = 10; // 每页加载的数据量
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<MushRoomVO> dataList = new ArrayList<>();
    private SearchView searchView;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.searchView);
        imgView = findViewById(R.id.header_image);


        // 获取按钮文本内容
        String buttonText = getIntent().getStringExtra("title");
        String img =getIntent().getStringExtra("img");
        Picasso.get().load(img).into(imgView);
        // 根据标题内容设置RelativeLayout背景色
        RelativeLayout relativeLayout = findViewById(R.id.relative_layout); // Assuming the id of your RelativeLayout is "relative_layout"
//
        if (buttonText.equals("可食菌菇")) {
            relativeLayout.setBackgroundResource(R.color.colorBackground1);
            isEat = 1;
            isPosion = 0;
        } else if (buttonText.equals("慎食菌菇")) {
            relativeLayout.setBackgroundResource(R.color.colorBackground2);
            isEat = 1;
            isPosion = 1;
        } else if (buttonText.equals("食疑菌菇")) {
            relativeLayout.setBackgroundResource(R.color.colorBackground3);
            isEat = 0;
            isPosion = 0;
        } else {
            relativeLayout.setBackgroundResource(R.color.colorBackground4);
            isEat = 0;
            isPosion = 1;
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        // Load initial data
        fetchData("");

        // 发送按钮文本到服务器
        Log.d("TargetActivity", "按钮文本内容: " + buttonText);
        // 数据回调后更新RecyclerView
        ButtonApi.sendDataToServer(isEat, isPosion, currentPage, this::updateRecyclerView);
        // 下拉刷新监听
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                requestData(currentPage);
            }
        });
        // RecyclerView滚动监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadMoreItems();
                    }
                }
            }
        });


    }
    private void fetchData(String query) {
        // Fetch data based on the current state and query
        // Update RecyclerView after data callback
        ButtonApi.fetchData(isEat, isPosion, currentPage, query, this::updateRecyclerView);
    }
    private void loadMoreItems() {
        isLoading = true;
        currentPage++; // 更新当前页数
        requestData(currentPage);
    }

    private void requestData(int page) {
        Log.d("page","page   "+page);
        ButtonApi.sendDataToServer(isEat, isPosion, page, mushroomList -> {
            // 数据回调后更新RecyclerView
            updateRecyclerView(mushroomList);
            isLoading = false;
            // 结束下拉刷新
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void updateRecyclerView(List<MushRoomVO> mushroomList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 创建适配器并设置到RecyclerView
                adapter = new TargetAdapter(TargetActivity.this, mushroomList);
                recyclerView.setAdapter(adapter);
                Log.d("TargetActivity", "文本内容: " + mushroomList);
            }
        });
    }

}

