package baidumapsdk.demo.map;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.UiSettings;

import baidumapsdk.demo.R;

/**
 * 演示地图UI控制功能
 */
public class UISettingDemo extends Activity {

    /**
     * MapView 是地图主控件
     */
    private MapView mMapView;
    private BaiduMap mMapControl;
    private UiSettings mUiSettings;
    private static final int paddingLeft = 0;
    private static final int paddingTop = 0;
    private static final int paddingRight = 0;
    private static final int paddingBottom = 200;
    TextView mTextView;

    /*
    * BaiduMap地图控制器调用getUiSettings();可获的UiSettings对象该对象可设置 是否允许指南针;是否允许俯视手势;是否允许旋转手势;是否允许拖拽手势;是否允许缩放手势;是否允许所有手势操作
    * 其中是否允许指南针有一点需要注意:一开始指南针是没有出现的,只有在旋转或者俯视变化时才会出现指南针,接着指南针会一直出现 直到设置是否出现指南针才会出现变化
    * BaiduMap地图控制器调用showMapPoi:是否显示底图的poi点的标注
    * 给父控件MapView添加子控件: 先BaiduMap地图控制器调用setPadding(根据具体情况而定),然后在创建具体的子控件,与子控件具体的布局MapViewLayoutParams,最后将两者作为参数添加进MapView
    *
    *
    * */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uisetting);

        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapControl = mMapView.getMap();
        mUiSettings = mMapControl.getUiSettings();

        MapStatus ms = new MapStatus.Builder().build();
        MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
        mMapControl.animateMapStatus(u, 1000);// 参数而为动画耗时

        mMapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 介绍获取比例尺的宽高，需在MapView绘制完成之后
                int scaleControlViewWidth = mMapView.getScaleControlViewWidth();
                int scaleControlViewHeight = mMapView.getScaleControlViewHeight();
                System.out.println(scaleControlViewWidth + "_-_" + scaleControlViewHeight);
            }
        }, 0);
    }

    /**
     * 是否启用缩放手势
     *
     * @param v
     */
    public void setZoomEnable(View v) {
        mUiSettings.setZoomGesturesEnabled(((CheckBox) v).isChecked());
    }

    /**
     * 是否启用平移手势
     *
     * @param v
     */
    public void setScrollEnable(View v) {
        mUiSettings.setScrollGesturesEnabled(((CheckBox) v).isChecked());
    }

    /**
     * 是否启用旋转手势
     *
     * @param v
     */
    public void setRotateEnable(View v) {
        mUiSettings.setRotateGesturesEnabled(((CheckBox) v).isChecked());
    }

    /**
     * 是否启用俯视手势
     *
     * @param v
     */
    public void setOverlookEnable(View v) {
        mUiSettings.setOverlookingGesturesEnabled(((CheckBox) v).isChecked());
    }

    /**
     * 是否启用指南针图层
     *
     * @param v
     */
    public void setCompassEnable(View v) {
        mUiSettings.setCompassEnabled(((CheckBox) v).isChecked());
    }

    /**
     * 是否禁用所有手势
     *
     * @param v
     */
    public void setAllGestureEnable(View v) {
        mUiSettings.setAllGesturesEnabled(!((CheckBox) v).isChecked());
    }

    /**
     * 是否显示底图默认标注
     *
     * @param v
     */
    public void setMapPoiEnable(View v) {
        mMapControl.showMapPoi(((CheckBox) v).isChecked());
    }

    /**
     * 给MapView添加子控件
     */
    public void setPadding(View v) {
        if (((CheckBox) v).isChecked()) {
            // 如果添加的控件在MapView其他原有控件附近,那给MapView添加子控件的前提:首先通过MapView控件获取地图控制器.通过地图控制器设置Padding: 其意义是为了使得MapView控件上的除了底图层外的其他图层控件让开空间，使得自定义的控件不会覆盖上去
            // 例如:logo图标和缩放+-控件就不能和自定义子控件重合,如此setViewPadding方法就是作用在logo图标和缩放+-控件上的.
            mMapControl.setViewPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            addView(mMapView);
        } else {
            mMapControl.setViewPadding(0, 0, 0, 0);
            mMapView.removeView(mTextView);
        }
    }

    private void addView(MapView mapView) {
        // 创建一个自定义控件
        mTextView = new TextView(this);
        mTextView.setText(getText(R.string.instruction));
        mTextView.setTextSize(15.0f);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextColor(Color.BLACK);
        mTextView.setBackgroundColor(Color.parseColor("#AA00FF00"));

        // 创建子控件在父控件上的具体布局的建造者
        MapViewLayoutParams.Builder builder = new MapViewLayoutParams.Builder();

        builder.layoutMode(MapViewLayoutParams.ELayoutMode.absoluteMode);// 首先设置布局模式: 子控件以(子控件的宽/2,子控件的高)为起始点(这个坐标点是以子控件为范围),模式就是指该起始点以MapView做占屏幕坐标系为坐标还是以经纬度为坐标(两种模式都在MapView控件范围内)

        builder.width(mapView.getWidth());// 布局的宽
        builder.height(paddingBottom);// 布局的高

        // 下面两个方法来确定布局的位置
        builder.point(new Point(0, mapView.getHeight()));// 以屏幕为坐标就调用point方法设置起始点的坐标位置, 以MapView控件为范围,则 起始点为左上角时就为(0,0);起始点为左下角时就为(0,+MapView高);起始点为右上角时就为(+MapView宽,0);起始点为左上角时就为(+MapView宽,+MapView高);
        builder.align(MapViewLayoutParams.ALIGN_LEFT, MapViewLayoutParams.ALIGN_BOTTOM);// 布局的对齐方式

        // builder.yOffset(2); // y轴偏移量

        // 父控件将子控件与布局作为参数进行添加
        mapView.addView(mTextView, builder.build());

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
