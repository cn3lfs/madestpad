package com.madest.pad.mqttclient;

import android.os.Handler;
import android.util.Log;


import com.madest.pad.common.Config;
import com.madest.pad.common.Constant;
import com.madest.pad.common.MessageToUi;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyMqttClient extends Thread {

    public static String TAG = "MyMqttClient";
    public Handler handler0 = null;//selef
    public Handler handler1 = null; //mainActivity handler
    public Handler handler2 = null; //fragrealinfo hadndler
    public Handler handler3 = null;


    public static String passWord="admin";
    public static String userName="admin";


    public static String myTopic=null;

    public static String clientId= null;
    public static String broker= null;
    public static int qos = 1; //消息传输等级
    public static MemoryPersistence persistence =null;
    public static MqttConnectOptions connOpts = null;
    public static MqttClient myClient = null;
    public boolean Stop = false;
    public static boolean myClientConn= false;
    public static ArrayList<Action> actionArrayList = new ArrayList<Action>();

  //  private static MqttAndroidClient myClient;
    public static MqttCallbackExtended mqttCallback = null;

    public static  int tt = 0;

    public MyMqttClient()
    {
        Stop = false;
        actionArrayList.clear();
        // Json 数据格式参照madest吊挂推送格式,若不需要自行定义
        Action tmp1 = new Action("mq.login",1);
        actionArrayList.add(tmp1);
        Action tmp2 = new Action("mq.loginOut",2);
        actionArrayList.add(tmp2);
        Action tmp3 = new Action("mq.realTime",3);
        actionArrayList.add(tmp3);
        Action tmp4 = new Action("mq.lastCostTime",4);
        actionArrayList.add(tmp4);
        Action tmp5 = new Action("orderReturnHgInfo",5);
        actionArrayList.add(tmp5);
        Action tmp6 = new Action("mq.stateCode",6);
        actionArrayList.add(tmp6);

        Action tmp7 = new Action("mq.orderPlanInfo",7);
        actionArrayList.add(tmp7);


        //{"data":{"success":1},"meta":{"type":"mq.cutPlanOverSuccess"}}
        //{"data":{"success":1},"meta":{"type":"mq.cutPlanAddSuccess"}}
        //裁床任务计划自动更新
        Action tmp8 = new Action("mq.cutPlanOverSuccess",8);
        actionArrayList.add(tmp8);
        Action tmp9 = new Action("mq.cutPlanAddSuccess",9);
        actionArrayList.add(tmp9);

        Action tmp10 = new Action("mq.stateInTime",10);
        actionArrayList.add(tmp10);

        Log.i(TAG,"actionArrayList:"+actionArrayList.size());

    }
    public void onStop()
    {
        Stop = true;
    }

    public void initMqttOptions()
    {
        try {
            // 清除缓存
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setPassword(passWord.toCharArray());
            connOpts.setUserName(userName);
            // 设置超时时间，单位：秒
            connOpts.setConnectionTimeout(4);
            // 心跳包发送间隔，单位：秒
            connOpts.setKeepAliveInterval(20);

            connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            connOpts.setMaxInflight(10);
            //不要自动连接，自己手动连接
            //connOpts.setAutomaticReconnect(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    public  static void initMqttCallback()
    {
        mqttCallback=new MqttCallbackExtended()
        {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.i(TAG, "connect Complete "+ Config.mqttServerAddr+" ok  thread id ->" + Thread.currentThread().getId());
                try
                {
                    myClientConn = true;
                    if(myClient!=null)
                        myClient.subscribe(myTopic,qos);//设置监听的topic
                     MessageToUi.send(Constant.MQTT_CON_STATE,"mq.statue","connect",Config.MainActivityHandler);
                }
                catch (MqttException e)
                {
                    myClientConn = false;
                    disConnectBroker();
                    Log.e(TAG,e.toString());
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection Lost ");
                myClientConn = false;
                MessageToUi.send(Constant.MQTT_CON_STATE,"mq.statue","disconnect",Config.MainActivityHandler);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String msg = new String(message.getPayload());
                if(msg.length()<1024*1)
                    Log.i(TAG, "topic:"+topic+" playlen:"+msg.length()+" palyload:"+msg);
                else
                {
                    //too long not print
                    Log.i(TAG, "topic:"+topic+" playlen:"+msg.length());
                }
                if(topic.equalsIgnoreCase(myTopic))
                {
                    //Log.i(TAG, "messageArrived: " + new String(message.getPayload()));
                    try
                    {
                        JSONObject jsonObj = new JSONObject(msg);
                        if (jsonObj == null)
                            return;
                        if(jsonObj.has("meta")==true)
                        {
                            JSONObject metaobj =  jsonObj.optJSONObject("meta");
                            if(metaobj!=null)
                            {
                                String type = metaobj.optString("type");
                                Log.i(TAG,"type:"+type);
                                int i ,code = 0;
                                for (i = 0;i<actionArrayList.size();i++)
                                {
                                    if(actionArrayList.get(i).type.compareTo(type)==0)
                                    {
                                        code = actionArrayList.get(i).code;
                                        break;
                                    }
                                }
                                Log.i(TAG,"out->"+code);

                                switch (code)
                                {
                                    case 1:
                                        workerLogoin(jsonObj);
                                        break;
                                    case 2:
                                       // workerLogoout(jsonObj);
                                        break;
                                    case 3:
                                        hangerRealinfo(jsonObj);
                                        break;
                                    case 4:
                                        workerTailorEfficiency(jsonObj);
                                        break;
                                    case 5:
                                        break;
                                    case 6:
                                        hangerStatue(jsonObj);
                                        break;
                                    case 7:
                                        orderPlanInfo(jsonObj);
                                        break;
                                    case 8:
                                    case 9:
                                        cutPlanOverSuccess(jsonObj);
                                        break;
                                    case 10:
                                        syncStateInTime(jsonObj);
                                        break;
                                    default:
                                        Log.i(TAG,"out-> not found"+code);
                                }
                            }
                        }
                    }
/*                    catch (MqttException mqte)
                    {
                        Log.i(TAG,mqte.toString());
                    }*/
                    catch (JSONException e)
                    {
                        Log.i(TAG,e.toString());
                    }

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "delivery Complete ");//即服务器成功delivery消息
                MessageToUi.send(Constant.MQTT_CON_STATE,"mq.statue","connect",Config.MainActivityHandler);

            }

        };
    }
    public static void workerLogoin(JSONObject jsonObj)
    {
        try
        {

        }
        catch (Exception e)
        {
            Log.i(TAG,e.toString());
        }
    }


    public static void syncStateInTime(JSONObject jsonObj)
    {
        try
        {

        }
        catch (Exception e)
        {
            Log.i(TAG,e.toString());
        }
    }



    public static void hangerRealinfo(JSONObject jsonObj)
    {
        try
        {

        }
        catch (Exception e)
        {
            Log.i(TAG,e.toString());
        }
    }

    public static void orderPlanInfo(JSONObject jsonObj)
    {
        try
        {

        }
        catch (Exception e)
        {
            Log.i(TAG,e.toString());
        }
    }
    public static void workerTailorEfficiency(JSONObject jsonObj)
    {
        try
        {

        }
        catch (Exception e)
        {
            Log.i(TAG,e.toString());
        }
    }


    public static void hangerStatue(JSONObject jsonObj)
    {
        try
        {

        }
        catch (Exception e)
        {
            Log.i(TAG,e.toString());
        }
    }

    public static void cutPlanOverSuccess(JSONObject jsonObj)
    {
        try
        {

        }
        catch (Exception e)
        {
            Log.i(TAG,e.toString());
        }
    }

    public static synchronized void connectBroker()
    {
       try
       {
           persistence = new MemoryPersistence();
           broker="tcp://"+Config.mqttServerAddr+":1883";
           clientId=Config.devId+"(v"+Config.ver+")#:"+Config.getUniquePsuedoID().replace("-","");
           myTopic = Config.devId;
           myClient = new MqttClient(broker, clientId, persistence);
           myClient.setCallback(mqttCallback);
           myClient.connect(connOpts);
       }
       catch(MqttException me)
       {
           Log.i(TAG,"reason "+me.getReasonCode());
           Log.i(TAG,"msg "+me.getMessage());
           Log.i(TAG,"loc "+me.getLocalizedMessage());
           Log.i(TAG,"cause "+me.getCause());
           Log.i(TAG,"excep "+me);
           me.printStackTrace();
           myClientConn = false;
           MessageToUi.send(Constant.MQTT_CON_STATE,"mq.statue","disconnect",Config.MainActivityHandler);
       }
    }

    public static synchronized void disConnectBroker()
    {
        try
        {
            if(myClient!=null)
            {
                myClient.disconnect();
                myClientConn = false;
            }
        }
        catch (MqttException ee)
        {
            Log.i(TAG,"excep "+ee);
        }
    }

    public static synchronized void publishBroker(String topic, String s)
    {
        MqttMessage msg=new MqttMessage();
        String msgStr=s;
        msg.setPayload(msgStr.getBytes());//设置消息内容
        msg.setQos(qos);//设置消息发送质量，可为0,1,2.
        msg.setRetained(false);//服务器是否保存最后一条消息，若保存，client再次上线时，将再次受到上次发送的最后一条消息。
        try
        {
            if(myClientConn||myClient.isConnected())
                myClient.publish(topic,msg);//设置消息的topic，并发送。
        }
        catch (MqttPersistenceException e)
        {
            Log.i(TAG,"Persistenceexcep "+e);
        }
        catch (MqttException ee)
        {
            Log.i(TAG,"excep "+ee);
        }
    }


    public void run()
    {
        String brokerIp = Config.mqttServerAddr;
        String devid = Config.devId;
        try
        {
            initMqttOptions();
            initMqttCallback();
            connectBroker();
            sleep(8000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        while (true)
        {
            try
            {
                if(Stop)
                {
                    this.disConnectBroker();
                    return;
                }

                if(myClientConn==false||myClient.isConnected()==false) //断开重连
                {
                    MessageToUi.send(Constant.MQTT_CON_STATE,"mq.statue","disconnect",Config.MainActivityHandler);
                    Log.i(TAG,"mqttclient reconnect。。。。。");
                    initMqttOptions();
                    initMqttCallback();
                    connectBroker();
                    Thread.sleep(8000);
                }
                if(brokerIp.compareTo(Config.mqttServerAddr)!=0
                        ||devid.compareTo(Config.devId)!=0)  //设备编号以及mqtt 修改后重新连接
                {
                    brokerIp = Config.mqttServerAddr;
                    devid = Config.devId;
                    Log.i(TAG,"mqttclient reconnect as brokerIp or devid changed");

                    this.disConnectBroker();
                    MessageToUi.send(Constant.MQTT_CON_STATE,"mq.statue","disconnect",Config.MainActivityHandler);

                  //  myClient.close();
                }
               Thread.sleep(1000);
               // Log.i(TAG,"myClientConn:"+myClientConn);

            }
            catch (InterruptedException e)
            {
                Log.e(TAG,"Thread "  + " interrupted.");
                e.printStackTrace();
                Log.e(TAG,"Thread1 "  + e.toString());
            }
            catch (Exception t)
            {
                t.printStackTrace();
                Log.e(TAG,"Thread2 "  + t.toString());
            }
        }
    }

    public final class Action
    {
        public String type;
        public int code;
        public Action(String type, int code)
        {
            this.type = type;
            this.code = code;
        }
    }
}
