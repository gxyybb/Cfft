package com.example.cfft.video;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.cfft.enity.CommentVO;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VideoApi {

    private static final String VIDEO_API_URL = "http://101.200.79.152:8080/video";

    public static void fetchVideoData(final VideoDataCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(VIDEO_API_URL + "/list")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                List<VideoData> videoDataList = new ArrayList<>();
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        // 解析返回的 JSON 数据
                        JSONObject jsonObject = new JSONObject(responseData);
                        int code = jsonObject.getInt("code");
                        String msg = jsonObject.getString("msg");

                        if (code == 200) { // 如果返回状态码为 200，表示请求成功
                            Object dataObject = jsonObject.get("data");
                            if (dataObject instanceof JSONArray) {
                                // 如果 data 是一个 JSON 数组，解析其中的视频数据
                                JSONArray videoArray = (JSONArray) dataObject;
                                for (int i = 0; i < videoArray.length(); i++) {
                                    JSONObject videoObject = videoArray.getJSONObject(i);
                                    int id = videoObject.getInt("videoid");
                                    String title = videoObject.getString("title");
                                    String description = videoObject.getString("description");
                                    String coverImage = videoObject.getString("coverimage");

                                    // 创建 VideoData 对象并将其添加到列表中
                                    VideoData videoData = new VideoData(id, title, description, coverImage);
                                    videoDataList.add(videoData);
                                }
                                callback.onSuccess(videoDataList);
                            } else {
                                callback.onFailure("Unexpected data format: " + dataObject.toString());
                            }
                        } else {
                            // 请求失败，处理错误信息
                            callback.onFailure("Request failed: " + msg);
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse JSON data: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Failed to fetch video data");
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }
        });
    }

    public interface VideoDataCallback {
        void onSuccess(List<VideoData> videoDataList);

        void onFailure(String errorMessage);
    }

    public interface VideoUrlCallback {
        void onSuccess(String videoUrl,Date uploadtime, List<CommentVO> commentVOList);

        void onFailure(String errorMessage);
    }

    public static void fetchVideoUrl(String videoId, final VideoUrlCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(VIDEO_API_URL + "?id=" + videoId)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        String videoUrl = dataObject.getString("filepath");
                        String publishTimeMillis1 = dataObject.getString("uploadtime");
                        Date publishTime1 = dateFormat.parse(publishTimeMillis1);
                        JSONArray commentArray = dataObject.getJSONArray("commentVOS");
                        List<CommentVO> commentVOList = new ArrayList<>();
                        for (int i = 0; i < commentArray.length(); i++) {
                            JSONObject commentObject = commentArray.getJSONObject(i);
                            String userImage = commentObject.getString("userImage");
                            Integer commentId = commentObject.getInt("commentId");
                            String publishTimeMillis = commentObject.getString("publishTime");

                            Date publishTime = dateFormat.parse(publishTimeMillis);

                            String username = commentObject.getString("username");
                            Integer postId = commentObject.getInt("postId");
                            Integer likeCount = commentObject.getInt("likeCount");
                            Integer replyCount = commentObject.getInt("replyCount");
                            String content = commentObject.getString("content");

                            // 创建 CommentVO 对象并添加到列表中
                            CommentVO commentVO = new CommentVO(userImage, commentId, content, publishTime, username, postId, 0, likeCount, replyCount);
                            commentVOList.add(commentVO);
                        }

                        callback.onSuccess(videoUrl,publishTime1, commentVOList);
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse JSON data: " + e.getMessage());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    callback.onFailure("Failed to fetch video data");
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }
        });

    }

    public interface VideoDataCallback1 {
        void onSuccess(List<VideoData> videoDataList);

        void onFailure(String errorMessage);
    }

    public static void searchVideos(String query, final VideoDataCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(VIDEO_API_URL + "/search/" + query)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        // 解析返回的 JSON 数据
                        JSONObject jsonObject = new JSONObject(responseData);
                        int code = jsonObject.getInt("code");
                        String msg = jsonObject.getString("msg");

                        if (code == 200) { // 如果返回状态码为 200，表示请求成功
                            List<VideoData> videoDataList = new ArrayList<>();
                            Object dataObject = jsonObject.get("data");
                            if (dataObject instanceof JSONArray) {
                                // 如果 data 是一个 JSON 数组，解析其中的视频数据
                                JSONArray videoArray = (JSONArray) dataObject;
                                for (int i = 0; i < videoArray.length(); i++) {
                                    JSONObject videoObject = videoArray.getJSONObject(i);
                                    int id = videoObject.getInt("videoid");
                                    String title = videoObject.getString("title");
                                    String description = videoObject.getString("description");
                                    String coverImage = videoObject.getString("coverimage");

                                    // 创建 VideoData 对象并将其添加到列表中
                                    VideoData videoData = new VideoData(id, title, description, coverImage);
                                    videoDataList.add(videoData);
                                }
                                callback.onSuccess(videoDataList);
                            } else {
                                callback.onFailure("Unexpected data format: " + dataObject.toString());
                            }
                        } else {
                            // 请求失败，处理错误信息
                            callback.onFailure("Request failed: " + msg);
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse JSON data: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Search request failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Search request failed: " + e.getMessage());
            }
        });
    }
}