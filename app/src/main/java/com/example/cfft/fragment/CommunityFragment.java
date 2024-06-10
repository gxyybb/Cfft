package com.example.cfft.fragment;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.cfft.CircleTransform;
import com.example.cfft.DetailActivity;
import com.example.cfft.MapActivity;
import com.example.cfft.PublishActivity;
import com.example.cfft.R;
import com.example.cfft.UrlConstants;
import com.example.cfft.adapter.CommunityAdapter;
import com.example.cfft.adapter.ImagePagerAdapter;
import com.example.cfft.adapter.RecyclerViewItemClickListener;
import com.example.cfft.enity.CommunityItem;
import com.example.cfft.enity.DataResponse;
import com.example.cfft.enity.ResultVO;
import com.example.cfft.enity.UserData;
import com.example.cfft.enity.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private CommunityAdapter adapter;
    private ArrayList<CommunityItem> dataList;
    private static final int REQUEST_CODE_ADD_ITEM = 1001;
    private ViewPager viewPager;
    private ArrayList<String> imageUrls;
    private int currentPosition = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        EditText searchEditText = view.findViewById(R.id.searchEditText);
        ImageButton searchButton = view.findViewById(R.id.searchButton);

        Bundle bundle = getArguments();
        String token = null;
        if (bundle != null) {
            token = bundle.getString("token");
        } else {
            // 处理未找到token的情况
        }

        final String finalToken = token;

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEditText.getText().toString();
                try {
                    performSearch(query);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dataList = new ArrayList<>();
        adapter = new CommunityAdapter(getActivity(), requireContext(), dataList, token);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAddItem = view.findViewById(R.id.fab_add_item);
        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PublishActivity.class);
                intent.putExtra("token", finalToken);
                startActivityForResult(intent, REQUEST_CODE_ADD_ITEM);
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), recyclerView, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                CommunityItem clickedItem = dataList.get(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("itemData", clickedItem);
                intent.putExtra("token", finalToken);
                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, int position) {
                // Handle long item click if needed
            }
        }));

        viewPager = view.findViewById(R.id.viewPager);



        // 添加点击事件监听器
