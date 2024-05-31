package com.example.cfft.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.DetailActivity;
import com.example.cfft.MushRoomDetilActivity;
import com.example.cfft.R;
import com.example.cfft.TargetActivity;
import com.example.cfft.enity.MushRoomVO;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class TargetAdapter extends RecyclerView.Adapter<TargetAdapter.ViewHolder> {

    private List<MushRoomVO> mushroomList;
    private Context context;

    public TargetAdapter(Context context, List<MushRoomVO> mushroomList) {
        this.context = context;
        this.mushroomList = mushroomList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.targat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MushRoomVO mushroom = mushroomList.get(position);
        holder.bind(mushroom);
    }

    @Override
    public int getItemCount() {
        return mushroomList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mushroomNameTextView;
        ImageView mushroomImageView;
        TextView mushroomTextView;
        private MushRoomVO mushroom;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mushroomNameTextView = itemView.findViewById(R.id.mushroomNameTextView);
            mushroomImageView = itemView.findViewById(R.id.mushroomImageView);
            mushroomTextView = itemView.findViewById(R.id.mushroomTextView);

            itemView.setOnClickListener(this);
        }

        public void bind(MushRoomVO mushroom) {
            this.mushroom = mushroom;
            mushroomNameTextView.setText(mushroom.getMushroomName());
            mushroomTextView.setText(mushroom.getCategory());
// 加载蘑菇图片并设置为圆形
            if (!mushroom.getMushroomImages().isEmpty()) {
                // 加载第一张蘑菇图片
                String imageUrl = mushroom.getMushroomImages().get(0);
            // 使用Picasso加载图片
            Picasso.get().load(imageUrl).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    // 将图像设置为圆形
                    Bitmap circularBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(circularBitmap);
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                    float radius = bitmap.getWidth() > bitmap.getHeight() ? bitmap.getHeight() / 2f : bitmap.getWidth() / 2f;
                    canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, radius, paint);
                    mushroomImageView.setImageBitmap(circularBitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    // 加载失败时设置默认图片
                    mushroomImageView.setImageResource(R.drawable.img_1);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    // 在加载之前设置占位符图片
                    mushroomImageView.setImageResource(R.drawable.img_1);
                }
            });
        } else {
            // 设置默认图片
            mushroomImageView.setImageResource(R.drawable.img_1);
        }
        }

        @Override
        public void onClick(View v) {
            // 点击事件中获取蘑菇数据并传递到详情页
            Intent intent = new Intent(context, MushRoomDetilActivity.class);
            Gson gson = new Gson();
            String mushroomJson = gson.toJson(mushroom);
            intent.putExtra("mushroomJson", mushroomJson);
            context.startActivity(intent);
        }
    }
}
