package baidumapsdk.demo.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;

import baidumapsdk.demo.R;

/**
 * 演示地图图层显示的控制方法
 */
public class LayersDemo extends Activity {

    /**
     * MapView 是地图主控件
     */

    private MapView mMapView;
    private BaiduMap mMapControl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layers);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapControl = mMapView.getMap();// 获取BaiduMap(地图控制器)
    }

    /**
     * 设置RadioGroup下的单选点击事件
     * 设置底图显示模式
     * 使用地图控制器可以调用setMapType来显示具体底图模式有三种可选 BaiduMap.MAP_TYPE_NORMAL:普通图; BaiduMap.MAP_TYPE_SATELLITE:卫星图; BaiduMap.MAP_TYPE_NONE:空白图
     */
    public void setMapMode(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.show_normal_map:// 显示普通图
                if (checked) {
                    mMapControl.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }
                break;
            case R.id.show_satellite_map:// 显示卫星图
                if (checked) {
                    mMapControl.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                }
                break;
            default:
                break;
        }
    }

    /**
     * setTrafficEnabled设置是否显示交通图
     */
    public void setTraffic(View view) {
        mMapControl.setTrafficEnabled(((CheckBox) view).isChecked());
    }

    /**
     * setBaiduHeatMapEnabled设置是否显示百度热力图
     */
    public void setBaiduHeatMap(View view) {
        mMapControl.setBaiduHeatMapEnabled(((CheckBox) view).isChecked());
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
