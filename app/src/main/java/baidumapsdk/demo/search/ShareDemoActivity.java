package baidumapsdk.demo.search;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.share.LocationShareURLOption;
import com.baidu.mapapi.search.share.OnGetShareUrlResultListener;
import com.baidu.mapapi.search.share.PoiDetailShareURLOption;
import com.baidu.mapapi.search.share.RouteShareURLOption;
import com.baidu.mapapi.search.share.ShareUrlResult;
import com.baidu.mapapi.search.share.ShareUrlSearch;

import baidumapsdk.demo.R;

import static com.baidu.mapapi.search.share.RouteShareURLOption.RouteShareMode;

/**
 * 演示短串分享功能，
 */
public class ShareDemoActivity extends Activity implements
        OnGetPoiSearchResultListener, OnGetShareUrlResultListener,
        OnGetGeoCoderResultListener, BaiduMap.OnMarkerClickListener {

    private MapView mMapView = null;
    private PoiSearch poiSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private ShareUrlSearch shareUrlSearch = null;
    private GeoCoder geoCoder = null;
    // 保存搜索结果地址
    private String currentAddr = null;

    private BaiduMap mMapControl = null;
    private Marker mAddrMarker = null;
    private RouteShareMode mRouteShareMode;


    /*
    * POI详情,Location,路线规划分享url查询:
    *   初始化: ShareUrlSearch.newInstance();
    *   设置查询结果监听: setOnGetShareUrlResultListener(new OnGetShareUrlResultListener())
    *   开启请求: shareUrlSearch.requestPoiDetailShareUrl(new PoiDetailShareURLOption().poiUid(需要POI的uid));
    *            shareUrlSearch.requestLocationShareUrl(new LocationShareURLOption().location(需要Location地理坐标).snippet("通过短URL调起客户端时作为附加信息显示在名称下面").name(需要Location的地址名称));
    *            shareUrlSearch.requestRouteShareUrl(new RouteShareURLOption().from(起始点坐标对象).to(终点坐标对象).routMode(行驶模式,例如:RouteShareMode.FOOT_ROUTE_SHARE_MODE,其余查api));如果行驶模式为公交还需要调用cityCode(int cityCode)方法设置城市id,如果有多线路,那还可以调用pn(int pn)设置要分享检索路线中的哪条
    *   监听结果: onGetLocationShareUrlResult(ShareUrlResult result)位置分享URL结果回调
    *            onGetPoiDetailShareUrlResult(ShareUrlResult result)poi详情分享URL结果回调
    *            onGetRouteShareUrlResult(ShareUrlResult result)路线规划分享URL结果回调
    *   最后不用的时候记得释放ShareUrlSearch对象
    * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_demo_activity);

        // 初始化地图控件
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapControl = mMapView.getMap();

        // 初始化POI搜索功能并注册监听接口
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化分享url查询功能并注册监听接口
        shareUrlSearch = ShareUrlSearch.newInstance();
        shareUrlSearch.setOnGetShareUrlResultListener(this);

        // 初始化地理编码和反地理编码搜索功能并注册监听接口
        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(this);

        mRouteShareMode = RouteShareMode.FOOT_ROUTE_SHARE_MODE;
    }

    // ****************************************************************************************************************************************************s

    /*
    * id为poishore的poi搜索结果分享按钮的点击事件
    * */
    public void sharePoi(View view) {
        // 发起poi搜索 北京城市中餐馆
        String mCity = "北京";
        String searchKey = "餐馆";
        poiSearch.searchInCity((new PoiCitySearchOption()).city(mCity).keyword(searchKey));
        Toast.makeText(this, "在" + mCity + "搜索 " + searchKey, Toast.LENGTH_SHORT).show();
    }

    /*
    * poi搜索结果监听返回方法, 将搜索的结果在MapView上展示,实质就是添加Marker覆盖物,并在PoiShareOverlay中重写Marker覆盖物的点击事件
    * */
    @Override
    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(ShareDemoActivity.this, "抱歉，未找到结果", Toast.LENGTH_LONG).show();
            return;
        }
        mMapControl.clear();
        PoiShareOverlay poiOverlay = new PoiShareOverlay(mMapControl);
        mMapControl.setOnMarkerClickListener(poiOverlay);
        poiOverlay.setData(result);
        poiOverlay.addToMap();
        poiOverlay.zoomToSpan();
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    /**
     * 使用PoiOverlay 展示poi点，在poi被点击时发起短串请求.
     */
    private class PoiShareOverlay extends PoiOverlay {

        PoiShareOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int i) {
            // TODO 此处请求POI详情分享的URL
            // 点击了Marker覆盖物后获取该POI点的PoiInfo信息对象,通过对象获取其具体地址,赋值该currentAddr字符串String,在正式一键分享时用
            PoiInfo info = getPoiResult().getAllPoi().get(i);
            currentAddr = info.address;
            shareUrlSearch.requestPoiDetailShareUrl(new PoiDetailShareURLOption().poiUid(info.uid));
            return true;
        }
    }

    // ******************************************************************************************************************************************************

    /*
     * id为addrshare的反向地理编码分享按钮的点击事件 ,将地理坐标反编为文字地址
     * */
    @SuppressLint("DefaultLocale")
    public void shareAddr(View view) {
        LatLng latLng = new LatLng(40.056878, 116.308141);
        // 发起反地理编码请求
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
        Toast.makeText(this, String.format("搜索位置： %f，%f", latLng.latitude, latLng.longitude), Toast.LENGTH_SHORT).show();
    }

    /*
    * 反向地理编码监听返回的结果
    * */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {

    }

    // 通过坐标反编为地址的结果,添加一个Marker到Mapview上
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(ShareDemoActivity.this, "抱歉，未找到结果", Toast.LENGTH_LONG).show();
            return;
        }
        mMapControl.clear();
        mMapControl.setOnMarkerClickListener(this);
        mAddrMarker = (Marker) mMapControl.addOverlay(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka))
                .title(result.getAddress()).position(result.getLocation()));
    }

    // 点击坐标反编为地址的Marker
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker == mAddrMarker) {
            // TODO 此处请求Location分享的URL,与反编码没太大的关系,只是LocationShareURLOption配置对象需要地理坐标与对应的地址
            // snippet通过短URL调起客户端时作为附加信息显示在名称下面
            shareUrlSearch.requestLocationShareUrl(new LocationShareURLOption().location(marker.getPosition()).snippet("测试分享点").name(marker.getTitle()));
        }
        return true;
    }

    // ******************************************************************************************************************************************************

    /*
    * id为routeMode的单选组的点击事件,选择RouteShareURLOption配置对象的routMode方法的参数
    * */
    public void setRouteMode(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.foot:
                if (checked) {
                    mRouteShareMode = RouteShareMode.FOOT_ROUTE_SHARE_MODE;
                }
                break;
            case R.id.cycle:
                if (checked) {
                    mRouteShareMode = RouteShareMode.CYCLE_ROUTE_SHARE_MODE;
                }
                break;
            case R.id.car:
                if (checked) {
                    mRouteShareMode = RouteShareMode.CAR_ROUTE_SHARE_MODE;
                }
                break;
            case R.id.bus:
                if (checked) {
                    mRouteShareMode = RouteShareMode.BUS_ROUTE_SHARE_MODE;
                }
                break;
            default:
                break;
        }
    }

    /*
    * id为routeShare的分享按钮的点击事件, 以起终点为参数并设置行驶模式,如果行驶模式为公交还需要调用cityCode(int cityCode)方法设置城市id,如果有多线路,那还可以调用pn(int pn)设置要分享检索路线中的哪条
    * */
    public void shareRoute(View view) {
        PlanNode startNode = PlanNode.withLocation(new LatLng(40.056885, 116.308142));
        PlanNode enPlanNode = PlanNode.withLocation(new LatLng(39.921933, 116.488962));
        // TODO 此处请求线路规划分享的URL
        shareUrlSearch.requestRouteShareUrl(new RouteShareURLOption().from(startNode).to(enPlanNode).routMode(mRouteShareMode));
    }

    // *****************************************************************************************************************************************************

    /*
    * 分享url查询请求监听结果
    * */
    @Override
    public void onGetPoiDetailShareUrlResult(ShareUrlResult result) {

        // 分享短串结果
        Intent it = new Intent(Intent.ACTION_SEND);
        it.putExtra(Intent.EXTRA_TEXT, "您的朋友通过百度地图SDK与您分享一个POI点详情: " + currentAddr + " -- " + result.getUrl());
        it.setType("text/plain");
        startActivity(Intent.createChooser(it, "将短串分享到"));

    }

    @Override
    public void onGetLocationShareUrlResult(ShareUrlResult result) {

        // 分享短串结果
        Intent it = new Intent(Intent.ACTION_SEND);
        it.putExtra(Intent.EXTRA_TEXT, "您的朋友通过百度地图SDK与您分享一个位置: " + currentAddr + " -- " + result.getUrl());// result.getUrl()获取共享URL
        it.setType("text/plain");
        startActivity(Intent.createChooser(it, "将短串分享到"));

    }

    @Override
    public void onGetRouteShareUrlResult(ShareUrlResult shareUrlResult) {
        Intent it = new Intent(Intent.ACTION_SEND);
        it.putExtra(Intent.EXTRA_TEXT, "您的朋友通过百度地图SDK与您分享一条路线，URL " + " -- " + shareUrlResult.getUrl());
        it.setType("text/plain");
        startActivity(Intent.createChooser(it, "将短串分享到"));
    }

    // ***********************************************************************************************************************************************************
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
        // 不用时记得释放对象资源
        mMapView.onDestroy();
        poiSearch.destroy();
        shareUrlSearch.destroy();
        super.onDestroy();
    }
}
