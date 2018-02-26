package com.example.homepc.roblue;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Bundle;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {

    ImageButton b1;
    ImageButton b2;
    ImageButton b3;
    ImageButton b4;

    int isOff1 = 0, isOff2 = 0, isOff3 = 0, isOff4 = 0;

    LinearLayout ll1;
    LinearLayout ll2;

    TextView tv;
    ArrayAdapter<String> listAdapter;
    ListView listView;
    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int REQUEST_COARSE_LOCATION = 5;
    protected static final int MESSAGE_READ = 1;
    IntentFilter filter;
    BroadcastReceiver receiver;
    ConnectedThread connectedThread;
    String tag = "debugging";


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.i(tag, "in handler");
            super.handleMessage(msg);
            Toast.makeText(getApplicationContext(), "IN HANDLER...\nmsg : " + msg, Toast.LENGTH_SHORT).show();
            switch (msg.what) {
                case SUCCESS_CONNECT:
                    // DO something
                    connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);
                    //      Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_SHORT).show();
                    //  listView.setVisibility(View.INVISIBLE);
                    ll2.setVisibility(View.INVISIBLE);
                    ll1.setVisibility(View.VISIBLE);

                    tv.setText("Lets Race.......");
                    String s = "success";
                    connectedThread.write(s.getBytes());
                   Log.i(tag, "connected");
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init();
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		
        if(btAdapter==null){
            Toast.makeText(getApplicationContext(), "No bluetooth detected", Toast.LENGTH_SHORT).show();
            finish();
        }
        else{
            if(!btAdapter.isEnabled()){
                Toast.makeText(getApplicationContext(), "Turning On BT", Toast.LENGTH_SHORT).show();
                turnOnBT();
            }
            //Toast.makeText(getApplicationContext(), "Search for paired Devices", Toast.LENGTH_SHORT).show();
            getPairedDevices();
            //Toast.makeText(getApplicationContext(), "Start Discovery", Toast.LENGTH_SHORT).show();
            checkLocationPermission();
        }


    }



    private void startDiscovery() {
        // TODO Auto-generated method stub
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();

    }

    private void turnOnBT() {
        // TODO Auto-generated method stub
        Intent intent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }

    private void getPairedDevices() {
        // TODO Auto-generated method stub
        devicesArray = btAdapter.getBondedDevices();
        int i = 0;
        String nm = "";
        if(devicesArray.size()>0){
            for(BluetoothDevice device:devicesArray){
				nm = device.getName().toString();
				if(nm.equals("Roblue"){                         <----------------------------------Add proper condition here
				       pairedDevices.add(device.getName());    
                }				
                
                i++;
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "No Paired Device Found", Toast.LENGTH_SHORT).show();
        }
        //Toast.makeText(getApplicationContext(), "Total "+i+" Device Found\n"+nm, Toast.LENGTH_SHORT).show();
    }


    private void init() {
        // TODO Auto-generated method stub
        ll1 = (LinearLayout)findViewById(R.id.ll1);
        ll2 = (LinearLayout)findViewById(R.id.ll2);

        tv = (TextView)findViewById(R.id.tvPD);

        btn_Init();

        listView=(ListView)findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
        listView.setAdapter(listAdapter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        devices = new ArrayList<BluetoothDevice>();
        receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                String action = intent.getAction();
                //Toast.makeText(getApplicationContext(), "RECEIVE : "+action, Toast.LENGTH_SHORT).show();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    Toast.makeText(getApplicationContext(), "Device : "+device.getName().toString(), Toast.LENGTH_SHORT).show();
                    String s = "";
                    for(int a = 0; a < pairedDevices.size(); a++){
                        if(device.getName().equals(pairedDevices.get(a))){
                            //append
                            s = "(Paired)";
                            break;
                        }
                    }
                    listAdapter.add(device.getName()+" "+s+" "+"\n"+device.getAddress());
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                    // run some code
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    // run some code
                }
                else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if(btAdapter.getState() == btAdapter.STATE_OFF){
                        turnOnBT();
                    }
                }
            }
        };

        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }

    protected void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
        }
        else{
            startDiscovery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION: {
                if (grantResults.length > 0&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDiscovery(); // --->
                } else {
                    //TODO re-request
                }
                break;
            }
        }
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }



    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                            long arg3) {
        // TODO Auto-generated method stub

        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
        if (listAdapter.getItem(arg2).contains("Paired")) {

            BluetoothDevice selectedDevice = devices.get(arg2);
            ConnectThread connect = new ConnectThread(selectedDevice);
            connect.start();
            Log.i(tag, "in click listener");
        } else {
            Toast.makeText(getApplicationContext(), "device is not paired", Toast.LENGTH_SHORT).show();
        }
    }



    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.i(tag, "construct");
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server codeN
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.i(tag, "get socket failed");

            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();
            Log.i(tag, "connect - run");
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                Log.i(tag, "connect - succeeded");
            } catch (IOException connectException) {	Log.i(tag, "connect failed");
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)

            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }



        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    boolean btn_Init(){
        b1 =(ImageButton)findViewById(R.id.iB1);
        b1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        connectedThread.write("A".getBytes());
                        b1.setImageResource(R.drawable.btn2);
                        break;
                    case MotionEvent.ACTION_UP:
                        connectedThread.write("B".getBytes());
                        b1.setImageResource(R.drawable.btn1);
                        break;
                }
                return false;
            }
        });

        b2 =(ImageButton)findViewById(R.id.iB2);
        b2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        connectedThread.write("C".getBytes());
                        b2.setImageResource(R.drawable.btn2);
                        break;
                    case MotionEvent.ACTION_UP:
                        connectedThread.write("D".getBytes());
                        b2.setImageResource(R.drawable.btn1);
                        break;
                }
                return false;
            }
        });

        b3 =(ImageButton)findViewById(R.id.iB3);
        b3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        connectedThread.write("E".getBytes());
                        b3.setImageResource(R.drawable.btn2);
                        break;
                    case MotionEvent.ACTION_UP:
                        connectedThread.write("F".getBytes());
                        b3.setImageResource(R.drawable.btn1);
                        break;
                }
                return false;
            }
        });

        b4 =(ImageButton)findViewById(R.id.iB4);
        b4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        connectedThread.write("G".getBytes());
                        b4.setImageResource(R.drawable.btn2);
                        break;
                    case MotionEvent.ACTION_UP:
                        connectedThread.write("H".getBytes());
                        b4.setImageResource(R.drawable.btn1);
                        break;
                }
                return false;
            }
        });
        return false;
    }
    /*
    void buttonListner(){

        left = (Button)findViewById(R.id.left);
        left.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                connectedThread.write("L".getBytes());
            }
        });

        right = (Button)findViewById(R.id.right);
        right.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                connectedThread.write("R".getBytes());
            }
        });

        forw = (Button)findViewById(R.id.forw);
        forw.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                connectedThread.write("F".getBytes());
            }
        });

        bakw = (Button)findViewById(R.id.bakw);
        bakw.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                connectedThread.write("B".getBytes());
            }
        });

        stop = (Button)findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                connectedThread.write("S".getBytes());
            }
        });
    }*/
    void delay(){
        int i = 0;
        i++;
        i--;
    }
}



