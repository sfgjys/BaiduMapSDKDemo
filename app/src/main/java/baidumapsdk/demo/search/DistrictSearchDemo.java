/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package baidumapsdk.demo.search;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.district.DistrictResult;
import com.baidu.mapapi.search.district.DistrictSearch;
import com.baidu.mapapi.search.district.DistrictSearchOption;
import com.baidu.mapapi.search.district.OnGetDistricSearchResultListener;

import java.util.List;

import baidumapsdk.demo.R;

public class DistrictSearchDemo extends Activity {

    private DistrictSearch mDistrictSearch;
    private EditText mCity;
    private EditText mDistrict;
    private MapView mMapView;
    private final int color = 0xAA00FF00;
    private BaiduMap mMapControl;
    private Button mSearchButton;

    /*
    * 检索行政区功能模块:
    *   初始化: DistrictSearch.newInstance()获得DistrictSearch对象;
    *   设置监听: DistrictSearch对象调用setOnDistrictSearchListener(传入OnGetDistricSearchResultListener()接口)方法;
    *   开启检索: DistrictSearch对象调用searchDistrict(传入参数为行政区检索配置对象,配置对象需要调用设置城市名称的cityName方法,和设置行政区名称的districtName方法)方法
    *   结果: OnGetDistricSearchResultListener接口返回的结果为DistrictResult对象,
    *           DistrictResult对象有以下四个方法:
    *               获取行政区域中心点:getCenterPt()
    *               获取行政区域编码:getCityCode()
    *               获取行政区域名称:getCityName()
    *               获取行政区域边界坐标点:getPolylines() 由于行政区可能有两块地方不重合,所以坐标集合可能有两个以上,用另一个集合存放多个坐标集合
    *   销毁: 当不再使用检索行政区功能模块时,记得释放对象资源 DistrictSearch对象调用destroy()方法;
    * */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district_search_demo);

        // 初始化检索行政区功能模块
        mDistrictSearch = DistrictSearch.newInstance();
        // 设置行政区域检索结果监听者
        mDistrictSearch.setOnDistrictSearchListener(new OnGetDistricSearchResultListener() {
            @Override
            public void onGetDistrictResult(DistrictResult districtResult) {


                // 先清空地图上的覆盖物与inforwindow
                mMapControl.clear();

                if (districtResult == null) {
                    return;
                }

                if (districtResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    // 获取行政区域边界坐标点
                    List<List<LatLng>> polyLines = districtResult.getPolylines();
                    if (polyLines == null) {
                        return;
                    }
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (List<LatLng> polyline : polyLines) { // 由于行政区可能有两块地方不重合,所以坐标集合可能有两个以上,用另一个集合存放集合
                        // 根据区域边界坐标点集合添加线覆盖物作为边界线,该边界线为绿色虚线
                        OverlayOptions ooPolyline11 = new PolylineOptions().width(10).points(polyline).dottedLine(true).color(color);
                        mMapControl.addOverlay(ooPolyline11);
                        // 根据区域边界坐标点集合添加多边形覆盖物展示行政区整体
                        OverlayOptions ooPolygon = new PolygonOptions().points(polyline).stroke(new Stroke(5, 0xAA00FF88)).fillColor(0xAAFFFF00);
                        mMapControl.addOverlay(ooPolygon);

                        // 将所有坐标点存入LatLngBounds.Builder,不管行政区有几块,都存进去
                        for (LatLng latLng : polyline) {
                            builder.include(latLng);
                        }
                    }

                    // 使用地图更新中心点
                    mMapControl.setMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build()));
                }
            }
        });

        // 初始化地图控件
        mMapView = (MapView) findViewById(R.id.map);
        mMapControl = mMapView.getMap();

        // 初始化其他控件
        mCity = (EditText) findViewById(R.id.city);
        mDistrict = (EditText) findViewById(R.id.district);
        mSearchButton = (Button) findViewById(R.id.districSearch);
        // 设置开启检索的按钮点击事件
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = "";
                String district = "";
                if (mCity.getText() != null && !"".equals(mCity.getText())) {
                    city = mCity.getText().toString();
                }
                if (mDistrict.getText() != null && !"".equals(mDistrict.getText())) {
                    district = mDistrict.getText().toString();
                }
                // 以上代码获取检索行政区方法所需要的条件参数

                // 此为开启检索行政区区域的核心代码,传入的参数为行政区检索配置对象,配置对象需要调用设置城市名称的cityName方法,和设置行政区名称的districtName方法
                mDistrictSearch.searchDistrict(new DistrictSearchOption().cityName(city).districtName(district));
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 当不再使用检索行政区功能模块时,记得释放对象资源
        mDistrictSearch.destroy();
        super.onDestroy();
    }
}
