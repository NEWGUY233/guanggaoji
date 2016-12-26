package com.shower.ncf.guanggaoji.myutil;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/11/28.
 */

public class MyUtil {
    Context c;
    public MyUtil(){}
    public MyUtil(Context c){
        this.c = c;
    }

    SimpleDateFormat sd;
    public String getTime(){
        Date date;
        String sTime;
        if(sd == null){
            sd = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        }
        date = new Date(System.currentTimeMillis());
        sTime = sd.format(date);

        return sTime;
    }

    public String getVideoTime(long currentTime, long totalTime){

        Date date;
        String time;
        String cTime;
        String tTime;
        if(sd == null){
            sd = new SimpleDateFormat("HH:mm:ss");
        }
        date = new Date(currentTime);
        cTime = changeTime(currentTime );

        date = new Date(totalTime);
        tTime = changeTime(totalTime );
        return cTime + "/" + tTime;
    }

    public String changeTime(long time){
        time = time /1000;
        String re = "";
        int h = (int) (time / 3600);
        if(h >= 10 && h != 0){
            re = h + ":";
        }else if ( h < 10 && h != 0){
            re = "0" + re + ":";
        }else {
            re = "00:";
        }

        int m = 0;
        if (time>= 3600){
          m = (int) (time % 3600 /60);
        }else {
            m = (int) (time / 60);
        }

        if ( m < 10 && m != 0){
            re = re + "0" + m + ":";
        }else
        if(m >= 10 && m != 0){
            re = re + m + ":";
        }else {
            re = re + "00:";
        }



        int s = (int) time % 60;
//                (int) (time / 60);

        if ( s < 10 && s != 0){
            re = re + "0" + s ;
        }else
        if(s >= 10 && s != 0){
            re = re + s;
        }else {
            re = re + "00";
        }

        return  re;

    }

}
