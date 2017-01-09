/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package baidumapsdk.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;

import baidumapsdk.demo.cloud.CloudSearchDemo;
import baidumapsdk.demo.map.BaseMapDemo;
import baidumapsdk.demo.map.FavoriteDemo;
import baidumapsdk.demo.map.GeometryDemo;
import baidumapsdk.demo.map.HeatMapDemo;
import baidumapsdk.demo.map.IndoorMapDemo;
import baidumapsdk.demo.map.LayersDemo;
import baidumapsdk.demo.map.LocationDemo;
import baidumapsdk.demo.map.MapControlDemo;
import baidumapsdk.demo.map.MapFragmentDemo;
import baidumapsdk.demo.map.MarkerClusterDemo;
import baidumapsdk.demo.map.MultiMapViewDemo;
import baidumapsdk.demo.map.OfflineDemo;
import baidumapsdk.demo.map.OpenglDemo;
import baidumapsdk.demo.map.OverlayDemo;
import baidumapsdk.demo.map.TextureMapViewDemo;
import baidumapsdk.demo.map.TileOverlayDemo;
import baidumapsdk.demo.map.TrackShowDemo;
import baidumapsdk.demo.map.UISettingDemo;
import baidumapsdk.demo.radar.RadarDemo;
import baidumapsdk.demo.search.BusLineSearchDemo;
import baidumapsdk.demo.search.DistrictSearchDemo;
import baidumapsdk.demo.search.GeoCoderDemo;
import baidumapsdk.demo.search.IndoorSearchDemo;
import baidumapsdk.demo.search.PoiSearchDemo;
import baidumapsdk.demo.search.RoutePlanDemo;
import baidumapsdk.demo.search.ShareDemo;
import baidumapsdk.demo.util.OpenBaiduMap;

/*
* // 如果只需要单独使用时,在初始化时要注意该initialize方法要再setContentView方法之前实现
* SDKInitializer.initialize(getApplicationContext());
* setContentView(R.layout.activity_main);
* */
/*
*  地图支持缩放至21级，暂不支持卫星图、热力图、交通路况图层的21级显示，打开以上类型图层，地图会自动缩放到20级
*  层级压盖关系: 1为最底层 13为最上层 
*   1、基础底图（包括底图、底图道路、卫星图、室内图等）;
*   2、瓦片图层（TileOverlay）;
*   3、地形图图层（GroundOverlay）;
*   4、热力图图层（HeatMap）;
*   5、实时路况图图层（BaiduMap.setTrafficEnabled(true);）;
*   6、百度城市热力图（BaiduMap.setBaiduHeatMapEnabled(true);）;
*   7、底图标注（指的是底图上面自带的那些POI元素）;
*   8、几何图形图层（点、折线、弧线、圆、多边形）;
*   9、标注图层（Marker），文字绘制图层（Text）;
*   10、指南针图层（当地图发生旋转和视角变化时，默认出现在左上角的指南针）;
*   11、定位图层（BaiduMap.setMyLocationEnabled(true);）;
*   12、弹出窗图层（InfoWindow）;
*   13、自定义View（MapView.addView(View);）;
*
* */
public class MainActivity extends Activity {

    /* 保存有所有功能的Activity信息*/
    private static final SingleFunctionBean[] mFunctionList = {
            new SingleFunctionBean(R.string.name_gexing_map, R.string.describe_gexing_map, BaseMapDemo.class),
            new SingleFunctionBean(R.string.name_fragment_map, R.string.describe_fragment_map, MapFragmentDemo.class),
            new SingleFunctionBean(R.string.name_tuceng_show_map, R.string.describe_tuceng_show_map, LayersDemo.class),
            new SingleFunctionBean(R.string.name_multi_map, R.string.describe_multi_map, MultiMapViewDemo.class),
            new SingleFunctionBean(R.string.name_control_map, R.string.describe_control_map, MapControlDemo.class),
            new SingleFunctionBean(R.string.name_ui_setting_map, R.string.describe_ui_setting_map, UISettingDemo.class),
            new SingleFunctionBean(R.string.name_location_map, R.string.describe_location_map, LocationDemo.class),
            new SingleFunctionBean(R.string.name_fu_gai_wu_map, R.string.describe_fu_gai_wu_map, GeometryDemo.class),
            new SingleFunctionBean(R.string.name_fu_gai_wu_yu_xia_two_map, R.string.describe_fu_gai_wu_yu_xia_two_map, OverlayDemo.class),
            new SingleFunctionBean(R.string.name_heat_map, R.string.describe_heat_map, HeatMapDemo.class),
            new SingleFunctionBean(R.string.name_search_address_zuobiao_map, R.string.describe_search_address_zuobiao_map, GeoCoderDemo.class),
            new SingleFunctionBean(R.string.name_search_poi_suggestion_search_map, R.string.describe_search_poi_suggestion_search_map, PoiSearchDemo.class),
            new SingleFunctionBean(R.string.demo_title_route, R.string.demo_desc_route, RoutePlanDemo.class),// 未完成
            new SingleFunctionBean(R.string.name_districsearch_map, R.string.describe_districsearch_map, DistrictSearchDemo.class),
            new SingleFunctionBean(R.string.name_search_bus_line_map, R.string.describe_search_bus_line_map, BusLineSearchDemo.class),
            new SingleFunctionBean(R.string.name_search_share_url_map, R.string.describe_search_share_url_map, ShareDemo.class),
            new SingleFunctionBean(R.string.demo_title_offline, R.string.demo_desc_offline, OfflineDemo.class),
            new SingleFunctionBean(R.string.demo_title_radar, R.string.demo_desc_radar, RadarDemo.class),
            new SingleFunctionBean(R.string.demo_title_open_baidumap, R.string.demo_desc_open_baidumap, OpenBaiduMap.class),
            new SingleFunctionBean(R.string.demo_title_favorite, R.string.demo_desc_favorite, FavoriteDemo.class),
            new SingleFunctionBean(R.string.demo_title_cloud, R.string.demo_desc_cloud, CloudSearchDemo.class),
            new SingleFunctionBean(R.string.demo_title_opengl, R.string.demo_desc_opengl, OpenglDemo.class),
            new SingleFunctionBean(R.string.demo_title_cluster, R.string.demo_desc_cluster, MarkerClusterDemo.class),
            new SingleFunctionBean(R.string.demo_title_tileoverlay, R.string.demo_desc_tileoverlay, TileOverlayDemo.class),
            new SingleFunctionBean(R.string.demo_title_texturemapview, R.string.demo_desc_texturemapview, TextureMapViewDemo.class),
            new SingleFunctionBean(R.string.demo_title_indoor, R.string.demo_desc_indoor, IndoorMapDemo.class),
            new SingleFunctionBean(R.string.demo_title_indoorsearch, R.string.demo_desc_indoorsearch, IndoorSearchDemo.class),
            new SingleFunctionBean(R.string.demo_title_track_show, R.string.demo_desc_track_show, TrackShowDemo.class)
    };
    private SDKInitResultReceiver mSDKInitResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        TextView text = (TextView) findViewById(R.id.init_sdk_result);
        text.setTextColor(Color.BLACK);

