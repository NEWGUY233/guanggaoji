package com.shower.ncf.guanggaoji.myutil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/9.
 */

public class MyParse {

    public MyInfo parseLogin(String str){
        MyInfo info = new MyInfo();
        try {
            JSONObject jsonObject = new JSONObject(str);
            info.setCode(jsonObject.getInt("code"));
            info.setMessage(jsonObject.getString("message"));

            JSONObject jb = jsonObject.getJSONObject("data");
            info.setUser_id(jb.getString("id"));
            info.setUser_name(jb.getString("username"));
            info.setUser_rand(jb.getString("rand"));
            info.setUser_email(jb.getString("email"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public List<String> parseImage(String str){
        List<String> list = new ArrayList<String>();
        try {
            JSONObject jsonObject = new JSONObject(str);

            JSONObject jb = jsonObject.getJSONObject("data");
            JSONArray jsonArray = jb.getJSONArray("photos");
            for (int i = 0 ; i <jsonArray.length() ; i++){
                String path = jsonArray.getString(i);
                if (!path.startsWith("http")){
                    path = "http://" + path;
                }
                list.add(path);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> parseVideo(String str){
        List<String> list = new ArrayList<String>();
        try {
            JSONObject jsonObject = new JSONObject(str);

            JSONObject jb = jsonObject.getJSONObject("data");
            JSONArray jsonArray = jb.getJSONArray("videos");
            for (int i = 0 ; i <jsonArray.length() ; i++){
                String path = jsonArray.getString(i);
                if (!path.startsWith("http")){
                    path = "http://" + path;
                }
                if (path.endsWith("videos/") || path.endsWith("/")){
                    continue;
                }
                list.add(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    public String parsePush(String content){
        try {
            JSONObject object = new JSONObject(content);
            String push = object.getJSONObject("msg").getString("text");
            return push;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  content;
    }

}
