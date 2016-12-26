package com.shower.ncf.guanggaoji.myutil;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.shower.ncf.guanggaoji.myinterface.OnDownloadFinished;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

/**
 * Created by Administrator on 2016/12/14.
 */

public class MyDownLoad extends Thread implements OnDownloadFinished {

    private String downloadUrl;// 下载链接地址
    private int threadNum;// 开启的线程数
    private String filePath = "";// 保存文件路径地址
    private int blockSize;// 每一个线程的下载量
    private Context c;
    private SharedPreferences sp;
    HashSet<String> video_set;

    public MyDownLoad(String downloadUrl, int threadNum, String filepath,Context c) {
        this.downloadUrl = downloadUrl;
        this.threadNum = threadNum;
        this.filePath = filepath;
        this.c = c;
        sp = c.getSharedPreferences(MyShared.SHARED,Context.MODE_PRIVATE);
        video_set = (HashSet<String>) sp.getStringSet(MyShared.VIDEO_SET,new HashSet<String>());
    }

    @Override
    public void run() {
        FileDownloadThread[] threads = new FileDownloadThread[threadNum];

        try {
            URL url = new URL(downloadUrl);
            Log.i("01010010", "download file http path:" + downloadUrl);
            // 创建连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //处理下载读取长度为-1 问题
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.connect();
            // 读取下载文件总大小
            int fileSize = conn.getContentLength();

            File file = new File(filePath);
            if (file.exists()){
                long videoSize = file.length();
                Log.i("01010010", "size : long =" +  videoSize);
                if (videoSize >= fileSize)
                    video_set.add(filePath);
                sp.edit().putStringSet(MyShared.VIDEO_SET,video_set).commit();
                    Log.i("01010010", "file exists : long =" +  videoSize);
                return ;
            }
            //判断是否下载完成
            if (fileSize <= 0) {
//                System.out.println("读取文件失败");
                Log.i("01010010", "读取文件失败:" + "  ..............");
                return;
            }


            // 计算每条线程下载的数据长度
            blockSize = (fileSize % threadNum) == 0 ? fileSize / threadNum
                    : fileSize / threadNum + 1;

            Log.i("01010010", "fileSize:" + fileSize + "  blockSize:");

//            File file = new File(filePath);
            for (int i = 0; i < threads.length; i++) {
                // 启动线程，分别下载每个线程需要下载的部分
                threads[i] = new FileDownloadThread(url, file, blockSize,
                        (i + 1),this);
                threads[i].setName("Thread:" + i);
                threads[i].start();
            }

            boolean isfinished = false;
            int downloadedAllSize = 0;
            Log.i("01010010", " all of downloadSize:" + downloadedAllSize);

        }catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFinished() {
        video_set.add(filePath);
        sp.edit().putStringSet(MyShared.VIDEO_SET,video_set).commit();
        Log.i("01010010","finished: filePath = " + filePath);
    }


    class FileDownloadThread extends Thread {

        private  final String TAG = FileDownloadThread.class.getSimpleName();
        OnDownloadFinished onFinished;

        /** 当前下载是否完成 */
        private boolean isCompleted = false;
        /** 当前下载文件长度 */
        private int downloadLength = 0;
        /** 文件保存路径 */
        private File file;
        /** 文件下载路径 */
        private URL downloadUrl;
        /** 当前下载线程ID */
        private int threadId;
        /** 线程下载数据长度 */
        private int blockSize;

        /**
         *
         * url:文件下载地址
         * file:文件保存路径
         *  blocksize:下载数据长度
         * threadId:线程ID
         */
        public FileDownloadThread(URL downloadUrl, File file, int blocksize,
                                  int threadId,OnDownloadFinished onFinished) {
            this.downloadUrl = downloadUrl;
            this.file = file;
            this.threadId = threadId;
            this.blockSize = blocksize;
            this.onFinished = onFinished;
        }

        @Override
        public void run() {

            BufferedInputStream bis = null;
            RandomAccessFile raf = null;

            try {
                URLConnection conn = downloadUrl.openConnection();
                conn.setAllowUserInteraction(true);

                int startPos = blockSize * (threadId - 1);//开始位置
                int endPos = blockSize * threadId - 1;//结束位置
                //设置当前线程下载的起点、终点
                conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
                Log.i("01010010", Thread.currentThread().getName() + "  bytes="
                        + startPos + "-" + endPos);

                byte[] buffer = new byte[1024];
                bis = new BufferedInputStream(conn.getInputStream());

                raf = new RandomAccessFile(file, "rwd");
                raf.seek(startPos);
                int len;
                while ((len = bis.read(buffer, 0, 1024)) != -1) {
                    raf.write(buffer, 0, len);
                    downloadLength += len;
                }
                isCompleted = true;
                Log.i("01010010", "current thread task has finished,all size:"
                        + downloadLength);
                onFinished.onFinished();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 线程文件是否下载完毕
         */
        public boolean isCompleted() {
            return isCompleted;
        }

        /**
         * 线程下载文件长度
         */
        public int getDownloadLength() {
            return downloadLength;
        }


    }

}