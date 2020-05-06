package com.madest.pad.common;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ll on 2018-08-30.
 */

public class MessageToUi {
    private static final String TAG = MessageToUi.class.getSimpleName();

    public MessageToUi(){}
    //发送字符串数据
    public static void send(int arg, String key, String info, Handler handler)
    {
        try {

            if(handler!=null)
            {
                Message msg1;
                Bundle bundle = new Bundle();
                bundle.putString(key, info);
                msg1 = handler.obtainMessage();//每发送一次都要重新获取
                msg1.arg1 = arg;
                msg1.setData(bundle);
                handler.sendMessage(msg1);
            }
            else
                Log.e(TAG,"handler is null");
        }
        catch (Exception io)
        {
            io.printStackTrace();
            Log.e(TAG,io.toString());
        }


    }
    //发送序列化数据
    public static void sendParcelable(int arg, String key, Parcelable info, Handler handler)
    {
        try
        {
            if(handler!=null)
            {
                Message msg1;
                Bundle bundle = new Bundle();
                bundle.putParcelable(key, info);
                msg1 = handler.obtainMessage();//每发送一次都要重新获取
                msg1.arg1 = arg;
                msg1.setData(bundle);
                handler.sendMessage(msg1);
            }
            else
                Log.e(TAG,"handler is null");
        }
        catch (Exception io)
        {
            io.printStackTrace();
            Log.e(TAG,io.toString());
        }


    }


    private static Toast toast;


    public static void sendInfo(String info, Handler handler) {
        try {

            if (handler != null) {
                Message msg1;
                Bundle bundle = new Bundle();
                bundle.putString("info", info);
                msg1 = handler.obtainMessage();//每发送一次都要重新获取
                msg1.arg1 = 23;
                msg1.setData(bundle);
                handler.sendMessage(msg1);
            } else {
                //LogUtil.writeLogToFile("e", TAG, "handler is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //LogUtil.writeLogToFile("e", TAG, e.getMessage());
        }
    }

    public static void sendCode(String code, Handler handler) {
        try {

            if (handler != null) {
                Message msg1;
                Bundle bundle = new Bundle();
                bundle.putString("code", code);
                msg1 = handler.obtainMessage();//每发送一次都要重新获取
                msg1.arg1 = 23;
                msg1.setData(bundle);
                handler.sendMessage(msg1);
            } else {
                //LogUtil.writeLogToFile("e", TAG, "handler is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
           // LogUtil.writeLogToFile("e", TAG, e.getMessage());
        }
    }

    public static void sendError(String info, Handler handler) {
        try {

            if (handler != null) {
                Message msg1;
                Bundle bundle = new Bundle();
                bundle.putString("info", "异常--" + info);
                msg1 = handler.obtainMessage();//每发送一次都要重新获取
                msg1.arg1 = 23;
                msg1.setData(bundle);
                handler.sendMessage(msg1);
            } else {
                //LogUtil.writeLogToFile("e", TAG, "handler is null");
            }


        } catch (Exception e) {
            e.printStackTrace();
            //LogUtil.writeLogToFile("e", TAG, e.getMessage());
        }
    }
    public static void sendError(String info, int second, Handler handler) {
        try {

            if (handler != null) {
                Message msg1;
                Bundle bundle = new Bundle();
                bundle.putString("info", "异常--" + info);
                bundle.putInt("second", second);
                msg1 = handler.obtainMessage();//每发送一次都要重新获取
                msg1.arg1 = 23;
                msg1.setData(bundle);
                handler.sendMessage(msg1);
            } else {
               // LogUtil.writeLogToFile("e", TAG, "handler is null");
            }


        } catch (Exception e) {
            e.printStackTrace();
            //LogUtil.writeLogToFile("e", TAG, e.getMessage());
        }
    }


    public static void toast(Context context, String message) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    public static void toast(Context context, String message, Integer second) {
        if (toast == null) {
            toast = Toast.makeText(context, message, second*1000);
        } else {
            toast.setText(message);
            toast.setDuration(second*1000);
        }
        toast.show();
    }


}
