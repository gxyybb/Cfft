package com.example.cfft.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context context;
    private List<Bitmap> images;

    public ImageAdapter(Context context, List<Bitmap> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Bitmap bitmap = images.get(position);
        holder.imageView.setImageBitmap(bitmap);

        // 设置 ImageView 的大小为固定大小 150dp
        int imageSizeInDp = 150;
        int imageSizeInPixels = (int) (imageSizeInDp * context.getResources().getDisplayMetrics().density); // 将 dp 转换为像素
        ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
        layoutParams.width = imageSizeInPixels;
        layoutParams.height = imageSizeInPixels;
        holder.imageView.setLayoutParams(layoutParams);
    }


    @Override
    public int getItemCount() {
        return images.size();
    }
    public void clearImages() {
        images.clear();
        notifyDataSetChanged();
    }
    public void setImages(List<Bitmap> updatedImages) {
        this.images = updatedImages;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView1);
        }
    }

    // 在您的 Activity 或 Fragment 中设置 GridLayoutManager
    public void setupRecyclerView(RecyclerView recyclerView) {
        int spanCount = 2; // 每行显示两张图片
        GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(this);

    }
}
