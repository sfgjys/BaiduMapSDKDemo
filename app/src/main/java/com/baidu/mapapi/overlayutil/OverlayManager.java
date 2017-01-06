package com.baidu.mapapi.overlayutil;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnPolylineClickListener;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;

/**
 * 本类的本质是Marker覆盖物的点击监听接口与Polyline覆盖物的点击监听接口,但在这里并没有具体实现两个接口,而是给子类去根据具体情况实现
 * 本类还添加了将多个覆盖物添加和移除显示到MapView上的功能 以及将所有覆盖物都能显示在一个屏幕内的缩放功能
 * 要想将覆盖物添加进MapView就需要使用BaiduMap对象调用addOverlay(传入覆盖物配置对象)方法
 * 如此在构造时获取到BaiduMap对象,为了有覆盖物配置对象作为参数,所以编写一个抽象方法getOverlayOptions()获取覆盖物配置对象集合,这样当别的方法调用这个方法就能有覆盖物配置对象集合
 */
public abstract class OverlayManager implements OnMarkerClickListener, OnPolylineClickListener {

    BaiduMap mBaiduMap = null;
    private List<OverlayOptions> mOverlayOptionList = null;
    List<Overlay> mOverlayList = null;

    /**
     * 通过对象构造,将BaiduMap对象传给本对象的mBaiduMap成员变量
     */
    public OverlayManager(BaiduMap baiduMap) {
        mBaiduMap = baiduMap;
        // 分别提前创建存储覆盖物对象与覆盖物配置对象的集合
        if (mOverlayOptionList == null) {
            mOverlayOptionList = new ArrayList<OverlayOptions>();
        }
        if (mOverlayList == null) {
            mOverlayList = new ArrayList<Overlay>();
        }
    }

    /**
     * 子类实现该方法,将覆盖物配置对象集合返回,如此调用了该方法就能获得覆盖物配置对象集合
     */
    public abstract List<OverlayOptions> getOverlayOptions();

    /**
     * 将所有Overlay添加到地图上,当该方法被调用时就会将getOverlayOptions()方法获取的覆盖物配置对象集合分别添加进MapView
     */
    public final void addToMap() {
        if (mBaiduMap == null) {
            return;
        }
        // 添加前先移除
        removeFromMap();
        // 获取覆盖物配置对象集合
        List<OverlayOptions> overlayOptions = getOverlayOptions();
        // 将覆盖物配置对象集合添加进本对象存储覆盖物配置对象的成员变量集合
        if (overlayOptions != null) {
            mOverlayOptionList.addAll(getOverlayOptions());
        }
        // 将覆盖物配置对象分别添加进MapView,并将返回的覆盖物对象添加进本对象存储覆盖物对象的成员变量集合
        for (OverlayOptions option : mOverlayOptionList) {
            mOverlayList.add(mBaiduMap.addOverlay(option));
        }
    }

    /**
     * 将所有Overlay从地图上消除
     */
    public final void removeFromMap() {
        if (mBaiduMap == null) {
            return;
        }
        // 根据本对象存储覆盖物对象的成员变量集合中的成员一个个进行移除
        for (Overlay marker : mOverlayList) {
            marker.remove();
        }
        mOverlayOptionList.clear();
        mOverlayList.clear();
    }

    /**
     * 缩放地图，使所有Overlay都在合适的视野内
     * 注 : 该方法只对Marker类型的overlay有效
     * 原理将所有Marker覆盖物的坐标include进LatLngBounds.Builder,然后地图在以这个为参数进行地图更新
     */
    public void zoomToSpan() {
        if (mBaiduMap == null) {
            return;
        }
        if (mOverlayList.size() > 0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Overlay overlay : mOverlayList) {
                if (overlay instanceof Marker) {
                    builder.include(((Marker) overlay).getPosition());
                }
            }
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build()));
        }
    }

}