//        viewPager.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), MapActivity.class);
//                startActivity(intent);
//                // 在这里处理点击事件
//                Toast.makeText(getContext(), "ViewPager clicked!", Toast.LENGTH_SHORT).show();
//            }
//        });
        fetchImageUrlsFromServer();
        fetchDataFromServer();
        fetchDataAndUpdateUI(token);

        return view;
    }
    private void fetchDataAndUpdateUI(String token) {
        OkHttpClient client = new OkHttpClient();

        // 将 token 添加到 URL 中作为查询参数
        HttpUrl.Builder urlBuilder = HttpUrl.parse(UrlConstants.USER_FIRST_URL).newBuilder();
        urlBuilder.addQueryParameter("token", token);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("ResponseData", "Received ResponseData: " + responseData); // 打印接收到的响应数据

                    Gson gson = new Gson();
                    ResultVO resultVO = gson.fromJson(responseData, ResultVO.class);

                    if (resultVO != null) {
                        // 检查data字段是否为空
                        if (resultVO.getData() != null) {
                            // 如果data字段不为空，则尝试将其转换为UserData对象
                            try {
                                UserData userData = gson.fromJson(gson.toJson(resultVO.getData()), UserData.class);
                                if (userData != null) {
                                    if (getActivity() != null) {
                                    // 更新UI
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateUI(userData);
                                        }
                                    });
                                    }
                                } else {
                                    Log.e("UserData", "Received null UserData from server");
                                }
                            } catch (JsonSyntaxException e) {
                                Log.e("UserData", "Error parsing UserData: " + e.getMessage());
                            }
                        } else {
                            Log.e("UserData", "Data field is null in ResultVO");
                        }
                    } else {
                        Log.e("ResultVO", "Received null ResultVO from server");
                    }
                } else {
                    Log.e("NetworkResponse", "Request failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 请求失败
                e.printStackTrace();
                Log.e("NetworkResponse", "Request failed: " + e.getMessage());
            }
        });


    }

    private void updateUI(UserData userData) {
        // 获取根视图
        View rootView = getView();
        if (rootView == null) {
            return; // 如果根视图为空，返回
        }

        // 从根视图中查找相关的 TextView
        TextView usernameTextView = rootView.findViewById(R.id.nicknameTextView);
        TextView addressTextView = rootView.findViewById(R.id.locationTextView);
        TextView timeTextView = rootView.findViewById(R.id.timeTextView);
        TextView textView1 = rootView.findViewById(R.id.textView1);
        TextView textView2 = rootView.findViewById(R.id.textView2);
        TextView textView3 = rootView.findViewById(R.id.textView3);

        // 检查 TextView 是否为空
        if (usernameTextView == null || addressTextView == null || timeTextView == null) {
            return; // 如果 TextView 为空，返回
        }
// 从根视图中查找头像 ImageView
        ImageView profileImageView = rootView.findViewById(R.id.profileImageView);

// 检查 ImageView 是否为空
        if (profileImageView != null) {
            // 使用Picasso库加载用户头像
            Picasso.get().load(userData.getUserImage()).transform(new CircleTransform()).into(profileImageView);
        }
        // 更新 TextView 的文本内容
        usernameTextView.setText(userData.getUsername());
        addressTextView.setText(userData.getAddress());
        timeTextView.setText(String.valueOf(userData.getTime())); // 如果时间是一个整数，请确保将其转换为字符串
        List<String> texts = userData.getTexts(1);
        if (texts != null && !texts.isEmpty()) {
            textView1.setText(texts.get(0));
            textView2.setText(texts.get(1));// 设置文本为列表中的第一个元素
            textView3.setText(texts.get(2));
        }

    }



    private void fetchImageUrlsFromServer() {
        // 发送网络请求获取轮播图图片URL
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(UrlConstants.CAROUSEL_ALL_URL) // 替换成实际的服务器URL
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 检查 Fragment 是否已经与 Activity 关联
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        JSONArray carousesArray = dataObject.getJSONArray("carouses");

                        imageUrls = new ArrayList<>();
                        for (int i = 0; i < carousesArray.length(); i++) {
                            JSONObject carouselObject = carousesArray.getJSONObject(i);
                            String imageUrl = carouselObject.getString("imageUrl");
                            imageUrls.add(imageUrl);
                        }

                        Log.d("ImageUrls", "Image URLs received: " + imageUrls.toString());

                        // 更新 UI，通知 TimerTask 更新 viewPager 中的图片
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 设置图片到ViewPager中
                                    if (imageUrls != null && imageUrls.size() > 0) {
                                        ImagePagerAdapter adapter = new ImagePagerAdapter(getActivity(), imageUrls);
                                        viewPager.setAdapter(adapter);

                                    }
//                                    adapter.setListViewHeight(listView); // 设置ListView的高度
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }


            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        });
    }


    private void performSearch(String query) throws UnsupportedEncodingException {
        String baseUrl = UrlConstants.POST_SEARCH_URL;

        // 对搜索关键字进行编码以防止特殊字符引起问题
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());

        // 构建带有查询参数的URL
        String urlWithQuery = baseUrl + "?keyword=" + encodedQuery;

        // 创建请求
        OkHttpClient client = new OkHttpClient();

        // 创建请求
        Request request = new Request.Builder()
                .url(urlWithQuery)
                .get()
                .build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("NetworkResponse", "Received: " + responseData);
                    handleSearchResults(responseData);
                } else {
                    Log.e("NetworkResponse", "Request failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("NetworkResponse", "Request failed: " + e.getMessage());
            }
        });
    }


    private void handleSearchResults(String responseData) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseData, JsonObject.class);

        // 检查是否存在数据
        if (jsonObject.has("data")) {
            JsonArray dataArray = jsonObject.getAsJsonArray("data");

            // 遍历数组中的每个元素，并将其转换为 CommunityItem 对象
            dataList.clear(); // 清除原有数据
            for (JsonElement element : dataArray) {
                CommunityItem communityItem = gson.fromJson(element, CommunityItem.class);
                dataList.add(communityItem);
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 通知适配器数据已更改
                    adapter.notifyDataSetChanged();
//                    adapter.setListViewHeight(listView); // 设置ListView的高度
                }
            });
        } else {
            Log.e("handleSearchResults", "No data found in response");
        }
    }



    // 执行点赞操作
    private void performLike() {
        // 这里执行向服务器发送 POST 请求的逻辑
        // 可以使用 OkHttp 或其他网络库发送请求
        // 在请求的回调中处理响应，并更新点赞文本视图的文本
    }


    private void fetchDataFromServer() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(UrlConstants.POST_LIST_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("NetworkResponse", "Received: " + responseData);
                    Gson gson = new Gson();
                    DataResponse dataResponse = gson.fromJson(responseData, DataResponse.class);
                    if (dataResponse != null && dataResponse.getData() != null) {
                        // 清除 dataList
                        dataList.clear();

                        List<CommunityItem> parsedDataList = dataResponse.getData();
                        dataList.addAll(parsedDataList);

                        if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                        }
                    } else {
                        Log.e("Data Parsing", "Data or Data list is null");
                    }
                } else {
                    Log.e("NetworkResponse", "Request failed");
                }
            }


            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("NetworkResponse", "Request failed: " + e.getMessage());
            }
        });
    }
}
