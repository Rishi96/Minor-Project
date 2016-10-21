package com.example.musicroom;

import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListOfDevices implements PeerListListener, GroupInfoListener, ConnectionInfoListener {

	public List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	View mContentView = null;
	private final static String TAG = "ListOfDevices";
	public static InitialConnection activity;
	private WifiP2pDevice owner;
	public static int clientPort = 8989;
	public static int serverPort = 8989;
	public static final int SocketTimeout = 5000;
	public static ServerThread serverThread = null;
	public static ClientThread clientThread = null;
	// public static SyncSongThread syncSongThread = null;
	public static int cnt = 2;
	public static boolean ff = false;

	//	public static long syncClock;
	public ListOfDevices(InitialConnection activity) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		owner = null;
	}

	public interface DeviceActionListner {
		public void connect(WifiP2pConfig config);
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peerList) {
		// TODO Auto-generated method stub
		Log.d(TAG, "ListOfDevices >> onPeersAvailable");
		peers.clear();
		peers.addAll(peerList.getDeviceList());
		//ArrayAdapter<String>adp=new ArrayAdapter<String>(getApplicationContext,R.layout.row1layout,R.id.textView,peers);
		Log.d("size..................", peers.size() + "");
		//Toast.makeText(, "Size of List of Peers : " + deviceList.peers.size(), Toast.LENGTH_SHORT).show();
		//       ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

		if (peers.size() == 0) {
			Log.d(TAG, "onPeersAvailable >> No devices found");
			return;
		} else {

			boolean f = false;
			for (int i = 0; i < peers.size(); i++) {
				Log.d(TAG, "onPeersAvailable >> device status : " + peers.get(i).status + " device name : " + peers.get(i).deviceName + " " + peers.get(i).deviceAddress);
				if (peers.get(i).status == WifiP2pDevice.CONNECTED || peers.get(i).status == WifiP2pDevice.INVITED)
					f = true;
			}

			if (!f) {

				if (cnt != 0) {
					cnt--;
					for (int i = 0; i < peers.size(); i++) {
						if (peers.get(i).isGroupOwner()) {
							Log.d(TAG, "connecting................");
							connect(peers.get(i));
							ff = true;
							break;
						}
					}

					if (ff) cnt = 0;
					else {
						activity.getManager().requestPeers(activity.getChannel(), this);
					}
				}
				if (ff == false) {
					peers.add(WiFiDirectBroadcastReceiver.ownDevice);
					MyComparator comparator = new MyComparator();
					Collections.sort(peers, comparator);
					if (peers.get(0) != WiFiDirectBroadcastReceiver.ownDevice) {
						Log.d(TAG, "connecting................");
						connect(peers.get(0));
						ff = true;
					}
				}

			}

		}
	}


	public void connect(WifiP2pDevice device) {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		activity.getManager().connect(activity.getChannel(), config, new ActionListener() {

			@Override
			public void onSuccess() {
				// WiFiDirectBroadcastReceiver will notify us. Ignore for now.
				Log.d(TAG, "ListOfDevices >> connect >> success");
				Toast.makeText(activity, "Connected successfully", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reason) {
				Log.d(TAG, "ListOfDevices >> connect >> failed");
				Toast.makeText(activity, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
			}
		});
	}


	@Override
	public void onGroupInfoAvailable(WifiP2pGroup group) {
		// TODO Auto-generated method stub
		Log.d(TAG, "ListOfDevices >> onGroupInfoAvailable");
		if (group != null) {
			Log.d(TAG, "ListOfDevices >> Owner Found");
			owner = group.getOwner();
		} else {
			Log.d(TAG, "ListOfDevices >> Group is null");
		}
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onConnectionInfoAvailable >> in the function called");
		if (info.groupFormed && info.isGroupOwner) {
			Log.d(TAG, "onConnectionInfoAvailable >> I am the owner");
			//Start ServerThread
			InitialConnection.isOwner = true;
			if (serverThread == null) {
				serverThread = new ServerThread(activity);
				serverThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
				Log.d(TAG, "server Thread opened");
				//syncSongThread = new SyncSongThread(activity);
				//syncSongThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
			}

		} else if (info.groupFormed) {
			Log.d(TAG, "onConnectionInfoAvailable >> client found");
			//Start ClientThread here
			if (clientThread == null) {
				clientThread = new ClientThread(activity);
				clientThread.execute();
			}
			Log.d(TAG, "onPeersAvailable >> Connection establish before");
			Intent serviceIntent1 = new Intent(activity, IpAddressSender.class);
			serviceIntent1.setAction(IpAddressSender.ACTION_FILE);
			serviceIntent1.putExtra(IpAddressSender.IP_addr, InitialConnection.ownerIP);
			serviceIntent1.putExtra(IpAddressSender.PORT_addr, InitialConnection.port);
			Log.d(TAG, "Client IP address sent");
			activity.startService(serviceIntent1);

		}
	}

	public static void transferSongToDevices(String filePath) {
		for (String deviceIP : ServerThread.ipAddrsList) {

			Intent serviceIntent = new Intent(activity, FileTransfer.class);
			String fileName = InitialConnection.extractSongName(filePath);
			serviceIntent.setAction(FileTransfer.ACTION_SEND_FILE);
			serviceIntent.putExtra(FileTransfer.FILE_PATH, filePath);
			serviceIntent.putExtra(FileTransfer.FILE_NAME, fileName);
			serviceIntent.putExtra(FileTransfer.IP_addr, deviceIP);
			serviceIntent.putExtra(FileTransfer.PORT_addr, InitialConnection.port);
			serviceIntent.putExtra(FileTransfer.PATH_TYPE, "FILEPATH");
			activity.startService(serviceIntent);
		}
	}

	public static void displayMessage(String msg) {
//       Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
	}


	class MyComparator implements Comparator<WifiP2pDevice> {
		@Override
		public int compare(WifiP2pDevice lhs, WifiP2pDevice rhs) {
			// TODO Auto-generated method stub
			return (lhs.deviceAddress.compareTo(rhs.deviceAddress));
		}
	}
}