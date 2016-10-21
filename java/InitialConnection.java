package com.example.musicroom;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class InitialConnection extends Activity implements OnClickListener {
	private WifiP2pManager manager;
	private final IntentFilter intentFilter = new IntentFilter();
	private Channel channel;
	private boolean isWifiP2pEnabled = false;
	private BroadcastReceiver receiver = null;
	public static String TAG = "MusicRoom";
	public final static String ownerIP = "192.168.49.1";
	public final static int port = 8989;
    public Button chooseButton;
    public static boolean isOwner = false;
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private static final int SOCKET_TIMEOUT = 5000;
    
	/**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
        
    }
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		Log.d(TAG, "InitialConnection >> Initial Connection Setup");
		
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	
	    manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
	    channel = manager.initialize(this, getMainLooper(), null);
	    
	    chooseButton = (Button)findViewById(R.id.btn_choose);


    	chooseButton.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.btn_choose) {
			Log.d(TAG, "Choose Button Pressed");
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == CHOOSE_FILE_RESULT_CODE) {
			try {
				Uri uri = data.getData();
				
		        Log.d(TAG, "Intent----------- " + uri);
		        String fileName = extractSongName(uri.getLastPathSegment());
		        
		        Log.d(TAG, "Intent----------- final file name to be sent : " + fileName);
		        Intent serviceIntent = new Intent(this, FileTransfer.class);
		        serviceIntent.setAction(FileTransfer.ACTION_SEND_FILE);
		        serviceIntent.putExtra(FileTransfer.FILE_PATH, uri.toString());
		        serviceIntent.putExtra(FileTransfer.FILE_NAME, fileName);
		        serviceIntent.putExtra(FileTransfer.IP_addr, ownerIP);
		        serviceIntent.putExtra(FileTransfer.PORT_addr, port);
		        serviceIntent.putExtra(FileTransfer.PATH_TYPE, "URI");
		        
		        startService(serviceIntent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String extractSongName(String lastPathSegment) {
		// TODO Auto-generated method stub
		String temp = "";
		for (int i = lastPathSegment.length() - 1; lastPathSegment.charAt(i) != '/'; i--) {
			temp = lastPathSegment.charAt(i) + temp;
			if (i == 0) break;
		}
		return temp;
	}

	@Override
	public void onResume() {
		ListOfDevices.cnt = 2;
	    super.onResume();
	    receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
	    registerReceiver(receiver, intentFilter);
	    initiateDiscovery();
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    unregisterReceiver(receiver);
	}
	
	private void initiateDiscovery() {
		// TODO Auto-generated method stub
		if (!isWifiP2pEnabled) {
            Toast.makeText(InitialConnection.this, "Enable P2P Connection", Toast.LENGTH_SHORT).show();
        }
//
		Toast.makeText(InitialConnection.this, "Finding Peers", Toast.LENGTH_SHORT).show();
		manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(InitialConnection.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(InitialConnection.this, "Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT).show();
            }
        });
	}

	public WifiP2pManager getManager() {
		return manager;
	}

	public Channel getChannel() {
		return channel;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {

            }

            @Override
            public void onSuccess() {
                
            }

        });
	}

	



	
	
	
}