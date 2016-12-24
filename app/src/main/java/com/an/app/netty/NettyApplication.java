package com.an.app.netty;

import android.app.Application;

/**
 * Created by qydda on 2016/12/24.
 */

public class NettyApplication extends Application {
    private String sendding = null;

    public String getSendding() {
        return sendding;
    }

    public void setSendding(String sendding) {
        this.sendding = sendding;
    }

    private static NettyApplication instance;

    public static NettyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
