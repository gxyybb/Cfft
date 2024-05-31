package com.example.cfft;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cfft.adapter.ImageAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.*;

public class PublishActivity extends Activity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    private EditText userNameEditText;
    private EditText contentEditText;
    private LinearLayout imageLayout;
    private List<Bitmap> images = new ArrayList<>();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 每行两列
        adapter = new ImageAdapter(this, images);
        recyclerView.setAdapter(adapter);
        userNameEditText = findViewById(R.id.editText_userName);
        contentEditText = findViewById(R.id.editText_content);
//        imageLayout = findViewById(R.id.imageLayout);
        ImageButton addImageButton = findViewById(R.id.button_addImage);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickIntent, REQUEST_IMAGE_PICK);
            }
        });
        ImageButton buttonCamera = findViewById(R.id.button_camare);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(PublishActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // 如果没有相机权限，则请求相机权限
                    ActivityCompat.requestPermissions(PublishActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_REQUEST_CODE);
                } else {
                    // 如果已经有相机权限，则可以执行启动相机应用的操作
                    dispatchTakePictureIntent();
                }

            }
        });
        ImageButton buttonDelete = findViewById(R.id.button_delete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearImages();
            }
        });
        ImageButton publishButton = findViewById(R.id.button_publish);
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户输入的信息
                String userName = userNameEditText.getText().toString();
                String content = contentEditText.getText().toString();
                String token = getIntent().getStringExtra("token");
                String strs = token;
                sendDataToServer(userName, content, strs, images);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户同意了相机权限，执行启动相机应用的操作
                dispatchTakePictureIntent();
            } else {
                // 用户拒绝了相机权限，可以给出相应提示或处理
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                // 从相册选择图片
                if (data != null && data.getData() != null) {
                    try {
                        Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                        // 检查图片尺寸，如果超出限制则进行缩放
                        int maxWidth = 800; // 设置最大宽度
                        int maxHeight = 800; // 设置最大高度
                        Bitmap scaledBitmap = scaleBitmapIfNeeded(originalBitmap, maxWidth, maxHeight);

                        // 将缩放后的图片添加到 images 列表中
                        images.add(scaledBitmap);

                        // 显示选择的图片
                        showSelectedImages(); // 调用此方法显示选定的图片
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // 拍照
                if (data != null && data.getExtras() != null) {
                    Bitmap originalBitmap = (Bitmap) data.getExtras().get("data");

                    // 检查图片尺寸，如果超出限制则进行缩放
                    int maxWidth = 800; // 设置最大宽度
                    int maxHeight = 800; // 设置最大高度
                    Bitmap scaledBitmap = scaleBitmapIfNeeded(originalBitmap, maxWidth, maxHeight);

                    // 将缩放后的图片添加到 images 列表中
                    images.add(scaledBitmap);

                    // 显示拍摄的照片
                    showSelectedImages(); // 调用此方法显示拍摄的照片
                }
            }
        }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    // 缩放图片方法
    private Bitmap scaleBitmapIfNeeded(Bitmap bitmap, int maxWidth, int maxHeight) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            // 需要缩放
            float scale = Math.min((float) maxWidth / originalWidth, (float) maxHeight / originalHeight);
            int newWidth = Math.round(originalWidth * scale);
            int newHeight = Math.round(originalHeight * scale);
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        } else {
            // 不需要缩放
            return bitmap;
        }
    }


    private void clearImages() {
        for (Bitmap bitmap : images) {
            // 释放位图对象的内存
            bitmap.recycle();
        }
        // 清除对位图列表的引用
        images.clear();
        // 清除适配器中的图片列表
        adapter.clearImages();
        // 通知适配器数据集已更新
        adapter.notifyDataSetChanged();
    }

    private void showSelectedImages() {
        // 更新图片列表
        List<Bitmap> updatedImages = new ArrayList<>(images);
        // 通知适配器数据集已更新
        adapter.setImages(updatedImages);
        adapter.notifyDataSetChanged();
    }


    private void sendDataToServer(String title, String content, String strs, List<Bitmap> images) {
        OkHttpClient client = new OkHttpClient();

        // 构建 Multipart 请求体
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", title)
                .addFormDataPart("content", content)
                .addFormDataPart("token", strs);

        // 添加图片文件
        for (int i = 0; i < images.size(); i++) {
            Bitmap bitmap = images.get(i);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            requestBodyBuilder.addFormDataPart("images", "image" + i + ".jpg",
                    RequestBody.create(MediaType.parse("image/jpeg"), byteArray));
        }
        RequestBody requestBody = requestBodyBuilder.build();
        // 构建请求
        Request request = new Request.Builder()
                .url("http://101.200.79.152:8080/post") // 替换成你的后端 API 地址
                .post(requestBody)
                .build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 请求成功
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // 打印响应内容到日志
                    Log.d("Response", responseBody);
                    // 可以在这里处理服务器返回的响应，例如显示成功提示等
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PublishActivity.this, "Data sent successfully", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // 返回数据给上一个 Activity（当前为 Fragment）
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("title", title);
                    resultIntent.putExtra("content", content);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish(); // 关闭当前 Activity，返回到上一个 Activity（当前为 Fragment）
                } else {
                    // 请求失败
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PublishActivity.this, "Failed to send data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 请求失败
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PublishActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}