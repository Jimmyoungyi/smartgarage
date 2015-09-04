package com.jimmyoungyi.bluetoothmytry;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    LinearLayout dbList;
    Button bBtn;
    TextView bText;
    BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
    static final int bluetoothcode = 125;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bBtn = (Button) findViewById(R.id.bbtn);
        bText = (TextView) findViewById(R.id.btext);
        dbList = (LinearLayout) findViewById(R.id.device_button_list);
        if(!bAdapter.isEnabled()){
            Intent bintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(bintent);
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        bBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bAdapter.startDiscovery();
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bText.setText(bText.getText() + "\n" + device.getName() + device.getAddress());
                final Button device_item_btn = new Button(getBaseContext());
                System.out.println(device.getName());
                //if (device.getName().equalsIgnoreCase(name)) {
                    bAdapter.cancelDiscovery();
                    int connectState = device.getBondState();
                    switch (connectState) {
                        case BluetoothDevice.BOND_NONE:

                            try {
                                bText.setText(bText.getText() + "\n" + "not match");
                                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                                createBondMethod.invoke(device);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            try {
                                bText.setText(bText.getText() + "\n" + "match");
                                connect(device);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                //}
            } else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //if (device.getName().equalsIgnoreCase(name)) {
                   int connectState = device.getBondState();
                    switch (connectState) {
                        case BluetoothDevice.BOND_NONE:
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            try {
                                connect(device);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                //}
            }

    }
    };
    private void connect(BluetoothDevice device) throws IOException {
        bText.setText(bText.getText()+ "\n" + "connect");
        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        UUID uuid = UUID.fromString(SPP_UUID);
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
        OutputStream output;
        socket.connect();
        output = socket.getOutputStream();
        output.write(1);
    }
}
