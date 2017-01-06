package baidumapsdk.demo;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 在SDK各功能组件使用之前都需要调用
        // 在使用 SDK 各组间之前初始化 Context 信息，
        // 猜测: 该初始化方法内部会将初始化结果,通过特定的意图广播发送出去,该广播为自定义广播
        //      特定的意图为三个action之中的一个，也代表了三个初始化的结果: SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK;
        //                                                            SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR;
        //                                                            SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR
        System.out.println("开始初始化");
        SDKInitializer.initialize(this);// this代表Application，其源头就是Context
        // TODO 有初始化那就有,那就有释放
    }

}