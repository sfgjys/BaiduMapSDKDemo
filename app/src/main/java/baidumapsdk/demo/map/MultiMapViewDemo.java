package baidumapsdk.demo.map;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;

import baidumapsdk.demo.R;

/**
 * 在一个Activity中展示多个地图
 */
public class MultiMapViewDemo extends FragmentActivity {

    // 地理坐标数据结构类  参数一纬度 参数二经度
    private static final LatLng GEO_BEIJING = new LatLng(39.945, 116.404);
    private static final LatLng GEO_SHANGHAI = new LatLng(31.227, 121.481);
    private static final LatLng GEO_GUANGZHOU = new LatLng(23.155, 113.264);
    private static final LatLng GEO_SHENGZHENG = new LatLng(22.560, 114.064);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimap);
        initMap();
    }

    /**
     * 初始化Map,设置不同城市为地图中心点，设置Logo不同位置
     */
    private void initMap() {

        // SupportMapFragment可以调用getBaiduMap()获取地图控制器;调用getMapView()获取地图控件
        // MapStatusUpdate类是一个 更新对图状态 的类:  当MapView通过MapStatus设置了一些参数后,并开启了显示,如果我们想要重新设置地图状态(更新地图状态),
        //          那么我们可以将MapStatusUpdate类作为参数设置进地图控制器:getBaiduMap().setMapStatus(u1);如此地图就会更新地图状态了 注意:这个更新是重新初始化MapView控件
        //                  对图状态包括地图俯仰角度;地图旋转角度;地图操作的中心点;地图缩放级别等.



        /*北京为地图中心，logo在左上角*/
        SupportMapFragment map1 = (SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map1)); // 这个方法是获取一个包含MapView控件的Fragment并进行显示
        MapStatusUpdate u1 = MapStatusUpdateFactory.newLatLng(GEO_BEIJING);// 设置地图状态的操作中心点
        map1.getBaiduMap().setMapStatus(u1);
        map1.getMapView().setLogoPosition(LogoPosition.logoPostionleftTop); // 通过地图控件设置logo图标位置

        /*上海为地图中心，logo在右上角*/
        SupportMapFragment map2 = (SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map2));
        MapStatusUpdate u2 = MapStatusUpdateFactory.newLatLng(GEO_SHANGHAI);
        map2.getBaiduMap().setMapStatus(u2);
        map2.getMapView().setLogoPosition(LogoPosition.logoPostionRightTop);

        /*广州为地图中心，logo在左下角*/
        SupportMapFragment map3 = (SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map3));
        MapStatusUpdate u3 = MapStatusUpdateFactory.newLatLng(GEO_GUANGZHOU);
        map3.getBaiduMap().setMapStatus(u3);
        map3.getMapView().setLogoPosition(LogoPosition.logoPostionleftBottom);

        /*深圳为地图中心，logo在右下角*/
        SupportMapFragment map4 = (SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map4));
        MapStatusUpdate u4 = MapStatusUpdateFactory.newLatLng(GEO_SHENGZHENG);
        map4.getBaiduMap().setMapStatus(u4);
        map4.getMapView().setLogoPosition(LogoPosition.logoPostionRightBottom);
    }

}
