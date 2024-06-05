package com.example.cfft
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.TextView
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
import com.baidu.mapapi.map.MapPoi

import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.example.cfft.enity.MushRoomVO
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var baiduMap: BaiduMap
    private lateinit var locationClient: LocationClient
    private lateinit var mushroom: MushRoomVO // 将mushroom设为全局变量
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
        option.setScanSpan(100000) // 定位间隔时间
        locationClient.locOption = option

        // 添加地图点击事件监听器
        baiduMap.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            override fun onMapClick(latLng: LatLng?) {
                // 检查是否点击在有效区域内
                latLng?.let {
                    // 移动地图中心到点击位置
                    val update = MapStatusUpdateFactory.newLatLng(latLng)
                    baiduMap.animateMapStatus(update)
                }
            }

            override fun onMapPoiClick(mapPoi: MapPoi?) {

            }
        })


        // 获取传递的数据
        val mushroomJson = intent.getStringExtra("mushroomJson")
        if (mushroomJson != null) {
            val gson = Gson()
            mushroom = gson.fromJson(mushroomJson, MushRoomVO::class.java)
            if (mushroom.locations.isNotEmpty()) {
                val firstLocation = mushroom.locations[0] // 获取第一个蘑菇地点
                val firstLatLng = LatLng(firstLocation.latitude.toDouble(), firstLocation.longitude.toDouble())

                // 移动地图中心到第一个蘑菇地点
                val update = MapStatusUpdateFactory.newLatLng(firstLatLng)
                baiduMap.animateMapStatus(update)
            }

            displayMushroomLocations(mushroom)
        }


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
//            val extraInfo = marker.extraInfo
            val value = mushroom.mushroomDesc
            val value1 = mushroom.mushroomName

            // 创建底部对话框
            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout)

            // 设置底部对话框中的文本
            val textView = bottomSheetDialog.findViewById<TextView>(R.id.textView)
            textView?.text = value
            val nameView = bottomSheetDialog.findViewById<TextView>(R.id.nameView)
            nameView?.text = value1
            // 显示底部对话框
            bottomSheetDialog.show()

            // 返回 true 表示消费了点击事件，false 表示未消费
            true
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
//                    val currentLatLng = LatLng(it.latitude, it.longitude)
//                    val update = MapStatusUpdateFactory.newLatLng(currentLatLng)
//                    baiduMap.animateMapStatus(update)
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

    // 在地图上显示蘑菇分布地点
    private fun displayMushroomLocations(mushroom: MushRoomVO) {
        val originalBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.img)
        val scaledBitmapDescriptor = scaleBitmap(originalBitmapDescriptor, 0.015f)
        for (location in mushroom.locations) {
            val latLng = LatLng(location.latitude.toDouble(), location.longitude.toDouble())

            val markerOptions = MarkerOptions()
                .position(latLng)
                .icon(scaledBitmapDescriptor) // 使用你的标记图标
                .title(location.description)
            baiduMap.addOverlay(markerOptions)
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
