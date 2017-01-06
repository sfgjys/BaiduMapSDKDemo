package baidumapsdk.demo.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import baidumapsdk.demo.R;

/**
 * 此demo用来展示如何在地图上用GraphicsOverlay添加点、线、多边形、圆
 * 并对Polyline进行点击事件响应
 * 同时展示如何在地图上用TextOverlay添加文字
 */
public class GeometryDemo extends Activity {

    // 地图相关
    MapView mMapView;
    BaiduMap mMapControl;
    // UI相关
    Button resetBtn;
    Button clearBtn;

    // 普通折线，点击时改变宽度
    Polyline mPolyline;
    // 多颜色折线，点击时消失
    Polyline mColorfulPolyline;
    // 纹理折线，点击时获取折线上点数及width
    Polyline mTexturePolyline;

    BitmapDescriptor mRedTexture = BitmapDescriptorFactory.fromAsset("icon_road_red_arrow.png");
    BitmapDescriptor mBlueTexture = BitmapDescriptorFactory.fromAsset("icon_road_blue_arrow.png");
    BitmapDescriptor mGreenTexture = BitmapDescriptorFactory.fromAsset("icon_road_green_arrow.png");
    CheckBox dottedLine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geometry);
        // 初始化地图
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapControl = mMapView.getMap();
        // UI初始化
        clearBtn = (Button) findViewById(R.id.button1);
        resetBtn = (Button) findViewById(R.id.button2);
        dottedLine = (CheckBox) findViewById(R.id.dottedline);

        clearBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                clearClick();
            }
        });
        resetBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                resetClick();
            }
        });
        dottedLine.setOnCheckedChangeListener(new DottedLineListener());

        // 界面加载时添加绘制图层(几何图形图层)
        addCustomElementsDemo();

        // 点击polyline的事件响应
        mMapControl.setOnPolylineClickListener(new BaiduMap.OnPolylineClickListener() {
            @Override
            public boolean onPolylineClick(Polyline polyline) {
                if (polyline == mPolyline) {
                    polyline.setWidth(20);
                } else if (polyline == mColorfulPolyline) {
                    polyline.remove();
                } else if (polyline == mTexturePolyline) {
                    Toast.makeText(getApplicationContext(), "点数：" + polyline.getPoints().size()
                                    + ",width:" + polyline.getWidth(),
                            Toast.LENGTH_SHORT).show();
                }
                // TODO 监听返回值的影响
                return true;
            }
        });
    }

    /**
     * 添加点、线、多边形、圆、文字
     */
    public void addCustomElementsDemo() {
        /*
        *  添加线类型的覆盖物
        *  首先创建一个集合用来存储LatLng坐标,最少两个LatLng,两个时为直线,两个以上为折线
        *  然后创建PolylineOptions线覆盖物的参数配置对象。设置其  宽度width()(单位像素);  color()颜色;  points(此处传入LatLng坐标集合)等。以其父类OverlayOptions来接收
        *  接着使用地图控制器mMapControl把上面创建的参数配置对象作为方法参数,去调用addOverlay方法;返回一个Overlay对象(我们可以把该对象强制转换为我需要的子类对象,如:Polyline)
        *  扩展: 1 在对PolylineOptions线覆盖物的参数配置对象进行方法调用时,还有一个方法colorsValues(),该方法需要包含颜色的集合参数,该集合存储Integer类型的颜色格式:0xAA0000FF
        *       颜色集合中第一个颜色就对应LatLng坐标集合中第一个坐标到第二个坐标的直线的颜色, 颜色集合中第二个颜色就对应LatLng坐标集合中第二个坐标到第三个坐标的直线的颜色,以此类推
        *       如果颜色集合只存储了三个颜色,而LatLng坐标集合有五个坐标点,那LatLng坐标集合的第四个坐标到第五个坐标的直线需要颜色集合第四个颜色,可颜色集合没有第四个,那就是用颜色集合最后一个颜色
        *        2 两点坐标直线使用图片来展示: PolylineOptions调用customTextureList(此处是存储BitmapDescriptor对象的集合)方法,将我们想使用的图片转换为BitmapDescriptor对象,并存如集合,然后放入PolylineOptions对象
        *         接下来设置每个直线要使用 存储BitmapDescriptor对象的集合 中的哪个BitmapDescriptor对象-->调用textureIndex(此处存储每个直线要使用的BitmapDescriptor对象在集合中所在位置)方法
        *         例如: textureIndex方法的集合存储的是 1 2 0 3; 那么折线画的第一条直线使用BitmapDescriptor对象的集合中的角标为1的BitmapDescriptor对象; 第三条直线使用BitmapDescriptor对象的集合中的角标为0的BitmapDescriptor对象;
        *              如果折线有五段线,那第五段线使用的是BitmapDescriptor对象的集合中的角标为 0 的BitmapDescriptor对象
        *        3 dottedLine(true)直线的绘制是否画成虚线,如果直线的绘制使用的是BitmapDescriptor对象,那么是否虚线将会展现不同的效果
        *  添加弧线类型的覆盖物
        *       ArcOptions  在调用points()时不要集合只要三个坐标作为三个参数,分别是:起点、中点、终点坐标
        *  添加圆类型的覆盖物
        *       CircleOptions  center(圆的中心点坐标)  stroke(new Stroke(5, 0xAAFF0000))(Stroke是边框类 参数一宽度单位像素 ) radius(700)设置圆半径 单位：米  fillColor(0x0000FF00)圆里的填充颜色
        *  添加点类型的覆盖物
        *       CircleOptions  center(点的中心点坐标)  radius(6)设置圆半径 单位：像素
        *  添加多边形类型的覆盖物
        *       PolygonOptions
        *  添加文本类型的覆盖物
        *       TextOptions  bgColor(0xAAFFFF00)文字底部背景颜色  fontColor(0xFFFF00FF)文字本身颜色  rotate(-30)设置文字覆盖物旋转角度,逆时针   position(llText);设置文字覆盖物地理坐标
        *  以上几个类型的覆盖物在被addOverlay以后会获得一个Overlay对象,该对象可以强制转换为对应的覆盖物对象,这样在后面可以使用服务概无对象调用方法修改样式
        *       例如: Polyline polyline = (Polyline) mMapControl.addOverlay(ooPolyline);  polyline.setWidth(2);
        *
        *  清除覆盖物有两种:一种覆盖物自己调用remove();二种mMapView.getMap().clear();清除所有覆盖物
        *
        *  BitmapDescriptor使用完以后记得recycle();//回收 bitmap 资源
        * */

        // 添加普通折线绘制
        LatLng p1 = new LatLng(39.97923, 116.357428);
        LatLng p2 = new LatLng(39.94923, 116.397428);
        LatLng p3 = new LatLng(39.97923, 116.437428);
        List<LatLng> points = new ArrayList<>();
        points.add(p1);
        points.add(p2);
        points.add(p3);

        // OverlayOptions是PolylineOptions的父类
        OverlayOptions ooPolyline = new PolylineOptions().width(10).color(0xAAFF0000).points(points);
        mPolyline = (Polyline) mMapControl.addOverlay(ooPolyline);

        // 添加多颜色分段的折线绘制
        LatLng p11 = new LatLng(39.965, 116.444);
        LatLng p21 = new LatLng(39.925, 116.494);
        LatLng p31 = new LatLng(39.955, 116.534);
        LatLng p41 = new LatLng(39.905, 116.594);
        LatLng p51 = new LatLng(39.965, 116.644);
        List<LatLng> points1 = new ArrayList<LatLng>();
        points1.add(p11);
        points1.add(p21);
        points1.add(p31);
        points1.add(p41);
        points1.add(p51);


        List<Integer> colorValue = new ArrayList<Integer>();
        colorValue.add(0xAA0000FF);
        colorValue.add(0xAAFF0000);
        colorValue.add(0xAA00FF00);


        OverlayOptions ooPolyline1 = new PolylineOptions().width(10)
                .color(0xAAFF0000).points(points1).colorsValues(colorValue);
        mColorfulPolyline = (Polyline) mMapControl.addOverlay(ooPolyline1);

        // 添加多纹理分段的折线绘制
        LatLng p111 = new LatLng(39.865, 116.444);
        LatLng p211 = new LatLng(39.825, 116.494);
        LatLng p311 = new LatLng(39.855, 116.534);
        LatLng p411 = new LatLng(39.805, 116.594);
        LatLng p511 = new LatLng(39.865, 116.644);
        List<LatLng> points11 = new ArrayList<LatLng>();
        points11.add(p111);
        points11.add(p211);
        points11.add(p311);
        points11.add(p411);
        points11.add(p511);


        List<BitmapDescriptor> textureList = new ArrayList<BitmapDescriptor>();
        textureList.add(mGreenTexture);
        textureList.add(mRedTexture);
        textureList.add(mBlueTexture);


        List<Integer> textureIndexs = new ArrayList<Integer>();
        textureIndexs.add(1);
        textureIndexs.add(1);
        textureIndexs.add(1);
        textureIndexs.add(1);

        OverlayOptions ooPolyline11 = new PolylineOptions().width(20)
                .points(points11).dottedLine(true).customTextureList(textureList).textureIndex(textureIndexs);
        mTexturePolyline = (Polyline) mMapControl.addOverlay(ooPolyline11);


        // 添加弧线
        OverlayOptions ooArc = new ArcOptions().color(0xAA00FF00).width(4)
                .points(p11, p21, p31);
        mMapControl.addOverlay(ooArc);

        // 添加圆
        LatLng llCircle = new LatLng(39.90923, 116.447428);
        OverlayOptions ooCircle = new CircleOptions().fillColor(0x0000FF00)
                .center(llCircle).stroke(new Stroke(5, 0xAAFF0000))
                .radius(700);
        mMapControl.addOverlay(ooCircle);

        // 添加点
        LatLng llDot = new LatLng(39.98923, 116.397428);
        OverlayOptions ooDot = new DotOptions().center(llDot).radius(6)
                .color(0xFF0000FF);
        mMapControl.addOverlay(ooDot);

        // 添加多边形
        LatLng pt1 = new LatLng(39.93923, 116.357428);
        LatLng pt2 = new LatLng(39.91923, 116.327428);
        LatLng pt3 = new LatLng(39.89923, 116.347428);
        LatLng pt4 = new LatLng(39.89923, 116.367428);
        LatLng pt5 = new LatLng(39.91923, 116.387428);
        List<LatLng> pts = new ArrayList<LatLng>();
        pts.add(pt1);
        pts.add(pt2);
        pts.add(pt3);
        pts.add(pt4);
        pts.add(pt5);


        OverlayOptions ooPolygon = new PolygonOptions().points(pts)
                .stroke(new Stroke(5, 0xAA00FF00)).fillColor(0xAAFFFF00);
        mMapControl.addOverlay(ooPolygon);

        // 添加文字
        LatLng llText = new LatLng(39.86923, 116.397428);
        OverlayOptions ooText = new TextOptions().bgColor(0xAAFFFF00)
                .fontSize(24).fontColor(0xFFFF00FF).text("百度地图SDK").rotate(-30)
                .position(llText);
        mMapControl.addOverlay(ooText);
    }

    public void resetClick() {
        dottedLine.setChecked(false);
        clearClick();
        // 添加绘制元素
        addCustomElementsDemo();
    }

    public void clearClick() {
        // 清除所有图层
        mMapView.getMap().clear();
    }

    private class DottedLineListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mPolyline == null) {
                return;
            }
            if (isChecked) {
                mPolyline.setDottedLine(true);
            } else {
                mPolyline.setDottedLine(false);
            }
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
        mMapView.onDestroy();
        if (mRedTexture != null) {
            mRedTexture.recycle();//回收 bitmap 资源
        }
        if (mBlueTexture != null) {
            mBlueTexture.recycle();//回收 bitmap 资源
        }
        if (mGreenTexture != null) {
            mGreenTexture.recycle();//回收 bitmap 资源
        }
        super.onDestroy();
    }

}
