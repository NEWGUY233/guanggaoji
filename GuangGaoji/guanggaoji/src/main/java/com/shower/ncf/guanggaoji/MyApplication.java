package com.shower.ncf.guanggaoji;

import android.app.Application;
import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;
import com.igexin.sdk.PushManager;
import com.shower.ncf.guanggaoji.myservice.PushService;

/**
 * Created by Administrator on 2016/12/8.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PushManager.getInstance().initialize(this.getApplicationContext(), PushService.class);
    }

    private HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        MyApplication app = (MyApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
//        HttpProxyCacheServer proxy = new HttpProxyCacheServer(this);
//        proxy.B
//        return proxy;
        return new HttpProxyCacheServer.Builder(this)
                .maxCacheFilesCount(20).maxCacheSize(1024 * 1024 * 1024)
                .build();
    }

}
