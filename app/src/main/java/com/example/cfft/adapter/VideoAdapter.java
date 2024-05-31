package com.example.cfft.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.R;
import com.example.cfft.enity.CommentVO;
import com.example.cfft.video.VideoApi;
import com.example.cfft.video.VideoData;
import com.example.cfft.video.VideoPlayerActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<VideoData> videoDataList;
    private Context context;

    public VideoAdapter(Context context) {
        this.context = context;
    }

    public void setVideoDataList(List<VideoData> videoDataList) {
        this.videoDataList = videoDataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        if (videoDataList != null && videoDataList.size() > position) {
            VideoData videoData = videoDataList.get(position);
            holder.bind(videoData);
        }
    }

    @Override
    public int getItemCount() {
        return videoDataList != null ? videoDataList.size() : 0;
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView thumbnailImageView;
        private TextView titleTextView;
        private TextView desTextView;
        private VideoData videoData;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnail_image_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            desTextView = itemView.findViewById(R.id.title_des_view);
            itemView.setOnClickListener(this);
        }

        public void bind(VideoData videoData) {
            this.videoData = videoData;
            titleTextView.setText(videoData.getTitle());
            desTextView.setText(videoData.getDescription());
            Picasso.get().load(videoData.getCoverimage()).into(thumbnailImageView);
        }

        @Override
        public void onClick(View v) {
            if (videoData != null) {
                VideoApi.fetchVideoUrl(String.valueOf(videoData.getVideoid()), new VideoApi.VideoUrlCallback() {
                    @Override
                    public void onSuccess(String videoUrl, Date uploadtime, List<CommentVO> commentVOList) {
                        // 获取视频URL成功后启动VideoPlayerActivity
                        Intent intent = new Intent(context, VideoPlayerActivity.class);
                        intent.putExtra("videoUrl", videoUrl);
                        intent.putExtra("uploadtime",  uploadtime.getTime());
                        intent.putExtra("videoData",videoData);
                        intent.putExtra("commentList", new ArrayList<>(commentVOList));
                        context.startActivity(intent);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                });
            } else {
                // 如果视频数据为空，则显示错误消息
                Toast.makeText(context, "视频数据错误", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
