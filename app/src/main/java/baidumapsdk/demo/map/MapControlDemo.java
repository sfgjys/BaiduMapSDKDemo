package baidumapsdk.demo.map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapDoubleClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMapTouchListener;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import baidumapsdk.demo.R;

/**
 * 演示地图缩放，旋转，视角控制，单击，双击，长按，截图的事件响应
 */
public class MapControlDemo extends Activity {


    /*
    * BaiduMap地图控制器可以设置单击，双击，长按地图监听回调事件; 三个回调事件都可以获取到LatLng对象,从而获取到点击点的经纬坐标
    * 其中单击地图事件内部又可以分为单击地图与单击地图中的POI点,单击地图获得LatLng,而单击地图中的POI点获得MapPoi对象,该对象可获取包含POI点经纬坐标的LatLng对象以及该兴趣点的名称和UID
    * 地图控制还可以调用getMapStatus();方法获取地图状态对象，从而获取各种地图状态
    * BaiduMap地图控制器可以设置地图状态变化监听回调事件
    * BaiduMap地图控制器也可以进行地图状态的改变,但前提需要一个MapStatusUpdate对象(具体的地图状态改变在该对象中设置),然后将MapStatusUpdate对象作为参数调用animateMapStatus方法或者setMapStatus方法(前者有动画,后者没有)
    * BaiduMap地图控制器截图:调用snapshot方法,回调接口会返回一个Bitmap对象,如此就可以进行图片保存,例子: bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    * */

    // 注意: 猜测地图状态的改变都是以第一个地图状态为准, 例如: 一开始我们以旋转状态为0开启了MapView控件,当我们旋转90后,再次旋转90是没有效果的,因为第二次旋转并不是以第一次旋转后的角度在旋转，而是以一开是的0旋转为角度

    /**
     * MapView 是地图主控件
     */
    private MapView mMapView;
    private BaiduMap mMapControl;

    /**
     * 当前地点击点
     */
    private LatLng currentPt;
    /**
     * 控制按钮
     */
    private Button zoomButton;
    private Button rotateButton;
    private Button overlookButton;
    private Button saveScreenButton;

    private String touchType;

    /**
     * 用于显示地图状态的面板
     */
    private TextView mStateBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapcontrol);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapControl = mMapView.getMap();
        mStateBar = (TextView) findViewById(R.id.state);
        initListener();
    }

    /**
     * 对地图事件的消息响应
     */
    private void initListener() {
        mMapControl.setOnMapTouchListener(new OnMapTouchListener() {

            @Override
            public void onTouch(MotionEvent event) {

            }
        });


        mMapControl.setOnMapClickListener(new OnMapClickListener() {
            /**
             * 单击地图
             */
            public void onMapClick(LatLng point) {
                touchType = "单击地图";
                currentPt = point;
                updateMapState();
            }

            /**
             * 单击地图中的POI点
             */
            public boolean onMapPoiClick(MapPoi poi) {
                touchType = "单击POI点";
                currentPt = poi.getPosition();
                updateMapState();
                return false;
            }
        });
        mMapControl.setOnMapLongClickListener(new OnMapLongClickListener() {
            /**
             * 长按地图
             */
            public void onMapLongClick(LatLng point) {
                touchType = "长按";
                currentPt = point;
                updateMapState();
            }
        });
        mMapControl.setOnMapDoubleClickListener(new OnMapDoubleClickListener() {
            /**
             * 双击地图
             */
            public void onMapDoubleClick(LatLng point) {
                touchType = "双击";
                currentPt = point;
                updateMapState();
            }
        });

        /**
         * 地图状态发生变化
         */
        mMapControl.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {
            public void onMapStatusChangeStart(MapStatus status) {
                updateMapState();
            }

            public void onMapStatusChangeFinish(MapStatus status) {
                updateMapState();
            }

            public void onMapStatusChange(MapStatus status) {
                updateMapState();
            }
        });


        zoomButton = (Button) findViewById(R.id.zoombutton);
        rotateButton = (Button) findViewById(R.id.rotatebutton);
        overlookButton = (Button) findViewById(R.id.overlookbutton);
        saveScreenButton = (Button) findViewById(R.id.savescreen);
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.equals(zoomButton)) {
                    perfomZoom();
                } else if (view.equals(rotateButton)) {
                    perfomRotate();
                } else if (view.equals(overlookButton)) {
                    perfomOverlook();
                } else if (view.equals(saveScreenButton)) {
                    // 截图，在SnapshotReadyCallback中保存图片到 sd 卡
                    mMapControl.snapshot(new SnapshotReadyCallback() {
                        public void onSnapshotReady(Bitmap snapshot) {
                            File file = new File("/mnt/sdcard/test.png");
                            FileOutputStream out;
                            try {
                                out = new FileOutputStream(file);
                                if (snapshot.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                                    out.flush();
                                    out.close();
                                }
                                Toast.makeText(MapControlDemo.this,
                                        "屏幕截图成功，图片存在: " + file.toString(),
                                        Toast.LENGTH_SHORT).show();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Toast.makeText(MapControlDemo.this, "正在截取屏幕图片...",
                            Toast.LENGTH_SHORT).show();

                }
                updateMapState();
            }

        };
        zoomButton.setOnClickListener(onClickListener);
        rotateButton.setOnClickListener(onClickListener);
        overlookButton.setOnClickListener(onClickListener);
        saveScreenButton.setOnClickListener(onClickListener);
    }

    /**
     * 处理缩放 sdk 缩放级别范围： [3.0,19.0]
     */
    private void perfomZoom() {
        EditText t = (EditText) findViewById(R.id.zoomlevel);
        try {
            float zoomLevel = Float.parseFloat(t.getText().toString());
            MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(zoomLevel);
            mMapControl.setMapStatus(u);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入正确的缩放级别", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理旋转 旋转角范围： -180 ~ 180 , 单位：度 逆时针旋转
     */
    private void perfomRotate() {
        EditText t = (EditText) findViewById(R.id.rotateangle);
        try {
            int rotateAngle = Integer.parseInt(t.getText().toString());
            MapStatus ms = new MapStatus.Builder(mMapControl.getMapStatus()).rotate(rotateAngle).build();
            MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
            mMapControl.animateMapStatus(u);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入正确的旋转角度", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理俯视 俯角范围： -45 ~ 0 , 单位： 度
     */
    private void perfomOverlook() {
        EditText t = (EditText) findViewById(R.id.overlookangle);
        try {
            int overlookAngle = Integer.parseInt(t.getText().toString());
            MapStatus ms = new MapStatus.Builder(mMapControl.getMapStatus()).overlook(overlookAngle).build();
            MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
            mMapControl.animateMapStatus(u);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入正确的俯角", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 更新地图状态显示面板
     */
    private void updateMapState() {
        if (mStateBar == null) {
            return;
        }
        String state = "";
        if (currentPt == null) {
            state = "点击、长按、双击地图以获取经纬度和地图状态";
        } else {
            state = String.format(touchType + ",当前经度： %f 当前纬度：%f",
                    currentPt.longitude, currentPt.latitude);
        }
        state += "\n";
        MapStatus ms = mMapControl.getMapStatus();
        state += String.format(
                "zoom=%.1f rotate=%d overlook=%d",
                ms.zoom, (int) ms.rotate, (int) ms.overlook);
        mStateBar.setText(state);
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
    }
}
