package baidumapsdk.demo.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.mapapi.favorite.FavoriteManager;
import com.baidu.mapapi.favorite.FavoritePoiInfo;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import baidumapsdk.demo.R;

/**
 * 演示如何使用本地点收藏功能
 */
public class FavoriteDemo extends Activity {

    // 地图相关
    private MapView mMapView;
    private BaiduMap mMapControl;

    // 界面控件相关
    private EditText wantCollectAddressLocation;
    private EditText wantCollectAddressName;
    private View clickMarkerPopup;
    EditText mdifyName;
    // 保存点中的点id
    private String currentID;
    // 现实marker的图标
    BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
    List<Marker> markers = new ArrayList<>();


    /*
    * 点收藏功能:FavoriteManager(核心类)
    *   收藏点配置类:FavoritePoiInfo 必须设置poi点名称(poiName(java.lang.String name)),必须设置poi点坐标(pt(LatLng pt)),可选设置poi点uid,可选设置poi点地址,可选设置poi点所在城市名
    *               注意: 在完成FavoritePoiInfo的创建和设置后,其id会自动生成,可通过getID()获取POI点的id
    *   具体的功能方法: 使用下面方法的前提->初始化:FavoriteManager.getInstance().init();  (FavoriteManager.getInstance()是获取实例)
    *       将一个已经配置好的FavoritePoiInfo添加进行添加:add(FavoritePoiInfo poiInfo)
    *       调用方法可以直接清空所有添加的FavoritePoiInfo:clearAllFavPois()
    *       根据指定的id可清除对应的FavoritePoiInfo:deleteFavPoi(java.lang.String id)
    *       调用方法可以直接获取所有已经添加的FavoritePoiInfo的集合:getAllFavPois()
    *       根据指定的id可获取对应的FavoritePoiInfo:getFavPoi(java.lang.String id)
    *       根据指定的id可将重新设置的FavoritePoiInfo替换圆FavoritePoiInfo:updateFavPoi(java.lang.String id, FavoritePoiInfo info)
    *   最后不用FavoriteManager是记得调用destroy()
    * */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        // 初始化地图
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapControl = mMapView.getMap();


