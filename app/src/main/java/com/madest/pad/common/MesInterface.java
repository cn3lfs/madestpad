package com.madest.pad.common;

public final class MesInterface {

    public static String TAG = MesInterface.class.getSimpleName();


    //原呆挂升级接口的接口
    public static String getUpdateInfo(String web_addr,String upType)
    {
        try
        {
            String url = "http://"+web_addr+"/rest/core/pad/upgradeInfo?up_type="+upType;
            return  url;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }
}
