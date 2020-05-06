package com.madest.pad.common;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.madest.pad.update.VersionInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpGetThread extends Thread
{
    public String TAG = HttpGetThread.class.getName();
    public Context context0 = null;
    public Handler handler0 = null;
    public String Url = null;
    public int exflag ;
    public String action;
    public String marg[];

    public HttpGetThread(Handler handlerin, Context contextin, String url, int flag, String mAction, String arg[]) {
        handler0 = handlerin;  //UI 界面handler
        context0 = contextin;  //UI 界面上下行文
        Url = url;
        exflag = flag;  // 简单指定要执行那个函数标志
        action = mAction;  //请求后可以用于回传，不用可为空
        marg = arg;  //传入参数
    }
    public void run()
    {
        String result = "";
        HttpClient httpCient = new DefaultHttpClient();
        try
        {
            httpCient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,5000);//连接时间
            httpCient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,5000);//数据传输时间
            HttpGet httpGet = new HttpGet(Url);
            int code = -1;

            Log.i(TAG,action+"--->"+exflag);
            Log.i(TAG, Url);
            HttpResponse response = httpCient.execute(httpGet);
            code = response.getStatusLine().getStatusCode();
            Log.i(TAG, "get server reponse code: "+Integer.toString(code));

            if (response.getStatusLine().getStatusCode() == 200)
            {
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity, HTTP.UTF_8);
                Log.i(TAG, result);

                switch (exflag)
                {
                    case 0:
                        getSoftUpdateInfo(result);
                        break;
                    case 1:
                        //cutBedgetPartList(result);
                        break;
                    case 2:
                       //cutBedgetStatistics(result);
                        break;
                    case 3:
                        //cutBedcardSender(result);
                        break;
                    case 4:
                       // EasyCardInfoOnLine(result);
                        break;
                    case 5:
                       // quickOnLineByCard(result);
                        break;
                    case 6:
                       // getHangOrderPlan(result);
                        break;
                    case 7:
                       // hangerCardSender(result);
                        break;
                    case 8:
                       // LineOutCardInfo(result);
                        break;
                    case 9:
                       // LineOutpaNum(result);
                        break;
                    case 10:
                       // submitProduction(result);
                        break;
                    case 11:
                      //  switchLight(result);
                        break;
                    case 12:
                        //switchPlan(result);
                        break;
                    case 13:
                       // houdaoBound2IdProcess(result);
                        break;
                    default:
                        break;
                }
            }else
            {
                Log.i(TAG,"服务端返回："+Integer.toString(code)+"#"+action);
                MessageToUi.send(Constant.EXCEPTION_EROR,"exception","服务端返回："+Integer.toString(code)+"#"+action,handler0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            MessageToUi.send(Constant.EXCEPTION_EROR,"exception",e.toString(),handler0);

        }
    }





   public void getSoftUpdateInfo(String result)
   {
       try
       {
           JSONObject jsonObj = new JSONObject(result);
           if (jsonObj == null)
               return;
           else
           {
               if(jsonObj.has("result")==true)
               {
                   String res = jsonObj.optString("result");
                   if(res.compareTo("false")==0)
                   {
                       String message = jsonObj.optString("message");
                       message = "获取软件版本:"+message;
                       MessageToUi.send(Constant.EXCEPTION_EROR,"exception",message,handler0);
                      //loadedversioned = true;
                       return;
                   }
                   else
                   {
                       long filesize = jsonObj.optInt("fileSize");
                       String fileUrl = jsonObj.optString("fileUrl");
                       String message = jsonObj.optString("message");
                       String version =  jsonObj.optString("version");
                       String md5 = jsonObj.optString("md5");
                       if(version.compareTo(Config.ver)==0)
                       {
                           String ss = "该版本已经是最新！";
                          // loadedversioned = true;
                           MessageToUi.send(Constant.UPDATE_TIP,"info",ss,handler0);
                       }
                       else
                       {
                           float tmp0 = Float.parseFloat(version);
                           float tmp1 = Float.parseFloat(Config.ver);
                           if(Float.compare(tmp0,tmp1)>0)
                           {
                               VersionInfo tmp = new VersionInfo();
                               tmp.ver = version;
                               tmp.fileurl = fileUrl;
                               tmp.filesize = filesize ;
                               tmp.apkname = version+".apk";
                               MessageToUi.sendParcelable(Constant.UPDATE_INFO,"softupdate",tmp,handler0);
                               return;
                           }
                           else  if(Float.compare(tmp0,tmp1)<=0)
                           {
                               String sss = "服务端APK版本较低！";
                              //loadedversioned = true;
                               MessageToUi.send(Constant.UPDATE_TIP,"info",sss,handler0);
                           }
                       }
                   }

               }
           }
       }catch (JSONException e)
       {
           e.printStackTrace();
           MessageToUi.send(Constant.EXCEPTION_EROR,"exception",e.toString(),handler0);
           return;
       }
       catch (Exception io)
       {
           io.printStackTrace();
           MessageToUi.send(Constant.EXCEPTION_EROR,"exception",io.toString(),handler0);
       }

   }









    public void hangerCardSender(String result)
    {

    }


    public void LineOutCardInfo(String result)
    {


    }


    public void LineOutpaNum(String result)
    {

    }

    public void getHangOrderPlan(String result)
    {

    }











}
