package com.example.cfft.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.cfft.R;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {

    private Context context;
    private List<String> imageUrls;

    public ImagePagerAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // 使用LayoutInflater从布局文件实例化视图
        View view = LayoutInflater.from(context).inflate(R.layout.view_pager_item, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);
        String imageUrl = imageUrls.get(position);

        // 创建 RoundedTransformationBuilder 对象，并设置圆角半径和边距
        int radius = 25;
        int borderWidth = 1; // 设置边框宽度
        int borderColor = Color.BLACK; // 设置边框颜色为黑色

// 创建 RoundedTransformationBuilder 对象，并设置圆角半径和边框
        RoundedTransformationBuilder roundedTransformationBuilder = new RoundedTransformationBuilder()
                .cornerRadiusDp(radius) // 设置圆角半径
                .oval(false) // 如果需要的话，设置为 true 以将图像裁剪为圆形
                .borderWidthDp(borderWidth) // 设置边框宽度
                .borderColor(borderColor); // 设置边框颜色

        Transformation transformation = roundedTransformationBuilder.build();

// 使用 Picasso 加载图像并应用转换
        Picasso.get().load(imageUrl).transform(transformation).into(imageView);

        container.addView(view);
        return view;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
