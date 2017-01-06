package baidumapsdk.demo.map;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.Gradient;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import baidumapsdk.demo.R;

/**
 * 热力图功能demo   //--使用用户自定义热力图数据
 */
public class HeatMapDemo extends Activity {
    @SuppressWarnings("unused")
    private static final String LTAG = BaseMapDemo.class.getSimpleName();
    private MapView mMapView;
    private BaiduMap mMapControl;
    private HeatMap heatmap;
    private Button mAdd;
    private Button mRemove;
    private boolean isDestroy;


    /*
    * 热力图:在缩放到比例尺最小的时候,热力图没啥效果,因为热力图是在高比例尺的时候,看一块区域内点的密集程度,根据其密集程度来渐变颜色,这里的热力并不是温度的意思
    * 开启热力图:地图控制器mMapControl.addHeatMap(热力图类HeatMap的对象); 移除热力图 已经开启过的热力图类HeatMap的对象调用removeHeatMap();
    * 核心代码是创建热力图类HeatMap的对象:
    *           // 设置渐变颜色值 这里暂时必须是Color.rgb类型的颜色 从左往右是从稀疏到密集的渐变颜色
    *           int[] DEFAULT_GRADIENT_COLORS = {Color.rgb(102, 225, 0), Color.rgb(102, 225, 225), Color.rgb(255, 0, 0)};
    *           // 设置渐变颜色起始值  角标为0的位置必须大于0f,否则那些没有坐标点的位置值也会有颜色
    *           float[] DEFAULT_GRADIENT_START_POINTS = {0.1f, 0.5f, 1f};
    *           // 渐变颜色对象
    *           Gradient gradient = new Gradient(DEFAULT_GRADIENT_COLORS, DEFAULT_GRADIENT_START_POINTS);
    *           // data(data)传递坐标集合 gradient(gradient)传递渐变颜色对象 opacity(0.6)透明度 radius(12)坐标点的半径,单位px
    *           HeatMap heatmap = new HeatMap.Builder().data(data).gradient(gradient).opacity(0.6).radius(12).build();
    * */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap);

        mMapView = (MapView) findViewById(R.id.mapview);
        mMapControl = mMapView.getMap();
        mMapControl.setMapStatus(MapStatusUpdateFactory.zoomTo(5));

        mAdd = (Button) findViewById(R.id.add);
        mRemove = (Button) findViewById(R.id.remove);
        mAdd.setEnabled(false);
        mRemove.setEnabled(false);
        mAdd.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                addHeatMap();
            }
        });
        mRemove.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                heatmap.removeHeatMap();
                mAdd.setEnabled(true);
                mRemove.setEnabled(false);
            }
        });
        addHeatMap();
    }

    private void addHeatMap() {
        final Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (!isDestroy) {
                    mMapControl.addHeatMap(heatmap);
                }
                mAdd.setEnabled(false);
                mRemove.setEnabled(true);
            }
        };
        new Thread() {
            @Override
            public void run() {
                super.run();
                // 获得包含坐标的集合
                List<LatLng> data = getLocations();
                //设置渐变颜色值
                int[] DEFAULT_GRADIENT_COLORS = {Color.rgb(102, 225, 0), Color.rgb(102, 225, 225), Color.rgb(255, 0, 0)};
                //设置渐变颜色起始值
                float[] DEFAULT_GRADIENT_START_POINTS = {0.1f, 0.5f, 1f};
                Gradient gradient = new Gradient(DEFAULT_GRADIENT_COLORS, DEFAULT_GRADIENT_START_POINTS);
                heatmap = new HeatMap.Builder().data(data).gradient(gradient).opacity(0.6).radius(12).build();
                h.sendEmptyMessage(0);
            }
        }.start();
    }

    /* 读取raw下json文件,将文件中的json数据进行解析存储进集合 */
    private List<LatLng> getLocations() {
        List<LatLng> list = new ArrayList<>();
        InputStream inputStream = getResources().openRawResource(R.raw.locations);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array;
        try {
            array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                double lat = object.getDouble("lat");
                double lng = object.getDouble("lng");
                list.add(new LatLng(lat, lng));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
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
        isDestroy = true;
        // activity 销毁时同时销毁地图控件
        mMapView.onDestroy();
    }
}
