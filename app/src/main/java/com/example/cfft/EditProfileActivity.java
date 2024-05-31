package com.example.cfft;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cfft.enity.UserProfile;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView profileImageView, themeBackground;;
    private EditText usernameEditText, nicknameEditText, bioEditText, emailEditText, addressEditText, genderEditText,birthdayEditText;
    private String token;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
//    private static final int REQUEST_GALLERY = 101;
//    private static final int REQUEST_CAMERA = 102;
    private static final int REQUEST_GALLERY_PROFILE = 101;
    private static final int REQUEST_GALLERY_BACKGROUND = 103;
    private static final int REQUEST_CAMERA_PROFILE = 102;
    private static final int REQUEST_CAMERA_BACKGROUND = 104;

    private Uri profileImageUri;
    private Uri backgroundImageUri;
    private String selectedDate;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileImageView = findViewById(R.id.profile_image_view);
        themeBackground = findViewById(R.id.profile_background_view);
        usernameEditText = findViewById(R.id.username_edit_text);
        nicknameEditText = findViewById(R.id.nickname_edit_text);
        bioEditText = findViewById(R.id.bio_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        addressEditText = findViewById(R.id.address_edit_text);
        genderEditText = findViewById(R.id.gender_edit_text);
        birthdayEditText = findViewById(R.id.birthday_edit_text);
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
        profileImageView.setOnClickListener(v -> showImagePickerDialog(true));
        themeBackground.setOnClickListener(v -> showImagePickerDialog(false));

        // 接收传递过来的用户信息和 token
        UserProfile userProfile = (UserProfile) getIntent().getSerializableExtra("userProfile");
        token = getIntent().getStringExtra("token");

        if (userProfile != null) {
            Picasso.get().load(userProfile.getUserImage()).transform(new CircleTransform()).into(profileImageView);
            Picasso.get().load(userProfile.getBackImg()).into(themeBackground);
            usernameEditText.setText(userProfile.getUsername());
            nicknameEditText.setText(userProfile.getNickName());
            genderEditText.setText(userProfile.getGender());
            bioEditText.setText(userProfile.getBio());
            emailEditText.setText(userProfile.getEmail());
            addressEditText.setText(userProfile.getAddress());
            Log.d("shengri",userProfile.getBirthdate());
//            birthdayEditText.setText(userProfile.getBirthdate());
            try {
                // 原始格式
                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
                Date date = originalFormat.parse(userProfile.getBirthdate());

                // 目标格式 "yyyy-MM-dd"
                SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = targetFormat.format(date);

                birthdayEditText.setText(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
                birthdayEditText.setText(userProfile.getBirthdate()); // 如果解析失败，保持原样
            }
        }
        birthdayEditText = findViewById(R.id.birthday_edit_text);
        birthdayEditText.setOnClickListener(v -> showDatePickerDialog());
    }
    private void showImagePickerDialog(boolean isProfile) {
        String[] options = {"相机", "相册"};
        new AlertDialog.Builder(this)
                .setTitle("选择上传方式")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // 拍照
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        } else {
                            openCamera(isProfile);
                        }
                    } else {
                        // 从相册选择
                        openGallery(isProfile);
                    }
                })
                .show();
    }
    private void openCamera(boolean isProfile) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, isProfile ? REQUEST_CAMERA_PROFILE : REQUEST_CAMERA_BACKGROUND);
        }
    }

    private void openGallery(boolean isProfile) {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, isProfile ? REQUEST_GALLERY_PROFILE : REQUEST_GALLERY_BACKGROUND);
    }
    private void showDatePickerDialog() {
        // 获取当前日期
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 创建日期选择器对话框
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                EditProfileActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // 设置选择的日期
                        // 设置选择的日期
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(year, month, dayOfMonth);
                        // 格式化日期为 "yyyy-MM-dd"
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        selectedDate = dateFormat.format(selectedCalendar.getTime());
                        birthdayEditText.setText(selectedDate);
                    }
                },
                year, month, day);

        datePickerDialog.show();
    }


    private void saveUserProfile() {
        String username = usernameEditText.getText().toString();
        String nickname = nicknameEditText.getText().toString();
        String bio = bioEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String address = addressEditText.getText().toString();
        String gender = genderEditText.getText().toString();
        String date = birthdayEditText.getText().toString();
        // Create a MultipartBody request body
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", token);

        if (!username.isEmpty()) {
            builder.addFormDataPart("username", username);
        }
        if (!nickname.isEmpty()) {
            builder.addFormDataPart("nickName", nickname);
        }
        if (!bio.isEmpty()) {
            builder.addFormDataPart("bio", bio);
        }
        if (!email.isEmpty()) {
            builder.addFormDataPart("email", email);
        }
        if (!address.isEmpty()) {
            builder.addFormDataPart("address", address);
        }
        if (!address.isEmpty()) {
            builder.addFormDataPart("gender", gender);
        }
        if (!address.isEmpty()) {
            builder.addFormDataPart("birthdate", date);
        }
        if (profileImageUri != null) {
            File profileImageFile = convertUriToFile(profileImageUri);
            if (profileImageFile != null) {
                builder.addFormDataPart("userImageFile", profileImageFile.getName(),
                        RequestBody.create(profileImageFile, MediaType.parse("image/*")));
            }
        }

        if (backgroundImageUri != null) {
            File backgroundImageFile = convertUriToFile(backgroundImageUri);
            if (backgroundImageFile != null) {
                builder.addFormDataPart("backImgFile", backgroundImageFile.getName(),
                        RequestBody.create(backgroundImageFile, MediaType.parse("image/*")));
            }
        }
        RequestBody requestBody = builder.build();

        // Create a PUT request with the MultipartBody request body
        Request request = new Request.Builder()
                .url("http://101.200.79.152:8080/user") // Replace with your server URL
                .put(requestBody)
                .build();

        // Create an OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Send the PUT request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle success
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK); // 设置结果为 RESULT_OK
                        finish(); // 结束当前页面
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    private File convertUriToFile(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            File file = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if ((requestCode == REQUEST_GALLERY_PROFILE || requestCode == REQUEST_GALLERY_BACKGROUND) && data != null) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    if (requestCode == REQUEST_GALLERY_PROFILE) {
                        profileImageView.setImageBitmap(bitmap);
                        profileImageUri = selectedImage;
                        // 使用Picasso加载并应用圆形转换
                        Picasso.get()
                                .load(selectedImage)
                                .transform(new CircleTransform())
                                .into(profileImageView);
                    } else if (requestCode == REQUEST_GALLERY_BACKGROUND) {
                        themeBackground.setImageBitmap(bitmap);
                        backgroundImageUri = selectedImage;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if ((requestCode == REQUEST_CAMERA_PROFILE || requestCode == REQUEST_CAMERA_BACKGROUND) && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Uri imageUri = getImageUri(imageBitmap);
                if (requestCode == REQUEST_CAMERA_PROFILE) {
                    profileImageView.setImageBitmap(imageBitmap);
                    profileImageUri = imageUri;
                    // 使用Picasso加载并应用圆形转换
                    Picasso.get()
                            .load(imageUri)
                            .transform(new CircleTransform())
                            .into(profileImageView);
                } else if (requestCode == REQUEST_CAMERA_BACKGROUND) {
                    themeBackground.setImageBitmap(imageBitmap);
                    backgroundImageUri = imageUri;
                }
            }
        }
    }


    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }
}
