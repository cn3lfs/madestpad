package com.madest.pad;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.madest.pad.common.Config;
import com.madest.pad.common.Constant;
import com.madest.pad.common.HttpGetThread;
import com.madest.pad.common.MesInterface;
import com.madest.pad.common.MessageToUi;
import com.madest.pad.common.MyToast;
import com.madest.pad.javascriptInterface.MyJavascriptInterface;
import com.madest.pad.mqttclient.MyMqttClient;
import com.madest.pad.nfc.NfcReader;
import com.madest.pad.rfid.RfidData;
import com.madest.pad.rfid.SerialTask;
import com.madest.pad.tcp.TcpClientThread;
import com.madest.pad.update.CommonProgressDialog;
import com.madest.pad.update.DownloadTask;
import com.madest.pad.update.VersionInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public static SerialTask serialtask = null;
    public ActivityHandler handler = null;
    public Context context = null;
    private WebView webView = null;

    public MyMqttClient myMqttClient = null;


    public String[] permissions = new String[]
            {
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH
            };
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
    List<String> mPermissionList = new ArrayList<>();

    // Used to load the 'native-lib' library on application startup.
  /*  static {
        System.loadLibrary("native-lib");
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //强制横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();
        handler = new ActivityHandler();

        getAppPermission();

        Config.init(handler,context);//初始化apk 需要的参数值
        Config.MainActivityHandler = handler ;
        initReadCardTask();
        initWebView();

        /* 需要使用请打开
        myMqttClient = new MyMqttClient();
        myMqttClient.start();
        */
        getAppVersion();

    }

    private void getAppPermission()
    {
        if (Build.VERSION.SDK_INT > 22) //android 6.0以后需要用户手动确认
        {
            mPermissionList.clear();
            for (int i = 0; i < permissions.length; i++)
            {
                if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (mPermissionList.isEmpty())
            {
                //未授予的权限为空，表示都授予了
                Toast.makeText(MainActivity.this, "已经授权", Toast.LENGTH_LONG).show();
            } else {//请求权限方法
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 2);
            }
        }
    }

    private void getAppVersion()
    {
        //开启一个线程处理耗时的操作
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i(TAG, "10s后检测app版本.....");
                    Thread.sleep(10*1000); //延时10 s
                    String url = MesInterface.getUpdateInfo(Config.webServerAddr,"t10-dg");
                    String arg[] = new String[1];//default arg
                    HttpGetThread thread = new HttpGetThread(handler, context, url, 0, "getUpdateInfo", arg);
                    thread.start();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    private void initWebView()
    {

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);// 打开本地缓存提供JS调用,至关重要
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 12);// 实现8倍缓存
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);

        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        // webView.getSettings().setSupportZoom(true);
        // webView.getSettings().setBuiltInZoomControls(true);
        //webView.setInitialScale(100);
        //webView.getSettings().setDefaultFontSize(15);
        String appCachePath = getApplication().getCacheDir().getAbsolutePath();
        // String appCachePath = Config.webViewCachePath;


        webView.getSettings().setAppCachePath(appCachePath);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);


        //优先使用缓存
        //webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        //添加javaScript接口

         webView.addJavascriptInterface(new MyJavascriptInterface(handler,context), "madestpad");
        // final String serverIp = "http://192.168.100.72:3000/";
        //String serverIp = "http://"+Config.devConfig.web_addr;
        //String serverIp = "http://"+"192.168.100.99:8888/rest/core/show/tv";

        // String serverIp = "http://192.168.100.99:8888/admin/login";
        //String serverIp = "http://192.168.100.99:8888/admin/login";

        //  String serverIp = "https://www.163.com/";
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url)
            {
                // Log.i(TAG, "onLoadResource url="+url);
                super.onLoadResource(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webview, String url)
            {
                webview.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {


            }
        });



 /*       String path ;
        path = "";
        Log.i(TAG,serverIp);
        String url = serverIp + path */;

        webView.loadUrl("file:///android_asset/www/test.html");
        Log.i(TAG,"=====================================");
    }

    private void initReadCardTask()
    {

        String deviceModel = Build.MODEL; // 设备型号
        Log.i(TAG,deviceModel);
        if(deviceModel.indexOf("madest")!=-1)  //a33 不带NFC
        {
            //适用于A33直板型10寸平板 默认带RFID模块
            serialtask = new SerialTask(handler, this);
            int fd = serialtask.fd;
            if (fd > 0)
            {
                Log.i("serial-task", Integer.toString(fd));
                serialtask.start();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示");
                builder.setMessage("读卡设备打开失败!");
                builder.setPositiveButton("是", null);
                builder.show();
            }
        }
        else  //带NFC 3288 L型平板 默认带NFC模块
        {
            NfcReader.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (NfcReader.mNfcAdapter == null) //不带NFC
            {
                Toast.makeText(this, "该设备不支持nfc", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (!NfcReader.mNfcAdapter.isEnabled())
            {
                Toast.makeText(this, "请打开nfc开关", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                startActivity(intent);
            }

            //创建PendingIntent对象，当检查到一个tag标签就会执行此Intent
            NfcReader.mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
        }

    }


    //发送语音给音箱 example
    public void sendMsgToloudSpeakerBox( int flag)
    {

        try
        {
            JSONObject data = new JSONObject();
            data.put("pkgtype","ttsplay");
            data.put("msgtype","call");


            String des = "需要工艺指导";

            String role = "组长";
            switch (flag)
            {
                case 1:
                    role = "组长";
                    break;
                case 2:
                    role = "机修";
                    break;
                case 3:
                    role = "物料";
                    break;
                default:
                    break;
            }
            String s = "1101-01号工位呼叫"+role+des;
            data.put("note",s);
            data.put("volume","22");

            TcpClientThread mThread = new TcpClientThread(handler, "192.168.100.220", 5000,
                    data.toString());
            mThread.start();
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        String msg = "{\"pkgtype\":\"ttsplay\",\"msgtype\":\"call\",\"note\":\"1101-01号工位 呼叫组长\",\"volume\":\"12\"}";
    }


    @Override
    public void onStart()
    {
        //  MyToast.showToast(context,"app运行onStart...");
        Log.i(TAG,"运行中onStart....！");
        super.onStart();
    }

    @Override
    public void onStop()
    {
        //  MyToast.showToast(context,"app后台禁止运行onStop,将自动退出...");
        Log.i(TAG,"后台运行中onStop....！");
        try
        {
          /*  if(serialtask !=null)
            {
                serialtask.SerialPortClose();
            }*/


    /*        if(timer!=null)
                timer.cancel();*/


         /*   if(webView!=null)
                webView.destroy();*/

         /*   if(myMqttClient!=null)
            {
                myMqttClient.onStop();
            }*/

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        super.onStop();
    }
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        //取出标签
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String techList[] = tag.getTechList();
        byte[] bytesId = tag.getId();

        String tagId = NfcReader.bytesToHexString(bytesId);
        Log.d(TAG, "这里是tagId:"+tagId);
        // 这里通过NFC获取到卡片的ID号进行业务处理(显示或者其他。。。)
       // mTagIdText.setText(tagId);
        for (String tech : techList)
        {
            System.out.print(tech);
            Log.d(TAG, tech);
        }
        NfcReader.readNdeftag(tag);
    }



    private  static  String barcode ="";
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        String code;
        Log.i(TAG,"##"+event.toString());
        {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    &&event.getKeyCode() != KeyEvent.KEYCODE_ENTER
                    &&event.getKeyCode()!=KeyEvent.KEYCODE_SHIFT_LEFT
                    &&event.getKeyCode()!=KeyEvent.KEYCODE_SHIFT_RIGHT)
            //过滤掉换行符,以及键盘shfit键,其他特殊字符按实际自行过滤
            {
                Log.i(TAG, "dispatchKeyEvent: " + event.toString());
                char pressedKey = (char) event.getUnicodeChar();
                barcode += pressedKey;
            }
            if ((event.getAction() == KeyEvent.ACTION_DOWN)
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
            {
                Log.i(TAG, "BARCODE:" + barcode);
                code = barcode;
                MessageToUi.send(Constant.QRCODE_0, "qrcode", code,handler);

                barcode = "";
            }
        }
        return super.dispatchKeyEvent(event);
    }


    /**
     * 接受消息，处理消息 ，此Handler会与当前主线程一块运行
     * */
    //自定义handler类
    class ActivityHandler extends Handler {
        @Override
        //接收别的线程的信息并处理
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            try
            {
                Log.i(TAG,"handler arg:"+msg.arg1) ;
                if (isFinishing() || isDestroyed())
                {
                    return;
                }

                switch (msg.arg1)
                {
                    case 1:

                        break;
                    case Constant.MQTT_CON_STATE:
                    {
                        bundle=msg.getData();
                        String state = bundle.get("mq.statue").toString();
                        if(state.compareTo("connect")==0)
                        {
                            Log.i(TAG,"mqtt 已经ok!");
                           //
                        }
                        if(state.compareTo("disconnect")==0)
                        {
                            Log.i(TAG,"mqtt 已经断开.");
                            //
                        }
                    }
                    break;
                    case Constant.RFID_READ: {
                        bundle = msg.getData();
                        RfidData tmp = bundle.getParcelable("rfiddata");
                        Log.i(TAG, tmp.id);

                        // 这里通过rfid获取到卡片的ID号进行业务处理(显示或者其他。。。)
                        Toast.makeText(context, tmp.id, Toast.LENGTH_SHORT).show();

                        break;
                    }

                   case Constant.UPDATE_INFO: //update software
                   {
                        bundle = msg.getData();
                        VersionInfo tt = bundle.getParcelable("softupdate");
                        String content = "有新版本发布: " + tt.ver;
                        String apkname = "" + tt.apkname;
                        long apkSize = tt.filesize;
                        String apkSavePath = Config.apkUpadetSavePath;
                        Config.APKNAME = apkname;

                        Log.i(TAG,content+" "+apkname+" "+apkSize+" "+apkSavePath);
                        //String url = "http://192.168.100.99"+tmp.fileurl; //ic-mes
                        String url = "http://" + Config.webServerAddr + tt.fileurl;
                        ShowDialog(0, "", content, url, apkname, apkSize, apkSavePath);
                    }
                    break;
                    case Constant.UPDATE_TIP:
                    {
                        bundle = msg.getData();
                        String info = bundle.getString("info");
                        MyToast.showToast(context, info);
                        break;
                    }

                    case Constant.APK_INSTALL: //apk下载完成后进行安装
                    {
                        bundle=msg.getData();
                        String par = bundle.get("par").toString();
                        Log.i(TAG,par);
                        if(par.compareTo("install")==0)
                        {
                            //安装应用
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            intent.setDataAndType(Uri.fromFile(new File(Config.apkUpadetSavePath, Config.APKNAME)),
                                    "application/vnd.android.package-archive");
                            startActivity(intent);
                        }
                    }
                    break;
                    case Constant.QRCODE_0:
                    {
                        bundle = msg.getData();
                        String mmCode = bundle.get("qrcode").toString();
                        Log.i(TAG,"qrcode:"+mmCode);

                    }
                    break;

                    case Constant.EXCEPTION_EROR:
                    {
                        bundle = msg.getData();
                        String info = bundle.getString("exception");
                        Log.i(TAG,info);
                        MyToast.showToast(context, info);
                    }
                    break;

                    default:
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    /**
     * 升级app 提示框
     *
     * @param content
     * @param url
     */
    private void ShowDialog(int vision, String newversion, final String content,
                            final String url,final String apkName,final long apkSize,final String apkSavePath) {

        new android.app.AlertDialog.Builder(this)
                .setTitle("版本更新")
                .setMessage(content)
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        CommonProgressDialog pBar = new CommonProgressDialog(MainActivity.this);
                        pBar.setCanceledOnTouchOutside(false);
                        pBar.setTitle("正在下载");
                        pBar.setCustomTitle(LayoutInflater.from(
                                MainActivity.this).inflate(
                                R.layout.title_dialog, null));
                        pBar.setMessage("正在下载");
                        pBar.setIndeterminate(true);
                        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        pBar.setCancelable(true);
                        final DownloadTask downloadTask = new DownloadTask(
                                context,handler,apkSize,apkSavePath,apkName,pBar);
                        Log.i(TAG,"APK down loading from: "+url);
                        downloadTask.execute(url);
                        pBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                downloadTask.cancel(true);
                            }
                        });

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
   // public native String stringFromJNI();
}

