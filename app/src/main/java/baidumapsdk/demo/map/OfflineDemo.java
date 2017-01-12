package baidumapsdk.demo.map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;

import java.util.ArrayList;

import baidumapsdk.demo.R;

/* 此Demo用来演示离线地图的下载和显示 */
public class OfflineDemo extends Activity {

    private MKOfflineMap offLineMap = null;
    private TextView cityIdText;
    private TextView alreadyDownloadText;
    private EditText cityNameEdit;
    /**
     * 已下载的离线地图信息列表
     */
    private ArrayList<MKOLUpdateElement> localMapList = null;
    private LocalMapAdapter lAdapter = null;


    /*
    *       注意: 以下的城市并不一定指的就市级,也有可能是省级,或者全国基础包
    * MKOfflineMap对象为连线地图下载的核心类,该类可调用getOfflineCityList()方法获取支持离线地图城市列表,也可以调用start(int cityID);pause(int cityID)等方法下载指定城市id的离线地图
    * MKOfflineMap对象调用init(MKOfflineMapListener listener)注册监听回调,在监听回调方法中type如果是MKOfflineMap.TYPE_DOWNLOAD_UPDATE,则state表示正在下载更新的城市ID。
    *   如此代表有离线地图在下载,这时我们可以使用MKOfflineMap对象调用getAllUpdateInfo()方法或者getUpdateInfo(state)方法 获取所有正在下载或停止下载的城市离线地图列表,或者指定城市id的离线地图更新状态
    *
    * */


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        // 创建离线地图功能对象
        offLineMap = new MKOfflineMap();

        // 初始化: 传入接口事件，离线地图更新会触发该回调
        offLineMap.init(new MKOfflineMapListener() {
            @Override
            // type: 事件类型: MKOfflineMap.TYPE_NEW_OFFLINE, MKOfflineMap.TYPE_DOWNLOAD_UPDATE, MKOfflineMap.TYPE_VER_UPDATE.
            // state: 事件状态: 当type为TYPE_NEW_OFFLINE时，表示新安装的离线地图数目. 当type为TYPE_DOWNLOAD_UPDATE时，表示更新的城市ID.
            public void onGetOfflineMapState(int type, int state) {
                switch (type) {
                    case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: // 处理下载进度更新提示
                        Log.d("onGetOfflineMapState", "正在下载(TYPE_DOWNLOAD_UPDATE),state为更新的城市ID: " + state);


                        MKOLUpdateElement update = offLineMap.getUpdateInfo(state);
                        if (update != null) {
                            alreadyDownloadText.setText(String.format("%s : %d%%", update.cityName, update.ratio));
                            updateView();
                        }
                        break;
                    case MKOfflineMap.TYPE_NEW_OFFLINE:// 有新离线地图安装
                        Log.d("onGetOfflineMapState", "有新安装的离线地图(TYPE_NEW_OFFLINE),state为新安装的离线地图数目: " + state);


                        break;
                    case MKOfflineMap.TYPE_VER_UPDATE:
                        Log.d("onGetOfflineMapState", "离线地图数据版本更新(TYPE_VER_UPDATE),state为版本更新的版本号(?): " + state);
                        // 版本更新提示
                        // MKOLUpdateElement e = offLineMap.getUpdateInfo(state);


                        break;
                    default:
                        break;
                }
            }
        });

