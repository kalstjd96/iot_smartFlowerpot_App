package com.example.admin.blue_thooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {

    private TextView tempTxt;
    private TextView humTxt;
    private BluetoothSPP bt;
    private TextView soilTxt;
    private TextView zodoTxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tempTxt = findViewById(R.id.temptxt);
        humTxt = findViewById(R.id.humtxt);
        soilTxt = findViewById(R.id.soilTxt);
        zodoTxt = findViewById(R.id.zodotxt);
        bt = new BluetoothSPP(this); //Initializing

        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {

                if (message.contains("temp")){
                    int idx =  message.indexOf("p");
                    String temperate =message.substring(idx+1);
                    int value = Integer.parseInt(temperate);
                    if( value<21) {
                        tempTxt.setTextColor(Color.BLUE);
                    }
                    else if( value<30) {
                        tempTxt.setTextColor(Color.GREEN);
                    }
                    else{
                        tempTxt.setTextColor(Color.RED);
                    }
                    tempTxt.setText(temperate);

                }
                if (message.contains("hum")){
                    int idx =  message.indexOf("m");
                    String humidity = message.substring(idx+1);
                    int value = Integer.parseInt(humidity);
                    if( value<301) {
                        humTxt.setTextColor(Color.RED);
                        humTxt.setText("높음");
                    }
                    else if( value<701) {
                        humTxt.setTextColor(Color.GREEN);
                        humTxt.setText("보통");
                    }
                    else{
                        humTxt.setTextColor(Color.RED);
                        humTxt.setText("건조");
                    }
                }
                if (message.contains("soil")) {
                    int idx =  message.indexOf("l");
                    String soil =message.substring(idx+1);
                    int value = Integer.parseInt(soil);
                    if(value <301) {
                        soilTxt.setTextColor(Color.BLUE);
                        soilTxt.setText("물 많음");
                    }else if(value <701){
                        soilTxt.setTextColor(Color.GREEN);
                        soilTxt.setText("물 보통");
                    }else {
                        soilTxt.setTextColor(Color.RED);
                        soilTxt.setText("물 부족");
                    }


                }
                if (message.contains("zdo")) {
                    int idx =  message.indexOf("o");
                    String zodo =message.substring(idx+1);
                    int value = Integer.parseInt(zodo);
                    if(value == 0) {
                        zodoTxt.setTextColor(Color.YELLOW);
                        zodoTxt.setText("밝음");
                    }else if(value == 1){
                        zodoTxt.setTextColor(Color.BLUE);
                        zodoTxt.setText("어두움");
                    }
                }
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = findViewById(R.id.btnConnect); //연결시도
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
                setup();
            }
        }
    }

    public void setup() {
        final Button btnPan = findViewById(R.id.btnPan); //데이터 전송
        btnPan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(btnPan.getText().equals("팬 OFF")){
                    btnPan.setText("팬 ON");
                }
                else if(btnPan.getText().equals("팬 ON")) {
                    btnPan.setText("팬 OFF");
                }
                bt.send("pan", true);


            }
        });
        final Button btnLed = findViewById(R.id.btnLed); //데이터 전송
        btnLed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(btnLed.getText().equals("LED OFF")){
                    btnLed.setText("LED ON");
                }
                else if(btnLed.getText().equals("LED ON")) {
                    btnLed.setText("LED OFF");
                }
                bt.send("led", true);


            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}


