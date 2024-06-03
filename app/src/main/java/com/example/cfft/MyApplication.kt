package com.example.cfft



import android.app.Application
import com.baidu.mapapi.SDKInitializer

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 同意隐私政策
        SDKInitializer.setAgreePrivacy(applicationContext, true)
        SDKInitializer.initialize(applicationContext)
    }
}
