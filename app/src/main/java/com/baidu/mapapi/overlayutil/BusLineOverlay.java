package com.baidu.mapapi.overlayutil;

import android.graphics.Color;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示一条公交详情结果的Overlay
 */
public class BusLineOverlay extends OverlayManager {

    private BusLineResult mBusLineResult = null;

    /**
     * 构造函数,引入BaiduMap对象
     */
    public BusLineOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }

    /**
     * 设置公交线数据BusLineResult对象
     */
    public void setData(BusLineResult result) {
        this.mBusLineResult = result;
    }

    /*
    * 解析公交线数据BusLineResult对象,并将数据放入List<OverlayOptions>
    * */
    @Override
    public final List<OverlayOptions> getOverlayOptions() {

        if (mBusLineResult == null || mBusLineResult.getStations() == null) {
            return null;
        }

        List<OverlayOptions> overlayOptionses = new ArrayList<OverlayOptions>();

        // mBusLineResult.getStations()获取站点信息集合
        for (BusLineResult.BusStation station : mBusLineResult.getStations()) {
            overlayOptionses.add(new MarkerOptions()
                    .position(station.getLocation())// 获取每个站点的坐标,并在该坐标上添加Marker类型公交车样式的图片的覆盖物
                    .zIndex(10)
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromAssetWithDpi("Icon_bus_station.png")));
        }

        // mBusLineResult.getSteps()获取路段集合
        List<LatLng> points = new ArrayList<LatLng>();
        for (BusLineResult.BusStep step : mBusLineResult.getSteps()) {
            if (step.getWayPoints() != null) {
                points.addAll(step.getWayPoints());// 获取每个路段,公交经过的地点坐标,并添加进坐标集合中
            }
        }


        if (points.size() > 0) {
            // 使用上面存储的坐标集合去添加线覆盖物,不用公交站点的坐标集合是因为,两个公交站点有时是有弯度的,直接在两点之间画线不行
            overlayOptionses.add(new PolylineOptions()
                    .width(10)
                    .color(Color.argb(178, 0, 78, 255))
                    .zIndex(0)
                    .points(points));
        }
        return overlayOptionses;
    }

    /**
     * 覆写此方法以改变默认点击行为
     *
     * @param index 被点击的站点在{@link com.baidu.mapapi.search.busline.BusLineResult#getStations()}中的索引
     * @return 是否处理了该点击事件
     */
    public boolean onBusStationClick(int index) {
        if (mBusLineResult.getStations() != null && mBusLineResult.getStations().get(index) != null) {
            Log.i("baidumapsdk", "BusLineOverlay onBusStationClick");
        }
        return false;
    }

    public final boolean onMarkerClick(Marker marker) {
        if (mOverlayList != null && mOverlayList.contains(marker)) {
            return onBusStationClick(mOverlayList.indexOf(marker));
        } else {
            return false;
        }

    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        return false;
    }
}
