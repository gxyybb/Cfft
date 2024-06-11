package com.example.cfft.adapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.CircleTransform;
import com.example.cfft.enity.CommunityItem;
import com.example.cfft.R;
import com.example.cfft.enity.Post;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {
    private static String token;

    private Context mContext;
    private ArrayList<CommunityItem> mCommunityList;
    private Activity mActivity;

    public CommunityAdapter(Activity activity, Context context, ArrayList<CommunityItem> communityList, String token) {
        this.token = token;
        mContext = context;
        mCommunityList = communityList != null ? communityList : new ArrayList<>();
        mActivity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_community, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CommunityItem currentItem = mCommunityList.get(position);

        Picasso.get().load(currentItem.getUserImg()).transform(new CircleTransform()).into(holder.userAvatarImageView);
        holder.userNameTextView.setText(currentItem.getUserName());
        holder.contentTitleView.setText(currentItem.getTitle());
        holder.contentTextView.setText(currentItem.getContent());

        holder.imageLayout.removeAllViews();
        List<String> imageUrls = currentItem.getImg();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size() && i < 2; i++) {
                ImageView imageView = new ImageView(mContext);
                int imageSize = (int) (150 * mContext.getResources().getDisplayMetrics().density);
//                ViewGroup.MarginLayoutParams layoutParams1 = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
//                layoutParams1.setMargins(0, 8, 8, 0); // 设置左右间距
//                imageView.setLayoutParams(layoutParams1);
                ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(imageSize, imageSize);
                // 设置左右间距
                layoutParams.setMargins(0, 8, 8, 0);
                imageView.setLayoutParams(layoutParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                // 使用Picasso加载图片，并裁剪为方形
                Picasso.get()
                        .load(imageUrls.get(i))
                        .resize(imageSize, imageSize) // 设置宽高
                        .centerCrop() // 中心裁剪
                        .into(imageView);
                holder.imageLayout.addView(imageView);
                // 设置点击事件
                String imageUrl = imageUrls.get(i);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImageDialog(imageUrl);
                    }
                });
            }
        }

        holder.likeTextView.setText(String.valueOf(currentItem.getLikeCount()));

        holder.likeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLikeCountOnServer(currentItem.getPostId(), holder.likeTextView);
            }
        });

        holder.commentTextView.setText(String.valueOf(currentItem.getCommentCount()));
    }
    private void showImageDialog(String imageUrl) {
        Dialog dialog = new Dialog(mContext,R.style.FullScreenDialog);
        dialog.setContentView(R.layout.dialog_image);

        ImageView imageView = dialog.findViewById(R.id.dialogImageView);
        Picasso.get().load(imageUrl).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return mCommunityList.size();
    }
    public void updateData(List<CommunityItem> newDataList) {
        mCommunityList.clear();
        if (newDataList != null) {
            mCommunityList.addAll(newDataList);
        }
        notifyDataSetChanged();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userAvatarImageView;
        TextView userNameTextView;
        TextView contentTitleView;
        TextView contentTextView;
        ViewGroup imageLayout;
        TextView likeTextView;
        TextView commentTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            userAvatarImageView = itemView.findViewById(R.id.userAvatarImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            contentTitleView = itemView.findViewById(R.id.titleTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            imageLayout = itemView.findViewById(R.id.imageLayout);
            likeTextView = itemView.findViewById(R.id.likeTextView);
            commentTextView = itemView.findViewById(R.id.commentEditText);
        }
    }


    // 更新服务器上的点赞数
    private void updateLikeCountOnServer(int postId, final TextView likeTextView) {
        OkHttpClient client = new OkHttpClient();
        // 构建请求体
        RequestBody formBody = new FormBody.Builder()
                .add("postId", String.valueOf(postId))
                .add("token", token)
                .build();

        // 创建请求
        Request request = new Request.Builder()
                .url("http://101.200.79.152:8080/post/like") // 替换为您的服务器端点
                .post(formBody)
                .build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        if (!jsonObject.isNull("data")) {
                            final int updatedLikeCount = jsonObject.getInt("data");
                            // 更新 UI 上的点赞数
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 更新点赞文本视图
                                    likeTextView.setText(String.valueOf(updatedLikeCount));
                                }
                            });
                        } else {
                            Log.e("UpdateLikeCount", "JSON object has null or missing 'data' field");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    // 处理请求失败或其他错误情况
                    Log.e("UpdateLikeCount", "Request failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                // 处理请求失败，如果需要的话
                Log.e("UpdateLikeCount", "Request failed: " + e.getMessage());
            }
        });

    }


//    public int getTotalHeight() {
//        // 计算ListView的总高度
//        int itemHeight = getItemHeight();
//        return itemHeight * getCount(); // getCount() 方法返回适配器中item的数量
//    }
//
//    public void setListViewHeight(ListView listView) {
//        // 设置ListView的高度
//        ViewGroup.LayoutParams params = listView.getLayoutParams();
//        params.height = (int) (getTotalHeight()*1.);
//        Log.d("wuw",String.valueOf(params.height));
//        listView.setLayoutParams(params);
//    }
}