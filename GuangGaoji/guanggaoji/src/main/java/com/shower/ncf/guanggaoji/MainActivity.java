package com.shower.ncf.guanggaoji;

import android.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.igexin.sdk.PushManager;
import com.shower.ncf.guanggaoji.myservice.MyService;
import com.shower.ncf.guanggaoji.myservice.PushService;
import com.shower.ncf.guanggaoji.myutil.MyInfo;
import com.shower.ncf.guanggaoji.myutil.MyParse;
import com.shower.ncf.guanggaoji.myutil.MyShared;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/11/25.
 */

public class MainActivity extends MyActivity implements View.OnClickListener {

    //view
    LinearLayout main_setting;
    LinearLayout main_video;
    LinearLayout main_image;
    LinearLayout main_quit;

    //
    MyInfo infoData;
    List<String> listImage;
    List<String> listVideo;


    //
    MyInfo info;
    SharedPreferences sp;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
//                    sp.edit().putString(MyShared.USER_NAME,"").commit();
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ButterKnife.bind(this);


        initView();
//        getData();
    }



    private void initView(){
        main_setting = (LinearLayout) findViewById(R.id.main_setting);
        main_video = (LinearLayout) findViewById(R.id.main_video);
        main_image = (LinearLayout) findViewById(R.id.main_image);
        main_quit = (LinearLayout) findViewById(R.id.main_quit);

        main_setting.setOnClickListener(this);
        main_video.setOnClickListener(this);
        main_image.setOnClickListener(this);
        main_quit.setOnClickListener(this);

        //get shared
        infoData = new MyInfo();
        sp = getSharedPreferences(MyShared.SHARED, Context.MODE_PRIVATE);
        infoData.setUser_rand(sp.getString(MyShared.USER_RAND,""));
        Log.i("01010010","rand = " + infoData.getUser_rand());
        infoData.setUser_id(sp.getString(MyShared.USER_ID,""));
        infoData.setUser_name(sp.getString(MyShared.USER_NAME,""));
        infoData.setUser_email(sp.getString(MyShared.USER_EMAIL,""));

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.main_setting:
                break;
            case R.id.main_video:
                startActivity(VideoActivity.class);
                break;
            case R.id.main_image:
                startActivity(ImageActivity.class);
                break;
            case R.id.main_quit:
                Log.i("01010010", "failed");
                postAsynHttp();
                break;
        }

    }

    private void startActivity(Class activity){
        Intent intent = new Intent(this,activity);
        intent.putExtra("video",0);
        startActivity(intent);
    }

//    private void getData(){
//        postAsynHttp();
//
//    }

    private void postAsynHttp() {
        OkHttpClient mOkHttpClient=new OkHttpClient();
//        RequestBody formBody = new FormBody.Builder()
//                .add("username", userName)
//                .add("password",userPassWord)
//                .build();
        Request request = new Request.Builder()
                .url("http://gg.gzybkj.cn/index.php/Admin/Api/userlogout?format=json&id="
                        + infoData.getUser_id()
                )
//                .post(formBody)
                .build();


        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("01010010", "failed");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.i("01010010", str);
                handler.sendEmptyMessage(1);
            }

        });
    }
}
