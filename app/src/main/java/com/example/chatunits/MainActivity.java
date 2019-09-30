package com.example.chatunits;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {

    Button send;
    EditText ed;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;
    TextView t;
    Handler handler = new Handler();
    Socket socket;
    WifiManager wifiManager;
    String ddd = "";
    Timer timer;
    int x = 0;

    void TTT() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @SuppressLint("NewApi")
                    @Override
                    public void run() {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                        if (wifiInfo.getIpAddress() == 0) {
                            t.setText("" + x);

                        } else {
                            timer.cancel();

                            int a = wifiInfo.getIpAddress();

                            t.setText("" + Formatter.formatIpAddress(a));
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("sa", "sss\n");
                            String s = hashMap.get("sa");


                            String bas = Base64.encodeToString(s.getBytes(), Base64.DEFAULT);
                            byte[] by = Base64.decode(bas, Base64.DEFAULT);
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(by)));
                            try {
                                String g = bufferedReader.readLine();
                                t.setText(t.getText().toString() + "\n" + s + "\n" + bas + "\n" + g);
                            } catch (IOException e) {
                                t.setText("");
                            }


                        }
                        x++;
                    }
                });
            }
        }, 0, 1000);


    }


    public boolean Client(String ip) {

        Boolean b=false;
        for (int y = 0; y < 256; y++) {
            try {
                Log.e("connect to :",ip + y);

                socket = new Socket();
                socket.connect(new InetSocketAddress(ip + y, 50551), 100);
                log("connected to " + ip + 1 + " / port:9000");
                Createddata();


                b=true;
                return true;
            } catch (IOException e) {

                b=false;

            }


        }

        return b;

    }


    void Createddata() {
        try {
            log("creating data");
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            log("data is created");
        } catch (IOException e) {
        }

    }


    void StartServer() {
        try {
            log("create server Socket....");
            ServerSocket serverSocket = new ServerSocket(50551, 1);
            log("createed server Socket....");

            socket = serverSocket.accept();
            log("created server Socket");

        /*ServerSocket serverSocket2=new ServerSocket(9001);
        socket2=serverSocket2.accept();
        log("created server Socket2");*/
        } catch (IOException e) {

            Log.e("error",e.getMessage());

        }
    }

    private void log(final String s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                t.setText(t.getText().toString() + "\n" + s);
            }
        });
    }


    @SuppressLint("WifiManagerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission();
        send = (Button) findViewById(R.id.button);
        ed = (EditText) findViewById(R.id.editText);


        t = (TextView) findViewById(R.id.wifi);

        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);


        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        int a = wifiInfo.getIpAddress();
        String sassa = Formatter.formatIpAddress(a) + "";
        ddd = sassa.substring(0, sassa.lastIndexOf('.') + 1);


        t.setText(Formatter.formatIpAddress(a));
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                log("search for network");
                if (Client(ddd) == true) {

                } else {
                    StartServer();
                    Createddata();
                }


                while (true) {
                    try {
                        String ali = dataInputStream.readUTF();
                        log("اون:\n" + ali);


                    } catch (Exception e) {
                    }
                }


            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (dataOutputStream == null) {
                    return;
                }
                if (!socket.isConnected()) {
                    Toast.makeText(getApplicationContext(), "connect is cancl", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    dataOutputStream.writeUTF(ed.getText().toString());
                    log("خودم:\n" + ed.getText().toString());
                    ed.setText("");
                } catch (IOException e) {

                }


            }
        });
        thread.start();


    }



    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void permission() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {

                android.Manifest.permission.INTERNET,
                android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                android.Manifest.permission.CHANGE_WIFI_STATE,
                android.Manifest.permission.ACCESS_WIFI_STATE
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (IOException e) {

        }


    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            System.exit(0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}