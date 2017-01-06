package baidumapsdk.demo.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 演示MapView的基本用法
 */
public class BaseMapDemo extends Activity {

    private MapView mMapView;
    private FrameLayout frameLayout;
    private static final int OPEN_ID = 0;
    private static final int CLOSE_ID = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // 设置个性化地图的样式是否生效显示 不用管MapView / TextureMapView / WearMapView是否创建了
        MapView.setMapCustomEnable(true);

        super.onCreate(savedInstanceState);

        // 该方法中会将样式模版的绝对路径 通过setCustomMapStylePath方法进行设置,这个方法必须在MapView / TextureMapView / WearMapView创建前调用
        setMapCustomFile(this);

        // 创建一个地图控件MapView, 其中参数二BaiduMapOptions对象包含的是各种地图初始化时的参数设置    猜测: MapView通过调用getBaiduMap()获得的BaiduMap对象的所有参数都是BaiduMapOptions的参数为模版的也可以说是BaiduMap地图控制器的参数设置对象
        // 例如:compassEnabled是否显示指南针;mapType设置地图模式;scaleControlEnabled设置是否显示比例尺;zoomGesturesEnabled设置是否允许缩放手势等等
        // new BaiduMapOptions().compassEnabled(true).mapType(BaiduMap.MAP_TYPE_NORMAL).scaleControlEnabled(true).zoomGesturesEnabled(true);
        // TODO BaiduMapOptions的设置没有进行过实测
        mMapView = new MapView(this, new BaiduMapOptions());

        initView(this);

        setContentView(frameLayout);
    }

    // 初始化View
    private void initView(Context context) {
        // 创建包含所有控件的父控件视图
        frameLayout = new FrameLayout(this);
        frameLayout.addView(mMapView);// 将地图控件添加进父控件

        // 点选组控件
        RadioGroup group = new RadioGroup(context);
        group.setBackgroundColor(Color.BLACK);

        // 开启个性化地图的点选控件
        final RadioButton openBtn = new RadioButton(context);
        openBtn.setText("开启个性化地图");
        openBtn.setId(OPEN_ID);
        openBtn.setTextColor(Color.WHITE);
        openBtn.setChecked(true);

        // 关闭个性化地图的点选控件
        final RadioButton closeBtn = new RadioButton(context);
        closeBtn.setText("关闭个性化地图");
        closeBtn.setTextColor(Color.WHITE);
        closeBtn.setId(CLOSE_ID);

        // 点选控件的布局位置
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        // 将点选控件加入点选组
        group.addView(openBtn, params);
        group.addView(closeBtn, params);

        // 点选组的布局位置，并添加进父控件
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        frameLayout.addView(group, layoutParams);

        // 设置点选组在不同点选下是否显示个性地图
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == OPEN_ID) {
                    MapView.setMapCustomEnable(true);
                } else if (checkedId == CLOSE_ID) {
                    MapView.setMapCustomEnable(false);
                }
            }
        });
    }

    // 设置个性化地图config文件路径
    private void setMapCustomFile(Context context) {

        // 下面的读写是将assets下的模版文件读写到sd卡中
        FileOutputStream out = null;
        InputStream inputStream = null;
        String moduleName = null;
        try {
            inputStream = context.getAssets().open("customConfigdir/custom_config.txt");
            byte[] b = new byte[inputStream.available()];// inputStream.available() 返回的值是该inputStream在不被阻塞的情况下一次可以读取到的数据长度
            inputStream.read(b);

            moduleName = context.getFilesDir().getAbsolutePath();
            File f = new File(moduleName + "/" + "custom_config.txt");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            out = new FileOutputStream(f);
            out.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 将已经读写到sd卡中的模版文件的绝对路径进行设置
        MapView.setCustomMapStylePath(moduleName + "/custom_config.txt");

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
        // 猜测: 有可能该方法的设置会影响到其他MapView / TextureMapView / WearMapView是否显示样式
        MapView.setMapCustomEnable(false);
    }

}
