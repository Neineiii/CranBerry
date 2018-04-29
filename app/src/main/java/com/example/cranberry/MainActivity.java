package com.example.cranberry;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public LocationClient mLocationClient;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLoc = true;
    private DrawerLayout mDrawerLayout;

    private MyOrientationListener myOrientationListener;

    float mCurrentX;

    private double currentLatitude;
    private double currentLongitude;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化地图并获得mapView实例
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            /**
             * 显示导航按钮
             * 默认图标叫HomeAsUp，是一个返回箭头
             * id为R.id.home
             * 功能是返回上一个活动
             */
            actionBar.setDisplayHomeAsUpEnabled(true);
            // 对默认图标进行更改
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }


        NavigationView navView= (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.nav_camera:
                        Toast.makeText(MainActivity.this, "Camera",Toast.LENGTH_SHORT).
                                show();
                        break;
                    default:
                }
                return true;
            }
        });


        mapView = (MapView) findViewById(R.id.bmapView);

        // 获得BaiduMap实例
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        // 对定位图标进行配置

        MyLocationConfiguration myLocationConfiguration =
                new MyLocationConfiguration(
                        MyLocationConfiguration.LocationMode.FOLLOWING, true,
                        null);
        baiduMap.setMyLocationConfiguration(myLocationConfiguration);

        // 将没获得授权的权限放入一个List集合中，集中询问权限
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BODY_SENSORS)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.BODY_SENSORS);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            // 获得用户权限，得到结果后回调onRequestPermissionsResult()方法
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }else{
            requestLocation();
        }

    }

    /**
     * 修改图标功能
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.START);
                break;
            case R.id.search:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }

    /**
     * 生命周期配置
     */
    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
        myOrientationListener.start();
    }
    @Override
    protected  void onPause(){
        super.onPause();
        mapView.onPause();
        myOrientationListener.stop();
    }
    @Override
    protected  void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    private void requestLocation(){
        initMyLocation();
        mLocationClient.start();
        initOrientationListener();
    }

    // 将用户授权的结果通过参数传递到该方法中，对结果进行操作
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "需同意所有权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location){

            // map view 销毁后不在处理新接收的位置
            if (location == null || mapView == null)
                return;

            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(mCurrentX)
                    .latitude(currentLatitude)
                    .longitude(currentLongitude)
                    .build();
            baiduMap.setMyLocationData(locData);

            /**
             * 如果是第一次定位，将地图移到当前位置
             */
            if(isFirstLoc){
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                baiduMap.animateMapStatus(update);
                update = MapStatusUpdateFactory.zoomTo(16.0f);
                baiduMap.animateMapStatus(update);
                isFirstLoc = false;
            }

        }
    }

    // 初始化方向传感器
    private void initOrientationListener()
    {
        myOrientationListener = new MyOrientationListener(getApplicationContext());
        myOrientationListener
                .setOnOrientationListener(new MyOrientationListener.OnOrientationListener()
                {
                    @Override
                    public void onOrientationChanged(float x)
                    {
                        mCurrentX = x;

                        // 构造定位数据
                        MyLocationData locData = new MyLocationData.Builder()
                                .accuracy(0.0f)
                                // 此处设置开发者获取到的方向信息，顺时针0-360
                                .direction(mCurrentX)
                                .latitude(currentLatitude)
                                .longitude(currentLongitude).build();
                        // 设置定位数据
                        baiduMap.setMyLocationData(locData);
                    }
                });
    }
    // 初始化定位相关代码
    private void initMyLocation()
    {
        // 定位初始化
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(new MyLocationListener());
        // 设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setIsNeedAddress(true);
        option.setScanSpan(5000);
        mLocationClient.setLocOption(option);
    }
}