        initView();

    }

    /*初始化ui控件*/
    private void initView() {

        cityIdText = (TextView) findViewById(R.id.cityid);
        cityNameEdit = (EditText) findViewById(R.id.city);
        alreadyDownloadText = (TextView) findViewById(R.id.state);

        // ****************************************展示热门城市列表(没有判断城市类型,这里不全)************************************************************
        ListView hostCityList = (ListView) findViewById(R.id.hotcitylist);
        ArrayList<String> hostCities = new ArrayList<String>();
        ArrayList<MKOLSearchRecord> records1 = offLineMap.getHotCityList();  // 获取热闹城市列表
        if (records1 != null) {
            for (MKOLSearchRecord r : records1) {
                hostCities.add(r.cityName + "(" + r.cityID + ")" + "   --" + this.formatDataSize(r.size));
            }
        }
        ListAdapter hAdapter = (ListAdapter) new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, hostCities);
        hostCityList.setAdapter(hAdapter);
        // ****************************************展示所有城市列表(没有判断城市类型,这里不全)*************************************************************
        ListView allCityList = (ListView) findViewById(R.id.allcitylist);
        ArrayList<String> allCities = new ArrayList<String>();
        ArrayList<MKOLSearchRecord> records2 = offLineMap.getOfflineCityList();// 获取所有支持离线地图的城市
        if (records1 != null) {
            for (MKOLSearchRecord r : records2) {
                allCities.add(r.cityName + "(" + r.cityID + ")" + "   --" + this.formatDataSize(r.size));
            }
        }
        ListAdapter aAdapter = (ListAdapter) new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, allCities);
        allCityList.setAdapter(aAdapter);
        // ******************** ***********************************************************************************************

        // 设置城市列表显示而下载管理列表隐藏
        LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
        LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
        lm.setVisibility(View.GONE);
        cl.setVisibility(View.VISIBLE);

        // 获取已下过的离线地图信息 猜测:是否下载过的地图在服务器有记录,可以获取到,而且我们下载好了,有可能无法删除
        localMapList = offLineMap.getAllUpdateInfo();
        System.out.println();
        if (localMapList == null) {// 没有下载过也要创建一个没有数据的集合对象,进行listView展示
            localMapList = new ArrayList<MKOLUpdateElement>();
        }
        ListView localMapListView = (ListView) findViewById(R.id.localmaplist);
        lAdapter = new LocalMapAdapter();
        localMapListView.setAdapter(lAdapter);

    }

    /**
     * 切换至城市列表
     */
    public void clickCityListButton(View view) {
        LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
        LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
        lm.setVisibility(View.GONE);
        cl.setVisibility(View.VISIBLE);

    }

    /**
     * 切换至下载管理列表
     */
    public void clickLocalMapListButton(View view) {
        LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
        LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
        lm.setVisibility(View.VISIBLE);
        cl.setVisibility(View.GONE);
    }

    /**
     * 根据城市名搜索该城市的MKOLSearchRecor对象从而获取id,并设置
     */
    public void search(View view) {
        // 根据城市名搜索该城市离线地图记录,猜测:该集合一直只有一个数据,但是MKOLSearchRecord对象有时可以调用childCities方法获取下级城市列表集合
        ArrayList<MKOLSearchRecord> records = offLineMap.searchCity(cityNameEdit.getText().toString());
        if (records == null || records.size() != 1) {
            return;
        }
        cityIdText.setText(String.valueOf(records.get(0).cityID));
    }

    /**
     * 开始下载点击按钮事件
     */
    public void start(View view) {
        // 获取cityIdText显示的id
        int cityid = Integer.parseInt(cityIdText.getText().toString());
        // 使用获取来的id开始下载  根据cityIdText显示的id开启下载
        offLineMap.start(cityid);
        clickLocalMapListButton(null);// 下载了就切换到下载管理列表
        Toast.makeText(this, "开始下载离线地图. cityid: " + cityid, Toast.LENGTH_SHORT).show();
        updateView();
    }

    /**
     * 暂停下载点击按钮事件
     */
    public void stop(View view) {
        int cityid = Integer.parseInt(cityIdText.getText().toString());
        offLineMap.pause(cityid);// 根据cityIdText显示的id 暂停下载
        Toast.makeText(this, "暂停下载离线地图. cityid: " + cityid, Toast.LENGTH_SHORT).show();
        updateView();
    }

    /**
     * 删除离线地图点击按钮事件
     */
    public void remove(View view) {
        int cityid = Integer.parseInt(cityIdText.getText().toString());
        offLineMap.remove(cityid);// 根据cityIdText显示的id 删除下载
        Toast.makeText(this, "删除离线地图. cityid: " + cityid, Toast.LENGTH_SHORT).show();
        updateView();
    }

    /**
     * 更新下载管理列表的正在下载或已经下载或暂停下载的城市数据
     */
    public void updateView() {
        localMapList = offLineMap.getAllUpdateInfo();
        System.out.println();
        if (localMapList == null) {
            localMapList = new ArrayList<MKOLUpdateElement>();
        }
        lAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        int cityid = Integer.parseInt(cityIdText.getText().toString());
        MKOLUpdateElement temp = offLineMap.getUpdateInfo(cityid);
        if (temp != null && temp.status == MKOLUpdateElement.DOWNLOADING) {
            offLineMap.pause(cityid);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
    * 文件大小单位转换
    * */
    public String formatDataSize(int size) {
        String ret = "";
        if (size < (1024 * 1024)) {
            ret = String.format("%dK", size / 1024);
        } else {
            ret = String.format("%.1fM", size / (1024 * 1024.0));
        }
        return ret;
    }

    @Override
    protected void onDestroy() {
        /**
         * 退出时，销毁离线地图模块
         */
        offLineMap.destroy();
        super.onDestroy();
    }


    /**
     * 离线地图管理列表适配器
     */
    public class LocalMapAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return localMapList.size();
        }

        @Override
        public Object getItem(int index) {
            return localMapList.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }

        @Override
        public View getView(int index, View view, ViewGroup arg2) {
            MKOLUpdateElement e = (MKOLUpdateElement) getItem(index);
            view = View.inflate(OfflineDemo.this, R.layout.offline_localmap_list, null);
            initViewItem(view, e);
            return view;
        }

        void initViewItem(View view, final MKOLUpdateElement e) {
            Button remove = (Button) view.findViewById(R.id.remove);
            TextView title = (TextView) view.findViewById(R.id.function_name);
            TextView update = (TextView) view.findViewById(R.id.update);
            TextView ratio = (TextView) view.findViewById(R.id.ratio);
            ratio.setText(e.ratio + "%");
            title.setText(e.cityName);
            if (e.update) {
                update.setText("可更新");
            } else {
                update.setText("最新");
            }

            remove.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    offLineMap.remove(e.cityID);
                    updateView();
                }
            });
        }

    }

}