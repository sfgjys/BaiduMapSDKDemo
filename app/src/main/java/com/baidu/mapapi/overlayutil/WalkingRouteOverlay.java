package com.baidu.mapapi.overlayutil;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteNode;
import com.baidu.mapapi.search.route.WalkingRouteLine;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示步行路线的overlay，自3.4.0版本起可实例化多个添加在地图中显示
 */
public class WalkingRouteOverlay extends OverlayManager {

    private WalkingRouteLine mRouteLine = null;

    public WalkingRouteOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }

    /**
     * 设置路线数据。
     */
    public void setData(WalkingRouteLine line) {
        mRouteLine = line;
    }

    @Override
    public final List<OverlayOptions> getOverlayOptions() {
        if (mRouteLine == null) {
            return null;
        }

        List<OverlayOptions> overlayList = new ArrayList<OverlayOptions>();
        List<WalkingRouteLine.WalkingStep> allStep = mRouteLine.getAllStep();// 获取路线中的所有路段集合

        // 多个路段组成路线  多个路段组成路线  多个路段组成路线

        // 先将 路段 的起终点Marker进行配置
        if (allStep != null && allStep.size() > 0) {
            for (WalkingRouteLine.WalkingStep walkingStep : allStep) {// WalkingRouteLine.WalkingStep : 所有路段集合中的存储对象
                Bundle b = new Bundle();
                b.putInt("index", allStep.indexOf(walkingStep));// 存储单个路段在集合中的角标

                // 根据路段起点信息进行覆盖物添加进OverlayOptions集合
                RouteNode stepOrigin = walkingStep.getEntrance();// 获取路段起点信息
                if (stepOrigin != null) {
                    overlayList.add((new MarkerOptions())
                            .position(stepOrigin.getLocation())
                            .rotate((360 - walkingStep.getDirection()))
                            .zIndex(10)// 将存储有单个路段在集合中的角标的bundle存储给起点Marker
                            .anchor(0.5f, 0.5f)
                            .extraInfo(b)
                            .icon(BitmapDescriptorFactory.fromAssetWithDpi("Icon_line_node.png")));
                }

                // 根据路段终点信息进行覆盖物绘制
                RouteNode stepExit = walkingStep.getExit();// 获取路段终点信息
                if (allStep.indexOf(walkingStep) == (mRouteLine.getAllStep().size() - 1) && stepExit != null) {
                    overlayList.add((new MarkerOptions())
                            .position(stepExit.getLocation())
                            .anchor(0.5f, 0.5f)
                            .zIndex(10)
                            .icon(BitmapDescriptorFactory.fromAssetWithDpi("Icon_line_node.png")));
                }
            }
        }

        // 路线的起点覆盖物添加进OverlayOptions集合
        if (mRouteLine.getStarting() != null) {
            overlayList.add((new MarkerOptions())
                    .position(mRouteLine.getStarting().getLocation())
                    .icon(getStartMarker() != null ? getStartMarker() : BitmapDescriptorFactory.fromAssetWithDpi("Icon_start.png"))
                    .zIndex(10));
        }

        // 路线的终点覆盖物添加进OverlayOptions集合
        if (mRouteLine.getTerminal() != null) {
            overlayList.add((new MarkerOptions())
                    .position(mRouteLine.getTerminal().getLocation())
                    .icon(getTerminalMarker() != null ? getTerminalMarker() : BitmapDescriptorFactory.fromAssetWithDpi("Icon_end.png"))
                    .zIndex(10));
        }

        // 根据路段的坐标集合配置各个路段线的线覆盖物配置,从而组成完整路线,然后添加进OverlayOptions集合
        if (allStep != null && allStep.size() > 0) {
            LatLng lastStepLastPoint = null;// 因为是分别画路段线的,第一个路段起始点不用在意,但是第二个路段的头部坐标需要是第一个路段的尾部坐标 本局部变量就是为此而来的
            List<LatLng> points = new ArrayList<>();
            for (WalkingRouteLine.WalkingStep step : allStep) {
                List<LatLng> watPoints = step.getWayPoints();
                if (watPoints != null) {
                    if (lastStepLastPoint != null) {
                        points.add(lastStepLastPoint);
                    }
                    points.addAll(watPoints);
                    // 将路段最后的坐标在添加下个路段时在添加一遍
                    lastStepLastPoint = watPoints.get(watPoints.size() - 1);
                }
            }
            overlayList.add(new PolylineOptions()
                    .points(points)
                    .width(10)
                    .color(getLineColor() != 0 ? getLineColor() : Color.argb(178, 0, 78, 255))
                    .zIndex(0));
        }

        return overlayList;
    }

    /**
     * 覆写此方法以改变线路默认起点图标
     */
    public BitmapDescriptor getStartMarker() {
        return null;
    }

    /**
     * 覆写此方法以改变线路默认颜色
     */
    public int getLineColor() {
        return 0;
    }

    /**
     * 覆写此方法以改变线路默认终点图标
     */
    public BitmapDescriptor getTerminalMarker() {
        return null;
    }

    /**
     * 处理点击事件,子类可以覆写
     */
    public boolean onRouteNodeClick(int i) {
        if (mRouteLine.getAllStep() != null && mRouteLine.getAllStep().get(i) != null) {
            Log.i("baidumapsdk", "WalkingRouteOverlay onRouteNodeClick");
        }
        return false;
    }

    @Override
    public final boolean onMarkerClick(Marker marker) {
        for (Overlay mMarker : mOverlayList) {
            // 判断从集合中取出的Overlay是否属于Marker类型,并判断从集合中取出的Overlay与接口传递过来的marker是否同一个
            if (mMarker instanceof Marker && mMarker.equals(marker)) {
                // 此处只有路段的起终点存储了bundle对象,所以只有点击路段的起终点,才会调用onRouteNodeClick()发生具体的操作
                if (marker.getExtraInfo() != null) {
                    // 取出存储的bundle的值并传递给onRouteNodeClick方法
                    onRouteNodeClick(marker.getExtraInfo().getInt("index"));
                }
            }
        }
        return true;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        return false;
    }
}
