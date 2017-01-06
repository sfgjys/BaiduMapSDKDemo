package baidumapsdk.demo.map;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;

import baidumapsdk.demo.R;

public class MapFragmentDemo extends FragmentActivity {
    @SuppressWarnings("unused")
    private static final String LTAG = MapFragmentDemo.class.getSimpleName();
    SupportMapFragment map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        Intent intent = getIntent();


        // 创建并设置地图状态建造者
        MapStatus.Builder builder = new MapStatus.Builder();// 地图状态建造者
        if (intent.hasExtra("x") && intent.hasExtra("y")) {
            // 当用intent参数时，设置中心点为指定点
            Bundle b = intent.getExtras();
            LatLng p = new LatLng(b.getDouble("y"), b.getDouble("x")); // LatLng为一个地理坐标数据结构类(百度自定义的) 参数一是纬度 参数二是经度
            builder.target(p);// 设置地图中心点
        }
        builder.overlook(-20).zoom(20);//   zoom(float zoom):设置地图缩放级别     overlook(float overlook):设置地图俯仰角

        // 以地图状态建造者为参数创建BaiduMapOptions对象，然后以BaiduMapOptions对象创建一个基于Fragment的Map
        BaiduMapOptions bo = new BaiduMapOptions().mapStatus(builder.build()).compassEnabled(true).zoomControlsEnabled(true);// BaiduMapOptions对象包含的是各种地图初始化时的参数设置 compassEnabled是否显示左上角的指南针  zoomControlsEnabled是否显示右下角的缩放控件
        map = SupportMapFragment.newInstance(bo);// 以BaiduMapOptions里的所有配置为核心创建一个基于Fragment的Map

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().add(R.id.map, map, "map_fragment").commit();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