        // VersionInfo.getApiVersion()获取百度地图库当前API的版本号

        ListView mFunctionListView = (ListView) findViewById(R.id.function_listView);
        // 添加adapter，设置事件响应
        mFunctionListView.setAdapter(new FunctionListAdapter());
        mFunctionListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                skipToFunctionActivity(position);
            }
        });

        // 创建意图过滤器 (该意图过滤器过滤出来的广播内容是 SDKInitializer.initialize(this)初始化结果 )
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        intentFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        intentFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        // 创建一个自定义的广播类，该广播类接收的是 SDKInitializer.initialize(this)初始化结果
        mSDKInitResult = new SDKInitResultReceiver();
        // 将上面创建的意图过滤器和自定义广播绑定在一起,并在本类中注册
        registerReceiver(mSDKInitResult, intentFilter);
    }

    private void skipToFunctionActivity(int position) {
        Intent intent;
        intent = new Intent(MainActivity.this, mFunctionList[position].mFunctionActivity);
        this.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销监听 SDK初始化的 广播
        unregisterReceiver(mSDKInitResult);
    }

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKInitResultReceiver extends BroadcastReceiver {

        @SuppressLint("SetTextI18n")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            TextView initSDKResult = (TextView) findViewById(R.id.init_sdk_result);

            Log.e("接收初始化SDK结果的广播的action是:", action);

            if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                // 初始sdk后如果key验证错误，在发送广播时, 会在Intent中put进"SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE"为key，错误码为value ;
                int errorInt = intent.getIntExtra(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0);
                initSDKResult.setText("key 验证出错! 错误码 :" + errorInt + " ; 请在 AndroidManifest.xml 文件中检查 key 设置");
                initSDKResult.setTextColor(Color.RED);
            } else if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                initSDKResult.setText("key 验证成功! 功能可以正常使用");
                initSDKResult.setTextColor(Color.BLACK);
            } else if (action.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                initSDKResult.setText("网络出错");
                initSDKResult.setTextColor(Color.RED);
            }
        }
    }

    private class FunctionListAdapter extends BaseAdapter {
        FunctionListAdapter() {
            super();
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            convertView = View.inflate(MainActivity.this, R.layout.item_function_intro, null);
            TextView functionName = (TextView) convertView.findViewById(R.id.function_name);
            TextView functionDescribe = (TextView) convertView.findViewById(R.id.function_describe);
            functionName.setText(mFunctionList[index].mFunctionName);
            functionDescribe.setText(mFunctionList[index].mFunctionDescribe);
            return convertView;
        }

        @Override
        public int getCount() {
            return mFunctionList.length;
        }

        @Override
        public Object getItem(int index) {
            return mFunctionList[index];
        }

        @Override
        public long getItemId(int id) {
            return id;
        }
    }

    /* adapter中的数据源的Bean类 */
    private static class SingleFunctionBean {
        private final int mFunctionName;
        private final int mFunctionDescribe;
        private final Class<? extends Activity> mFunctionActivity;

        SingleFunctionBean(int mFunctionName, int mFunctionDescribe, Class<? extends Activity> mFunctionActivity) {
            this.mFunctionName = mFunctionName;
            this.mFunctionDescribe = mFunctionDescribe;
            this.mFunctionActivity = mFunctionActivity;
        }
    }
}