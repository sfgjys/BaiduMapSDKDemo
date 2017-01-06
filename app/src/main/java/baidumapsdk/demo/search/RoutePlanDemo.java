/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package baidumapsdk.demo.search;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BikingRouteOverlay;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.MassTransitRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.core.TaxiInfo;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.SuggestAddrInfo;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.ArrayList;
import java.util.List;

import baidumapsdk.demo.R;

/**
 * 此demo用来展示如何进行驾车、步行、公交、骑行、跨城综合路线搜索并在地图使用RouteOverlay、TransitOverlay绘制
 * 同时展示如何进行节点浏览并弹出泡泡
 */
public class RoutePlanDemo extends Activity implements BaiduMap.OnMapClickListener {

    // 浏览路线节点相关
    Button mBtnPre = null; // 上一个节点
    Button mBtnNext = null; // 下一个节点
    int nodeIndex = -1; // 节点索引,供浏览节点时使用
    RouteLine route = null;// 该成员变量是为了展示节点的时候有一个对应的信息
    MassTransitRouteLine massroute = null;
    OverlayManager routeOverlay = null;// 该成员变量是为了在切换自定义起点终点图标时,可以将MapView上本对象添加的覆盖物先移除,在根据选择添加新的覆盖物
    boolean useDefaultIcon = false;
    private TextView popupText = null; // 泡泡view

    // 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    MapView mMapView = null;    // 地图View
    BaiduMap mMapControl = null;
    // 搜索相关
    RoutePlanSearch routePlanSearch = null;    // 搜索模块，也可去掉地图模块独立使用

    WalkingRouteResult nowResultwalk = null;
    BikingRouteResult nowResultbike = null;
    TransitRouteResult nowResultransit = null;
    DrivingRouteResult nowResultdrive = null;
    MassTransitRouteResult nowResultmass = null;

    int nowSearchType = -1; // 当前进行的检索，供判断浏览节点时结果使用。