        // 获取长按点的坐标,并将坐标设置给wantCollectAddressLocation控件
        mMapControl.setOnMapLongClickListener(new OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                wantCollectAddressLocation.setText(String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude));
            }

            public void text() {
            }
        });
        // 点击Marker展示InfoWindow,该InfoWindow可以修改该Marker的名称,可以删除本Marker
        mMapControl.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMapControl.hideInfoWindow();// 先清除InfoWindow
                if (marker == null) {
                    return false;
                }
                // 在以clickMarkerPopup控件为基础展示InfoWindow
                InfoWindow mInfoWindow = new InfoWindow(clickMarkerPopup, marker.getPosition(), -47);
                mMapControl.showInfoWindow(mInfoWindow);
                // 以marker的坐标为中新更新地图状态
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(marker.getPosition());
                mMapControl.setMapStatus(update);

                // 获取在展示该Marker时通过bundle放入的id数据
                currentID = marker.getExtraInfo().getString("id");
                return true;
            }

            public void text() {
            }
        });
        // 单机地图清除地图上存在的InfoWindow
        mMapControl.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mMapControl.hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi poi) {
                return false;
            }
        });


        // 初始化收藏夹
        FavoriteManager.getInstance().init();
        // 初始化UI
        initUI();
    }

    public void initUI() {
        wantCollectAddressLocation = (EditText) findViewById(R.id.pt);
        wantCollectAddressName = (EditText) findViewById(R.id.name);
        LayoutInflater mInflater = getLayoutInflater();
        clickMarkerPopup = (View) mInflater.inflate(R.layout.activity_favorite_infowindow, null, false);
    }

    /**
     * 添加收藏点
     */
    public void saveClick(View v) {

        // 要想收藏点,必须要有点的名称与坐标
        if (wantCollectAddressName.getText().toString() == null || wantCollectAddressName.getText().toString().equals("")) {
            Toast.makeText(FavoriteDemo.this, "名称必填", Toast.LENGTH_LONG).show();
            return;
        }
        if (wantCollectAddressLocation.getText().toString() == null || wantCollectAddressLocation.getText().toString().equals("")) {
            Toast.makeText(FavoriteDemo.this, "坐标点必填", Toast.LENGTH_LONG).show();
            return;
        }

        FavoritePoiInfo favoritePoiInfo = new FavoritePoiInfo();
        // 设置点收藏的名称
        favoritePoiInfo.poiName(wantCollectAddressName.getText().toString());

        LatLng latLng;
        try {
            // 从wantCollectAddressLocation获取坐标
            String strPt = wantCollectAddressLocation.getText().toString();
            String lat = strPt.substring(0, strPt.indexOf(","));
            String lng = strPt.substring(strPt.indexOf(",") + 1);
            // 将Stringr格式的坐标转换为double创建坐标对象LatLng
            latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            // 将坐标对象设置进FavoritePoiInfo对象
            favoritePoiInfo.pt(latLng);

            if (FavoriteManager.getInstance().add(favoritePoiInfo) == 1) {// add方法返回值-2:收藏夹已满，-1:重名或名称为空，0：添加失败，1：添加成功
                Toast.makeText(FavoriteDemo.this, "添加成功", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(FavoriteDemo.this, "添加失败", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(FavoriteDemo.this, "坐标解析错误", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 修改收藏点
     */
    public void modifyClick(View v) {
        mMapControl.hideInfoWindow();
        // 弹框修改
        LayoutInflater mInflater = getLayoutInflater();
        View mModify = (LinearLayout) mInflater.inflate(R.layout.activity_favorite_alert, null);
        mdifyName = (EditText) mModify.findViewById(R.id.modifyedittext);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(mModify);
        // 根据id获取FavoritePoiInfo信息对象中的名称,将名称设置给ui等待使用人去修改
        String oldName = FavoriteManager.getInstance().getFavPoi(currentID).getPoiName();
        mdifyName.setText(oldName);
        builder.setPositiveButton("确认", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = mdifyName.getText().toString();
                if (newName != null && !newName.equals("")) {
                    // 根据id获取FavoritePoiInfo信息对象
                    FavoritePoiInfo info = FavoriteManager.getInstance().getFavPoi(currentID);
                    info.poiName(newName);// 将新的名称设置覆盖原先的名称变量
                    // 然后调用FavoritePoiInfo的updateFavPoi方法,将id和新的FavoritePoiInfo对象作为参数更新id对应的原FavoritePoiInfo对象
                    if (FavoriteManager.getInstance().updateFavPoi(currentID, info)) {
                        Toast.makeText(FavoriteDemo.this, "修改成功", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(FavoriteDemo.this, "名称不能为空，修改失败", Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }


        });

        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 删除一个指定点
     */
    public void deleteOneClick(View v) {
        // 先根据id删除收藏夹中的点信息,然后在将地图上对应的Marker移除
        if (FavoriteManager.getInstance().deleteFavPoi(currentID)) {
            Toast.makeText(FavoriteDemo.this, "删除点成功", Toast.LENGTH_LONG).show();
            if (markers != null) {
                for (int i = 0; i < markers.size(); i++) {
                    if (markers.get(i).getExtraInfo().getString("id").equals(currentID)) {
                        markers.get(i).remove();
                        markers.remove(i);
                        mMapControl.hideInfoWindow();
                        break;
                    }
                }
            }
        } else {
            Toast.makeText(FavoriteDemo.this, "删除点失败", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 获取全部收藏点
     */
    public void getAllClick(View v) {
        mMapControl.clear();
        List<FavoritePoiInfo> list = FavoriteManager.getInstance().getAllFavPois();// 获取所有收藏点FavoritePoiInfo信息对象的集合
        if (list == null || list.size() == 0) {
            Toast.makeText(FavoriteDemo.this, "没有收藏点", Toast.LENGTH_LONG).show();
            return;
        }
        // 绘制在地图
        markers.clear();
        for (int i = 0; i < list.size(); i++) {
            // 将FavoritePoiInfo对象中的坐标对象通过position()方法进行设置
            MarkerOptions option = new MarkerOptions().icon(bitmapDescriptor).position(list.get(i).getPt());

            // 通过bundle对象将FavoritePoiInfo对象的id(该id在通过坐标与名称创建FavoritePoiInfo对象时就自动生成了)设置进Marker
            Bundle b = new Bundle();
            b.putString("id", list.get(i).getID());
            option.extraInfo(b);
            markers.add((Marker) mMapControl.addOverlay(option));
        }
    }

    /**
     * 删除全部点
     */
    public void deleteAllClick(View v) {
        // 清空所有收藏点
        if (FavoriteManager.getInstance().clearAllFavPois()) {
            Toast.makeText(FavoriteDemo.this, "全部删除成功", Toast.LENGTH_LONG).show();
            mMapControl.clear();
            mMapControl.hideInfoWindow();
        } else {
            Toast.makeText(FavoriteDemo.this, "全部删除失败", Toast.LENGTH_LONG).show();
        }
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
        // 释放收藏夹功能资源
        FavoriteManager.getInstance().destroy();
        bitmapDescriptor.recycle();
        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
        mMapView.onDestroy();
        mMapControl = null;
        super.onDestroy();
    }

}
