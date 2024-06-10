package com.example.cfft

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

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
        SDKInitializer.setAgreePrivacy(applicationContext, true)
        SDKInitializer.initialize(applicationContext)
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
        fetchLocationsFromServer()

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

        // 设置标记点击事件
        baiduMap.setOnMarkerClickListener { marker ->
            // 获取额外信息
            val description = marker.extraInfo.getString("description")
            val title1 = marker.extraInfo.getString("title")
            // 创建底部对话框
            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout)

            // 设置底部对话框中的文本
            val textView = bottomSheetDialog.findViewById<TextView>(R.id.textView)
            textView?.text = description
            val nameView = bottomSheetDialog.findViewById<TextView>(R.id.nameView)
            nameView?.text =title1
            Log.d("11", title1.toString())
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
                    val bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.location1)
                    val adjustedBitmapDescriptor = scaleBitmap(bitmapDescriptor, 0.15f) // 0.5f 为缩放比例

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

    private fun fetchLocationsFromServer() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://101.200.79.152:8080/location") // 替换为你的服务器URL
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 处理失败
                runOnUiThread {
                    Toast.makeText(this@MapActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let {
                        val gson = Gson()
                        val locationResponse = gson.fromJson(it, LocationResponse::class.java)
                        if (locationResponse.success) {
                            runOnUiThread {
                                displayServerLocations(locationResponse.data)
                                Log.d("MapActivity", "Location data from server: $it")
                            }
                        }
                    }
                }
            }
        })
    }

    // 在地图上显示蘑菇分布地点
    private fun displayServerLocations(locations: List<LocationData>) {
        val originalBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.location)
        val scaledBitmapDescriptor = scaleBitmap(originalBitmapDescriptor, 0.2f)

        for (location in locations) {
            val latLng = LatLng(location.latitude, location.longitude)

            val markerOptions = MarkerOptions()
                .position(latLng)
                .icon(scaledBitmapDescriptor)
//                .title("${location.province} - ${location.city}") // 设置标题为省市信息
                .extraInfo(Bundle().apply {
                    putString("description", location.description ?: "")
                    putString("title", "${location.province} - ${location.city}") // 设置额外信息为描述，如果为空则设置为空字符串
                })
            baiduMap.addOverlay(markerOptions)
        }
    }

    // 在地图上显示蘑菇分布地点
    private fun displayMushroomLocations(mushroom: MushRoomVO) {
        val originalBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.location)
        val scaledBitmapDescriptor = scaleBitmap(originalBitmapDescriptor, 0.2f)
        for (location in mushroom.locations) {
            val latLng = LatLng(location.latitude.toDouble(), location.longitude.toDouble())

            val markerOptions = MarkerOptions()
                .position(latLng)
                .icon(scaledBitmapDescriptor)
//                .title(location.description ?: "") // 设置标题为描述，如果为空则设置为空字符串
                .extraInfo(Bundle().apply {
                    putString("description", location.description ?: "")
                    putString("title", "${location.province} - ${location.city}") // 设置额外信息为描述，如果为空则设置为空字符串
                })
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

data class LocationResponse(
    val code: Int,
    val msg: String?,
    val data: List<LocationData>,
    val success: Boolean
)

data class LocationData(
    val id: Int,
    val province: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val description: String?
)