    String startNodeStr = "西二旗";
    String endNodeStr = "龙泽";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routeplan);
        CharSequence titleLable = "路线规划功能";
        setTitle(titleLable);


        // 初始化地图
        mMapView = (MapView) findViewById(R.id.map);
        mMapControl = mMapView.getMap();

        // 上下一个节点按钮,初始时先隐藏
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);

        // 地图点击事件处理
        mMapControl.setOnMapClickListener(this);// 地图点击事件就是为了消除地图上的InfoWindow

        // 初始化路径规划搜索模块，注册事件监听(route:路径 plan:计划)
        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            // 步行路线结果回调
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(RoutePlanDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();// 获取建议信息
                    System.out.println();
                    return;
                }

                TaxiInfo taxiInfo = result.getTaxiInfo();// 获取打车信息对象
                System.out.println();

                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    nodeIndex = -1;
                    mBtnPre.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);

                    List<WalkingRouteLine> routeLines = result.getRouteLines();// 获取所有步行规划路线
                    if (routeLines.size() > 1) {
                        nowResultwalk = result;

                        // 多条路线规划,所以用一个对话框列表来选择
                        MyTransitDlg myTransitDlg = new MyTransitDlg(RoutePlanDemo.this, result.getRouteLines(), RouteLineAdapter.Type.WALKING_ROUTE);
                        myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                            public void onItemClick(int position) {
                                route = nowResultwalk.getRouteLines().get(position);
                                WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mMapControl);
                                mMapControl.setOnMarkerClickListener(overlay);
                                routeOverlay = overlay;
                                overlay.setData(nowResultwalk.getRouteLines().get(position));
                                overlay.addToMap();
                                overlay.zoomToSpan();
                            }
                        });
                        myTransitDlg.show();
                    } else if (result.getRouteLines().size() == 1) {
                        // 直接显示
                        route = result.getRouteLines().get(0);
                        WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mMapControl);
                        mMapControl.setOnMarkerClickListener(overlay);
                        routeOverlay = overlay;
                        overlay.setData(result.getRouteLines().get(0));
                        overlay.addToMap();
                        overlay.zoomToSpan();
                    } else {
                        Log.d("route result", "结果数<0");
                        return;
                    }

                }

            }

            // 骑行路线结果回调
            @Override
            public void onGetBikingRouteResult(BikingRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(RoutePlanDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    nodeIndex = -1;
                    mBtnPre.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);

                    if (result.getRouteLines().size() > 1) {
                        nowResultbike = result;

                        MyTransitDlg myTransitDlg = new MyTransitDlg(RoutePlanDemo.this, result.getRouteLines(), RouteLineAdapter.Type.DRIVING_ROUTE);
                        myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                            public void onItemClick(int position) {
                                route = nowResultbike.getRouteLines().get(position);
                                BikingRouteOverlay overlay = new MyBikingRouteOverlay(mMapControl);
                                routeOverlay = overlay;
                                mMapControl.setOnMarkerClickListener(overlay);
                                overlay.setData(nowResultbike.getRouteLines().get(position));
                                overlay.addToMap();
                                overlay.zoomToSpan();
                            }

                        });
                        myTransitDlg.show();

                    } else if (result.getRouteLines().size() == 1) {
                        route = result.getRouteLines().get(0);
                        BikingRouteOverlay overlay = new MyBikingRouteOverlay(mMapControl);
                        routeOverlay = overlay;
                        mMapControl.setOnMarkerClickListener(overlay);
                        overlay.setData(result.getRouteLines().get(0));
                        overlay.addToMap();
                        overlay.zoomToSpan();
                        mBtnPre.setVisibility(View.VISIBLE);
                        mBtnNext.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("route result", "结果数<0");
                    }
                }
            }

            // 公交换乘路线结果回调
            @Override
            public void onGetTransitRouteResult(TransitRouteResult result) {

                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(RoutePlanDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    // result.getSuggestAddrInfo()
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    nodeIndex = -1;
                    mBtnPre.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);


                    if (result.getRouteLines().size() > 1) {
                        nowResultransit = result;

                        MyTransitDlg myTransitDlg = new MyTransitDlg(RoutePlanDemo.this,
                                result.getRouteLines(),
                                RouteLineAdapter.Type.TRANSIT_ROUTE);
                        myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                            public void onItemClick(int position) {
                                route = nowResultransit.getRouteLines().get(position);
                                TransitRouteOverlay overlay = new MyTransitRouteOverlay(mMapControl);
                                mMapControl.setOnMarkerClickListener(overlay);
                                routeOverlay = overlay;
                                overlay.setData(nowResultransit.getRouteLines().get(position));
                                overlay.addToMap();
                                overlay.zoomToSpan();
                            }

                        });
                        myTransitDlg.show();


                    } else if (result.getRouteLines().size() == 1) {
                        // 直接显示
                        route = result.getRouteLines().get(0);
                        TransitRouteOverlay overlay = new MyTransitRouteOverlay(mMapControl);
                        mMapControl.setOnMarkerClickListener(overlay);
                        routeOverlay = overlay;
                        overlay.setData(result.getRouteLines().get(0));
                        overlay.addToMap();
                        overlay.zoomToSpan();

                    } else {
                        Log.d("route result", "结果数<0");
                        return;
                    }


                }
            }

            // 跨城 公共 交通 路线结果回调
            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(RoutePlanDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // 起终点模糊，获取建议列表
                    result.getSuggestAddrInfo();
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    nowResultmass = result;

                    nodeIndex = -1;
                    mBtnPre.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);


                    // 列表选择
                    MyTransitDlg myTransitDlg = new MyTransitDlg(RoutePlanDemo.this,
                            result.getRouteLines(),
                            RouteLineAdapter.Type.MASS_TRANSIT_ROUTE);
                    nowResultmass = result;
                    myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            MyMassTransitRouteOverlay overlay = new MyMassTransitRouteOverlay(mMapControl);
                            mMapControl.setOnMarkerClickListener(overlay);
                            routeOverlay = overlay;
                            massroute = nowResultmass.getRouteLines().get(position);
                            overlay.setData(nowResultmass.getRouteLines().get(position));

                            MassTransitRouteLine line = nowResultmass.getRouteLines().get(position);
                            overlay.setData(line);
                            if (nowResultmass.getOrigin().getCityId() == nowResultmass.getDestination().getCityId()) {
                                // 同城
                                overlay.setSameCity(true);
                            } else {
                                // 跨城
                                overlay.setSameCity(false);

                            }
                            overlay.addToMap();
                            overlay.zoomToSpan();
                        }

                    });
                    myTransitDlg.show();
                }

            }

            // 驾车路线结果回调
            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(RoutePlanDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    // result.getSuggestAddrInfo()
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    nodeIndex = -1;


                    if (result.getRouteLines().size() > 1) {
                        nowResultdrive = result;

                        MyTransitDlg myTransitDlg = new MyTransitDlg(RoutePlanDemo.this,
                                result.getRouteLines(),
                                RouteLineAdapter.Type.DRIVING_ROUTE);
                        myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                            public void onItemClick(int position) {
                                route = nowResultdrive.getRouteLines().get(position);
                                DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mMapControl);
                                mMapControl.setOnMarkerClickListener(overlay);
                                routeOverlay = overlay;
                                overlay.setData(nowResultdrive.getRouteLines().get(position));
                                overlay.addToMap();
                                overlay.zoomToSpan();
                            }

                        });
                        myTransitDlg.show();

                    } else if (result.getRouteLines().size() == 1) {
                        route = result.getRouteLines().get(0);
                        DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mMapControl);
                        routeOverlay = overlay;
                        mMapControl.setOnMarkerClickListener(overlay);
                        overlay.setData(result.getRouteLines().get(0));
                        overlay.addToMap();
                        overlay.zoomToSpan();
                        mBtnPre.setVisibility(View.VISIBLE);
                        mBtnNext.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("route result", "结果数<0");
                        return;
                    }

                }
            }

            // 室内路线规划回调
            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }
        });
    }

    /**
     * 发起路线规划搜索示例:步行,驾车,骑行,跨城公共交通,公交
     */
    public void searchButtonProcess(View v) {
        // 重置浏览节点的路线数据
        route = null; // 重置代表路线数据的对象
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        mMapControl.clear();// 清空MapView上的覆盖物与InfoWindow

        // 处理搜索按钮响应
        // 设置起终点信息，对于transitSearch来说，城市名无意义
        PlanNode stNode = PlanNode.withCityCodeAndPlaceName(131, startNodeStr);// 北京西二旗
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", endNodeStr);// 北京龙泽
        PlanNode withLocation = PlanNode.withLocation(new LatLng(39.92235, 116.380338));// 北京灵镜胡同地铁站

        // 途径节点(对驾车路径检索有用)
        List<PlanNode> passNodes = new ArrayList<>();

        // 跨城节点
        PlanNode stMassNode = PlanNode.withCityNameAndPlaceName("北京", "天安门");
        PlanNode enMassNode = PlanNode.withCityNameAndPlaceName("上海", "东方明珠");

        // 实际使用中请对起点终点城市进行正确的设定
        if (v.getId() == R.id.mass) {
            routePlanSearch.masstransitSearch(new MassTransitRoutePlanOption().from(stMassNode).to(enMassNode));
            nowSearchType = 0;
        } else if (v.getId() == R.id.drive) {
            routePlanSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
            nowSearchType = 1;
        } else if (v.getId() == R.id.transit) {
            routePlanSearch.transitSearch((new TransitRoutePlanOption()).from(stNode).city("北京").to(enNode));
            nowSearchType = 2;

        } else if (v.getId() == R.id.walk) {
            routePlanSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));
            nowSearchType = 3;
        } else if (v.getId() == R.id.bike) {
            routePlanSearch.bikingSearch((new BikingRoutePlanOption()).from(stNode).to(enNode));
            nowSearchType = 4;
        }
    }

    /**
     * 上下节点浏览示例
     */
    public void nodeClick(View v) {
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = null;

        if (nowSearchType != 0 && nowSearchType != -1) {
            // 非跨城综合交通
            if (route == null || route.getAllStep() == null) {
                return;
            }
            if (nodeIndex == -1 && v.getId() == R.id.pre) {
                return;
            }
            // 设置节点索引
            if (v.getId() == R.id.next) {
                if (nodeIndex < route.getAllStep().size() - 1) {
                    nodeIndex++;
                } else {
                    return;
                }
            } else if (v.getId() == R.id.pre) {
                if (nodeIndex > 0) {
                    nodeIndex--;
                } else {
                    return;
                }
            }
            // 获取节结果信息
            step = route.getAllStep().get(nodeIndex);
            if (step instanceof DrivingRouteLine.DrivingStep) {
                nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
                nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
            } else if (step instanceof WalkingRouteLine.WalkingStep) {
                nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
                nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
            } else if (step instanceof TransitRouteLine.TransitStep) {
                nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
                nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
            } else if (step instanceof BikingRouteLine.BikingStep) {
                nodeLocation = ((BikingRouteLine.BikingStep) step).getEntrance().getLocation();
                nodeTitle = ((BikingRouteLine.BikingStep) step).getInstructions();
            }
        } else if (nowSearchType == 0) {
            // 跨城综合交通  综合跨城公交的结果判断方式不一样


            if (massroute == null || massroute.getNewSteps() == null) {
                return;
            }
            if (nodeIndex == -1 && v.getId() == R.id.pre) {
                return;
            }
            boolean isSamecity = nowResultmass.getOrigin().getCityId() == nowResultmass.getDestination().getCityId();
            int size = 0;
            if (isSamecity) {
                size = massroute.getNewSteps().size();
            } else {
                for (int i = 0; i < massroute.getNewSteps().size(); i++) {
                    size += massroute.getNewSteps().get(i).size();
                }
            }

            // 设置节点索引
            if (v.getId() == R.id.next) {
                if (nodeIndex < size - 1) {
                    nodeIndex++;
                } else {
                    return;
                }
            } else if (v.getId() == R.id.pre) {
                if (nodeIndex > 0) {
                    nodeIndex--;
                } else {
                    return;
                }
            }
            if (isSamecity) {
                // 同城
                step = massroute.getNewSteps().get(nodeIndex).get(0);
            } else {
                // 跨城
                int num = 0;
                for (int j = 0; j < massroute.getNewSteps().size(); j++) {
                    num += massroute.getNewSteps().get(j).size();
                    if (nodeIndex - num < 0) {
                        int k = massroute.getNewSteps().get(j).size() + nodeIndex - num;
                        step = massroute.getNewSteps().get(j).get(k);
                        break;
                    }
                }
            }

            nodeLocation = ((MassTransitRouteLine.TransitStep) step).getStartLocation();
            nodeTitle = ((MassTransitRouteLine.TransitStep) step).getInstructions();
        }

        if (nodeLocation == null || nodeTitle == null) {
            return;
        }

        // 移动节点至中心
        mMapControl.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        popupText = new TextView(RoutePlanDemo.this);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(0xFF000000);
        popupText.setText(nodeTitle);
        mMapControl.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
    }

    /**
     * 切换路线图标(系统定义与自定义起始点的切换)，刷新地图使其生效
     * 注意： 起终点图标使用中心对齐.
     */
    public void changeRouteIcon(View v) {
        if (routeOverlay == null) {
            return;
        }
        if (useDefaultIcon) {
            ((Button) v).setText("自定义起终点图标");
            Toast.makeText(this, "将使用系统起终点图标", Toast.LENGTH_SHORT).show();
        } else {
            ((Button) v).setText("系统起终点图标");
            Toast.makeText(this, "将使用自定义起终点图标", Toast.LENGTH_SHORT).show();
        }

        // 核心 : 修改判断条件 该变量在覆写OverlayManager子类的获取图标线的颜色时可以区分是否用自定义的
        useDefaultIcon = !useDefaultIcon;

        // 去除所有覆盖物重新添加(重新添加的覆盖物受useDefaultIcon影响)
        routeOverlay.removeFromMap();
        routeOverlay.addToMap();
    }

    // 定制RouteOverly:通过覆写一些方法来自定义一些图标路线的样式
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyBikingRouteOverlay extends BikingRouteOverlay {
        public MyBikingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }


    }

    private class MyMassTransitRouteOverlay extends MassTransitRouteOverlay {
        public MyMassTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }


    }

    // 响应DLg中的List item 点击
    interface OnItemInDlgClickListener {
        public void onItemClick(int position);
    }

    // 供路线选择的Dialog
    class MyTransitDlg extends Dialog {

        private List<? extends RouteLine> mtransitRouteLines;
        private ListView transitRouteList;
        private RouteLineAdapter mTransitAdapter;

        OnItemInDlgClickListener onItemInDlgClickListener;

        public MyTransitDlg(Context context, int theme) {
            super(context, theme);
        }

        public MyTransitDlg(Context context, List<? extends RouteLine> transitRouteLines, RouteLineAdapter.Type
                type) {
            this(context, 0);
            mtransitRouteLines = transitRouteLines;
            mTransitAdapter = new RouteLineAdapter(context, mtransitRouteLines, type);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_transit_dialog);

            transitRouteList = (ListView) findViewById(R.id.transitList);
            transitRouteList.setAdapter(mTransitAdapter);

            transitRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onItemInDlgClickListener.onItemClick(position);
                    mBtnPre.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);
                    dismiss();

                }
            });
        }

        public void setOnItemInDlgClickLinster(OnItemInDlgClickListener itemListener) {
            onItemInDlgClickListener = itemListener;
        }

    }


    //*************************************************************************************************************************************************
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onMapClick(LatLng point) {
        mMapControl.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi poi) {
        return false;
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
        if (routePlanSearch != null) {
            routePlanSearch.destroy();// 释放搜索模块资源
        }
        mMapView.onDestroy();
        super.onDestroy();
    }
}
