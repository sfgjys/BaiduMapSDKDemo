package baidumapsdk.demo.map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.List;

import baidumapsdk.demo.R;

/**
 * 此demo用来展示如何结合定位SDK实现定位，并使用MyLocationOverlay绘制定位位置 同时展示如何使用自定义图标绘制并点击时弹出泡泡
 */
public class LocationDemo extends Activity {

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListener myLocationListener = new MyLocationListener();
    private LocationMode mCurrentLocationMode;// 现在的:Current
    BitmapDescriptor mCurrentLocationIcon;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;

    MapView mMapView;
    BaiduMap mMapControl;

    // UI相关
    Button mLocationModeButton;
    boolean isFirstLocation = true; // 是否首次定位
    private RadioGroup radioGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapControl = mMapView.getMap();


        // 开启定位图层,该方法是 setMyLocationConfigeration()设置定位图层配置信息方法的前提 setMyLocationEnabled设置为true,setMyLocationConfigeration()方法才会生效
        mMapControl.setMyLocationEnabled(true);


        // 定位初始化 LocationClient类必须在主线程中声明 创建定位客户端
        mLocClient = new LocationClient(this);
        // 给定位客户端注册定位监听接口
        mLocClient.registerLocationListener(myLocationListener);


        // 定位参数配置对象
        LocationClientOption option = new LocationClientOption();
        // 高精度定位模式Hight_Accuracy：这种定位模式下，会同时使用网络定位和GPS定位，优先返回最高精度的定位结果；
        // 低功耗定位模式Battery_Saving：这种定位模式下，不会使用GPS进行定位，只会使用网络定位（WiFi定位和基站定位）；
        // 仅设备定位模式Device_Sensors：这种定位模式下，不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位。
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系 gcj02 --兲朝已安全原因为由,要求在中国使用的地图产品使用的都必须是加密后的坐标.这套加密后的坐标就是gcj02  百度又在gcj02的技术上将坐标加密就成了 bd09ll坐标..
        option.setScanSpan(1000);//可选，默认0，即仅定位一次。 设置发起定位请求的间隔: 需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在LocationClient.stop的时候杀死这个进程，默认不杀死 Ignore:忽视
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集异常信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否允许模拟GPS ，默认不允许

        mLocClient.setLocOption(option);// 将定位参数配置对象设置进定位客户端
        mLocClient.start();
        // 如果开发者想按照自己逻辑请求定位，可以在start之后按照自己的逻辑请求mLocClient.requestLocation()函数，会主动触发定位SDK内部定位逻辑，等待定位回调即可。


        // *************************************************上面是开启具体的定位功能,下面是对定位点控件进行设置修改*************************************************


        mLocationModeButton = (Button) findViewById(R.id.button1);
        radioGroup = (RadioGroup) this.findViewById(R.id.radioGroup);
        mLocationModeButton.setText("普通");

        // 什么都不设置的话 定位模式就是普通
        mCurrentLocationMode = LocationMode.NORMAL;

        mLocationModeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentLocationMode) {
                    case NORMAL:// 点击时是普通
                        mLocationModeButton.setText("跟随");
                        mCurrentLocationMode = LocationMode.FOLLOWING;// 模式改为跟随
                        mMapControl.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentLocationMode, true, mCurrentLocationIcon));
                        break;
                    case COMPASS:// 点击时是罗盘
                        mLocationModeButton.setText("普通");
                        mCurrentLocationMode = LocationMode.NORMAL;// 模式改为普通
                        mMapControl.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentLocationMode, true, mCurrentLocationIcon));
                        break;
                    case FOLLOWING:// 点击时是跟随
                        mLocationModeButton.setText("罗盘");
                        mCurrentLocationMode = LocationMode.COMPASS;// 模式改为罗盘
                        mMapControl.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentLocationMode, true, mCurrentLocationIcon));
                        break;
                    default:
                        break;
                }
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.defaulticon) {
                    // 传入null则，恢复默认图标
                    mCurrentLocationIcon = null;
                    mMapControl.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentLocationMode, true, mCurrentLocationIcon));
                }
                if (checkedId == R.id.customicon) {
                    // 修改为自定义marker
                    mCurrentLocationIcon = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
                    mMapControl.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentLocationMode, true, mCurrentLocationIcon, accuracyCircleFillColor, accuracyCircleStrokeColor));
                }
            }
        });


    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }


            // 显示定位点   猜测: setMyLocationData setMyLocationConfigeration 这两个方法应该可以随时调用进行设置
            // MyLocationData代表地图上定位时显示的定位点的样式数据  注意:该对象并不一定要在定位时才可以使用,但前提必须先开启定位图层
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius()) // 单位:米 显示的是定位时显示的定位点的周边圆圈
                    .direction(0)    // 此处设置方向信息，顺时针0-360 代表定位时显示的定位点的箭头指向 正北为0
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 通过地图控制器设置MyLocationData对象,这样MapView会根据MyLocationData对象中设置好的参数去显示定位时显示的定位点
            mMapControl.setMyLocationData(locData);
            // MyLocationConfiguration代表地图上定位时显示的定位点的具体样式展现
            // 参数一:显示定位点的定位模式(普通,跟随,罗盘); 参数二:是否根据MyLocationData的方向信息显示箭头; 参数三:设置用户自定义定位图标; 参数四:设置精度圈填充颜色; 参数五:设置精度圈填充颜色
            // new MyLocationConfiguration(mCurrentLocationMode, true, mCurrentLocationIcon, accuracyCircleFillColor, accuracyCircleStrokeColor)
            // 其中参数三具体需要的是经过BitmapDescriptorFactory调用from系列方法获得的BitmapDescriptor对象,该参数也可传递null代表使用默认的
            //                                              例如:BitmapDescriptorFactory.fromResource(R.drawable.icon_geo); BitmapDescriptorFactory.fromView();
            mMapControl.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentLocationMode, true, mCurrentLocationIcon));
            // setMyLocationConfigeration方法不进行调用,只调用setMyLocationData方法时定位点也会显示,其内部会使用MyLocationConfiguration的默认参数参数一为普通模式;参数二为不显示方向;参数三为null


            // 将操作地图跳转到定位结果所在位置
            if (isFirstLocation) {// 这是第一进行定位
                isFirstLocation = false;
                // 第一次定位所以直接将地图跳转至定位所在坐标
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(latLng).zoom(18.0f);
                mMapControl.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            onReceivePrint(location);
        }

        private void onReceivePrint(BDLocation location) {
            System.out.println("************************************************************************************************************************");
            //Receive Location
            StringBuilder stringBuilder = new StringBuilder(256);
            stringBuilder.append("定位请求时间(时间并不精确到秒) : ").append(location.getTime());
            stringBuilder.append("\n定位返回结果码或者错误码 : ").append(location.getLocType());
            stringBuilder.append("\n定位返回的纬度 : ").append(location.getLatitude());
            stringBuilder.append("\n定位返回的经度 : ").append(location.getLongitude());
            stringBuilder.append("\n定位返回的精确范围 : ").append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                stringBuilder.append("\n速度为 : ").append(location.getSpeed());// 单位：公里每小时
                stringBuilder.append("\nGPS锁定用的卫星数 : ").append(location.getSatelliteNumber());
                stringBuilder.append("\n高度信息 : ").append(location.getAltitude());// 单位：米
                stringBuilder.append("\n行进的方向 : ").append(location.getDirection());// 单位度
                stringBuilder.append("\n定位地址 : ").append(location.getAddrStr());
                stringBuilder.append("\n定位结果描述 : GPS定位成功");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                stringBuilder.append("\n定位返回的地址信息 : ").append(location.getAddrStr());
                stringBuilder.append("\n运营商信息 : ").append(location.getOperators());// 值为0:未知运营商 值为1:中国移动 值为2:中国联通 值为3:中国电信  这个是在返回网络定位结果时才有的
                stringBuilder.append("\n定位结果描述 : 网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                stringBuilder.append("\n定位结果描述 : 离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                stringBuilder.append("\n定位结果描述 : 服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                stringBuilder.append("\n定位结果描述 : 网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                stringBuilder.append("\n定位结果描述 : 无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            stringBuilder.append("\n位置语义化信息 : ").append(location.getLocationDescribe());// 没有的话返回NULL  语义化信息,例如:在什么什么附近

            List<Poi> list = location.getPoiList();// 仅在开发者设置需要POI信息时才会返回，在网络不通或无法获取时有可能返回null  猜测:返回的是百度数据库中该定位点的精确范围内中存在的POI点对象集合
            if (list != null) {
                stringBuilder.append("\n存在").append(list.size()).append("个POI点信息对象");
                for (Poi p : list) {
                    stringBuilder.append("\nPOI的具体信息 : ");
                    stringBuilder.append("POI的ID:").append(p.getId()).append(" ").append("POI的名字:").append(p.getName()).append(" ").append("POI概率值:").append(p.getRank());
                }
            }
            Log.i("定位返回信息结果", stringBuilder.toString());
        }
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
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mMapControl.setMyLocationEnabled(false);


        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

}
