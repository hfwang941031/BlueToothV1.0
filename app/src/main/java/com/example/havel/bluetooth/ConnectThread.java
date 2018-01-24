package com.example.havel.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by 王海峰 on 2018/1/22.
 */

public class ConnectThread extends Thread {
    private  BluetoothDevice device;
    private  BluetoothSocket clientSocket;
    private final UUID MY_UUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter adapter;
    private String address;
       public ConnectThread(BluetoothAdapter adapter,String address)
    {
        this.adapter=adapter;
        this.address=address;
        device=null;
        clientSocket=null;
        Log.d("TAG", "client thread has started");
    }
//检测是否在搜索，如果是，取消搜索
    public  void checkConnectState(BluetoothAdapter adapter)
    {
        if (adapter.isDiscovering())
        {
            adapter.cancelDiscovery();
        }
    }
    @Override
    public void run() {
        if (device==null)
        {
            device=adapter.getRemoteDevice(address);
            Log.d("TAG", device.getName()+device.getAddress());
        }
        if (clientSocket==null)
        {
            try {

                clientSocket=device.createRfcommSocketToServiceRecord(MY_UUID);
                clientSocket.connect();
                Log.d("TAG", "创建socket成功");
            } catch (IOException e) {
                Log.d("TAG", "创建socket失败");
            }
        }
        try {
            OutputStream os=clientSocket.getOutputStream();
            Log.d("TAG", "创建输出流对象成功");
            if (os!=null)
            {
                String msg = "这是来自另一个手机的信息";
                os.write(msg.getBytes("utf-8"));
            }
        } catch (IOException e) {
            Log.d("TAG", "创建输出流对象失败");
        }
    }
}
