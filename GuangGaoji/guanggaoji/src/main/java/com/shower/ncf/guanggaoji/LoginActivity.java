package com.shower.ncf.guanggaoji;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.igexin.sdk.PushManager;
import com.shower.ncf.guanggaoji.myservice.MyService;
import com.shower.ncf.guanggaoji.myservice.PushService;
import com.shower.ncf.guanggaoji.myutil.MyInfo;
import com.shower.ncf.guanggaoji.myutil.MyParse;
import com.shower.ncf.guanggaoji.myutil.MyShared;

import java.io.IOException;

import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class LoginActivity extends MyActivity implements View.OnClickListener {

    //View
    EditText login_user;
    EditText login_password;
    TextView login_login;

    //
    String userName;
    String userPassWord;
    private Class pushService;
    private Class myService;

    //
    MyInfo info;
    SharedPreferences sp;

    //handler
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    isLogin = false;
                    check();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), MyService.class);
        initGeTui();
        initView();
    }
    private void initGeTui() {
        Log.d("01010010", "initializing sdk...");
        pushService =  PushService.class;
        myService = MyService.class;
        PackageManager pkgManager = getPackageManager();

        // 读写 sd card 权限非常重要, android6.0默认禁止的, 建议初始化之前就弹窗让用户赋予该权限
        boolean sdCardWritePermission =
                pkgManager.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, getPackageName()) == PackageManager.PERMISSION_GRANTED;

        // read phone state用于获取 imei 设备信息
        boolean phoneSatePermission =
                pkgManager.checkPermission(android.Manifest.permission.READ_PHONE_STATE, getPackageName()) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= 23 && !sdCardWritePermission || !phoneSatePermission) {
            requestPermission();
        } else {
            PushManager.getInstance().initialize(this.getApplicationContext(), pushService);
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                REQUEST_PERMISSION);
    }
    private static final int REQUEST_PERMISSION = 0;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if ((grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                PushManager.getInstance().initialize(this.getApplicationContext(), pushService);
            } else {
                Log.e("01010010", "We highly recommend that you need to grant the special permissions before initializing the SDK, otherwise some "
                        + "functions will not work");
                PushManager.getInstance().initialize(this.getApplicationContext(), pushService);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }
    private void initView(){
        sp = getSharedPreferences(MyShared.SHARED, Context.MODE_PRIVATE);
        userPassWord = sp.getString(MyShared.USER_PASSWORD,"");
        userName = sp.getString(MyShared.USER_NAME,"");

        login_user = (EditText) findViewById(R.id.login_user);
        login_password = (EditText) findViewById(R.id.login_password);
        login_login = (TextView) findViewById(R.id.login_login);

        login_login.setOnClickListener(this);

        if (userName != null && !"".equals(userName)){
            login_user.setText(userName);
            if (userPassWord != null && !"".equals(userPassWord))
                login_password.setText(userPassWord);
        }
    }

    private void login(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void postAsynHttp() {
        isLogin = true;
        OkHttpClient mOkHttpClient=new OkHttpClient();
//        RequestBody formBody = new FormBody.Builder()
//                .add("username", userName)
//                .add("password",userPassWord)
//                .build();
        Request request = new Request.Builder()
                .url("http://gg.gzybkj.cn/index.php/Admin/Api/userinfo?format=json&"
                        + "username=" + userName
                        + "&password=" + userPassWord
                )
//                .post(formBody)
                .build();


        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.i("01010010", str);
                info = new MyParse().parseLogin(str);
                handler.sendEmptyMessage(1);
            }

        });
    }

    boolean isLogin = false;
    @Override
    public void onClick(View view) {
        userName = login_user.getText().toString();
        userPassWord = login_password.getText().toString();

        if (userName == null || "".equals(userName)){
            userName = "";
            Toast.makeText(this,"请输入用户名",Toast.LENGTH_SHORT).show();
            return;
        }

        if (userPassWord == null || "".equals(userPassWord)){
            userPassWord = "";
            Toast.makeText(this,"请输入密码",Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isLogin){
            postAsynHttp();
        }
//        login();
    }

    private void check(){
        if (info == null)
            return;
        int code = info.getCode();
        Toast.makeText(this,info.getMessage(),Toast.LENGTH_LONG).show();
        if (code != 200){
            return;
        }
        setData(info);
        login();
    }


    private void setData(MyInfo info1){
        MyInfo info = info1;
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(MyShared.USER_EMAIL,info.getUser_email());
        ed.putString(MyShared.USER_ID,info.getUser_id());
        ed.putString(MyShared.USER_NAME,info.getUser_name());
        ed.putString(MyShared.USER_PASSWORD,userPassWord);
        ed.putString(MyShared.USER_RAND,info.getUser_rand());
        ed.commit();
    }
}
