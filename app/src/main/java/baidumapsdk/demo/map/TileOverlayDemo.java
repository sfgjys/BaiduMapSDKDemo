/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package baidumapsdk.demo.map;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.FileTileProvider;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Tile;
import com.baidu.mapapi.map.TileOverlay;
import com.baidu.mapapi.map.TileOverlayOptions;
import com.baidu.mapapi.map.TileProvider;
import com.baidu.mapapi.map.UrlTileProvider;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import java.io.InputStream;
import java.nio.ByteBuffer;

import baidumapsdk.demo.R;


/**
 * TileOverlay 测试demo
 * <p/>
 */
public class TileOverlayDemo extends Activity {
    @SuppressWarnings("unused")
    private static final String LTAG = BaseMapDemo.class.getSimpleName();
    private MapView mMapView;
    private BaiduMap mMapControl;
    // 设置瓦片图的在线缓存大小，默认为20 M
    private static final int TILE_TMP = 20 * 1024 * 1024;
    private static final int MAX_LEVEL = 21;
    private static final int MIN_LEVEL = 3;
    private EditText mEditText;
    TileProvider tileProvider;
    TileOverlay tileOverlay;
    Tile offlineTile;
    MapStatusUpdate mMapStatusUpdate;
    private boolean mapLoaded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_overlay_demo);
        mMapView = (MapView) findViewById(R.id.mapview);
        mMapControl = mMapView.getMap();

        // 设置地图是否加载完成的监听
        mMapControl.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                System.out.println("**************************************************************************************************************");
                mapLoaded = true;
            }
        });

        // 更新地图状态的中以及缩放级别
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(16.0f);
        builder.target(new LatLng(39.914935D, 116.403119D));
        mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(builder.build());
        mMapControl.setMapStatus(mMapStatusUpdate);

        // 初始化UI控件
        Button mOnline = (Button) findViewById(R.id.online);
        Button mOffline = (Button) findViewById(R.id.offline);
        mEditText = (EditText) findViewById(R.id.online_url);
        CheckBox hidePoiInfo = (CheckBox) findViewById(R.id.hide_poiinfo);

        // 设置在线方法点击按钮事件的监听
        mOnline.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onlineTile();
            }
        });
        // 设置离线方法点击按钮事件的监听
        mOffline.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                offlineTile();
            }
        });

        // 设置MapView是否显示Poi点
        hidePoiInfo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mMapControl.showMapPoi(false);
                } else {
                    mMapControl.showMapPoi(true);
                }
            }
        });

        // 设置在线瓦图的url
        String onlineUrl = "http://api0.map.bdimg.com/customimage/tile?x={x}&y={y}&z={z}&udt=20150601&customid=light";
        mEditText.setText(onlineUrl);

    }


    /*
    * 使用瓦图替换底图显示:
    *   设置mMapControl.setMapType(BaiduMap.MAP_TYPE_NONE)使得地图为空白模式;(视情况需要,来确定要不要设定)
    *   然后判断下TileOverlay对象是否为空,TileOverlay是地图控制器添加TileOverlayOptions瓦图配置对象的返回值,如果TileOverlay对象不为空,那么为了添加另一种瓦图,需要调用removeTileOverlay()方法,去除正在显示的瓦图
    *   此处出现分叉:
    *       在线瓦图:
    *           创建瓦图图片提供UrlTileProvider抽象对象,实现三个抽象方法,分别是设置瓦图最大显示(就是缩放)级别,瓦图最小显示(就是缩放)级别以及在线瓦图的url
    *               url例如:http://api0.map.bdimg.com/customimage/tile?x={x}&y={y}&z={z}&udt=20150601&customid=light
    *           接着创建瓦图配置对象TileOverlayOptions,使用对象调用tileProvider方法设置瓦图图片提供对象,调用setMaxTileTmp方法设置在线瓦片图的内存缓存大小,默认值为20MB,
    *               调用setPositionFromBounds方法设置瓦图的显示区域,瓦片图会以多个瓦片图连接并覆盖该区域,默认值为世界范围显示瓦片图(这个方法暂时找不到用途)
    *           最后使用地图控制器调用addTileLayer方法添加瓦图配置对象TileOverlayOptions,返回TileOverlay对象
    *           注意:根据瓦图存在的最大与最小级别限制地图缩放级别
    *       离线瓦图:
    *           与在线瓦图的主要区别在于瓦图图片提供对象,离线的提供对象是FileTileProvider对象,两者都是TileProvider的子类
    *               设置最大最小显示没变,但是设置在线瓦图的url的getTileUrl()方法变为getTile(int x, int y, int z)方法
    *               getTile方法需要返回Tile对象,创建Tile对象需要Bitmap对象(由瓦片图转换来的)的宽,高以及Bitmap对象转为byt[]的值
    *                   Bitmap对象转为byt[]的方法看本类中有
    * */


    /**
     * 使用瓦片图的在线方式
     */
    private void onlineTile() {

        mMapControl.setMapType(BaiduMap.MAP_TYPE_NONE);


        if (tileOverlay != null && mMapControl != null) {
            tileOverlay.removeTileOverlay();
        }
        final String urlString = mEditText.getText().toString();
        /**
         * 定义瓦片图的在线Provider，并实现相关接口
         * MAX_LEVEL、MIN_LEVEL 表示地图显示瓦片图的最大、最小级别
         * urlString 表示在线瓦片图的URL地址
         */
        TileProvider tileProvider = new UrlTileProvider() {
            @Override
            public int getMaxDisLevel() {
                return MAX_LEVEL;
            }

            @Override
            public int getMinDisLevel() {
                return MIN_LEVEL;
            }

            @Override
            public String getTileUrl() {
                return urlString;
            }

        };
        TileOverlayOptions options = new TileOverlayOptions();
        // 构造显示瓦片图范围，当前为世界范围
        LatLng northeast = new LatLng(80, 180);
        LatLng southwest = new LatLng(-80, -180);
        // 通过option指定相关属性，向地图添加在线瓦片图对象
        tileOverlay = mMapControl.addTileLayer(options.tileProvider(tileProvider).setMaxTileTmp(TILE_TMP)// 缓存大小,默认20mb
                // 设置TileOverlay的显示区域，瓦片图会以多个瓦片图连接并覆盖该区域 默认值为世界范围显示瓦片图
                .setPositionFromBounds(new LatLngBounds.Builder().include(northeast).include(southwest).build()));
        if (mapLoaded) {
            mMapControl.setMaxAndMinZoomLevel(21.0f, 3.0f);// 根据瓦图存在的最大与最小级别限制地图缩放级别
            mMapControl.setMapStatusLimits(new LatLngBounds.Builder().include(northeast).include(southwest).build());// 设置针对需要展示部分固定范围的地图，只有在 OnMapLoadedCallback.onMapLoaded() 之后设置才生效
            mMapControl.setMapStatus(mMapStatusUpdate);
        }
    }

    /**
     * 瓦片图的离线添加
     */
    private void offlineTile() {
        if (tileOverlay != null && mMapControl != null) {
            tileOverlay.removeTileOverlay();
        }

        /**
         * 设置离线显示TileOverlay的Tile对象 注：使用该类，传入的Tile尺寸必须满足256*256的要求，开发者可自行转换图片尺寸大小
         * 定义瓦片图的离线Provider，并实现相关接口
         * MAX_LEVEL、MIN_LEVEL 表示地图显示瓦片图的最大、最小级别
         * Tile 对象表示地图每个x、y、z状态下的瓦片对象
         */
        tileProvider = new FileTileProvider() {
            @Override
            public Tile getTile(int x, int y, int z) {// x,y,z - x,y表示瓦片在地图的横纵坐标，z表示地图的缩放级别
                // 根据地图某一状态下x、y、z加载指定的瓦片图
                String filedir = "LocalTileImage/" + z + "/" + z + "_" + x + "_" + y + ".jpg";
                Bitmap bm = getFromAssets(filedir);
                if (bm == null) {
                    return null;
                }
                // 瓦片图尺寸必须满足256 * 256
                offlineTile = new Tile(bm.getWidth(), bm.getHeight(), toRawData(bm));
                bm.recycle();
                return offlineTile;
            }

            @Override
            public int getMaxDisLevel() {
                return MAX_LEVEL;
            }

            @Override
            public int getMinDisLevel() {
                return MIN_LEVEL;
            }

        };
        TileOverlayOptions options = new TileOverlayOptions();
        // 构造显示瓦片图范围，当前为世界范围
        LatLng northeast = new LatLng(80, 180);
        LatLng southwest = new LatLng(-80, -180);
        // 设置离线瓦片图属性option
        options.tileProvider(tileProvider).setPositionFromBounds(new LatLngBounds.Builder().include(northeast).include(southwest).build());
        // 通过option指定相关属性，向地图添加离线瓦片图对象
        tileOverlay = mMapControl.addTileLayer(options);
        if (mapLoaded) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(39.94001804746338, 116.41224644234747)).include(new LatLng(39.90299859954822, 116.38359947963427));
            mMapControl.setMapStatusLimits(builder.build());
            mMapControl.setMaxAndMinZoomLevel(17.0f, 16.0f);
            mMapControl.setMapType(BaiduMap.MAP_TYPE_NONE);
        }

    }


    /**
     * 瓦片文件解析为Bitmap,从assets文件夹下获取图片,并将图片转为Bitmap对象
     */
    public Bitmap getFromAssets(String fileName) {
        AssetManager am = this.getAssets();
        InputStream is = null;
        Bitmap bm;

        try {
            is = am.open(fileName);
            bm = BitmapFactory.decodeStream(is);
            return bm;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析Bitmap,将包含了瓦片图的Bitmap对象转换为byte[]数组
     */
    byte[] toRawData(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getWidth() * bitmap.getHeight() * 4);
        bitmap.copyPixelsToBuffer(buffer);
        byte[] data = buffer.array();
        buffer.clear();
        return data;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // activity 暂停时同时暂停地图控件
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activity 恢复时同时恢复地图控件
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // activity 销毁时同时销毁地图控件
        mMapView.onDestroy();
    }
}
