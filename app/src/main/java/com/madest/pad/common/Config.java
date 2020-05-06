package com.madest.pad.common;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.util.UUID;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

public final class Config
{
    public static String TAG = "Config";
    public static String ver = null; // 在gradle 里修改版本号
    public static String webServerAddr = "192.168.100.99:8888";//default value
    public static String devId = "1101-01" ; //default value
    public static String mqttServerAddr = "192.168.100.99" ;//default value
    public static String mainDir = "/madestpad/" ;//default value main dir
    public static String apkUpadetSavePath = "";//default value
    public static String APKNAME="APKNAME";//default value

    public static Handler MainActivityHandler = null ;

    public static void init(Handler handler, Context context )
    {
        try
        {   //读取apk本身版本号
            ver = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;

            //创建app 主目录
            File file = new File(Environment.getExternalStorageDirectory() + mainDir);
            if (!file.exists())
            {
                Log.d(TAG, "create result:" + file.toString() + ":" + file.mkdirs());
                mainDir = file.toString();
            }
            else
                mainDir = file.toString();
            Log.i(TAG,file.toString());

            apkUpadetSavePath  = mainDir + "/update/" ;
            File file1 = new File(apkUpadetSavePath);
            if (!file1.exists()) {
                Log.d(TAG, "create result:" + file1.toString() + ":" + file1.mkdirs());
            }
            apkUpadetSavePath = file1.toString();
            Log.i(TAG,file1.toString());
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    public static String getUniquePsuedoID()
    {
        String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);
        String serial = null;
        try
        {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            // Go ahead and return the serial for api => 9
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString().toUpperCase();
        }
        catch (Exception e)
        {
            // String needs to be initialized
            e.printStackTrace();
            serial = "serial"; // some value
        }
        // Finally, combine the values we have found by using the UUID class to create a unique identifier
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString().toUpperCase();
    }
}
