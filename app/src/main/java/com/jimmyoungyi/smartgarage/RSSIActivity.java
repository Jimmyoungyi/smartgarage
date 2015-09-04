package com.jimmyoungyi.smartgarage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class RSSIActivity extends AppCompatActivity {

    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssi);

        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        Button boton = (Button) findViewById(R.id.button);
        boton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BTAdapter.startDiscovery();
            }
        });
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                TextView rssi_msg = (TextView) findViewById(R.id.textView);
                rssi_msg.setText(rssi_msg.getText() + name + " => " + rssi + "dBm\n");
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rssi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
