package com.example.musicroom;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ClientThread extends AsyncTask<Void, Void, Void> {

    private Context context;
	private ServerSocket server = null;
    private static final String TAG = "ClientThread";
	String ip;
    /**
     * @param context
     //* @param statusText
     */
    public ClientThread (Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
    	try {
			server = new ServerSocket(InitialConnection.port);
			Log.d(TAG, "ClientThread >> Client Socket Opened");
			while (true) {
				Socket client = server.accept();
				Log.d(TAG, "ClientThread >> Client Encountered A Request");
				InputStream inputstream = client.getInputStream();
				int flag = checkformsg(inputstream);
				//The incoming file is a message or a string
				if (flag == 1) {
					Scanner ss = new Scanner(inputstream);
					String temp = ss.next();
					Log.d(TAG, "syncing the song ...............");
		            int currentSeekTime = Integer.parseInt(temp);

		            long tt = System.currentTimeMillis();

		            Log.d(TAG, "time the player used to seek the player" + (tt - System.currentTimeMillis()));

				} else if (flag == 2) {
					Log.d(TAG, "ClientThread >> Resolving file name");
					ListOfDevices.displayMessage("Recieving file");
					int size = 0, len;
					String fileName = "";
					byte buf[] = new byte[3];
					if ((len = inputstream.read(buf)) != -1) {
						for (int i = 0; i < 3; i++) {
							size = size * 10 + buf[i] - 48;
						}
					}
					byte buf1[] = new byte[size];
					if ((len = inputstream.read(buf1)) != -1) {
						fileName = new String(buf1);
					}
					
					Log.d(TAG, "ClientThread >> Recieving the requested audio file from a client");
					String filePath = Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/" + fileName;
					final File f = new File(filePath);
					//change the name of the song from c.mp3 to its original name in the bove line
	                File dirs = new File(f.getParent());
	                if (!dirs.exists())
	                    dirs.mkdirs();
	                f.createNewFile();

	                Log.d(TAG, "ClientThread >> server: copying files " + f.toString());
	                copyFile(inputstream, new FileOutputStream(f));
	                //Player.addSong(filePath, context);
	                
	                Log.d(TAG, "ClientThread >> File received successfully");
	                ListOfDevices.displayMessage("file Recieved");
	                
	               // ListOfDevices.callingSyncSong();
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }

    private static int checkformsg(InputStream inputStream) {
		// TODO Auto-generated method stub
		byte buf[] = new byte[1];
        int len, k = 0;
        try {
            if ((len = inputStream.read(buf)) != -1) {
                Byte b = buf[0];
                k = b.intValue();
                k -= 48;
            }
            
            //inputStream.close();
            Log.d(TAG, "checkformsg >> inputstream is not empty " + k);
            return k;
        } catch (IOException e) {
            Log.d(TAG, "checkformsg >> nothing in the inputstream");
            return 0;
        }
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