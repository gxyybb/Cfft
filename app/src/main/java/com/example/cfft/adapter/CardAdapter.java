package com.example.cfft.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cfft.TargetActivity;
import com.example.cfft.enity.CardItem;
import com.example.cfft.R;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private ArrayList<CardItem> dataList;
    private OkHttpClient client;
    private Context context;

    public CardAdapter(Context context) {
        this.context = context;
        dataList = new ArrayList<>();
        client = new OkHttpClient();
        fetchCardData(); // 获取卡片数据
    }

    private void fetchCardData() {
        Request request = new Request.Builder()
                .url("http://101.200.79.152:8080/categoryList") // 替换为你的服务器端点
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.e("CardAdapter", "Failed to fetch card data: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("CardAdapter", "Response data: " + responseData);
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        int code = jsonObject.getInt("code");
                        if (code == 200) {
                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject itemObject = dataArray.getJSONObject(i);
                                int id = itemObject.getInt("id");
                                String categoryName = itemObject.getString("categoryName");
                                String image = itemObject.getString("image");
                                int number = itemObject.getInt("number");
                                String tips = itemObject.optString("tips", null); // 获取可选字段
                                Log.d("CardAdapter", "Adding card item: " + categoryName);
                                // 创建 CardItem 对象并添加到 dataList 中
                                CardItem cardItem = new CardItem(id, categoryName, image, number, tips);
                                dataList.add(cardItem);
                            }
                            // 在 UI 线程中通知 RecyclerView 更新数据
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notifyDataSetChanged();
                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("CardAdapter", "Failed to parse JSON response: " + e.getMessage());
                    }
                } else {
                    Log.e("CardAdapter", "Failed to fetch card data: " + response.code());
                }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        // 设置列表项目数量为一个很大的值，以实现循环滑动
        return Integer.MAX_VALUE;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 使用取余操作符确定实际要显示的项目位置
        // 如果数据列表为空，则返回
        if (dataList.isEmpty()) {
            return;
        }
        CardItem item = dataList.get(position % dataList.size());
        holder.bind(item);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

        private TextView titleTextView;
        private ImageView imageView;
        private TextView descriptionTextView;
        private TextView hintTextTextView;
        private Context context;
        private CardItem currentItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            titleTextView = itemView.findViewById(R.id.title_text_view);
            imageView = itemView.findViewById(R.id.image_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            hintTextTextView = itemView.findViewById(R.id.hint_text_view);
            itemView.setOnClickListener(this); // 设置点击监听器
        }

        @SuppressLint("SetTextI18n")
        public void bind(CardItem item) {
            currentItem = item;
            titleTextView.setText(item.getCategoryName());
            Glide.with(itemView.getContext())
                    .load(item.getImage())
                    .into(imageView);
            descriptionTextView.setText("该类型菌菇总收录" + item.getNumber() + "种");
            hintTextTextView.setText(item.getTips());
            // 根据标题设置背景色
            switch (item.getCategoryName()) {
                case "可食菌菇":
                    itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorBackground1));
                    break;
                case "慎食菌菇":
                    itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorBackground2));
                    break;
                case "食疑菌菇":
                    itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorBackground3));
                    break;
                case "有毒菌菇":
                    itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorBackground4));
                    break;
                // 添加更多类别和对应的背景色
                default:
                    itemView.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.transparent));
                    break;
            }
        }
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, TargetActivity.class);
            intent.putExtra("title", currentItem.getCategoryName()); // 传递 title
            intent.putExtra("img",currentItem.getImage());
            context.startActivity(intent);
        }

    }
}
