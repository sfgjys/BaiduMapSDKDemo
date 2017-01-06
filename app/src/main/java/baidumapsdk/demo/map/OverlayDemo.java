package baidumapsdk.demo.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import java.util.ArrayList;

import baidumapsdk.demo.R;

/**
 * 演示覆盖物的用法
 */
public class OverlayDemo extends Activity {

    /**
     * MapView 是地图主控件
     */
    private MapView mMapView;
    private BaiduMap mMapControl;
    private Marker mMarkerA;
    private Marker mMarkerB;
    private Marker mMarkerC;
    private Marker mMarkerD;
    private InfoWindow mInfoWindow;
    private CheckBox animationBox = null;

    // 初始化全局 bitmap 信息，不用时及时 recycle
    BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_marka);
    BitmapDescriptor bdB = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_markb);
    BitmapDescriptor bdC = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_markc);
    BitmapDescriptor bdD = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_markd);
    BitmapDescriptor bd = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_gcoding);
    BitmapDescriptor bdGround = BitmapDescriptorFactory
            .fromResource(R.drawable.ground_overlay);



    /*
    * 添加Marker类型的覆盖物
    *       创建MarkerOptions配置类对象,调用position()方法设置坐标;    icon(传入经过BitmapDescriptor转换的图标图像)设置Marker的图像;   draggable(true/false)设置是否允许拖拽,默认不允许;
    *           zIndex(9)设置Marker的覆盖层级,值大的会覆盖值小的;   animateType(MarkerAnimateType.drop)设置Marker显示动画,该动画在BaiduMap.addOverlay(Marker)调用后显示在MapView上时开启;
    *           perspective(false)是否开启在俯视状态下的近大远小;  anchor(0.5f, 0.5f)设置Marker的锚点在坐标区域内小范围移动(一个坐标在地图上有可能是有一点区域的)默认（0.5f, 1.0f）水平居中,垂直下对齐,两个参数[0.0f , 1.0f],否则不生效;
    *           rotate(30)设置 marker 覆盖物旋转角度,逆时针;    icons(传入存储多个经过BitmapDescriptor转换的图标图像的集合)与icon()相比名字多了s,这样Marker就有多张图,Marker图标图像会在集合中不断切换
    *           period(10)设置多少帧刷新一次图片资源,值越小动画越快(与icons()方法相配合) 参数默认为20，最小为1;    Marker.setAlpha(alpha)设置Maker图标的透明度,取值[0,1]
    * 添加GroundOverlay类型的覆盖物
    *       创建GroundOverlay配置类对象,调用transparency(0.8f)设置透明度;image(bdGround)设置覆盖物显示图片
    *           因为该覆盖物显示的是一个矩形,所以有两种设置坐标的方法:
    *           position(LatLng latLng)设置 Ground 覆盖物位置信息方式一,与 dimensions(int, int)配合使用(设置 ground 覆盖物的宽度和高度，单位：米)
    *           positionFromBounds(LatLngBounds bounds)设置 ground 覆盖物的位置信息方式二，设置西南与东北坐标范围 LatLngBounds坐标类:new LatLngBounds.Builder().include(LatLng坐标对象).include(LatLng坐标对象).build();
    * InfoWindow()控件
    *       地图控制器调用showInfoWindow(InfoWindow对象)方法,就可以显示InfoWindow控件,调用hideInfoWindow()就可以隐藏当前InfoWindow控件,调用clear()清空地图所有的 Overlay 覆盖物以及 InfoWindow;
    *       重点在于InfoWindow对象如何创建
    *           构造函数一: InfoWindow(BitmapDescriptor bd, LatLng position, int yOffset, InfoWindow.OnInfoWindowClickListener listener)
    *           构造函数二: InfoWindow(View view, LatLng position, int yOffset)
    *           两个构造函数的参数二:InfoWindow 显示的地理位置;参数三:InfoWindow Y 轴偏移量
    *           构一的参数一是将要显示的资源通过BitmapDescriptor.from进行转换,如此参数四就成了点击InfoWindow的监听事件 这样创建的InfoWindow其只有一个点击事件就是点击InfoWindow本身,过于单一
    *           而构二就比较多元化,参数一需要的是一个View,如此我们可以打气一个多元化布局进去,设置多种点击显示事件
    * */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overlay);

        // 初始化控件
        SeekBar alphaSeekBar = (SeekBar) findViewById(R.id.alphaBar);
        animationBox = (CheckBox) findViewById(R.id.animation);
        mMapView = (MapView) findViewById(R.id.bmapView);

        // 设置滑动进度条的改变监听事件
        alphaSeekBar.setOnSeekBarChangeListener(new SeekBarListener());

        mMapControl = mMapView.getMap();

        // 更新地图状态 缩放至1公里
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
        mMapControl.setMapStatus(msu);

        // 初始化Marker覆盖物并添加进MapView
        initOverlay();

        // Marker覆盖物点击监听事件
        mMapControl.setOnMarkerClickListener(new OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
                Button button = new Button(getApplicationContext());
                button.setBackgroundResource(R.drawable.popup);
                if (marker == mMarkerA || marker == mMarkerD) {
                    button.setText("更改位置");
                    button.setBackgroundColor(0x0000f);
                    button.setWidth(300);

                    OnInfoWindowClickListener listener = new OnInfoWindowClickListener() {
                        public void onInfoWindowClick() {
                            LatLng ll = marker.getPosition();
                            LatLng llNew = new LatLng(ll.latitude + 0.005,
                                    ll.longitude + 0.005);
                            marker.setPosition(llNew);
                            mMapControl.hideInfoWindow();
                        }
                    };
                    LatLng ll = marker.getPosition();
                    mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47, listener);
                    mMapControl.showInfoWindow(mInfoWindow);
                } else if (marker == mMarkerB) {
                    button.setText("更改图标");
                    button.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            marker.setIcon(bd);
                            mMapControl.hideInfoWindow();
                        }
                    });
                    LatLng ll = marker.getPosition();
                    mInfoWindow = new InfoWindow(button, ll, -47);
                    mMapControl.showInfoWindow(mInfoWindow);
                } else if (marker == mMarkerC) {
                    button.setText("删除");
                    button.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            marker.remove();
                            mMapControl.hideInfoWindow();
                        }
                    });
                    LatLng ll = marker.getPosition();
                    mInfoWindow = new InfoWindow(button, ll, -47);
                    mMapControl.showInfoWindow(mInfoWindow);
                }
                return true;
            }
        });
    }

    public void initOverlay() {
        // add marker overlay
        LatLng llA = new LatLng(39.963175, 116.400244);
        LatLng llB = new LatLng(39.942821, 116.369199);
        LatLng llC = new LatLng(39.939723, 116.425541);
        LatLng llD = new LatLng(39.906965, 116.401394);


        MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdA).zIndex(9).draggable(true);
        if (animationBox.isChecked()) {
            // 掉下动画
            ooA.animateType(MarkerAnimateType.drop);
        }
        mMarkerA = (Marker) (mMapControl.addOverlay(ooA));


        MarkerOptions ooB = new MarkerOptions().position(llB).icon(bdB).zIndex(5);
        if (animationBox.isChecked()) {
            // 掉下动画
            ooB.animateType(MarkerAnimateType.drop);
        }
        mMarkerB = (Marker) (mMapControl.addOverlay(ooB));


        MarkerOptions ooC = new MarkerOptions().position(llC).icon(bdC).perspective(false).anchor(0.1f, 0.1f).rotate(30).zIndex(7);
        if (animationBox.isChecked()) {
            // 生长动画
            ooC.animateType(MarkerAnimateType.grow);
        }
        mMarkerC = (Marker) (mMapControl.addOverlay(ooC));


        ArrayList<BitmapDescriptor> giflist = new ArrayList<>();
        giflist.add(bdA);
        giflist.add(bdB);
        giflist.add(bdC);


        MarkerOptions ooD = new MarkerOptions().position(llD).icons(giflist)
                .zIndex(0).period(10);
        if (animationBox.isChecked()) {
            // 生长动画
            ooD.animateType(MarkerAnimateType.grow);
        }
        mMarkerD = (Marker) (mMapControl.addOverlay(ooD));


        LatLng southwest = new LatLng(39.92235, 116.380338);
        LatLng northeast = new LatLng(39.947246, 116.414977);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();


        OverlayOptions ooGround = new GroundOverlayOptions().positionFromBounds(bounds).image(bdGround).transparency(0.8f);
        mMapControl.addOverlay(ooGround);


        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(bounds.getCenter());
        mMapControl.setMapStatus(u);


        mMapControl.setOnMarkerDragListener(new OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
            }

            public void onMarkerDragEnd(Marker marker) {
                Toast.makeText(OverlayDemo.this, "拖拽结束，新位置：" + marker.getPosition().latitude + ", " + marker.getPosition().longitude, Toast.LENGTH_LONG).show();
            }

            public void onMarkerDragStart(Marker marker) {
            }
        });
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            float alpha = ((float) seekBar.getProgress()) / 10;
            if (mMarkerA != null) {
                mMarkerA.setAlpha(alpha);
            }
            if (mMarkerB != null) {
                mMarkerB.setAlpha(alpha);
            }
            if (mMarkerC != null) {
                mMarkerC.setAlpha(alpha);
            }
            if (mMarkerD != null) {
                mMarkerD.setAlpha(alpha);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

    }


    /**
     * 清除所有Overlay
     */
    public void clearOverlay(View view) {
        mMapControl.clear();
        mMarkerA = null;// 设置null 以免下次添加的是上次清空的
        mMarkerB = null;
        mMarkerC = null;
        mMarkerD = null;
    }

    /**
     * 重新添加Overlay
     */
    public void resetOverlay(View view) {
        clearOverlay(null);
        initOverlay();
    }

    @Override
    protected void onPause() {
        // MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
        mMapView.onDestroy();
        super.onDestroy();
        // 回收 bitmap 资源
        bdA.recycle();
        bdB.recycle();
        bdC.recycle();
        bdD.recycle();
        bd.recycle();
        bdGround.recycle();
    }

}
