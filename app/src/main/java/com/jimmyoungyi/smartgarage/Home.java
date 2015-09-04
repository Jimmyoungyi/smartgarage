package com.jimmyoungyi.smartgarage;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class Home extends AppCompatActivity {
    BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
    Button home_status_button;
    TextView home_status;
    Boolean status = true;
    ImageView image;
    ImageView voice_btn;
    TextView voice_state;
    static final int check = 111;
    TextToSpeech tts;
    static String address_name;
    Boolean sent = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        voice_btn = (ImageView) findViewById(R.id.voice_btn);
        image = (ImageView) findViewById(R.id.menu);
        home_status_button = (Button) findViewById(R.id.home_status_button);
        home_status = (TextView) findViewById(R.id.home_status);
        voice_state = (TextView) findViewById(R.id.voice_state);
        tts = new TextToSpeech(this,null);
        tts.setLanguage(Locale.US);




        ConnectivityManager cManager = (ConnectivityManager) getSystemService(Home.this.CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cManager.getActiveNetworkInfo();
        if(ninfo!=null && ninfo.isConnected()){
        }else{
            Toast.makeText(Home.this,"Network is not available", Toast.LENGTH_LONG).show();
        }

        if(!bAdapter.isEnabled()){
            Intent bintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(bintent);
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        Intent get_address = getIntent();
        address_name = get_address.getStringExtra(input_address_page.ADDRESS_NAME);
        if(address_name == null || address_name.equals("")){
            voice_state.setText("please go setting input device name");
            bAdapter.startDiscovery();
        }else{
            voice_state.setText("searching" + address_name);
            bAdapter.startDiscovery();
        }

        home_status_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status) {
                    home_status.setText(R.string.home_status_on);
                    home_status_button.setText(R.string.home_button_on);
                    status = false;
                } else {
                    home_status.setText(R.string.home_status_off);
                    home_status_button.setText(R.string.home_button_off);
                    status = true;
                }
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this,Menu_page.class));
            }
        });
    }

    public void bleRSSI(View view) {
        Intent intent = new Intent(this, RSSIActivity.class);
        startActivity(intent);
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



    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                final Button device_item_btn = new Button(getBaseContext());
                System.out.println(device.getName());
                if (device.getName().equalsIgnoreCase(address_name)) {
                    bAdapter.cancelDiscovery();
                    int connectState = device.getBondState();
                    switch (connectState) {
                        case BluetoothDevice.BOND_NONE:

                            try {
                                voice_state.setText("not match");
                                tts.speak("please pair the device first", TextToSpeech.QUEUE_FLUSH, null);
                                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                                createBondMethod.invoke(device);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            try {
                                voice_state.setText("match");
                                connect(device);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            } else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName().equalsIgnoreCase(address_name)) {
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
                }
            }

        }
    };
    private void connect(BluetoothDevice device) throws IOException {
        voice_state.setText("connect");
        tts.speak("please say yes or no", TextToSpeech.QUEUE_FLUSH, null);
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say yes or no.");
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        startActivityForResult(i, check);
        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        UUID uuid = UUID.fromString(SPP_UUID);
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
        OutputStream output;

        socket.connect();
        output = socket.getOutputStream();
        voice_state.setText(sent.toString());
        while(sent){

            output.write(1);
            sent = false;
            voice_state.setText("door is open");
        }

    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data){
        int sreturn = 0;
        if(requestCode == check && resultCode == RESULT_OK){
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String res = results.get(0);
            if(res.equalsIgnoreCase("yes")|| res.equalsIgnoreCase("No")){
                if(res.equalsIgnoreCase("yes")){
                    sent = true;
                    sreturn = 1;
                    //voice_state.setText("door is open");
                }else{
                    voice_state.setText("door is not open");
                }
            }else{
                tts.speak("Please say yes or no.", TextToSpeech.QUEUE_FLUSH, null);
                voice_state.setText("say yes or no");
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
        //return sreturn;
    }
}
