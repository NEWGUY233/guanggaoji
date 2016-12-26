package com.shower.ncf.guanggaoji;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.shower.ncf.guanggaoji.myutil.MyInfo;
import com.shower.ncf.guanggaoji.myutil.MyParse;
import com.shower.ncf.guanggaoji.myutil.MyShared;
import com.shower.ncf.guanggaoji.myview.MyViewPager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/12/9.
 */

public class ImageActivity extends MyActivity implements ViewPager.OnPageChangeListener {
    //View
    MyViewPager imagePager;

    //List
    List<String> listPath;
    List<ImageView> listImage;

    //
    MyInfo infoData;
//    List<String> listImagePath;
    //
    List<String> listPath_check;

    //isdowan
    boolean isFinish = false;

    //handler
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Log.i("01010010"," currentItem = " + imagePager.getCurrentItem());
                    if (imagePager.getCurrentItem() >= listPath.size() - 1){
                        postAsynHttp2();
                        imagePager.setCurrentItem(0);
                    }else {
                    imagePager.setCurrentItem(imagePager.getCurrentItem()+1);
                    }
                    break;
                case 2:
                    initListData();
                    break;
                case 3:
                    try {
                        resetData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        initView();
        postAsynHttp();
    }

    private void initView(){
        imagePager  = (MyViewPager) findViewById(R.id.image_viewpager);

        //get shared
        infoData = new MyInfo();
        SharedPreferences sp = getSharedPreferences(MyShared.SHARED, Context.MODE_PRIVATE);
        infoData.setUser_rand(sp.getString(MyShared.USER_RAND,""));
        Log.i("01010010","rand = " + infoData.getUser_rand());
        infoData.setUser_id(sp.getString(MyShared.USER_ID,""));
        infoData.setUser_name(sp.getString(MyShared.USER_NAME,""));
        infoData.setUser_email(sp.getString(MyShared.USER_EMAIL,""));

        imagePager.setOnPageChangeListener(this);

    }

    private void initListData(){
//        listPath = new ArrayList<String>();
//        listPath.add("http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1207/26/c0/12556454_1343287284238.jpg");
//        listPath.add("http://i.epetbar.com/2013-08/11/32472b0bb9556672ec6c0afb6b502661.jpg");
//        listPath.add("http://pic73.huitu.com/res/20160429/851091_20160429230438413400_1.jpg");
//        listPath.add("http://h.hiphotos.baidu.com/zhidao/pic/item/0eb30f2442a7d9334f268ca9a84bd11372f00159.jpg");
//        listPath.add("http://sc.jb51.net/uploads/allimg/150703/14-150F3164339355.jpg");
//        listPath.add("http://a.hiphotos.baidu.com/zhidao/pic/item/f9dcd100baa1cd11aa2ca018bf12c8fcc3ce2d74.jpg");

        if (listPath == null || listPath.size() == 0)
            return;

        listImage = new ArrayList<ImageView>();
        for (int i = 0 ; i < listPath.size() ; i++){
            ImageView image = new ImageView(this);
            Glide.with(this).load(listPath.get(i)).fitCenter().into(image);
            ViewPager.LayoutParams lp = new ViewPager.LayoutParams();
            lp.height = ViewPager.LayoutParams.MATCH_PARENT;
            lp.width = ViewPager.LayoutParams.MATCH_PARENT;
            image.setLayoutParams(lp);
            image.setScaleType(ImageView.ScaleType.FIT_XY);

            listImage.add(image);
        }


        imagePager.setAdapter(new MyAdapter(listImage));
        setTimer();
    }

    private void initListData2(){
//        listPath = new ArrayList<String>();
//        listPath.add("http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1207/26/c0/12556454_1343287284238.jpg");
//        listPath.add("http://i.epetbar.com/2013-08/11/32472b0bb9556672ec6c0afb6b502661.jpg");
//        listPath.add("http://pic73.huitu.com/res/20160429/851091_20160429230438413400_1.jpg");
//        listPath.add("http://h.hiphotos.baidu.com/zhidao/pic/item/0eb30f2442a7d9334f268ca9a84bd11372f00159.jpg");
//        listPath.add("http://sc.jb51.net/uploads/allimg/150703/14-150F3164339355.jpg");
//        listPath.add("http://a.hiphotos.baidu.com/zhidao/pic/item/f9dcd100baa1cd11aa2ca018bf12c8fcc3ce2d74.jpg");

        if (listPath == null || listPath.size() == 0)
            return;

        listImage = new ArrayList<ImageView>();
        for (int i = 0 ; i < listPath.size() ; i++){
            ImageView image = new ImageView(this);
            Glide.with(this).load(listPath.get(i)).into(image);
            ViewPager.LayoutParams lp = new ViewPager.LayoutParams();
            lp.height = ViewPager.LayoutParams.MATCH_PARENT;
            lp.width = ViewPager.LayoutParams.MATCH_PARENT;
            image.setLayoutParams(lp);
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            listImage.add(image);
        }
        imagePager.setAdapter(new MyAdapter(listImage));
//        imagePager.getAdapter().notifyDataSetChanged();
    }

    private void setTimer(){
        new Thread(){
            @Override
            public void run() {
                try {
                    while (true && !isFinish){
                        sleep(3000);
                        handler.sendEmptyMessage(1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isFinish = true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        Log.i("01010010","psotion = " + position);
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    class MyAdapter extends PagerAdapter{
        List<ImageView> listImage;
        public MyAdapter( List<ImageView> listImage){
            this.listImage = listImage;
        }



        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager)container).removeView(listImage.get(position));

        }

        /**
         * 载入图片进去，用当前的position 除以 图片数组长度取余数是关键
         */
        @Override
        public Object instantiateItem(View container, int position) {
            ((ViewPager)container).addView(listImage.get(position));
            return listImage.get(position);
        }


        @Override
        public int getCount() {
            return listImage.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private void resetData(){
//        try {
//            for (int i = 0; i < listPath_check.size(); i++) {
//                String path = listPath_check.get(i);
//                if (!path.equals(listPath.get(i))) {
//                    listPath = listPath_check;
//                    initListData2();
//                    Log.i("01010010", "reset");
//                    return;
//                }
//            }
//        }catch (Exception e){
        if (listPath_check == null || listPath_check.size() == 0)
            return;
            listPath = listPath_check;
            initListData2();
//        }
    }


    private void postAsynHttp() {
        OkHttpClient mOkHttpClient=new OkHttpClient();
//        RequestBody formBody = new FormBody.Builder()
//                .add("username", userName)
//                .add("password",userPassWord)
//                .build();
        Request request = new Request.Builder()
                .url("http://gg.gzybkj.cn/index.php/Admin/Api/getPV?format=json&rand="
                        + infoData.getUser_rand()
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
                listPath = new MyParse().parseImage(str);
                handler.sendEmptyMessage(2);
            }

        });
    }


    private void postAsynHttp2() {
        OkHttpClient mOkHttpClient=new OkHttpClient();
//        RequestBody formBody = new FormBody.Builder()
//                .add("username", userName)
//                .add("password",userPassWord)
//                .build();
        Request request = new Request.Builder()
                .url("http://gg.gzybkj.cn/index.php/Admin/Api/getPV?format=json&rand="
                        + infoData.getUser_rand()
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
                listPath_check = new MyParse().parseImage(str);
                handler.sendEmptyMessage(3);
            }

        });
    }

}
