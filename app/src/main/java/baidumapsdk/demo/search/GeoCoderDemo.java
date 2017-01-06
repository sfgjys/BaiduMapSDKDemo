package baidumapsdk.demo.search;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import baidumapsdk.demo.R;

/**
 * 此demo用来展示如何进行地理编码搜索（用地址检索坐标）、反地理编码搜索（用坐标检索地址）
 */
public class GeoCoderDemo extends Activity implements OnGetGeoCoderResultListener {
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    BaiduMap mMapControl = null;
    MapView mMapView = null;


    /*
    * 地理编码搜索（用地址检索对应的坐标,这里的地址最好用列表的形式展示出进行选择）、反地理编码搜索（用坐标检索对应的地址）
    * 要实现上面两个功能,需要的是GeoCoder地理编码查询接口,使用步骤:
    *       首先: GeoCoder类直接调用静态方法newInstance()获得GeoCoder对象
    *       再来: GeoCoder对象调用setOnGetGeoCodeResultListener(this)方法,注册回调接口,回调接口有两个方法onGetGeoCodeResult(GeoCodeResult result);onGetReverseGeoCodeResult(ReverseGeoCodeResult result)
    *       再来: GeoCoder对象调用reverseGeoCode(new ReverseGeoCodeOption().location(此处传递要检索的坐标对象))方法,该方法是通过坐标来检索对应的地址
    *               reverseGeoCode方法的检索结果会返回到onGetReverseGeoCodeResult(ReverseGeoCodeResult result)接口方法,result就是结果对象,该对象可以获取坐标对象,简要地址信息,层次化地址信息,位置所属商圈名称,位置附近的POI信息集合
    *       再来: GeoCoder对象调用geocode(new GeoCodeOption().city(此处传入城市字符串).address(此处传入地址字符串))方法,该方法是通过地址来检索对应的坐标
    *               geocode方法的检索结果会返回到onGetGeoCodeResult(GeoCodeResult result)接口方法,result就是结果对象,该对象可以获取坐标对象与地址信息
    *       最后: 不再使用本功能时:GeoCoder对象调用destroy()方法释放资源
    *       注意: 在两个回调接口中,要记得判断是否有检索结果, 判断条件result == null || result.error != SearchResult.ERRORNO.NO_ERROR --> 没有结果
    * */


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geocoder);
        CharSequence titleLable = "地理编码功能";
        setTitle(titleLable);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapControl = mMapView.getMap();

        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
    }

    /**
     * 发起搜索
     *
     * @param v
     */
    public void searchButtonProcess(View v) {
        if (v.getId() == R.id.reversegeocode) {
            EditText lat = (EditText) findViewById(R.id.lat);
            EditText lon = (EditText) findViewById(R.id.lon);
            LatLng ptCenter = new LatLng((Float.valueOf(lat.getText().toString())), (Float.valueOf(lon.getText().toString())));
            // 反Geo搜索
            mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter));
        } else if (v.getId() == R.id.geocode) {
            EditText editCity = (EditText) findViewById(R.id.city);
            EditText editGeoCodeKey = (EditText) findViewById(R.id.geocodekey);
            // Geo搜索
            mSearch.geocode(new GeoCodeOption().city(editCity.getText().toString()).address(editGeoCodeKey.getText().toString()));
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(GeoCoderDemo.this, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
            return;
        }
        // 根据返回结果在MapView上进行展示
        mMapControl.clear();
        mMapControl.addOverlay(new MarkerOptions().position(result.getLocation()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka)));
        mMapControl.setMapStatus(MapStatusUpdateFactory.newLatLng(result.getLocation()));
        String strInfo = String.format("纬度：%f 经度：%f", result.getLocation().latitude, result.getLocation().longitude);
        Toast.makeText(GeoCoderDemo.this, strInfo, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(GeoCoderDemo.this, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
            return;
        }
        // 根据返回结果在MapView上进行展示
        mMapControl.clear();
        mMapControl.addOverlay(new MarkerOptions().position(result.getLocation()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka)));
        mMapControl.setMapStatus(MapStatusUpdateFactory.newLatLng(result.getLocation()));
        Toast.makeText(GeoCoderDemo.this, result.getAddress(), Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        mSearch.destroy();
        super.onDestroy();
    }
}
