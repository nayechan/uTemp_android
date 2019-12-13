package com.example.nayec.arduinoproject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {

    private BluetoothSPP bt;
    TextView connect_status, temp_status, tempr;
    float temperature;
    ImageView bg;
    char CFStatus = 'C';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connect_status = findViewById(R.id.status_connect);
        temp_status = findViewById(R.id.status_temp);
        tempr = findViewById(R.id.temp);
        bg = findViewById(R.id.bg);
        bt = new BluetoothSPP(this);
        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            connect_status.setText("Bluetooth is not available");
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {
                tempr.setText(message+"°"+CFStatus);
                temperature = Float.parseFloat(message);
                if(temperature < -10 && CFStatus == 'C' || temperature < 14 && CFStatus == 'F' )
                {
                    bg.setImageBitmap(
                            ((BitmapDrawable)getResources().getDrawable(R.drawable.arctic)).getBitmap()
                    );
                    temp_status.setText("Extremely Cold Temperature");
                }else if(temperature < 10 && CFStatus == 'C' || temperature < 50 && CFStatus == 'F' )
                {
                    bg.setImageBitmap(
                            ((BitmapDrawable)getResources().getDrawable(R.drawable.winter)).getBitmap()
                    );
                    temp_status.setText("Pretty Cold Temperature");
                }else if(temperature < 30 && CFStatus == 'C' || temperature < 86 && CFStatus == 'F' )
                {
                    bg.setImageBitmap(
                            ((BitmapDrawable)getResources().getDrawable(R.drawable.spring)).getBitmap()
                    );
                    temp_status.setText("Adequate Temperature");
                }else{
                    bg.setImageBitmap(
                            ((BitmapDrawable)getResources().getDrawable(R.drawable.desert)).getBitmap()
                    );
                    temp_status.setText("Hot Temperature");
                }
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                connect_status.setText("Connected to " + name + " (" + address + ")");
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() { //연결해제
                connect_status.setText("Connection lost");
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                connect_status.setText("Unable to connect");
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView btnConnect = findViewById(R.id.connect); //연결시도
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
        tempr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(CFStatus == 'C') CFStatus = 'F';
                else CFStatus = 'C';
                bt.send(CFStatus+"", true);
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
                connect_status.setText("Bluetooth was not enabled.");
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
