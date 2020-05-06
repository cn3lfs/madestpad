package com.madest.pad.update;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by ll on 2020-04-28.
 */
public  class VersionInfo implements Parcelable {

    public String ver ;
    public String fileurl;
    public String apkname;

    public  long filesize ;


    public VersionInfo()
    {
        ver = null;
        fileurl = null;
        apkname = null;
        long filesize;
    }
    public  VersionInfo(Parcel in)
    {
        //顺序要和writeToParcel写的顺序一样
        ver = in.readString();
        fileurl =in.readString();
        apkname = in.readString();
        filesize = in.readLong();
    }
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(ver);
        dest.writeString(fileurl);
        dest.writeString(apkname);
        dest.writeLong(filesize);
    }

    public static final Parcelable.Creator<VersionInfo> CREATOR = new Parcelable.Creator<VersionInfo>() {
        public VersionInfo createFromParcel(Parcel in) {
            return new VersionInfo(in);
        }

        public VersionInfo[] newArray(int size) {
            return new VersionInfo[size];
        }
    };
}
