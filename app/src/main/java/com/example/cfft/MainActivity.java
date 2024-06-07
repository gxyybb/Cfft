package com.example.cfft;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.view.MenuItem;
import android.widget.ListView;
import androidx.annotation.NonNull;

import com.example.cfft.fragment.CommunityFragment;
import com.example.cfft.fragment.MyFragment;
import com.example.cfft.fragment.StudyFragment;
import com.example.cfft.fragment.VideoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ListView communityListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 获取传递的 token
        String token = getIntent().getStringExtra("msg");
//        // 打印 token
//        Log.d("MainActivity", "Received token: " + token);
//
//        // 启动要显示的 Fragment
//        CommunityFragment fragment = CommunityFragment.newInstance(token);
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, fragment)
//                .commit();

        // 设置底部导航栏点击事件
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {


                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.navigation_home) {
                            openHomeFragment();
                            // 处理点击Home选项
                            return true;
                        } else if (item.getItemId() == R.id.navigation_dashboard) {
                            // 处理点击Dashboard选项
                            openDashboardFragment();
                            return true;
                        } else if (item.getItemId() == R.id.navigation_notifications) {
                            // 处理点击Notifications选项
                            openNotificationsFragment();
                            return true;
                        } else if (item.getItemId() == R.id.navigation_community) {
                            // 处理点击Community选项
                            // 可以在这里切换显示社区功能页
                            openCommunityFragment();
                            return true;
                        }
                        return false;
                    }

        });
        // 默认打开社区 Fragment
        openCommunityFragment();
    }
// 以下是一些打开Fragment的示例方法，你可以根据自己的需要来实现

        private void openHomeFragment() {
            String token = getIntent().getStringExtra("msg");
            Bundle bundle = new Bundle();
            bundle.putString("token", token);

            MyFragment MyFragment = new MyFragment();
            MyFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, MyFragment)
                    .commit();
        }

        private void openDashboardFragment() {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new StudyFragment())
                    .commit();
        }

        private void openNotificationsFragment() {
            String token = getIntent().getStringExtra("msg");
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            VideoFragment VideoFragment = new VideoFragment();
            VideoFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,  VideoFragment)
                    .commit();
        }

    private void openCommunityFragment() {
        String token = getIntent().getStringExtra("msg");
        Bundle bundle = new Bundle();
        bundle.putString("token", token);

        CommunityFragment communityFragment = new CommunityFragment();
        communityFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, communityFragment)
                .commit();
    }


}


