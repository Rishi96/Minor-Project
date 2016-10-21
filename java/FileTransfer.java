package com.example.musicroom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class FileTransfer extends IntentService{

	
	private static final int SOCKET_TIMEOUT = 5000;
	private static final String TAG = "FileTransfer";
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String FILE_PATH = "file_url";
    public static final String FILE_NAME = "file_name";
    public static final String PATH_TYPE = "path_type";
    public static final String IP_addr = "go_host";
    public static final String PORT_addr = "go_port";
	
	
	public FileTransfer(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public FileTransfer()
	{
		super("FileTransfer");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Context context = getApplicationContext();
		ListOfDevices.displayMessage("Sending File");
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String filePath = intent.getExtras().getString(FILE_PATH);
            String fileName = intent.getExtras().getString(FILE_NAME);
            String host = intent.getExtras().getString(IP_addr);
            String pathType = intent.getExtras().getString(PATH_TYPE);
            
            Socket socket = new Socket();
            
            int port = intent.getExtras().getInt(PORT_addr);
            try {
                Log.d(TAG, "Opening client socket - " + host);
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                Log.d(TAG, "Client socket - " + socket.isConnected());
                
                OutputStream stream = socket.getOutputStream();
                //flag at the begining of the song to indicate that it is a song type msg
                byte buf[] = addSongName(fileName);
                stream.write(buf);
                if (pathType.equals("URI")) {
	                ContentResolver cr = context.getContentResolver();
	                InputStream is = null;
	                try {
	                    is = cr.openInputStream(Uri.parse(filePath));
	                } catch (FileNotFoundException e) {
	                    Log.d(TAG, e.toString());
	                }
	               copyFile(is, stream);
                } else if (pathType.equals("FILEPATH")) {
        			File source = new File(filePath);
        			FileInputStream ff = new FileInputStream(source);
        			copyFile(ff, stream);
                }
                
                Log.d(TAG, "Client: Data written");
            } catch (IOException e) {
            	e.printStackTrace();
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
	}
	
	private byte[] addSongName(String fileName) {
		// TODO Auto-generated method stub
		Log.d(TAG, "resloving song name and setting flag");
		int size = fileName.length();
		byte buf[] = new byte[4 + size];
        String s = "2";
		s = s + ((size / 100));
		size %= 100;
		s = s + ((size / 10));
		size %= 10;
		s = s + (size);
		s = s + fileName;
		Log.d(TAG, "resloved song name : " + s);
		buf = s.getBytes();
		return buf;
	}

	public static boolean copyFile(InputStream inputStream, OutputStream out) {
	    byte buf[] = new byte[1024];
	    int len;
	    try {
	        while ((len = inputStream.read(buf)) != -1) {
	            out.write(buf, 0, len);
	
	        }
	        out.close();
	        inputStream.close();
	    } catch (IOException e) {
	        Log.d(TAG, e.toString());
	        return false;
	    }
	    return true;
	}
}
