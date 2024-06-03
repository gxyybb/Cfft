package com.example.cfft
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var baiduMap: BaiduMap
    private lateinit var locationClient: LocationClient
    // 调整图片资源的缩放比例
    private fun scaleBitmap(bitmapDescriptor: BitmapDescriptor, scale: Float): BitmapDescriptor {
        val originBitmap = bitmapDescriptor.bitmap
        val newWidth = (originBitmap.width * scale).toInt()
        val newHeight = (originBitmap.height * scale).toInt()
        val resizedBitmap = Bitmap.createScaledBitmap(originBitmap, newWidth, newHeight, false)
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化百度地图 SDK
        SDKInitializer.setAgreePrivacy(applicationContext, true);
        SDKInitializer.initialize(applicationContext);
        Log.d("MapActivity", "Privacy policy agreed and SDK initialized")
        setContentView(R.layout.activity_map)

        // 获取 MapView
        mapView = findViewById(R.id.bmapView)
        baiduMap = mapView.map
        baiduMap.isMyLocationEnabled = true // 启用定位
        // 设置地图类型
        baiduMap.mapType = BaiduMap.MAP_TYPE_NORMAL
        // 初始化LocationClient
        LocationClient.setAgreePrivacy(true)
        locationClient = LocationClient(applicationContext)

        val option = LocationClientOption()
        option.isOpenGps = true // 打开GPS
        option.setCoorType("bd09ll") // 设置百度坐标类型
        option.setScanSpan(1000) // 定位间隔时间
        locationClient.locOption = option
// 添加点标记
        val originalBitmapDescriptor1 = BitmapDescriptorFactory.fromResource(R.drawable.img)
        val scaledBitmapDescriptor = scaleBitmap(originalBitmapDescriptor1, 0.015f) // 缩放比例为0.5

// 添加点标记
        val markerOptions = MarkerOptions()
            .position(LatLng(39.90923, 116.397428)) // 设置位置
            .icon(scaledBitmapDescriptor) // 设置缩放后的图标
            .animateType(MarkerOptions.MarkerAnimateType.grow) // 设置生长动画
            .zIndex(10) // 设置层级
            .draggable(true) // 设置可拖拽
            .title("Marker Title") // 设置标题
            .extraInfo(Bundle().apply {
                // 可以在这里设置一些额外信息
                putString("key", "value")
            })
        baiduMap.addOverlay(markerOptions)

        // 设置点标记点击事件
        baiduMap.setOnMarkerClickListener { marker ->
            // 获取额外信息
            val extraInfo = marker.extraInfo
            val value = extraInfo.getString("key")
            // 在这里处理点击事件，比如显示信息窗口
            true // 返回 true 表示消费了点击事件，false 表示未消费
        }
        // 设置定位监听器
        locationClient.registerLocationListener(object : BDAbstractLocationListener() {
            override fun onReceiveLocation(location: BDLocation?) {
                location?.let {
                    val locData = MyLocationData.Builder()
                        .accuracy(it.radius)
                        .latitude(it.latitude)
                        .longitude(it.longitude)
                        .build()

                    // 创建自定义定位图标
                    // 在 onReceiveLocation 方法中使用
                    val bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.img)
                    val adjustedBitmapDescriptor = scaleBitmap(bitmapDescriptor, 0.015f) // 0.5f 为缩放比例
                    // 设置自定义定位图标
                    val myLocationConfig = MyLocationConfiguration(
                        MyLocationConfiguration.LocationMode.NORMAL,
                        true, // 是否显示方向信息

                        adjustedBitmapDescriptor,
                        ContextCompat.getColor(applicationContext, R.color.accuracy_circle_fill_color), // 填充颜色
                        ContextCompat.getColor(applicationContext, R.color.accuracy_circle_stroke_color) // 边框颜色
                    )
                    baiduMap.setMyLocationConfiguration(myLocationConfig)
                    // 将地图中心移动到当前位置
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    val update = MapStatusUpdateFactory.newLatLng(currentLatLng)
                    baiduMap.animateMapStatus(update)
                    baiduMap.setMyLocationData(locData)
                }
            }
        })

        // 检查权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            locationClient.start()
        }
    }

    override fun onResume() {
        super.onResume()
        // 在 onResume 方法中调用 MapView 的 onResume 方法
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        // 在 onPause 方法中调用 MapView 的 onPause 方法
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放地图资源
        mapView.onDestroy()
        locationClient.stop()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationClient.start()
        }
    }
}
