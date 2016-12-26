package com.shower.ncf.guanggaoji;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.dou361.ijkplayer.widget.IjkVideoView;
import com.shower.ncf.guanggaoji.myservice.MyBroadCast;
import com.shower.ncf.guanggaoji.myutil.MyDownLoad;
import com.shower.ncf.guanggaoji.myutil.MyInfo;
import com.shower.ncf.guanggaoji.myutil.MyParse;
import com.shower.ncf.guanggaoji.myutil.MyShared;
import com.shower.ncf.guanggaoji.myutil.MyUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Administrator on 2016/12/7.
 */

public class VideoActivity extends  MyActivity implements IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnCompletionListener, View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, Animation.AnimationListener, CacheListener, IMediaPlayer.OnErrorListener {
    //view
    IjkVideoView videoView;
    ImageView video_stop_start;
    ImageView video_volume;
    ImageView video_full;
    TextView video_time;
    TextView video_hd;
    LinearLayout video_btn;
//    TextView video_bg;
    TextView video_content;

    SeekBar video_seek;

    //
    ProgressBar video_waiting;

    //
    LinearLayout video_ll;

    //
    boolean isPrepare = false;
    boolean isStop = true;
    boolean isPlaying = false;

    //
    MyBroadCast myBroadCast;
    IntentFilter filter;
    SharedPreferences sp;
    HashSet<String> video_set;
//    private HttpProxyCacheServer proxy;

    //
    MyInfo infoData;
    List<String> listVideo;
    List<String> listVideo_check;

