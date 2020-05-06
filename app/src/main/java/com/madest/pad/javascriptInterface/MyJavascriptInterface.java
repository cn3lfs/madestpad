package com.madest.pad.javascriptInterface;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.madest.pad.common.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public final class MyJavascriptInterface
{
    private static String TAG = "MyJavascriptInterface";
    public Handler handlerm = null ;
    public Context contextm = null;


    public  MyJavascriptInterface( Handler handler, Context context)
    {
        this.handlerm = handler ;
        this.contextm = context ;
    }

    /**
     * h5调用继电器控制打铃
     *
     * @param line 代表第几路，总共2路 值为1或2
     * @param statues 状态0或者1 打开或者关闭
     */
    @JavascriptInterface
    public void switchRelay(final String line,final  String statues)
    {
        //
    }


    /**
     * 前端读文件
     *
     * @param filename 文件名
     *
     */
    @JavascriptInterface
    public String  readSomeFile(final String filename)
    {
        //
        String strjson = ""; //默认以json格式存储数据
        try
        {
            if(filename.isEmpty())
                return null;

            String someFile = Config.mainDir+filename ;
            File file = new File(someFile);
            if (!file.exists())
            {
                Log.d(TAG, "create result:" + file.toString() + ":" + file.createNewFile());
            }
            if(file == null)
                return null;
            FileInputStream in = new FileInputStream(file);
            int len = 0;
            byte[] buf = new byte[1024];
            String strbuf;

            while ((len = in.read(buf)) != -1)
            {
                strbuf = new String(buf, 0, len);
                strjson = strjson + strbuf;
            }
            in.close();
            return strjson ;
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally
        {
            return strjson;
        }
    }



    /**
     * 前端写文件
     *
     * @param filename 文件名
     *
     */
    @JavascriptInterface
    public   boolean writeSomeFile(final String filename,String data)
    {
       try
       {
           String someFile = Config.mainDir+filename ;
           OutputStream os = new FileOutputStream(someFile);
           byte[] bytes = data.getBytes("UTF-8");
           os.write(bytes, 0, bytes.length);
           //关闭流
           os.close();
           os.flush();
           return true ;
       }
       catch (IOException e)
       {
           e.printStackTrace();
       }
       finally
       {
           return false;
       }
    }
}
