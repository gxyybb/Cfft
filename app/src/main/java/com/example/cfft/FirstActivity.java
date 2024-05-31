package com.example.cfft;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.cfft.adapter.CardAdapter;

public class FirstActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private CardAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        viewPager = findViewById(R.id.viewPager);
        adapter = new CardAdapter(this);
        viewPager.setAdapter(adapter);

        // 数据加载完成后通知 ViewPager2 更新
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                viewPager.setCurrentItem(adapter.getItemCount() / 2, false); // 将 ViewPager2 滚动到中间位置
            }
        });
    }
}

