package com.example.cfft.fragment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cfft.R;
import com.example.cfft.adapter.VideoAdapter;
import com.example.cfft.video.VideoApi;
import com.example.cfft.video.VideoData;
import java.util.List;
public class VideoFragment extends Fragment {

    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private SearchView searchView;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        videoAdapter = new VideoAdapter(requireContext());
        searchView = view.findViewById(R.id.search_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoAdapter);
// 设置搜索框的监听器
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 当用户点击搜索按钮时触发
                if (query.isEmpty()) {
                    // 如果搜索关键字为空，则加载所有视频数据
                    fetchVideoData();
                } else {
                    // 否则，执行搜索操作
                    searchVideos(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 当搜索框的文本发生变化时触发
                // 在此可以实现实时搜索的功能，比如随着用户输入的关键字实时更新搜索结果
                return false;
            }
        });
        fetchVideoData();

        return view;
    }
    private void searchVideos(String query) {
        VideoApi.searchVideos(query, new VideoApi.VideoDataCallback() {
            @Override
            public void onSuccess(List<VideoData> videoDataList) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoAdapter.setVideoDataList(videoDataList);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void fetchVideoData() {
        VideoApi.fetchVideoData(new VideoApi.VideoDataCallback() {
            @Override
            public void onSuccess(List<VideoData> videoDataList) {
                if (isAdded()) {
                    Activity activity = requireActivity();
                if (videoDataList != null && !videoDataList.isEmpty()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            videoAdapter.setVideoDataList(videoDataList);
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "未找到视频数据", Toast.LENGTH_SHORT).show();
                }
                } else {
                    // 如果 fragment 没有附加到 activity 上，则不执行任何操作，避免空指针异常
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(requireContext(), "获取视频数据失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