    //position
    int currentPosition = 0;
    //hanlder
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    changeSeekBar();
                    break;
                case 2:
                    setVideoPath();
                    break;
                case 3:
                    replayOrRestart();
                    break;
                case 100:
                    setPushData();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initView();
        register();
        Intent intent = getIntent();
        if (intent != null)
            currentPosition = intent.getIntExtra("video",0);
        getData();
    }

    private void initView(){
//        videoView = (IjkVideoView) findViewById(R.id.video_view);
//        videoView.setVideoURI(Uri.parse(url));
//        proxy = ((MyApplication)getApplication()).getProxy(this);

//        proxy.registerCacheListener(this,"");

        video_stop_start = (ImageView) findViewById(R.id.video_stop_start);
        video_volume = (ImageView) findViewById(R.id.video_volume);
        video_full = (ImageView) findViewById(R.id.video_full);
        video_time = (TextView) findViewById(R.id.video_time);
        video_hd = (TextView) findViewById(R.id.video_hd);
//        video_bg = (TextView) findViewById(R.id.video_bg);
        video_btn = (LinearLayout) findViewById(R.id.video_btn);
        video_content = (TextView) findViewById(R.id.video_content);

        video_seek = (SeekBar) findViewById(R.id.video_seek);

        video_waiting = (ProgressBar) findViewById(R.id.video_waiting);
        video_ll = (LinearLayout) findViewById(R.id.video_ll);




        //
        //get shared
        infoData = new MyInfo();
        sp = getSharedPreferences(MyShared.SHARED, Context.MODE_PRIVATE);
        infoData.setUser_rand(sp.getString(MyShared.USER_RAND,""));
        infoData.setUser_id(sp.getString(MyShared.USER_ID,""));
        infoData.setUser_name(sp.getString(MyShared.USER_NAME,""));
        infoData.setUser_email(sp.getString(MyShared.USER_EMAIL,""));

        initVideo();
    }

    private void initVideo(){
        video_ll.removeAllViews();

        videoView = new IjkVideoView(this);
        videoView.setOnPreparedListener(this);
        videoView.setOnCompletionListener(this);
        videoView.setOnErrorListener(this);

        video_ll.addView(videoView);
    }


    private void initSeekBar(){
        video_seek.setMax(videoView.getDuration());
        video_seek.setProgress(0);
        video_seek.setOnSeekBarChangeListener(this);
        setSeekBar();
    }

    private void setSeekBar(){
        new Thread(){
            @Override
            public void run() {
                while (isPlaying){
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(1);
                }
            }
        }.start();
    }

    private void register(){
        filter = new IntentFilter();
        filter.addAction("com.shower.ncf.guanggaoji.video");
        myBroadCast = new MyBroadCast(this);
        this.registerReceiver(myBroadCast,filter);
    }


    private void changeSeekBar(){
         video_seek.setProgress(videoView.getCurrentPosition());
    }


    private void prepare(){
        video_stop_start.setOnClickListener(this);
        video_volume.setOnClickListener(this);
        video_full.setOnClickListener(this);
        video_hd.setOnClickListener(this);
//        video_bg.setOnClickListener(this);
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        video_waiting.setVisibility(View.GONE);
        videoView.start();
        video_time.setText(new MyUtil().changeTime(iMediaPlayer.getDuration()));
        isPrepare = true;
        isStop = false;
        isPlaying = true;
        video_stop_start.setImageDrawable(getResources().getDrawable(R.drawable.video_stop));
//        prepare();
//        initSeekBar();
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        video_stop_start.setImageDrawable(getResources().getDrawable(R.drawable.video_start));
        isPlaying = false;

        video_waiting.setVisibility(View.VISIBLE);

        initVideo();
        currentPosition ++ ;

        saveVideo();
        postAsynHttp2();

    }

    private void replayOrRestart(){

        // 获取SD卡路径
        String v_path = Environment.getExternalStorageDirectory()
                + "/amosdownload/";
        File file = new File(v_path);
        // 如果SD卡目录不存在创建
        if (!file.exists()) {
            file.mkdir();
        }

        if (listVideo_check != null && listVideo_check.size()!=0){
            listVideo = listVideo_check;

        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.video_stop_start:
//                stopOrStart();
                break;
            case R.id.video_volume:
                break;
            case R.id.video_hd:
                break;
            case R.id.video_full:
                video_btn.setVisibility(View.GONE);
                break;
//            case R.id.video_bg:
////                video_btn.setVisibility(View.VISIBLE);
//                break;
        }

    }

    private void stopOrStart(){
//        if (!isPlaying){
//            return;
//        }
        if (isStop){
            isStop = false;
            videoView.start();
            video_stop_start.setImageDrawable(getResources().getDrawable(R.drawable.video_stop));
        }else {
            isStop = true;
            videoView.pause();
            video_stop_start.setImageDrawable(getResources().getDrawable(R.drawable.video_start));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isPlaying = false;
        videoView.seekTo(seekBar.getProgress());
        isPlaying = true;
        setSeekBar();
    }

    String pushContent = "";
    public void getData(final String str){
        pushContent = new MyParse().parsePush(str);
        new Thread(){
            @Override
            public void run() {
                handler.sendEmptyMessage(100);
            }
        }.start();


    }

    private void setPushData(){
        String content = pushContent;
        content = content.replaceAll("\n","");
        content = content.replaceAll("\r","");
        video_content.setText(content);
        Log.i("01010010","content = " + content);
        Paint paint = new Paint();
        paint.setTextSize(video_content.getTextSize());
        float size =paint.measureText(content);
//        video_content.getLayoutParams().width =  (int) size;
        Log.i("01010010","size = " + size + " ; width = " + video_content.getWidth());
        int with = getWindowManager().getDefaultDisplay().getWidth();
        TranslateAnimation trans = new TranslateAnimation(with,-with - size,0,0);
        int tranTime = 10000;
        tranTime = (int) (10000 + size/with*10000);
        trans.setDuration(tranTime);
        trans.setAnimationListener(VideoActivity.this);
        video_content.startAnimation(trans);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadCast);
        videoView.stopPlayback();
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        video_content.setText("");
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    private void getData(){
        new Thread(){
            @Override
            public void run() {
                postAsynHttp();
            }
        }.start();
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
//                listImage = new MyParse().parseImage(str);
                Log.i("01010010","str = " + str);
                listVideo = new MyParse().parseVideo(str);
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
                listVideo_check = new MyParse().parseVideo(str);
                handler.sendEmptyMessage(3);
            }

        });
    }



    private void setVideoPath(){
        if (listVideo == null || listVideo.size() == 0){
            Toast.makeText(this,"没有视频",Toast.LENGTH_SHORT).show();
            return;
        }
        saveVideo();

    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {

    }

    private void saveVideo(){
        Log.i("01010010","start.........................");
        if (currentPosition < 0 && currentPosition >= listVideo.size())
            currentPosition = 0;
        String url = "";
        try{
            url = listVideo.get(currentPosition);
        }catch (Exception e){
            currentPosition = 0;
            url = listVideo.get(0);
        }

        // 获取SD卡路径
        String path = Environment.getExternalStorageDirectory()
                + "/amosdownload/";
        File file = new File(path);
        // 如果SD卡目录不存在创建
        if (!file.exists()) {
            file.mkdir();
        }
        try {
            video_set = (HashSet<String>) sp.getStringSet(MyShared.VIDEO_SET, new HashSet<String>());
            Iterator it2 = video_set.iterator();
            while (it2.hasNext()) {
                String video = (String) it2.next();

                for (int i = 0; i < listVideo.size(); i++) {
                    String v_path = listVideo.get(i);
                    v_path = path + v_path.substring(v_path.lastIndexOf("/") + 1);
                    if (video.equals(v_path)) {
                        Log.i("01010010", "exists file");
                        break;
                    }
                    if (i == listVideo.size() - 1) {
                        video_set.remove(video);
                        sp.edit().putStringSet(MyShared.VIDEO_SET, video_set).commit();
                        Log.i("01010010", "delete file path = " + video);
                        File f = new File(video);
                        f.delete();
                    }
                }

            }
        }catch (Exception e){

        }






        String fileName = url.substring(url.lastIndexOf("/")+1);
        String filepath = path + fileName;

        MyDownLoad myDownLoad = new MyDownLoad(url,3,filepath,this);
        myDownLoad.start();


        video_set = (HashSet<String>) sp.getStringSet(MyShared.VIDEO_SET,new HashSet<String>());
        Iterator it = video_set.iterator();
        while (it.hasNext()){
            if (filepath.equals(it.next())){
                if (new File(filepath).exists()){
                    videoView.setVideoPath(filepath);
                    Log.i("01010010","location");
                }else {
                    videoView.setVideoURI(Uri.parse(listVideo.get(currentPosition)));
                    Log.i("01010010","network");
                }
                return;
            }
        }
        videoView.setVideoURI(Uri.parse(listVideo.get(currentPosition)));
        Log.i("01010010","network");
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
//        video_stop_start.setImageDrawable(getResources().getDrawable(R.drawable.video_start));
//        isPlaying = false;

        video_waiting.setVisibility(View.VISIBLE);

        initVideo();
        currentPosition ++ ;

        saveVideo();
//        postAsynHttp2();

        return true;
    }
}
