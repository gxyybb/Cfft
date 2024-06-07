package com.example.cfft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.SynthesizerTool;
import com.baidu.tts.client.TtsMode;

import com.example.cfft.control.InitConfig;
import com.example.cfft.control.MySyntherizer;
import com.example.cfft.control.NonBlockSyntherizer;
import com.example.cfft.listener.UiMessageListener;
import com.example.cfft.util.Auth;
import com.example.cfft.util.AutoCheck;
import com.example.cfft.util.FileUtil;
import com.example.cfft.util.IOfflineResourceConst;
import com.example.cfft.util.OfflineResource;

public class SynthActivity extends BaseActivity implements View.OnClickListener {

    protected String appId;
    protected String appKey;
    protected String secretKey;
    protected String sn;
    protected TtsMode ttsMode = IOfflineResourceConst.DEFAULT_SDK_TTS_MODE;
    protected boolean isOnlineSDK = TtsMode.ONLINE.equals(IOfflineResourceConst.DEFAULT_SDK_TTS_MODE);
    protected String offlineVoice = OfflineResource.VOICE_MALE;
    protected MySyntherizer synthesizer;
    protected int descTextId = R.raw.sync_activity_description;
    private static final String TAG = "com.example.cfft.SynthActivity";
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestStoragePermission();
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            initialize();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initialize();
            } else {
                // 用户拒绝了权限请求，可以向用户解释为什么应用程序需要这些权限，并提供手动授予权限的方法
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // 向用户解释为什么需要这个权限
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("This app needs storage permission to function properly.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 用户点击了确定，重新请求权限
                                    ActivityCompat.requestPermissions(SynthActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 用户点击了取消，可以采取适当的操作，比如关闭应用程序
                                    finish();
                                }
                            })
                            .show();
                } else {
                    // 用户选择了不再询问，需要引导用户手动授予权限
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("You have denied storage permission. Please grant the permission manually from the settings.")
                            .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 打开应用程序的设置界面，让用户手动授予权限
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 用户点击了取消，可以采取适当的操作，比如关闭应用程序
                                    finish();
                                }
                            })
                            .show();
                }
            }
        }
    }


    private void initialize() {
        try {
            Auth.getInstance(this);
        } catch (Auth.AuthCheckException e) {
            mShowText.setText(e.getMessage());
            return;
        }
        mShowText.setText(FileUtil.getResourceText(this, descTextId));

        appId = Auth.getInstance(this).getAppId();
        appKey = Auth.getInstance(this).getAppKey();
        secretKey = Auth.getInstance(this).getSecretKey();
        sn = Auth.getInstance(this).getSn();
        initializeOfflineResource(); // 初始化离线资源
        initialButtons();
        initialTts();

        if (!isOnlineSDK) {
            Log.i(TAG, "so version:" + SynthesizerTool.getEngineInfo());
        }
    }

    private void initializeOfflineResource() {
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        if (offlineResource == null) {
            Toast.makeText(this, "Failed to initialize offline resource", Toast.LENGTH_SHORT).show();
            return;
        }
        int result = synthesizer.loadModel(offlineResource.getModelFilename(), offlineResource.getTextFilename());
        if (result != 0) {
            Toast.makeText(this, "Failed to load offline model", Toast.LENGTH_SHORT).show();
        }
    }

    private void initialButtons() {
        Button loadModel = findViewById(R.id.loadModel);
        if (isOnlineSDK) {
            loadModel.setText("");
            loadModel.setEnabled(false);
        } else {
            buttons.add(loadModel);
        }
        for (Button b : buttons) {
            b.setOnClickListener(this);
            b.setEnabled(false);
        }
        mHelp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.speak) {
            speak();
        } else if (id == R.id.synthesize) {
            synthesize();
        } else if (id == R.id.batchSpeak) {
            batchSpeak();
        } else if (id == R.id.loadModel) {
            showModelSelectionDialog();
        } else if (id == R.id.pause) {
            pause();
        } else if (id == R.id.resume) {
            resume();
        } else if (id == R.id.stop) {
            stop();
        } else if (id == R.id.help) {
            mShowText.setText(FileUtil.getResourceText(this, descTextId));
        }
    }

    private void showModelSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_Dialog);
        builder.setTitle("引擎空闲时切换");
        final Map<String, String> map = new LinkedHashMap<>(4);
        map.put("离线女声", OfflineResource.VOICE_FEMALE);
        map.put("离线男声", OfflineResource.VOICE_MALE);
        map.put("离线度逍遥", OfflineResource.VOICE_DUXY);
        map.put("离线度丫丫", OfflineResource.VOICE_DUYY);
        final String[] keysTemp = new String[4];
        final String[] keys = map.keySet().toArray(keysTemp);
        builder.setItems(keys, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadModel(map.get(keys[which]));
            }
        });
        builder.show();
    }

    private void loadModel(String mode) {
        offlineVoice = mode;
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        toPrint("切换离线语音：" + offlineResource.getModelFilename());
        int result = synthesizer.loadModel(offlineResource.getModelFilename(), offlineResource.getTextFilename());
        checkResult(result, "loadModel");
    }

    protected void initialTts() {
        LoggerProxy.printable(true);
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);
        InitConfig config = getInitConfig(listener);
        synthesizer = new NonBlockSyntherizer(this, config, mainHandler);
    }

    protected InitConfig getInitConfig(SpeechSynthesizerListener listener) {
        Map<String, String> params = getMyParams();
        InitConfig initConfig;
        if (sn == null) {
            initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        } else {
            initConfig = new InitConfig(appId, appKey, secretKey, sn, ttsMode, params, listener);
        }
        AutoCheck.getInstance(getApplicationContext()).check(initConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                        toPrint(message);
                    }
                }
            }
        });
        return initConfig;
    }

    private Map<String, String> getMyParams() {
        Map<String, String> params = new HashMap<>();
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        params.put(SpeechSynthesizer.PARAM_VOLUME, "15");
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");
        if (!isOnlineSDK) {
            OfflineResource offlineResource = createOfflineResource(offlineVoice);
            if (offlineResource == null) {
                Log.e(TAG, "OfflineResource is null");
            } else {
                String textFilename = offlineResource.getTextFilename();
                if (textFilename == null) {
                    Log.e(TAG, "Text filename is null");
                } else {
                    params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, textFilename);
                }
                String modelFilename = offlineResource.getModelFilename();
                if (modelFilename == null) {
                    Log.e(TAG, "Model filename is null");
                } else {
                    params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, modelFilename);
                }
            }
        }
        return params;
    }


    private OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            toPrint("【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }

    private void speak() {
        String text = mInput.getText().toString();
        if (TextUtils.isEmpty(text)) {
            text = "百度语音，面向广大开发者永久免费开放语音合成技术。";
            mInput.setText(text);
        }
        int result = synthesizer.speak(text);
        checkResult(result, "speak");
    }

    private void synthesize() {
        String text = mInput.getText().toString();
        if (TextUtils.isEmpty(text)) {
            text = "百度语音，面向广大开发者永久免费开放语音合成技术。";
            mInput.setText(text);
        }
        int result = synthesizer.synthesize(text);
        checkResult(result, "synthesize");
    }

    private void batchSpeak() {
        List<Pair<String, String>> texts = new ArrayList<>();
        String text1 = "123456";
        String text2 = "百度语音，面向广大开发者永久免费开放语音合成技术。";
        texts.add(new Pair<>(text1, "a0"));
        texts.add(new Pair<>(text2, "a1"));
        int result = synthesizer.batchSpeak(texts);
        checkResult(result, "batchSpeak");
    }

    private void pause() {
        int result = synthesizer.pause();
        checkResult(result, "pause");
    }

    private void resume() {
        int result = synthesizer.resume();
        checkResult(result, "resume");
    }

    private void stop() {
        int result = synthesizer.stop();
        checkResult(result, "stop");
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            Log.e(TAG, "error code :" + result + " method:" + method);
        }
    }
}
