package com.madest.pad.tcp;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by ll on 2019/09/21.
 */

public class TcpClientThread extends Thread {

    //IP地址
    private String address;
    //端口
    private int port;
    //发送内容
    private String msg;
    private Handler mHandler;

    public TcpClientThread(Handler handler, String address, int port, String msg) {
        this.mHandler = handler;
        this.address = address;
        this.port = port;
        this.msg = msg;
    }

    @Override
    public void run()
    {
        super.run();
        sendSocket();
    }

    /**
     * 设置
     */
    private void sendSocket()
    {
        InputStreamReader reader = null;
        BufferedReader bufReader = null;
        Socket socket = null;
        try
        {
            //1.创建监听指定服务器地址以及指定服务器监听的端口号
            //IP地址，端口号
            socket = new Socket(address, port);
            // 2.拿到客户端的socket对象的输出流发送给服务器数据
            OutputStream os = socket.getOutputStream();
            //写入要发送给服务器的数据
            os.write(msg.getBytes("GBK"));
            os.flush();
            socket.shutdownOutput();
            //拿到socket的输入流，这里存储的是服务器返回的数据
            InputStream is = socket.getInputStream();
            //解析服务器返回的数据
            reader = new InputStreamReader(is);
            bufReader = new BufferedReader(reader);
            String s = null;
            final StringBuffer sb = new StringBuffer();
            while ((s = bufReader.readLine()) != null) {
                sb.append(s);
            }
            sendMsg(0, sb.toString());
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        { //3、关闭IO资源
            try
            {
                if (bufReader != null)
                    bufReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try
            {
                if (socket != null)
                    socket.close();
            }catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 发送消息
     */
    private void sendMsg(int what, Object object) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = object;
        mHandler.sendMessage(msg);
    }
}