package com.example.cfft;

import android.util.Log;

import com.example.cfft.enity.Location;
import com.example.cfft.enity.MushRoomVO;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class ButtonApi {

    private static final String TAG = "ButtonApi";

    public interface DataCallback {
        void onDataReceived(List<MushRoomVO> mushroomList);
    }

    public static void sendDataToServer(int isEat, int isPosion,int page, final DataCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // 创建请求
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://101.200.79.152:8080/mushrooms/getMushroomInAndroid").newBuilder();
        urlBuilder.addQueryParameter("isEat", String.valueOf(isEat));
        urlBuilder.addQueryParameter("isPosion", String.valueOf(isPosion));
        urlBuilder.addQueryParameter("page", String.valueOf(page));
        String url = urlBuilder.build().toString();

        // 创建请求
        Request request = new Request.Builder()
                .url(url) // 修改为带参数的 URL
                .build();

        // 发起异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "请求失败", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "服务器响应数据：" + responseData);
                    List<MushRoomVO> mushroomList = parseResponseData(responseData);
                    callback.onDataReceived(mushroomList); // 回调方法通知数据已经接收到
                } else {
                    Log.e(TAG, "服务器响应失败：" + response.code() + " " + response.message());
                }
            }
        });
    }
    public static void fetchData(int isEat, int isPosion, int page, String query, final DataCallback callback) {
        OkHttpClient client = new OkHttpClient();
        page = 1;
        // Build URL with query parameters
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://101.200.79.152:8080/mushrooms/searchMushroomInLibrary").newBuilder();
        urlBuilder.addQueryParameter("isEat", String.valueOf(isEat));
        urlBuilder.addQueryParameter("isPosion", String.valueOf(isPosion));
        urlBuilder.addQueryParameter("page", String.valueOf(page));
        urlBuilder.addQueryParameter("key", query);
        String url = urlBuilder.build().toString();

        // Create request
        Request request = new Request.Builder()
                .url(url)
                .build();

        // Send asynchronous request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "Request failed", e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Server response data: " + responseData);
                    List<MushRoomVO> mushroomList = parseResponseData(responseData);
                    callback.onDataReceived(mushroomList); // Notify that data has been received
                } else {
                    Log.e(TAG, "Server response failed: " + response.code() + " " + response.message());
                }
            }
        });
    }
    private static List<MushRoomVO> parseResponseData(String responseData) {
        List<MushRoomVO> mushroomList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            int code = jsonObject.getInt("code");
            JSONArray dataArray = jsonObject.getJSONArray("data");
            if (code == 200 && dataArray.length() > 0) {
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    MushRoomVO mushRoomVO = new MushRoomVO();
                    mushRoomVO.setMushroomId(dataObject.getInt("mushroomId"));
                    mushRoomVO.setMushroomName(dataObject.getString("mushroomName"));
                    mushRoomVO.setCategory(dataObject.getString("category"));
                    mushRoomVO.setIsEat(dataObject.getInt("isEat"));
                    mushRoomVO.setMushroomLocation(dataObject.getString("mushroomLocation"));
                    mushRoomVO.setMushroomDesc(dataObject.getString("mushroomDesc"));
                    mushRoomVO.setIsPoison(dataObject.getInt("isPoison"));
                    // 解析 locations
                    JSONArray locationsArray = dataObject.getJSONArray("locations");
                    List<Location> locations = new ArrayList<>();
                    for (int k = 0; k < locationsArray.length(); k++) {
                        JSONObject locationObject = locationsArray.getJSONObject(k);
                        Location location = new Location();
                        location.setId(locationObject.getInt("id"));
                        location.setProvince(locationObject.getString("province"));
                        location.setCity(locationObject.getString("city"));
                        location.setLatitude(BigDecimal.valueOf(locationObject.getDouble("latitude")));
                        location.setLongitude(BigDecimal.valueOf(locationObject.getDouble("longitude")));
                        location.setDescription(locationObject.getString("description"));
                        locations.add(location);
                    }
                    mushRoomVO.setLocations(locations);
                    // 解析图片列表
                    JSONArray imageArray = dataObject.getJSONArray("mushroomImages");
                    if (imageArray.length() > 0) {
                        JSONObject imageObject = imageArray.getJSONObject(0); // 只取第一个图片对象
                        String imageUrl = imageObject.getString("imgUrl");
                        List<String> images = new ArrayList<>();
                        images.add(imageUrl);
                        mushRoomVO.setMushroomImages(images);
                    }

                    mushroomList.add(mushRoomVO);
                    // 输出当前解析的蘑菇名
                    Log.d(TAG, "解析得到的蘑菇名：" + mushRoomVO.getMushroomName());
                }
            } else {
                String msg = jsonObject.isNull("msg") ? "Unknown Error" : jsonObject.getString("msg");
                Log.e(TAG, "服务器返回错误：" + msg);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON 解析错误", e);
        }
        return mushroomList;
    }

}