package com.example.musicroom;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class IpAddressSender extends IntentService {

	private static final int SOCKET_TIMEOUT = 5000;
	public static final String ACTION_FILE = "SEND_FILE";
	public static final String IP_addr = "go_host";
	public static final String PORT_addr = "go_port";
	private String TAG = "IpAddressSender";
	
	public IpAddressSender(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public IpAddressSender() {
        super("IpAddressSender");

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		int flag = 5;
		Context context = getApplicationContext();
		if (intent.getAction().equals(ACTION_FILE)) {
			String host = intent.getExtras().getString(IP_addr);
			int port = intent.getExtras().getInt(PORT_addr);
			Socket socket = new Socket();
			while (flag != 0) {
				flag--;
				try {
		        	socket.bind(null);
		            socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
		            OutputStream out = socket.getOutputStream();
		            String s = "1HI";
		            InputStream inp = new ByteArrayInputStream(s.getBytes());
		            ClientThread.copyFile(inp, out);
		            Log.d(TAG, "onConnectionInfoAvailable >> client successfully sent its ip");
		            flag = 0;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
