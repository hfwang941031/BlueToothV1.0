package com.example.havel.bluetooth;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.btnSearch)
    Button btnSearch;
    @BindView(R.id.tbtnSwitch)
    ToggleButton tbtnSwitch;
    @BindView(R.id.lvDevices)
    ListView lvDevices;
    public ArrayAdapter<String> adapter;
    @BindView(R.id.linearlaout1)
    LinearLayout linearlaout1;
    @BindView(R.id.btnExit)
    Button btnExit;
    private BluetoothAdapter bluetoothAdapter;
    public Context context;
    private boolean btIsOpen = false;
    public static final int REQUEST_OPEN = 0X01;
    public static final String TAG = "BLUETOOTH";
    private ArrayList<String> list = new ArrayList<String>();
    private Set<BluetoothDevice> bondDevices;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String NAME = "my bluetooth";

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(MainActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, list);
        lvDevices.setAdapter(adapter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(searchReceiver, intent);
        if (bluetoothAdapter == null)
        {
            showToast("该设备不支持蓝牙功能");
            return;
        }
        new AcceptThread().start();
        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String s = adapter.getItem(i);
                String address=s.substring(s.indexOf(",")+1).trim();
                ConnectThread connectThread = new ConnectThread(bluetoothAdapter,address);
                connectThread.checkConnectState(bluetoothAdapter);
                Log.d("TAG", "connected state has checked");
                connectThread.start();
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(searchReceiver);
        EventBus.getDefault().unregister(MainActivity.this);
    }
    @Subscribe(threadMode =ThreadMode.MAIN)
    public void onMessageEvent(EventText eventtext)
    {
        Toast.makeText(this,eventtext.getEventText(), Toast.LENGTH_SHORT).show();
    }

   /* private Handler handler=new Handler(){
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            Toast.makeText(MainActivity.this,String.valueOf(msg.obj),Toast.LENGTH_LONG).show();
        }
    };*/
    private class AcceptThread extends Thread{
        private BluetoothServerSocket severSocket;
        private InputStream is;
        public AcceptThread()
        {
            try
            {
                severSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                Log.d("TAG", "severSocket创建成功");
            }
            catch (Exception e)
            {
                Log.d("TAG", "severSocket创建失败");
            }
        }
        public void run()
        {
            try
            {
                BluetoothSocket socket=severSocket.accept();
                Log.d("TAG", "服务监听中··");
                is=socket.getInputStream();
                while(true)
                {
                    try
                    {
                        byte[] buffer = new byte[128];
                        int count=is.read(buffer);
                        EventText eventext = new EventText(new String(buffer,0,count,"utf-8"));
                        EventBus.getDefault().post(eventext);
                        //Message msg=new Message();
                        //msg.obj = new String(buffer, 0, count, "utf-8");
                        //handler.sendMessage(msg);
                    }
                    catch (IOException e)
                    {
                        Log.d("TAG","接收线程出问题了");
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    @OnClick({R.id.btnSearch, R.id.tbtnSwitch,R.id.btnExit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnSearch: {
                checkBondedDevices();
            }
            break;
            //查找已配对的设备
            case R.id.btnExit:
                MainActivity.this.finish();
                break;
            case R.id.tbtnSwitch: {
                if (tbtnSwitch.isChecked() == true) {
                    Intent openBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(openBlueTooth, REQUEST_OPEN);
                } else if (tbtnSwitch.isChecked() == false) {
                    bluetoothAdapter.disable();
                }
            }
            break;
        }
    }
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN) {
            if (requestCode == RESULT_CANCELED)
                showToast("请求失败");
            else {
                showToast("请求成功");
            }
        }
    }
    //以上是打开蓝牙操作
    public void checkBondedDevices() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        list.clear();
        bondDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondDevices) {
            String str =device.getName() + "," + device.getAddress();
            list.add(str);
            adapter.notifyDataSetChanged();
        }
        bluetoothAdapter.startDiscovery();
    }
    private final BroadcastReceiver searchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = null;
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    //Toast.makeText(context, device.getName() + "", Toast.LENGTH_LONG).show();
                    String str = "未配对完成  " + "名称："+device.getName() + " ," + device.getAddress();
                    if (list.indexOf(str) == -1)
                        list.add(str);
                }
                adapter.notifyDataSetChanged();
            }
        }
    };

}

