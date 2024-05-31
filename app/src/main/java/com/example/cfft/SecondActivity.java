package com.example.cfft;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cfft.Game1.Main1Activity;
import com.example.cfft.saolei.Main3Activity;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }


    public void startSaoLeiActivity(View view) {
        // 创建意图来启动另一个模块的 activity
        // 创建意图来启动另一个模块的 activity

        Intent intent = new Intent(this, Main3Activity.class);

        startActivity(intent);

    }
    public void start2048Activity(View view) {
        // 创建意图来启动另一个模块的 activity

        Intent intent = new Intent(this, Main1Activity.class);

        startActivity(intent);
    }
}
