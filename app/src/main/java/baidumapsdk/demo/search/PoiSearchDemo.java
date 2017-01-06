package baidumapsdk.demo.search;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiAddrInfo;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.util.ArrayList;
import java.util.List;

import baidumapsdk.demo.R;

/**
 * 演示poi搜索功能
 */
public class PoiSearchDemo extends FragmentActivity implements
        OnGetPoiSearchResultListener, OnGetSuggestionResultListener {

    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    private BaiduMap mBaiduMap = null;
    private List<String> suggestData;
    /**
     * 搜索关键字输入窗口
     */
    private EditText editCity = null;
    private AutoCompleteTextView keyWorldsView = null;
    private ArrayAdapter<String> sugAdapter = null;
    private int loadIndex = 0;

    LatLng center = new LatLng(39.92235, 116.380338);
    int radius = 100;

    LatLng southwest = new LatLng(39.92235, 116.380338);
    LatLng northeast = new LatLng(39.947246, 116.414977);
    LatLngBounds searchbound = new LatLngBounds.Builder().include(southwest).include(northeast).build();

    int searchType = 0;  // 搜索的类型，在显示时区分

    /*
    *   在线建议查询功能 : SuggestionSearch
    *       要使用该功能,需要一个SuggestionSearch对象(SuggestionSearch.newInstance():类调用静态方法获得实例对象),当我们不再需要使用该功能时记得使用SuggestionSearch对象调用destroy()方法从而释放对象资源;
    *       然后在正式使用功能前,记得让SuggestionSearch对象注册一个请求结果监听器:setOnGetSuggestionResultListener(OnGetSuggestionResultListener listener),该接口接收在线建议查询的建议结果;
    *       如此我们就可以使用SuggestionSearch对象调用requestSuggestion(在此处我们需要传入SuggestionSearchOption建议查询请求参数配置对象)方法进行网络请求获取在线建议查询的建议结果,requestSuggestion成功发起检索返回true ,失败返回false;
    *           SuggestionSearchOption建议查询请求参数配置对象,该对象我们有两种配置方式: 第一种,直接使用该对象调用location(传入指定位置坐标对象),这个貌似现在有问题。第二种,分别调用city(传入建议请求城市的字符串),keyword(传入建议关键字的字符串);
    *       最后我们在OnGetSuggestionResultListener监听接口中的onGetSuggestionResult(SuggestionResult res)方法中可以对结果进行处理,SuggestionResult对象包含最终结果集合;
    *           SuggestionResult对象调用getAllSuggestions()方法,返回SuggestionResult.SuggestionInfo内部类的集合;
    *           SuggestionInfo这个内部类是包含一个结果的详细信息的类,其各个成员变量存储了不同的信息,其中最主要的是根据我们传入的要查询的关键字联想出来的全称key,还有这个key的uid,所在城市,所在行政区,坐标等;
    *   Poi点搜索功能 : PoiSearch
    *       要使用该功能,需要一个PoiSearch对象(PoiSearch.newInstance():类调用静态方法获得实例对象),当我们不再需要使用该功能时记得使用PoiSearch对象调用destroy()方法从而释放对象资源;
    *       然后在正式使用功能前,记得让PoiSearch对象注册一个请求搜索结果监听器:setOnGetPoiSearchResultListener(OnGetPoiSearchResultListener listener),该接口接收Poi点搜索结果;
    *       如此我们就可以使用SuggestionSearch对象调用各种请求搜索Poi点(下列检索请求方法成功发起检索返回true,失败返回false):
    *           城市内检索 : SuggestionSearch对象调searchInCity(传入PoiCitySearchOption城市检索Poi点配置对象)方法,PoiCitySearchOption对象配置方法:调用city(传入检索限定的城市String),keyword(传入要检索的关键字String),pageNum(传入分页页数),pageCapacity(传入一页结果能保存多少POI点信息,并根据该参数计算可以分多少页)
    *           范围内检索 : SuggestionSearch对象调searchInBound(传入PoiBoundSearchOption范围检索Poi点配置对象)方法,PoiBoundSearchOption对象配置方法:调用bound(传入想要检索的范围的LatLngBounds范围坐标对象),keyword()和pageNum()和pageCapacity()上面一样;
    *           以坐标为中心进行周边检索 : SuggestionSearch对象调searchNearby(传入PoiNearbySearchOption周边检索Poi点配置对象)方法,PoiNearbySearchOption对象配置方法:调用location(传入坐标对象,为周边检索的中心点),radius(设置检索半径参数),sortType(搜索结果排序规则),keyword()和pageNum()和pageCapacity()上面一样;
    *           POI详情检索 : SuggestionSearch对象调searchPoiDetail(传入PoiDetailSearchOption检索Poi点详情的配置对象)方法,PoiDetailSearchOption对象配置方法就一个poiUid(传入欲检索的poi的uid)方法
    *           POI室内检索 :  TODO 暂时空着
    *       最后我们在OnGetPoiSearchResultListener监听接口中有三个监听回调方法:
    *           onGetPoiResult(PoiResult result) : 城市内检索,范围内检索,以坐标为中心进行周边检索三种请求检索的结果会返回到该方法内
    *               result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND判断条件为true说明没有结果
    *               result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD判断条件为true有结果,但是关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表(PoiResult对象调用getSuggestCityList方法可以获得城市列表)
    *               result.error == SearchResult.ERRORNO.NO_ERROR判断条件为true有结果,PoiResult对象调用getAllPoi()方法可以获得按照需求检索后的POI信息PoiInfo列表(PoiInfo类的集合),以及其他页数编码总页数poi总数等
    *                   扩展:在检索结果是城市内检索的请求时,该结果当isHasAddrInfo()返回true时，除了原poi列表外，还包含门址结果可以通过getAllAddr()方法获得
    *           onGetPoiDetailResult(PoiDetailResult result) : POI详情检索的请求检索的结果会返回到该方法内
    *               使用该result.error == SearchResult.ERRORNO.NO_ERROR判断条件去判断是否有结果,如果有结果那PoiDetailResult对象可以通过get系列方法获得具体信息
    *           onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) : POI室内检索的请求检索的结果会返回到该方法内
    *               TODO 暂时空着
    *
    *   LatLngBounds范围坐标对象: 该对象的建造者的include(LatLng point)方法可以不断调用,最后内部进行算法,所有坐标都在同一个矩形区域内,该对象就代表该区域
    *
    * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poisearch);

        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);

        editCity = (EditText) findViewById(R.id.city);
        keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
        sugAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
        keyWorldsView.setAdapter(sugAdapter);
        keyWorldsView.setThreshold(1);// 设置从输入第几个字符起出现提示

        mBaiduMap = ((SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map))).getBaiduMap();
        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        keyWorldsView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                if (cs.length() <= 0) {
                    return;
                }

                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).keyword(keyWorldsView.getText().toString()).city(editCity.getText().toString()));
            }
        });

    }

    /**
     * 响应城市内搜索按钮点击事件
     */
    public void searchButtonProcess(View v) {
        searchType = 1;
        String citystr = editCity.getText().toString();
        String keystr = keyWorldsView.getText().toString();
        mPoiSearch.searchInCity((new PoiCitySearchOption()).city(citystr).keyword(keystr).pageNum(loadIndex).pageCapacity(20));
        loadIndex++;
    }

    /**
     * 响应周边搜索按钮点击事件
     */
    public void searchNearbyProcess(View v) {
        searchType = 2;
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption().keyword(keyWorldsView.getText().toString())
                .sortType(PoiSortType.distance_from_near_to_far).location(center)
                .radius(radius).pageNum(loadIndex);
        mPoiSearch.searchNearby(nearbySearchOption);
    }

    /**
     * 响应区域搜索按钮点击事件
     */
    public void searchBoundProcess(View v) {
        searchType = 3;
        mPoiSearch.searchInBound(new PoiBoundSearchOption().bound(searchbound).keyword(keyWorldsView.getText().toString()));
    }


    /**
     * 获取POI搜索结果，包括searchInCity，searchNearby，searchInBound返回的搜索结果
     */
    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(PoiSearchDemo.this, "未找到结果", Toast.LENGTH_LONG).show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            if (result.isHasAddrInfo()) {
                System.out.println("*****************************************************************************");
                for (PoiAddrInfo poiAddrInfo : result.getAllAddr()) {
                    System.out.println(poiAddrInfo.address + "---" + poiAddrInfo.name);
                }
            }
            mBaiduMap.clear();
            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result);
            overlay.addToMap();
            overlay.zoomToSpan();

            switch (searchType) {
                case 2:
                    showNearbyArea(center, radius);
                    break;
                case 3:
                    showBound(searchbound);// 该变地图状态中心为检索区域中心
                    break;
                default:
                    break;
            }

            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(PoiSearchDemo.this, strInfo, Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * 获取POI详情搜索结果，得到searchPoiDetail返回的搜索结果
     */
    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(PoiSearchDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(PoiSearchDemo.this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取POI室内搜索结果，得到PoiIndoorResult返回的搜索结果
     */
    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    /**
     * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
     */
    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }
        suggestData = new ArrayList<String>();
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null) {
                suggestData.add(info.key);
            }
        }
        sugAdapter = new ArrayAdapter<String>(PoiSearchDemo.this, android.R.layout.simple_dropdown_item_1line, suggestData);
        keyWorldsView.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
    }

    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poi.uid));
            return true;
        }
    }

    /**
     * 对周边检索的范围进行绘制: 添加一个自定义图标的Marker覆盖物和一个Circlet圆覆盖物
     */
    public void showNearbyArea(LatLng center, int radius) {
        BitmapDescriptor centerBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_geo);
        MarkerOptions ooMarker = new MarkerOptions().position(center).icon(centerBitmap);
        mBaiduMap.addOverlay(ooMarker);

        OverlayOptions ooCircle = new CircleOptions().fillColor(0xCCCCCC00)
                .center(center).stroke(new Stroke(5, 0xFFFF00FF))
                .radius(radius);
        mBaiduMap.addOverlay(ooCircle);
    }

    /**
     * 在MapView上添加一个GroundOverlay覆盖物,该覆盖物是以区域检索时使用的区域坐标为模版的,并根据区域坐标的中心来改变地图状态中心
     */
    public void showBound(LatLngBounds bounds) {
        BitmapDescriptor bdGround = BitmapDescriptorFactory
                .fromResource(R.drawable.ground_overlay);

        OverlayOptions ooGround = new GroundOverlayOptions()
                .positionFromBounds(bounds).image(bdGround).transparency(0.8f);
        mBaiduMap.addOverlay(ooGround);

        MapStatusUpdate u = MapStatusUpdateFactory
                .newLatLng(bounds.getCenter());
        mBaiduMap.setMapStatus(u);

        bdGround.recycle();
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
        mPoiSearch.destroy();// 释放PoiSearch资源
        mSuggestionSearch.destroy();// 释放在线建议搜索资源
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
