package com.shower.ncf.guanggaoji.myservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shower.ncf.guanggaoji.VideoActivity;

/**
 * Created by Administrator on 2016/12/9.
 */

public class MyBroadCast extends BroadcastReceiver {
    VideoActivity activity;

    public MyBroadCast(VideoActivity activity){
        this.activity = activity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
//        intent.getBundleExtra("");
        String str =  intent.getStringExtra("com.shower.ncf.guanggaoji.video");
        activity.getData(str);
    }
}
