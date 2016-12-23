package com.an.app.netty;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by qydda on 2016/12/22.
 */

public class DataConnectBroadcastReceiver extends BroadcastReceiver {
    private String TAG = "DataConnectBroadcastReceiver";
    private boolean isServiceRunning = false;//判断服务是否启动。
    private String START_SERVICE = "com.an.app.netty.DataConnectService";
    private boolean sendding = false;//是否正在发送。

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println(TAG + "--qydq--收到DataConnectService启动的广播--");
        if (intent.getIntExtra("kill", 1) == 0) {
            abortBroadcast();
            System.out.println(TAG + "--qydq--收到DataConnectService启动的广播--終止廣播--");
            return;
        }
        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if ("com.an.app.netty.DataConnectService".equals(service.service.getClassName())) {
                    System.out.println(TAG + "--qydq--服务打开--");
                    isServiceRunning = true;
                }
            }
            //如果服务没有启动，。则根据保存的状态的editor，sendding来判断是否需要重新开启服务，sendding正在发送，service却死了，则需要重启服务。
            sendding = context.getSharedPreferences("SuperActivity", Context.MODE_PRIVATE).getBoolean("sendding", false);
            if (!isServiceRunning && sendding) {
                System.out.println(TAG + "--qydq--服务已关闭--");
                Intent serviceIntent = new Intent(context, DataConnectService.class);
                serviceIntent.setAction(START_SERVICE);
                context.startService(serviceIntent);
            }
        }
    }
}
