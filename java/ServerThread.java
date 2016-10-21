package com.example.musicroom;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ServerThread extends AsyncTask<Void, Void, Void> {

    private Context context;
	private ServerSocket server = null;
	private static final String TAG = "ServerThread"; 
	
    public static Set<String> ipAddrsList = new HashSet<String>();
    
	String ip;
    /**
     * @param context
     * @param //statusText
     */
    public ServerThread (Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
    	try {
			server = new ServerSocket(InitialConnection.port);
			
			while (true) {
				Log.d(TAG, "ServerThread >> Owner Socket Opened");
				Socket client = server.accept();
				Log.d(TAG, "ServerThread >> Owner Encountered A Request");
				InputStream inputstream = client.getInputStream();
				//The incoming flag is 1 = a msg of type string
				int flag = checkformsg(inputstream);
				if (flag == 1) {
					Scanner ss = new Scanner(inputstream);
					Log.d(TAG, "ServerThread >> inputstream recieved");
					String temp = getStringFromInputStream(inputstream);
					Log.d(TAG, "ServerThread >> Recieved String : " + temp);
//
					if (temp.equals("HI")) {
						Log.d(TAG, "ServerThread >> Request is HI");
						
						ip = client.getInetAddress().toString();
						ip = ip.substring(1);
						
						Log.d(TAG, "ServerThread >> Recieved Client IP Address : " + ip);
		                
						ipAddrsList.add(ip);
						Log.d(TAG, "ServerThread >> ipAddressList size : " + ipAddrsList.size());
					} else if (temp.equals("SYNCMSG")){
						Log.d(TAG, "ServerThread >> Request is syncmsg");
						//Send the seek time Code left blank create a new socket that connects to client and send the file
					
					}
				
				//the incoming msg is a mp3 file 				
				} else if (flag == 2) {
					Log.d(TAG, "ServerThread >> reloving song name");
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
					Log.d(TAG, "ServerThread >> Recieving the requested audio file from a client ");
					String filePath = Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/" + fileName;
					final File f = new File(filePath);
					File dirs = new File(f.getParent());
	                if (!dirs.exists())
	                    dirs.mkdirs();
	                f.createNewFile();

	                Log.d(TAG, "ServerThread >> server: copying files " + f.toString());
	                copyFile(inputstream, new FileOutputStream(f));
	               // Player.addSong(filePath, context);
	                Log.d(TAG, "ServerThread >> File received successfully");
	                ListOfDevices.displayMessage("file Recieved");
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
	
	public static String getStringFromInputStream(InputStream is) {
		 
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
 
		String line;
		try {
 
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
 
		return sb.toString();
 
	}
}