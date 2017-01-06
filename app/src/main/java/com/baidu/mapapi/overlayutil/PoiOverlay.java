package com.baidu.mapapi.overlayutil;

import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.search.poi.PoiResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示poi的,并且实现监听接口,实质是Marker覆盖物的点击监听接口与Polyline覆盖物的点击监听接口
 */
public class PoiOverlay extends OverlayManager {

    private static final int MAX_POI_SIZE = 10;// 最多显示多少Marker

    private PoiResult mPoiResult = null;

    /**
     * 构造函数,将BaiduMap对象传递给父类
     */
    public PoiOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }

    /**
     * 设置POI数据,将带有覆盖物所需信息的对象传递给成员变量mPoiResult
     */
    public void setData(PoiResult poiResult) {
        this.mPoiResult = poiResult;
    }

    /*
    * 具体实现获取覆盖物配置对象的集合方法,因为本对象并没有传List<OverlayOptions>类型的数据集合
    * 所以该类需要对有数据信息的对象进行处理转换,在本类中mPoiResult就是需要转换的对象,所以setData()方法是调用该方法的前提,而该方法被父类的addToMap方法调用,所以在调用addToMap方法前必须先setData()
    * */
    @Override
    public final List<OverlayOptions> getOverlayOptions() {
        if (mPoiResult == null || mPoiResult.getAllPoi() == null) {
            return null;
        }
        List<OverlayOptions> markerList = new ArrayList<OverlayOptions>();
        int markerSize = 0;
        for (int i = 0; i < mPoiResult.getAllPoi().size() && markerSize < MAX_POI_SIZE; i++) {
            if (mPoiResult.getAllPoi().get(i).location == null) {
                continue;
            }
            markerSize++;
            Bundle bundle = new Bundle();
            bundle.putInt("index", i);
            // 在创建Marker配置对象的时候将一个包含角标的信息添加进去
            markerList.add(new MarkerOptions().icon(BitmapDescriptorFactory.fromAssetWithDpi("Icon_mark" + markerSize + ".png")).extraInfo(bundle).position(mPoiResult.getAllPoi().get(i).location));
        }
        return markerList;
    }

    /**
     * 获取该 PoiOverlay 的 poi数据
     */
    public PoiResult getPoiResult() {
        return mPoiResult;
    }

    /**
     * 覆写此方法以改变默认点击行为
     */
    public boolean onPoiClick(int i) {
        return false;
    }


    /*
    * Marker覆盖物点击监听接口
    * */
    @Override
    public final boolean onMarkerClick(Marker marker) {
        if (!mOverlayList.contains(marker)) {
            return false;
        }
        if (marker.getExtraInfo() != null) {
            // 因为在创建Marker配置对象时将角标存入,所以marker.getExtraInfo()可以获取当是存储的Bundle对象获取去存储的角标
            // 该角标对应了成员变量mPoiResult.getAllPoi()后的集合,并与Marker对应(以角标所在的源数据创建了Maraker配置对象)
            // 这里在编写了一个方法,并将角标作为参数
            return onPoiClick(marker.getExtraInfo().getInt("index"));
        }
        return false;
    }

    /*
    * 该接口在此没什么用
    * */
    @Override
    public boolean onPolylineClick(Polyline polyline) {
        return false;
    }
}
