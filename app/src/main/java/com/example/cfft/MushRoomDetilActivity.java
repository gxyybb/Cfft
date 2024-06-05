package com.example.cfft;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.cfft.adapter.ImagePagerAdapter;
import com.example.cfft.enity.MushRoomVO;
import com.google.gson.Gson;

public class MushRoomDetilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mushroom_detail); // Assuming you have a layout file named activity_mushroom_detail.xml

        // Retrieve the JSON string from the intent extra
        String mushroomJson = getIntent().getStringExtra("mushroomJson");

        // Deserialize the JSON string into a MushRoomVO object
        Gson gson = new Gson();
        MushRoomVO mushroom = gson.fromJson(mushroomJson, MushRoomVO.class);


        if (mushroom != null) {
// 在您的 Activity 或 Fragment 中
            ViewPager viewPager = findViewById(R.id.viewPager1);
            ImagePagerAdapter adapter = new ImagePagerAdapter(this, mushroom.getMushroomImages());
            viewPager.setAdapter(adapter);

            TextView mushroomNameTextView = findViewById(R.id.mushroomNameTextView);
            mushroomNameTextView.setText(mushroom.getMushroomName());
            TextView typeTextView = findViewById(R.id.typeTextView);
            typeTextView.setText(mushroom.getCategory());
            TextView CaneatTextView = findViewById(R.id.CaneatTextView);
            // 根据isEat和isPosion的值设置显示文本
            if (mushroom.getIsEat() == 1 && mushroom.getIsPoison() == 0) {
                // 可以吃，没有毒
                CaneatTextView.setTextColor(Color.GREEN);
                CaneatTextView.setText("可以食用");
            } else if (mushroom.getIsEat() == 1 && mushroom.getIsPoison() == 1) {
                // 慎吃，有毒
                CaneatTextView.setTextColor(Color.RED);
                CaneatTextView.setText("慎食");
            } else if (mushroom.getIsEat() == 0 && mushroom.getIsPoison() == 0) {
                // 不可吃，没有毒
                CaneatTextView.setTextColor(Color.RED);
                CaneatTextView.setText("不能食用");
            } else {
                // 不可吃，有毒
                CaneatTextView.setTextColor(Color.RED);
                CaneatTextView.setText("不能食用，有毒");
            }

            TextView LocationTextView1 = findViewById(R.id.LocationTextView1);
            LocationTextView1.setText(mushroom.getMushroomLocation());
            TextView DescTextView11 = findViewById(R.id.DescTextView11);
            DescTextView11.setText(mushroom.getMushroomDesc());
            // 为 LocationTextView1 添加点击事件
            LocationTextView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MushRoomDetilActivity.this, MapActivity.class);
                    intent.putExtra("mushroomJson", mushroomJson);
                    startActivity(intent);
                }
            });
        }
    }
}
