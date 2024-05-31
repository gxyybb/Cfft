package com.example.cfft.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cfft.FirstActivity;
import com.example.cfft.R;
import com.example.cfft.SecondActivity;

public class StudyFragment extends Fragment implements View.OnClickListener {

    private ImageButton imageButton1;
    private ImageButton imageButton2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study, container, false);

        // 初始化按钮
        imageButton1 = view.findViewById(R.id.imageButton1);
        imageButton2 = view.findViewById(R.id.imageButton2);

        // 设置按钮点击事件监听器
        imageButton1.setOnClickListener(this);
        imageButton2.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        // 根据点击的按钮进行相应的跳转
        if (v.getId() == R.id.imageButton1) {
            // 跳转到某个页面，这里用一个示例 Activity 替代
            Intent intent1 = new Intent(getActivity(), FirstActivity.class);
            startActivity(intent1);
        } else if (v.getId() == R.id.imageButton2) {
            // 跳转到另一个页面，这里用一个示例 Activity 替代
            Intent intent2 = new Intent(getActivity(), SecondActivity.class);
            startActivity(intent2);
        }
    }

}
