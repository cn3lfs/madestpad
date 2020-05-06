package com.madest.pad.update;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.madest.pad.common.Constant;
import com.madest.pad.common.MessageToUi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ll on 2020-04-28.
 */

public class DownloadTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private Handler handler;
    private String name ;
    private long appSize;
    private String appSavePath;
    private CommonProgressDialog pBar;

    private PowerManager.WakeLock mWakeLock;
    private static final String DOWNLOAD_NAME = "channelWe";
    private static final String TAG = DownloadTask.class.getSimpleName();

    public DownloadTask(Context context, Handler handlerin,long apkSize, String apkSavePath,String apkname, CommonProgressDialog mpBar)
    {
        this.context = context;
        this.handler = handlerin;
        this.name = apkname;
        this.pBar = mpBar;
        appSavePath = apkSavePath;
        appSize = apkSize;
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        File file = null;
        try
        {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            // expect HTTP 200 OK, so we don't mistakenly save error
            // report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP "
                        + connection.getResponseCode() + " "
                        + connection.getResponseMessage();
            }
            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();


            file = new File(appSavePath+"/"+name);
            Log.e(TAG,file.toString());
            input = connection.getInputStream();
            output = new FileOutputStream(file);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                else
                {
                    publishProgress((int) (total * 100 / appSize));
                }
                output.write(data, 0, count);

            }
        } catch (Exception e) {
            System.out.println(e.toString());
            MessageToUi.send(Constant.EXCEPTION_EROR,"exception",e.toString(),handler);
            return e.toString();

        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        pBar.show();

    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        pBar.setIndeterminate(false);
        pBar.setMax(100);
        pBar.setProgress(progress[0]);

       // Log.i(TAG,"down load :"+Integer.toString(progress[0]));
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        pBar.dismiss();

        Log.i(TAG,"down load completed!:");

        if (result != null) {

            // 申请多个权限。
/*            AndPermission.with(MainActivity.this)
                    .requestCode(REQUEST_CODE_PERMISSION_SD)
                    .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
                    .rationale(rationaleListener
                    )
                    .send();*/


            Toast.makeText(context, "打开目录读取存储权限：" + result, Toast.LENGTH_LONG).show();
        }
        else
        {
            // Toast.makeText(context, "File downloaded",
            // Toast.LENGTH_SHORT)
            // .show();
           // update();
            //
        Bundle bundle2=new Bundle();
        bundle2.putString("par","install");
        Message msg1;
        msg1=handler.obtainMessage();//每发送一次都要重新获取
        msg1.setData(bundle2);
        msg1.arg1 = Constant.APK_INSTALL;
        handler.sendMessage(msg1);//用handler向主线程发送信息
            //==================
        }
    }
}
