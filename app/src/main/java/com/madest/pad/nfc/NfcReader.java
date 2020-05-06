package com.madest.pad.nfc;

import android.app.PendingIntent;
import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Handler;

public final class NfcReader
{
    private static String TAG = "NfcReader";
    public static  NfcAdapter mNfcAdapter = null;
    public static  PendingIntent mPendingIntent = null;
    public static Context context =null ;
    public static Handler handler = null;



  /*  public void NfcReader(Context context, Handler handler)
    {
        context = context;
        handler = handler;
    }*/


    public static  String readMifareUltralight(Tag tag){
        MifareUltralight mifare=MifareUltralight.get(tag);
        try
        {
            if(mifare == null)
                return null;

            mifare.connect();
            byte[] payload=mifare.readPages(4);
            String data= new String(payload, Charset.forName("GB2312"));
            Log.d(TAG, data);

        } catch (IOException e) {
            Log.e(TAG, "IOException while reading MifareUltralight message...", e);
        } finally {
            if (mifare!=null){
                try {
                    mifare.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing tag...", e);
                }
            }
        }
        return null;
    }

    public static  String readNdeftag(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef != null)
        {
            //获取tag的type是第几
//        String tagType=ndef.getType();
            try
            {
                if(ndef == null)
                    return null;

                ndef.connect();
                NdefMessage ndefMessage = ndef.getNdefMessage();
                if (ndefMessage != null)
                {
                   // mReadText.setText(parseTextRecord(ndefMessage.getRecords()[0]));
                    Toast.makeText(context, "成功", Toast.LENGTH_SHORT).show();
                } else
                {
                   // mReadText.setText("该标签为空标签");
                    Toast.makeText(context, "该标签为空标签", Toast.LENGTH_SHORT).show();

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            } finally {
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            readMifareUltralight(tag);
        }
        return  null;
    }
    public static String parseTextRecord(NdefRecord ndefRecord) {
        //判断TNF
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }
        //判断长度和类型
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 0X80) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0X3f;
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            String textRecord = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
            return textRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public static String bytesToHexString(byte[] src)
    {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit(src[i] >>> 4 & 0X0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0X0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();

    }



}
