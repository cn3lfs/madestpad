package com.madest.pad.common;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by ll on 2018-08-30.
 */

public class MyToast {
    private static String TAG = "MyToast";
    private static Toast toast = null;
    private static Toast toast2 = null;

    public static void showToast(Context context,
                                 String content) {
        try {

            if(content.length()==0&&content==null)
                return;


            if (toast == null) {
                toast = Toast.makeText(context,
                        content,
                        Toast.LENGTH_SHORT);
            } else {
                toast.setText(content);
            }
            toast.show();
        }
        catch (Exception io)
        {
            io.printStackTrace();
        }
    }


    public static void showToastOnThread(Context context,
                                 String content) {
        try
        {

            if(content.length()==0&&content==null)
                return;

            Looper.prepare();
            if (toast == null) {
                toast = Toast.makeText(context,
                        content,
                        Toast.LENGTH_SHORT);
            } else {
                toast.setText(content);
            }
            toast.show();
            Looper.loop();
        }
        catch (Exception io)
        {
            io.printStackTrace();
        }
    }




/*    public static void showMoreLongToast(Context context,
                                 String content) {
        try {

            if(content.length()==0&&content==null)
                return;
            toast2 = Toast.makeText(context,
                        content,
                        Toast.LENGTH_LONG);
            toast2.show();
        }
        catch (Exception io)
        {
            io.printStackTrace();
        }
    }*/


    public static void showLongToastOnThead(Context context,
                                 String content) {
        try {

            Looper.prepare();
            if (toast == null) {
                toast = Toast.makeText(context,
                        content,
                        Toast.LENGTH_LONG);
            } else {
                toast.setText(content);
            }
            toast.show();
            Looper.loop();
        }
        catch (Exception io)
        {
            io.printStackTrace();
        }

    }

    public static void showLongToast(Context context,
                                     String content) {
        try {


            if (toast == null) {
                toast = Toast.makeText(context,
                        content,
                        Toast.LENGTH_LONG);
            } else {
                toast.setText(content);
            }
            toast.show();

        }
        catch (Exception io)
        {
            io.printStackTrace();
        }

    }


}
