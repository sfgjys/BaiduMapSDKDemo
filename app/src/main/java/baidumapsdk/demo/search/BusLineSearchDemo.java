package baidumapsdk.demo.search;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BusLineOverlay;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import baidumapsdk.demo.R;

/**
 * 此demo用来展示如何进行公交线路详情检索，并使用RouteOverlay在地图上绘制 同时展示如何浏览路线节点并弹出泡泡
 */
public class BusLineSearchDemo extends FragmentActivity {
    private Button mBtnPre = null; // 上一个节点
    private Button mBtnNext = null; // 下一个节点
    private int nodeIndex = -2; // 节点索引,供浏览节点时使用
    private BusLineResult busLineResult = null; // 保存驾车/步行路线数据的变量，供浏览节点时使用
    private List<String> busLineUIDList = null;
    private int busLineIndex = 0;
    // 搜索相关
    private PoiSearch poiSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private BusLineSearch busLineSearch = null;
    private BaiduMap mBaiduMap = null;
    BusLineOverlay overlay; // 公交路线绘制对象


    /*
    * 公交线路检索(要想进行公交线路检索,前提需要知道要检索的公交线路的uid):
    *   首先通过POI搜索获取要检索的公交线路的uid:
    *       初始化功能模块对象:PoiSearch.newInstance();获取PoiSearch对象
    *       设置POI搜索结果监听接口:PoiSearch对象调用setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener())方法
    *       开始搜索:PoiSearch对象调用searchInCity((new PoiCitySearchOption()).city(要检索的公交线路所在城市).keyword(要检索的公交线路关键字).pageCapacity(检索结果容量,因为返回的结果都是公交线路在最前面,可以不用担心容量大小问题？));
    *               因为检索公交线路一般都是城内的,所以用城内检索
    *       对监听接口中返回结果进行处理:城内检索的返回结果在onGetPoiResult方法中,PoiResult对象调用getAllPoi()方法获取所有PoiInfo信息对象的集合,
    *               并通过PoiInfo.POITYPE.BUS_LINE和PoiInfo.POITYPE.SUBWAY_LINE来判断本PoiInfo类型是否是公交路线和地铁路线,注意是路线不是站点而且符合类型的PoiInfo有可能是多个,是的话就获取PoiInfo的uid,存储进集合
    *   然后使用uid集合通过公交路线搜索模块进行查询:
    *       初始化功能模块对象:BusLineSearch.newInstance();获取BusLineSearch对象
    *       设置公交路线搜索结果监听接口:BusLineSearch对象调用setOnGetBusLineSearchResultListener(new OnGetBusLineSearchResultListener())方法
    *       开始搜索:BusLineSearch对象调用searchBusLine((new BusLineSearchOption().city(设置查询城市).uid(设置公交路线uid));
    *       对监听接口中返回结果进行处理:接口监听方法获得BusLineResult结果对象,首先busLineResult == null || busLineResult.error != SearchResult.ERRORNO.NO_ERROR判断是否有结果
    *           有结果就可以调用getStations()方法获取站点信息集合添加Marker公交车图标的覆盖物,调用getSteps()获得路段信息集合,路段对象在调用getWayPoints()获取每个路段经过的地点坐标,从而获取所有路段坐标组成一个路线坐标集合,添加线覆盖物
    *   最后不使用功能时记得释放PoiSearch对象和BusLineSearch对象资源
    * */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busline);
        CharSequence titleLable = "公交线路查询功能";
        setTitle(titleLable);

        // 初始化其他ui控件
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);

        // 初始化地图
        mBaiduMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.bmapView)).getBaiduMap();
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mBaiduMap.hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });// 设置地图点击事件隐藏inforwindow

        // 初始化poi点搜索模块
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (poiResult == null || poiResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(BusLineSearchDemo.this, "抱歉，未找到结果", Toast.LENGTH_LONG).show();
                    return;
                }
                // 遍历所有poi，找到类型为公交线路的poi // 处理poi搜索结果获取线路UID
                busLineUIDList.clear();
                for (PoiInfo poi : poiResult.getAllPoi()) {
                    if (poi.type == PoiInfo.POITYPE.BUS_LINE || poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
                        busLineUIDList.add(poi.uid);
                    }
                }
                searchNextBusline(null);
                busLineResult = null;
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });

        // 初始化公交路线搜索模块
        busLineSearch = BusLineSearch.newInstance();
        busLineSearch.setOnGetBusLineSearchResultListener(new OnGetBusLineSearchResultListener() {
            @Override
            public void onGetBusLineResult(BusLineResult busLineResult) {

                if (busLineResult == null || busLineResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(BusLineSearchDemo.this, "抱歉，未找到结果", Toast.LENGTH_LONG).show();
                    return;
                }
                mBaiduMap.clear();
                BusLineSearchDemo.this.busLineResult = busLineResult;
                nodeIndex = -1;
                overlay.removeFromMap();
                overlay.setData(busLineResult);
                overlay.addToMap();
                overlay.zoomToSpan();
                mBtnPre.setVisibility(View.VISIBLE);
                mBtnNext.setVisibility(View.VISIBLE);
                Toast.makeText(BusLineSearchDemo.this, busLineResult.getBusLineName(), Toast.LENGTH_SHORT).show();
            }

            private void text() {
            }
        });


        busLineUIDList = new ArrayList<String>();
        overlay = new BusLineOverlay(mBaiduMap);

        mBaiduMap.setOnMarkerClickListener(overlay);
    }

    /**
     * 发起公交线路检索
     */
    public void searchButtonProcess(View v) {
        busLineUIDList.clear();
        busLineIndex = 0;
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        EditText editCity = (EditText) findViewById(R.id.city);
        EditText editSearchKey = (EditText) findViewById(R.id.searchkey);
        // 发起poi检索，从得到所有poi中找到公交线路类型的poi，再使用该poi的uid进行公交详情搜索
        poiSearch.searchInCity((new PoiCitySearchOption()).city(editCity.getText().toString()).keyword(editSearchKey.getText().toString()));
    }

    /**
     * 公交线路检索下一条
     */
    public void searchNextBusline(View v) {
        if (busLineIndex >= busLineUIDList.size()) {
            busLineIndex = 0;
        }
        if (busLineIndex >= 0 && busLineIndex < busLineUIDList.size() && busLineUIDList.size() > 0) {
            busLineSearch.searchBusLine((new BusLineSearchOption().city(((EditText) findViewById(R.id.city)).getText().toString()).uid(busLineUIDList.get(busLineIndex))));
            busLineIndex++;
        }
    }

    /**
     * 公交线路节点浏览示例
     */
    public void nodeClick(View v) {

        if (nodeIndex < -1 || busLineResult == null
                || nodeIndex >= busLineResult.getStations().size()) {
            return;
        }
        TextView popupText = new TextView(this);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(0xff000000);
        // 上一个节点
        if (mBtnPre.equals(v) && nodeIndex > 0) {
            // 索引减
            nodeIndex--;
        }
        // 下一个节点
        if (mBtnNext.equals(v) && nodeIndex < (busLineResult.getStations().size() - 1)) {
            // 索引加
            nodeIndex++;
        }
        if (nodeIndex >= 0) {
            // 移动到指定索引的坐标
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(busLineResult
                    .getStations().get(nodeIndex).getLocation()));
            // 弹出泡泡
            popupText.setText(busLineResult.getStations().get(nodeIndex).getTitle());
            mBaiduMap.showInfoWindow(new InfoWindow(popupText, busLineResult.getStations()
                    .get(nodeIndex).getLocation(), 10));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 不用时记得释放对象资源
        poiSearch.destroy();
        busLineSearch.destroy();
        super.onDestroy();
    }


}
